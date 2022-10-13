import socket
import json


PORT = 4000
HEADER = 1024
FORMAT = "utf-8"
DISCONNECT_MESSAGE = "!DISCONNECTED!"
SERVER = socket.gethostbyname(socket.gethostname())
ADDRESS = (SERVER, PORT)


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
    if msg == DISCONNECT_MESSAGE:
        connected = False
    json_object = {'msg': msg}
    msg = json.dumps(json_object)
    sendMessage(msg)

client.close()