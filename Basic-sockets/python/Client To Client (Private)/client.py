from datetime import datetime
import socket
import json
import threading


PORT = 5000
HEADER = 1024
FORMAT = "utf-8"
DISCONNECT_MESSAGE = "!DISCONNECTED!"
FIRST_CONNECTION = "!FIRST_CONNECTION!"
STOP_THREAD = False
SERVER = socket.gethostbyname(socket.gethostname())
ADDRESS = (SERVER, PORT)


client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect(ADDRESS)


def sendMessage(msg):
    client.send(msg.encode(FORMAT))


user_name = input("Enter your name : ")
chat_room_id = input("Enter chat room id you want to join : ")

json_object = {'name': user_name,
               'msg': FIRST_CONNECTION,
               'chat_room_id': chat_room_id}

msg = json.dumps(json_object)
sendMessage(msg)

all_chats = f"_____[JOINED ROOM ID : {chat_room_id}]_____"


def showChatMessages():

    print(all_chats)


def serverMessage():
    global STOP_THREAD
    global all_chats
    while not STOP_THREAD:
        msg = client.recv(HEADER).decode(FORMAT)
        if len(msg) == 0:
            continue
        if msg == DISCONNECT_MESSAGE:
            break
        all_chats += "\n\n"+msg
        showChatMessages()


def addTimeStampToMessage(msg):

    date_time = datetime.fromtimestamp(datetime.timestamp(datetime.now()))
    str_date_time = date_time.strftime("%d-%m-%Y, %H:%M:%S")
    msg += " \t  "+str_date_time
    return msg


connected = True
while connected:
    thread = threading.Thread(target=serverMessage)
    thread.start()
    msg = input()
    if msg == DISCONNECT_MESSAGE:

        STOP_THREAD = True
        connected = False
        json_object = {'msg': msg}
        msg = json.dumps(json_object)
        sendMessage(msg)
        thread.join()

    msg = addTimeStampToMessage(msg)
    all_chats += "\n\nYou : "+msg
    json_object = {'msg': msg}
    msg = json.dumps(json_object)
    showChatMessages()
    sendMessage(msg)


client.close()
print("[CLOSED]")
