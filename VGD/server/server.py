import random
import socket
from threading import Thread, Lock

import pygame
import time

from common.bomb import Bomb
from common.decoder import Decoder
from common.defines import *
from common.encoder import Encoder
from common.player import Player
from server.engine import GameEngine


class GameServer:
    def __init__(self, host, port):
        self.host = host
        self.port = port
        self.server = None
        self.clients = []
        self.engine = None
        self.players = None
        self.guid_generator = 1
        self.packet_threads = []
        self.object_lock = Lock()
        self.destroy_lock = Lock()
        self.destroy_objects = []
        self.quit = False

    def connect(self, players=2):
        self.players = players
        self.engine = GameEngine()
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server.bind((self.host, self.port))
        self.server.listen(players)

        for x in range(players):
            (client, address) = self.server.accept()
            self.clients.append((client, address))
            client.sendall(Encoder.encode_string(str(self.engine.height * 50) + 'x' + str(self.engine.width * 50)))

            # SMSG_INIT_DATA contains the map, the number of players and their guids
            client.sendall(Encoder.encode_int(SMSG_INIT_DATA))
            for row in self.engine.raw_map:
                for data in row:
                    client.sendall(Encoder.encode_int(data))
            pl = Player(self.guid_generator, client)
            pl.position = start_positions[pl.guid]
            self.engine.players.append(pl)
            self.engine.objects.append(pl)
            self.guid_generator += 1

        for cl in self.clients:
            # SMSG_INIT_PLAYERS contains the number of players and the guid of each (plus a boolean to mark if it is the player)
            cl[0].sendall(Encoder.encode_int(SMSG_INIT_PLAYERS))
            cl[0].sendall(Encoder.encode_int(players))
            for k in range(players):
                cl[0].sendall(Encoder.encode_int(self.engine.players[k].guid))
                cl[0].sendall(Encoder.encode_int(1 if self.engine.players[k].client == cl[0] else 0))

        self.packet_threads = [Thread(target=self.packets, args=(client[0], )) for client in self.clients]
        for th in self.packet_threads:
            th.start()
        self.update()


    def packets(self, client):
        while not self.quit:
            try:
                opcode = Decoder.decode_int(client.recv(4))
                if opcode == CMSG_MOVE_UPDATE:
                    player_guid = Decoder.decode_int(client.recv(4))
                    movement_flags = Decoder.decode_int(client.recv(4))
                    for pl in self.engine.players:
                        if pl.guid == player_guid:
                            pl.movement_flags = movement_flags
                if opcode == CMSG_PUT_BOMB:
                    pos_x = Decoder.decode_int(client.recv(4))
                    pos_y = Decoder.decode_int(client.recv(4))
                    b = Bomb(self.guid_generator, self.engine.get_closest_tile(pos_x, pos_y))
                    self.object_lock.acquire()
                    self.engine.objects.append(b)
                    self.object_lock.release()
            except:
                self.quit = True

    def update(self):
        while not self.quit:
            self.engine.update_objects()
            for obj in self.engine.objects:
                if isinstance(obj, Player):
                    if obj.movement_flags:
                        for c in self.clients:
                            c[0].sendall(Encoder.encode_int(SMSG_UPDATE_POSITION))
                            c[0].sendall(Encoder.encode_int(obj.guid))
                            c[0].sendall(Encoder.encode_int(obj.position[0]))
                            c[0].sendall(Encoder.encode_int(obj.position[1]))
                    if obj.invulnerability_timer > 0:
                        obj.invulnerability_timer -= 3
                        if obj.invulnerability_timer <= 0:
                            for c in self.clients:
                                c[0].sendall(Encoder.encode_int(SMSG_REMOVE_INVULNERABILITY))
                                c[0].sendall(Encoder.encode_int(obj.guid))
                if isinstance(obj, Bomb):
                    if obj.done:
                        continue
                    if not obj.sent:
                        obj.sent = True
                        for c in self.clients:
                            c[0].sendall(Encoder.encode_int(SMSG_PUT_BOMB_ACK))
                            c[0].sendall(Encoder.encode_int(obj.guid))
                            c[0].sendall(Encoder.encode_int(obj.position[0]))
                            c[0].sendall(Encoder.encode_int(obj.position[1]))
                    else:
                        obj.timer += 3
                        if obj.timer > 1500:
                            obj.done = True
                            hit_units = self.engine.get_bomb_hit_units(obj)
                            destroyed_walls = self.engine.get_bomb_hit_walls(obj)
                            self.destroy_objects.append(obj)
                            for c in self.clients:
                                c[0].sendall(Encoder.encode_int(SMSG_DETONATE_BOMB))
                                c[0].sendall(Encoder.encode_int(obj.guid))
                                c[0].sendall(Encoder.encode_int(len(hit_units)))
                                for guid in hit_units:
                                    c[0].sendall(Encoder.encode_int(guid))
                                c[0].sendall(Encoder.encode_int(len(destroyed_walls)))
                                for pos in destroyed_walls:
                                    c[0].sendall(Encoder.encode_int(pos[0]))
                                    c[0].sendall(Encoder.encode_int(pos[1]))
            self.object_lock.acquire()
            for obj in self.destroy_objects:
                self.engine.objects.remove(obj)
            self.destroy_objects.clear()
            self.object_lock.release()
            time.sleep(0.003) # 60 FPS
        for th in self.packet_threads:
            th.join(1)
        self.server.close()