import os
import socket
import time
from cmath import cos
from threading import Thread

import pygame
from copy import deepcopy

from common.bomb import Bomb
from common.decoder import Decoder
from common.defines import *
from common.encoder import Encoder
from common.explosion import Explosion
from common.player import Player
from server.engine import GameEngine


class GameClient:
    ASSETS_ROOT = os.path.join('assets')
    LOGO_FILE = os.path.join(ASSETS_ROOT, 'logo.png')
    BACKGROUND = (66, 200, 244)

    def __init__(self, host, port):
        self.assets = {
            'player1': pygame.image.load(os.path.join(GameClient.ASSETS_ROOT, 'bomberman1.png')),
            'player2': pygame.image.load(os.path.join(GameClient.ASSETS_ROOT, 'bomberman3.png')),
            'explosion': pygame.image.load(os.path.join(GameClient.ASSETS_ROOT, 'explosion.png')),
            'obstacle': pygame.image.load(os.path.join(GameClient.ASSETS_ROOT, 'obstacle.png')),
            'wall': pygame.image.load(os.path.join(GameClient.ASSETS_ROOT, 'wall.png')),
            'bomb': pygame.image.load(os.path.join(GameClient.ASSETS_ROOT, 'bomb.png')),
            'life1': pygame.image.load(os.path.join(GameClient.ASSETS_ROOT, 'life1.png')),
            'life2': pygame.image.load(os.path.join(GameClient.ASSETS_ROOT, 'life2.png')),
        }
        self.host = host
        self.port = port
        self.server = None
        self.height = 0
        self.width = 0
        self.font = 0
        self.screen = None
        self.player = None
        self.packet_receiver = None
        self.quit = False
        self.clock = pygame.time.Clock()
        self.raw_map = []
        self.rows = 0
        self.cols = 0
        self.objects = []
        self.map_ready = False
        self.bomb_cd = 0
        self.expired_invulnerabilities = []
        self.game_started = False
        self.game_over = None


    def connect(self):
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server.connect((self.host, self.port))

        data_size = Decoder.decode_int(self.server.recv(4))
        resolution_string = Decoder.decode_string(self.server.recv(data_size))
        self.height, self.width = resolution_string.split('x')

        self.height = int(self.height)
        self.width = int(self.width)

        self.rows = int(self.height / 50)
        self.cols = int(self.width / 50)

        for i in range(self.rows):
            self.raw_map.append([0 for _ in range(self.cols)])

        pygame.init()
        pygame.font.init()

        pygame.display.set_icon(pygame.image.load(GameClient.LOGO_FILE))
        pygame.display.set_caption("Bomberman")

        self.font = pygame.font.SysFont('Arial', 40, bold=True)
        self.screen = pygame.display.set_mode((self.width, self.height))

        for asset in self.assets.keys():
            self.assets[asset] = pygame.transform.scale(self.assets[asset], (50, 50))

        self.assets['explosion'] = pygame.transform.scale(self.assets['explosion'], (150, 150))

        self.packet_receiver = Thread(target=self.packets)
        self.packet_receiver.start()
        self.update()

    def packets(self):
        while not self.quit:
            try:
                opcode = Decoder.decode_int(self.server.recv(4))
                if opcode == SMSG_INIT_DATA:
                    for i in range(self.rows):
                        for j in range(self.cols):
                            self.raw_map[i][j] = Decoder.decode_int(self.server.recv(4))
                    self.map_ready = True
                if opcode == SMSG_INIT_PLAYERS:
                    nr_of_players = Decoder.decode_int(self.server.recv(4))
                    for k in range(nr_of_players):
                        guid = Decoder.decode_int(self.server.recv(4))
                        is_me = Decoder.decode_int(self.server.recv(4))
                        pl = Player(guid)
                        pl.position = start_positions[pl.guid]
                        self.objects.append(pl)
                        if is_me == 1:
                            self.player = pl
                    self.game_started = True
                if opcode == SMSG_UPDATE_POSITION:
                    player_guid = Decoder.decode_int(self.server.recv(4))
                    pos_x = Decoder.decode_int(self.server.recv(4))
                    pos_y = Decoder.decode_int(self.server.recv(4))
                    for obj in self.objects:
                        if obj.guid == player_guid:
                            obj.position = (pos_x, pos_y)
                if opcode == SMSG_PUT_BOMB_ACK:
                    bomb_guid = Decoder.decode_int(self.server.recv(4))
                    pos_x = Decoder.decode_int(self.server.recv(4))
                    pos_y = Decoder.decode_int(self.server.recv(4))
                    b = Bomb(bomb_guid, (pos_x, pos_y))
                    self.objects.append(b)
                if opcode == SMSG_DETONATE_BOMB:
                    bomb_guid = Decoder.decode_int(self.server.recv(4))
                    hit_units = Decoder.decode_int(self.server.recv(4))
                    for i in range(hit_units):
                        guid = Decoder.decode_int(self.server.recv(4))
                        for ob in self.objects:
                            if ob.guid == guid:
                                ob.healths -= 1
                                ob.invulnerability_timer = 1
                    dead_players = []
                    for ob in self.objects:
                        if isinstance(ob, Player):
                            if ob.healths <= 0:
                                dead_players.append(ob.guid)
                    if len(dead_players) != 0:
                        if len(dead_players) > 1:
                            self.game_over = -1
                        else:
                            self.game_over = 1 if dead_players[0] == 2 else 2
                    hit_walls = Decoder.decode_int(self.server.recv(4))
                    for i in range(hit_walls):
                        x = Decoder.decode_int(self.server.recv(4))
                        y = Decoder.decode_int(self.server.recv(4))
                        self.raw_map[x][y] = GameEngine.EMPTY
                    bomb = None
                    for obj in self.objects:
                        if obj.guid == bomb_guid:
                            bomb = obj
                            break
                    if bomb is not None:
                        self.objects.append(Explosion((bomb.position[0] - 50, bomb.position[1] - 50), 100))
                        self.objects.remove(bomb)
                if opcode == SMSG_REMOVE_INVULNERABILITY:
                    guid = Decoder.decode_int(self.server.recv(4))
                    for ob in self.objects:
                        if ob.guid == guid:
                            self.expired_invulnerabilities.append(ob)
            except:
                self.quit = True

    def update(self):
        while not self.quit:
            try:
                if self.player is None:
                    self.screen.fill(GameClient.BACKGROUND)
                    wait_surface = self.font.render('Waiting for other players...', False, (255, 255, 255))
                    self.screen.blit(wait_surface, (130, 250))
                    pygame.display.update()
                    self.clock.tick(60)
                    for event in pygame.event.get():
                        if event.type == pygame.QUIT:
                            self.quit = True
                    continue
                if self.game_over is not None:
                    self.screen.fill(GameClient.BACKGROUND)
                    if self.game_over != -1:
                        wait_surface = self.font.render('Player {0} has won!'.format(self.game_over), False, (255, 255, 255))
                        self.screen.blit(self.assets['player' + str(self.game_over)], (300, 280))
                        self.screen.blit(wait_surface, (180, 170))
                    else:
                        wait_surface = self.font.render('Draw!', False, (255,255,255))
                        self.screen.blit(wait_surface, (300, 250))
                    pygame.display.update()
                    self.clock.tick(60)
                    for event in pygame.event.get():
                        if event.type == pygame.QUIT:
                            self.quit = True
                    continue
                if self.bomb_cd > 0:
                    self.bomb_cd -= 5
                for ob in self.expired_invulnerabilities:
                    ob.invulnerability_timer = 0
                self.expired_invulnerabilities.clear()

                crt_flags = self.player.movement_flags
                for event in pygame.event.get():
                    if event.type == pygame.QUIT:
                        self.quit = True
                    elif event.type == pygame.KEYDOWN:
                        if event.key == KEY_UP:
                            self.player.movement_flags |= MOVEMENT_FLAG_UP
                        if event.key == KEY_DOWN:
                            self.player.movement_flags |= MOVEMENT_FLAG_DOWN
                        if event.key == KEY_RIGHT:
                            self.player.movement_flags |= MOVEMENT_FLAG_RIGHT
                        if event.key == KEY_LEFT:
                            self.player.movement_flags |= MOVEMENT_FLAG_LEFT
                    elif event.type == pygame.KEYUP:
                        if event.key == KEY_UP:
                            self.player.movement_flags &= ~MOVEMENT_FLAG_UP
                        if event.key == KEY_DOWN:
                            self.player.movement_flags &= ~MOVEMENT_FLAG_DOWN
                        if event.key == KEY_RIGHT:
                            self.player.movement_flags &= ~MOVEMENT_FLAG_RIGHT
                        if event.key == KEY_LEFT:
                            self.player.movement_flags &= ~MOVEMENT_FLAG_LEFT
                        if event.key == pygame.K_SPACE and self.bomb_cd <= 0:
                            self.bomb_cd = 300
                            self.server.sendall(Encoder.encode_int(CMSG_PUT_BOMB))
                            self.server.sendall(Encoder.encode_int(self.player.position[0]))
                            self.server.sendall(Encoder.encode_int(self.player.position[1]))

                self.screen.fill(GameClient.BACKGROUND)
                if self.map_ready:
                    for i in range(self.rows):
                        for j in range(self.cols):
                            if self.raw_map[i][j] == GameEngine.WALL:
                                self.screen.blit(self.assets['wall'], (j * 50, i * 50))
                            if self.raw_map[i][j] == GameEngine.OBSTACLE:
                                self.screen.blit(self.assets['obstacle'], (j * 50, i * 50))
                    remove_objects = []
                    for obj in self.objects:
                        if isinstance(obj, Player):
                            if obj.invulnerability_timer > 0:
                                obj.invulnerability_timer += 1
                                if obj.invulnerability_timer % 11 == 0 or obj.invulnerability_timer % 12 == 0 or \
                                    obj.invulnerability_timer % 13 == 0 or obj.invulnerability_timer % 14 == 0:
                                    pass
                                else:
                                    self.screen.blit(self.assets['player' + str(obj.guid)], obj.position)
                            else:
                                self.screen.blit(self.assets['player' + str(obj.guid)], obj.position)
                            if obj.guid == 1:
                                for i in range(obj.healths):
                                    self.screen.blit(self.assets['life1'], (50 * i, 0))
                            else:
                                for i in range(obj.healths):
                                    self.screen.blit(self.assets['life2'], ((12 - i) * 50, 0))
                        if isinstance(obj, Bomb):
                            obj.timer += 17
                            multiplier = cos(3.14 * obj.timer / 400).real / 8 + 7/8
                            scaled_bomb = pygame.transform.scale(self.assets['bomb'], (int(50 * multiplier), int(50*multiplier)))
                            self.screen.blit(scaled_bomb, (obj.position[0] + 25 * (1 - multiplier), obj.position[1] + 25 * (1-multiplier)))
                        if isinstance(obj, Explosion):
                            self.screen.blit(self.assets['explosion'], obj.position)
                            obj.duration -= 3
                            if obj.duration <= 0:
                                remove_objects.append(obj)
                    for obj in remove_objects:
                        self.objects.remove(obj)
                pygame.display.update()
                self.clock.tick(60)

                # Movement flags changed, send update to the server
                if crt_flags != self.player.movement_flags:
                    self.server.sendall(Encoder.encode_int(CMSG_MOVE_UPDATE))
                    self.server.sendall(Encoder.encode_int(self.player.guid))
                    self.server.sendall(Encoder.encode_int(self.player.movement_flags))
            except:
                self.quit = True
        self.close()

    def close(self):
        if self.packet_receiver:
            self.packet_receiver.join(1)
        self.server.close()
        pygame.quit()
        quit()

