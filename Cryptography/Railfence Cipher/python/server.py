
import ast
import socket
import threading
import json


PORT = 4000
HEADER = 1024
FORMAT = "utf-8"
MAX_CLIENT = 2
DISCONNECT_MESSAGE = "!DISCONNECTED!"
FIRST_CONNECTION = "!FIRST_CONNECTION!"
SERVER = socket.gethostbyname(socket.gethostname())
ADDRESS = (SERVER, PORT)

# creates a socket
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(ADDRESS)

USERS_LIST = {}
USERS_LIST2 = {}

# Decrypting Rail Fence Cipher from client


def decryptRailFence(cipher, key):
    # create the matrix to cipher
    # plain text key = rows , length(text) = columns
    # filling the rail matrix to distinguish filled
    # spaces from blank ones
    rail = [['\n' for i in range(len(cipher))]
            for j in range(key)]

    # to find the direction
    dir_down = None
    row, col = 0, 0

    # mark the places with '*'
    for i in range(len(cipher)):
        # check the direction of flow
        if row == 0:
            dir_down = True
        if row == key - 1:
            dir_down = False

        # place the marker
        rail[row][col] = '*'
        col += 1

        # find the next row using
        # direction flag
        if dir_down:
            row += 1
        else:
            row -= 1

    # now we can construct the
    # fill the rail matrix
    index = 0
    for i in range(key):
        for j in range(len(cipher)):
            if ((rail[i][j] == '*') and
                    (index < len(cipher))):
                rail[i][j] = cipher[index]
                index += 1

    # now read the matrix in
    # zig-zag manner to construct
    # the resultant text
    result = []
    row, col = 0, 0
    for i in range(len(cipher)):

        # check the direction of flow
        if row == 0:
            dir_down = True
        if row == key - 1:
            dir_down = False

        # place the marker
        if (rail[row][col] != '*'):
            result.append(rail[row][col])
            col += 1

        # find the next row using
        # direction flag
        if dir_down:
            row += 1
        else:
            row -= 1
    return ("".join(result))


def decodeMessage(str, client_address):
    client_object = json.loads(str)
    global USERS_LIST
    global USERS_LIST2

    print(str)
    if client_object["msg"] == FIRST_CONNECTION:
        # for first connection stores the name of the user corresponding to the client address
        USERS_LIST[client_address] = client_object["name"]
        return f"joined the server."
    else:
        USERS_LIST2[client_address] = client_object["key"]
        msg = decryptRailFence(client_object["msg"], USERS_LIST2[client_address])

        return msg

def handleClient(client_connection, client_address):
    print(f"[NEW CONNECTION] {client_address} connected.\n")
    global USERS_LIST2
    global USERS_LIST

    connected = True
    while connected:
        str = client_connection.recv(HEADER).decode(FORMAT)
        if len(str) == 0:
            continue

        msg = decodeMessage(str, client_address)
        if msg == DISCONNECT_MESSAGE:
            connected = False
            print(f"{USERS_LIST[client_address]} is offline now.")
            continue

        print(f"{USERS_LIST[client_address]} : {msg}")

    client_connection.close()


def start():
    server.listen(MAX_CLIENT)
    print(f"[LISTENING]  server is listening on {SERVER}\n")
    connected = True
    while connected:
        client_connection, client_address = server.accept()
        thread = threading.Thread(target=handleClient, args=(
            client_connection, client_address))
        thread.start()

        print(f"[ACTIVE CONNECTIONS] {threading.active_count()-1}\n")


print("[STARTING] server is starting...\n")
start()
