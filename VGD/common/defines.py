BASE_HEALTH = 3

# Movement Flags
MOVEMENT_FLAG_NONE              = 0x0000
MOVEMENT_FLAG_UP                = 0x0001
MOVEMENT_FLAG_DOWN              = 0x0002
MOVEMENT_FLAG_LEFT              = 0x0004
MOVEMENT_FLAG_RIGHT             = 0x0008

# ServerMessages
SMSG_INIT_DATA                  = 0x0001
SMSG_UPDATE_POSITION            = 0x0002
SMSG_DETONATE_BOMB              = 0x0003
SMSG_PUT_BOMB_ACK               = 0x0004
SMSG_INIT_PLAYERS               = 0x0005
SMSG_REMOVE_INVULNERABILITY     = 0x0006

# ClientMessages
CMSG_MOVE_UPDATE                = 0x0001
CMSG_PUT_BOMB                   = 0x0002

# Keys
KEY_UP      = 119
KEY_DOWN    = 115
KEY_LEFT    = 97
KEY_RIGHT   = 100

start_positions = {
    1: (50, 50),
    2: (550, 450)
}