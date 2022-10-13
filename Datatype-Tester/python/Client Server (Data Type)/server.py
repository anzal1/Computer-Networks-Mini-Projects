
import socket
import threading
import json
import re



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


def decodeMessage(str, client_address):
    client_object = json.loads(str)
    if client_object['msg'] == FIRST_CONNECTION:
        # for first connection stores the name of the user corresponding to the client address
        USERS_LIST[client_address] = client_object['name']
        return f"joined the server."
    else:
        return client_object['msg']


def check(message):
    if re.match(r'^[0-9]+$', message):
        print("Number")
    if re.match(r'^[a-zA-Z ]+$', message):
        print("String")
    if re.match(r'^[0-9]+\.[0-9]+$', message):
        print("Float")
    if re.match(r'^[0-9]+[a-zA-Z]+$', message):
        print("Number and String")
    if re.match(r'^[a-zA-Z]+[0-9]+$', message):
        print("Number and String")    


def handleClient(client_connection, client_address):
    print(f"[NEW CONNECTION] {client_address} connected.\n")

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
        check(msg)

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
