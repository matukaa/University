from common.object import Object


class Explosion(Object):
    def __init__(self, position, duration):
        super().__init__(0)
        self.position = position
        self.duration = duration