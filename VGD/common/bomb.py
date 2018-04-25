from common.object import Object


class Bomb(Object):
    def __init__(self, guid, position):
        super().__init__(guid)
        self.position = position
        self.timer = 0
        self.sent = False
        self.done = False