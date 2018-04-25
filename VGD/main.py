import sys

from client.gameclient import GameClient
from server.server import GameServer

def main(cmd, players = 1):
    players = int(players)
    if cmd == 'server':
        game_server = GameServer('localhost', int(5005))
        game_server.connect(players)
    if cmd == 'client':
        client = GameClient('localhost', int(5005))
        client.connect()

if __name__ == '__main__':
    main(*sys.argv[1:])