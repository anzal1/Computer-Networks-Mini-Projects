import socket
import threading
import json


PORT = 5000
HEADER = 1024
FORMAT = "utf-8"
MAX_CLIENT = 5
DISCONNECT_MESSAGE = "!CLIENT_DISCONNECTED!"
FIRST_CONNECTION = "!CLIENT_FIRST_CONNECTION!"
SERVER = socket.gethostbyname(socket.gethostname())
ADDRESS = (SERVER, PORT)


server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(ADDRESS)


user_list = {}


chat_rooms = {}


def sendMessage(msg, client_connection):
    client_connection.send(msg.encode(FORMAT))


def broadcastMessage(msg, client_address):
    target_chat_room_id = user_list[client_address]['chat_room_id']
    for user_address in user_list:
        if client_address == user_address:
            continue
        if target_chat_room_id != user_list[user_address]['chat_room_id']:
            continue
        sendMessage(msg, user_list[user_address]['connection'])


def decodeMessage(str, client_address, client_connection):
    client_object = json.loads(str)

    if client_object['msg'] == FIRST_CONNECTION:
        global user_list
        user_list[client_address] = {
            'name': client_object['name'],
            'chat_room_id': client_object['chat_room_id'],
            'connection': client_connection,
        }

        if chat_rooms.get(client_object['chat_room_id']) == None:
            chat_rooms[client_object['chat_room_id']] = ""

        sendMessage(
            chat_rooms[client_object['chat_room_id']], client_connection)
        return f"[JOINED THE SERVER]"
    else:
        return client_object['msg']


def handleClient(client_connection, client_address):
    global chat_rooms
    print(f"[NEW CONNECTION] {client_address} connected.\n")

    connected = True
    while connected:
        str = client_connection.recv(HEADER).decode(FORMAT)
        if len(str) == 0:
            continue

        msg = decodeMessage(str, client_address, client_connection)

        if msg == DISCONNECT_MESSAGE:
            connected = False
            msg = f"{user_list[client_address]['name']} is offline now."
            chat_rooms[user_list[client_address]['chat_room_id']] += "\n"+msg
            broadcastMessage(msg, client_address)

            sendMessage(DISCONNECT_MESSAGE, client_connection)
            continue

        msg = f"{user_list[client_address]['name']} : {msg}"
        chat_rooms[user_list[client_address]['chat_room_id']] += "\n"+msg
        broadcastMessage(msg, client_address)

    del user_list[client_address]
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
