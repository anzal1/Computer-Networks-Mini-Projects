import socket
import json


PORT = 4000
HEADER = 1024
FORMAT = "utf-8"
DISCONNECT_MESSAGE = "!DISCONNECTED!"
SERVER = socket.gethostbyname(socket.gethostname())
ADDRESS = (SERVER, PORT)

# Rail Fence Cipher Encryption


def encryptRailFence(msg, key):
    rail = [['\n' for i in range(len(msg))]
            for j in range(key)]

    # to find the direction
    dir_down = False
    row, col = 0, 0

    for i in range(len(msg)):

        # check the direction of flow
        # reverse the direction if we've just
        # filled the top or bottom rail
        if (row == 0) or (row == key - 1):
            dir_down = not dir_down

        # fill the corresponding alphabet
        rail[row][col] = msg[i]
        col += 1

        # find the next row using
        # direction flag
        if dir_down:
            row += 1
        else:
            row -= 1

    # now we can construct the cipher
    # using the rail matrix
    result = []
    for i in range(key):
        for j in range(len(msg)):
            if rail[i][j] != '\n':
                result.append(rail[i][j])
    return ("" . join(result))


client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect(ADDRESS)


def sendMessage(msg):
    client.send(msg.encode(FORMAT))


user_name = input("Enter your name : ")
json_object = {'name': user_name, 'msg': '!FIRST_CONNECTION!'}

msg = json.dumps(json_object)
sendMessage(msg)

connected = True
while connected:
    msg = input("Enter a message : ")
    key = int(input("Enter the key : "))
    if msg == DISCONNECT_MESSAGE:
        connected = False
    msg=encryptRailFence(msg, key)
    json_object = {"msg": msg, "key": key}
    msg = json.dumps(json_object)
    sendMessage(msg)

client.close()
