from common.defines import *
from common.object import Object


class Player(Object):
    def __init__(self, guid, client = None):
        super().__init__(guid)
        self.movement_flags = 0
        self.healths = BASE_HEALTH
        self.bomb_timer = 0
        self.client = client
        self.invulnerability_timer = 0

    def get_movement(self):
        flags = ''
        if self.movement_flags & MOVEMENT_FLAG_UP:
            flags = flags + 'up '
        if self.movement_flags & MOVEMENT_FLAG_DOWN:
            flags = flags + 'down '
        if self.movement_flags & MOVEMENT_FLAG_LEFT:
            flags = flags + 'left '
        if self.movement_flags & MOVEMENT_FLAG_RIGHT:
            flags = flags + 'right '
        return flags
