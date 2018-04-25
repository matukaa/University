import random
from cmath import sqrt

from common.bomb import Bomb
from common.player import Player
from common.defines import *


class GameEngine:
    EMPTY       = 0
    WALL        = 1
    OBSTACLE    = 2

    def __init__(self):
        self.raw_map = []
        self.players = []
        self.objects = []
        self.height = 11
        self.width = 13
        self.generate_map()

    def generate_map(self):
        for i in range(self.height):
            self.raw_map.append([0 for _ in range(self.width)])

        for i in range(self.height):
            for j in range(self.width):
                if j == 0 or i == 0 or j == self.width - 1 or i == self.height - 1:
                    self.raw_map[i][j] = GameEngine.WALL
                elif j % 2 == 0 and i % 2 == 0:
                    self.raw_map[i][j] = GameEngine.WALL
                elif random.randrange(100) < 50:
                    self.raw_map[i][j] = GameEngine.OBSTACLE

        self.raw_map[1][1] = self.raw_map[1][2] = self.raw_map[2][1] = GameEngine.EMPTY
        self.raw_map[self.height-2][self.width-2] = self.raw_map[self.height-2][self.width-3] = self.raw_map[self.height-3][self.width-2] = GameEngine.EMPTY

        for i in range(self.height):
            print(self.raw_map[i])

    def check_movement(self, player):
        up = down = left = right = True
        for i in range(self.height):
            for j in range(self.width):
                if self.raw_map[i][j]:
                    if i * 50 < player.position[1] - 1 < (i+1) * 50 and j * 50 - 10 < player.position[0] + 25 < (j + 1) * 50 + 10:
                        up = False
                    if i * 50 < player.position[1] + 51 < (i + 1) * 50 and j * 50 - 10 < player.position[0] + 25 < (j + 1) * 50 + 10:
                        down = False
                    if j * 50 < player.position[0] - 1 < (j + 1) * 50 and i * 50 - 10 < player.position[1] + 25 < (i + 1) * 50 + 10:
                        left = False
                    if j * 50 < player.position[0] + 51 < (j + 1) * 50 and i * 50 - 10 < player.position[1] + 25 < (i + 1) * 50 + 10:
                        right = False

        for obj in self.objects:
            if isinstance(obj, Bomb):
                if obj.timer < 300 or obj.done:
                    continue
                p = obj.position
                if p[1] < player.position[1] - 1 < p[1] + 50 and p[0] - 10 < player.position[0] + 25 < p[0] + 60:
                    up = False
                if p[1] < player.position[1] + 51 < p[1] + 50 and p[0] - 10 < player.position[0] + 25 < p[0] + 60:
                    down = False
                if p[0] < player.position[0] - 1 < p[0] + 50 and p[1] - 10 < player.position[1] + 25 < p[1] + 60:
                    left = False
                if p[0] < player.position[0] +51 < p[0] + 50 and p[1] - 10 < player.position[1] + 25 < p[1] + 60:
                    right = False
        return up, down, left, right

    def handle_player_movement(self, player):
        up, down, left, right = self.check_movement(player)
        if player.movement_flags & MOVEMENT_FLAG_UP and up:
            player.position = (player.position[0], player.position[1] - 1)
        if player.movement_flags & MOVEMENT_FLAG_DOWN and down:
            player.position = (player.position[0], player.position[1] + 1)
        if player.movement_flags & MOVEMENT_FLAG_RIGHT and right:
            player.position = (player.position[0] + 1, player.position[1])
        if player.movement_flags & MOVEMENT_FLAG_LEFT and left:
            player.position = (player.position[0] - 1, player.position[1])

    def update_objects(self):
        for obj in self.objects:
            if isinstance(obj, Player):
                self.handle_player_movement(obj)

    @staticmethod
    def get_distance(pos1, pos2):
        return sqrt((pos1[0] - pos2[0]) ** 2 + (pos1[1] - pos2[1]) ** 2).real

    def get_closest_tile(self, pos_x, pos_y):
        center_pos = (pos_x + 25, pos_y + 25)
        min_dist = 9999999
        best_pos = None
        for i in range(self.height):
            for j in range(self.width):
                if self.raw_map[i][j] != GameEngine.EMPTY:
                    continue
                dst = self.get_distance(center_pos, (j * 50 + 25, i * 50 + 25))
                if dst < min_dist:
                    min_dist = dst
                    best_pos = (j * 50, i * 50)

        return best_pos

    def get_bomb_hit_units(self, bomb):
        p = bomb.position
        hit_players = []
        for player in self.players:
            if player.invulnerability_timer > 0:
                continue
            if p[0] == player.position[0] and p[1] == player.position[1]:
                hit_players.append(player.guid)
                player.healths -= 1
                player.invulnerability_timer = 3000
                continue
            if p[1] > player.position[1] > p[1] - 100 and p[0] - 10 < player.position[0] + 25 < p[0] + 60:
                hit_players.append(player.guid)
                player.healths -= 1
                player.invulnerability_timer = 3000
                continue
            if p[1] < player.position[1] < p[1] + 100 and p[0] - 10 < player.position[0] + 25 < p[0] + 60:
                hit_players.append(player.guid)
                player.healths -= 1
                player.invulnerability_timer = 3000
                continue
            if p[0] > player.position[0] > p[0] - 100 and p[1] - 10 < player.position[1] + 25 < p[1] + 60:
                hit_players.append(player.guid)
                player.healths -= 1
                player.invulnerability_timer = 3000
                continue
            if p[0] < player.position[0] < p[0] + 99 and p[1] - 10 < player.position[1] + 25 < p[1] + 60:
                hit_players.append(player.guid)
                player.healths -= 1
                player.invulnerability_timer = 3000
                continue
        return hit_players

    def get_bomb_hit_walls(self, bomb):
        j = int(bomb.position[0] / 50)
        i = int(bomb.position[1] / 50)
        hit_walls = []
        if i - 1 >= 0 and self.raw_map[i - 1][j] == GameEngine.OBSTACLE:
            hit_walls.append((i - 1, j))
            self.raw_map[i - 1][j] = GameEngine.EMPTY
        if i + 1 <= 10 and self.raw_map[i + 1][j] == GameEngine.OBSTACLE:
            hit_walls.append((i + 1, j))
            self.raw_map[i + 1][j] = GameEngine.EMPTY
        if j - 1 >= 0 and self.raw_map[i][j - 1] == GameEngine.OBSTACLE:
            hit_walls.append((i, j - 1))
            self.raw_map[i][j - 1] = GameEngine.EMPTY
        if j + 1 <= 12 and self.raw_map[i][j + 1] == GameEngine.OBSTACLE:
            hit_walls.append((i, j + 1))
            self.raw_map[i][j + 1] = GameEngine.EMPTY
        return hit_walls
