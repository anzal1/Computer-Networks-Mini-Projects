import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    // Client class for storing client information
    static class Client {
        String clientId;
        String clientName;
        BufferedReader reader;
        BufferedWriter writer;
        Socket socket;
    }

    // Room class for storing room information
    static class Room {
        String roomId;
        List<Client> clients;
    }

    // Constants
    public static final String METHOD_GET_ID = "get id";
    public static final String METHOD_NEW_USER = "new user";
    public static final String METHOD_SEND_MSG = "send message";
    public static final String METHOD_CREATE_ROOM = "create room";
    public static final String METHOD_JOIN_ROOM = "join room";

    public static final String KEY_TYPE = "type";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_ROOM_ID = "roomId";

    ServerSocket serverSocket;

    // List of rooms
    List<Room> rooms;

    Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.rooms = new ArrayList<>();
    }


    /**
     * Method to start the server
     */
    public void startServer() {
        //infinite loop for accepting new clients
        while (!serverSocket.isClosed()) {
            try {
                //wait for client to connect
                Socket socket = serverSocket.accept();

                //create a new client
                Client client = new Client();
                client.socket = socket;
                client.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                client.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                //generate a unique id for the client and send it to the client
                client.clientId = Utils.generateId();
                sendIDToClient(client);

                //forward each client to a new thread
                new Thread(() -> {
                    while (!client.socket.isClosed()) {
                        try {
                            //read data from client
                            String data = client.reader.readLine();

                            //parse the data to a map
                            Map<String, String> map = Utils.messageToMap(data);

                            //if method new user
                            if (map.get(KEY_TYPE).equals(METHOD_NEW_USER)) {
                                newClient(map);
                            }
                            //if method send message
                            else if (map.get(KEY_TYPE).equals(METHOD_SEND_MSG)) {
                                sendChat(map);
                            }
                            //if method create room
                            else if (map.get(KEY_TYPE).equals(METHOD_CREATE_ROOM)) {
                                createRoom(client);
                            }
                            //if method join room
                            else if (map.get(KEY_TYPE).equals(METHOD_JOIN_ROOM)) {
                                joinRoom(map, client);
                            }
                        } catch (Exception e) {
                            disconnectClient(client);
                        }
                    }
                }).start();
            } catch (Exception e) {
                System.out.println("Server Disconnected");
            }
        }
    }


    /**
     * Method for sending id to the client
     *
     * @param client client to send the id to
     */
    public void sendIDToClient(Client client) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put(Server.KEY_USER_ID, client.clientId);
            map.put(Server.KEY_USER_NAME, client.clientName);
            map.put(Server.KEY_TYPE, Server.METHOD_GET_ID);

            client.writer.write(map.toString());
            client.writer.newLine();
            client.writer.flush();
        } catch (Exception e) {
            System.out.println("Error sending id to client");
        }
    }


    /**
     * Method to send a unique id to the client
     *
     * @param map map containing the client information
     */
    private void newClient(Map<String, String> map) {
        //get client info from map
        String senderId = map.get(KEY_USER_ID);
        String senderName = map.get(KEY_USER_NAME);
        String roomId = map.get(KEY_ROOM_ID);

        //getting room of the client from the list of rooms
        int roomIndex = 0;
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).roomId.equals(roomId)) {
                roomIndex = i;
                break;
            }
        }

        //setting the client name in client list in the room
        List<Client> clients = rooms.get(roomIndex).clients;
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).clientId.equals(senderId)) {
                clients.get(i).clientName = senderName;
                break;
            }
        }

        //updating the room list as update only happens in the copy of client list
        Room updatedRoom = new Room();
        updatedRoom.roomId = roomId;
        updatedRoom.clients = clients;
        rooms.set(roomIndex, updatedRoom);

        System.out.println("Client Connected: " + senderName);
        //broadcast the new client to all the clients
        broadcastMessage("joined the chat", senderId, senderName, clients);
    }


    /**
     * Method to send a message to all the clients
     *
     * @param map map containing the message information
     */
    private void sendChat(Map<String, String> map) {
        //getting sender info from map
        String senderId = map.get(KEY_USER_ID);
        String senderName = map.get(KEY_USER_NAME);
        String roomId = map.get(KEY_ROOM_ID);
        String msg = map.get(KEY_MESSAGE);

        //getting client list from the room list using room id
        List<Client> clients = null;
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).roomId.equals(roomId)) {
                clients = rooms.get(i).clients;
                break;
            }
        }

        //this will never happen
        if (clients == null) return;

        //broadcast the message to all the clients
        broadcastMessage(msg, senderId, senderName, clients);
    }


    private void createRoom(Client client) {

        //generate a unique room id in range 1000-9999
        String roomId = String.valueOf(Utils.generateRoomId(rooms));

        //create a new room
        Room room = new Room();
        room.roomId = roomId;

        //create a new client list and add the client to it
        room.clients = new ArrayList<>();
        room.clients.add(client);
        rooms.add(room);

        try {
            //send the room id to the client
            Map<String, String> response = new HashMap<>();
            response.put(KEY_TYPE, METHOD_CREATE_ROOM);
            response.put(KEY_ROOM_ID, roomId);
            client.writer.write(response.toString());
            client.writer.newLine();
            client.writer.flush();
        } catch (Exception e) {
            System.out.println("Error sending room id to client");
        }
    }


    private void joinRoom(Map<String, String> map, Client client) {

        //get room id from map to which the client wants to join
        String roomId = map.get(KEY_ROOM_ID);

        //for checking if the room exists
        boolean isRoomExist = false;

        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).roomId.equals(roomId)) {
                isRoomExist = true;
                break;
            }
        }

        //if room exists
        if (isRoomExist) {
            for (int i = 0; i < rooms.size(); i++) {
                if (rooms.get(i).roomId.equals(roomId)) {
                    //add the client to the client list of the room
                    rooms.get(i).clients.add(client);
                    break;
                }
            }

            try {
                //send the room id to the client with success message
                Map<String, String> response = new HashMap<>();
                response.put(KEY_TYPE, METHOD_JOIN_ROOM);
                response.put(KEY_ROOM_ID, roomId);
                response.put(KEY_MESSAGE, "success");
                client.writer.write(response.toString());
                client.writer.newLine();
                client.writer.flush();
            } catch (Exception e) {
                System.out.println("Error sending room id to client");
            }
        }
        //room not exists
        else {
            try {
                //send the null room id to the client with failure message
                Map<String, String> response = new HashMap<>();
                response.put(KEY_TYPE, METHOD_JOIN_ROOM);
                response.put(KEY_MESSAGE, "fail");
                client.writer.write(response.toString());
                client.writer.newLine();
                client.writer.flush();
            } catch (Exception e) {
                System.out.println("Error sending room id to client");
            }
        }
    }

    /**
     * Method to disconnect a client
     *
     * @param client client to disconnect
     */
    public void disconnectClient(Client client) {
        //client disconnected before joining any room
        if (client.clientName == null) {
            return;
        }

        System.out.println("Client Disconnected: " + client.clientName);

        //iterate through all the rooms for searching the client to disconnect
        for (Room room : rooms) {

            //iterate through all the clients in the current room
            List<Client> clients = room.clients;

            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).clientId.equals(client.clientId)) {
                    //remove the client from the client list
                    clients.remove(i);
                    break;
                }
            }

            //update the room list as update only happens in the copy of client list
            Room updatedRoom = new Room();
            updatedRoom.roomId = room.roomId;
            updatedRoom.clients = clients;
            rooms.set(rooms.indexOf(room), updatedRoom);

            //broadcast the client disconnect to all the clients in the room
            broadcastMessage("left the chat", client.clientId, client.clientName, clients);

            //if client list is empty then remove the room from the room list
            if (clients.size() == 0) {
                rooms.remove(room);
                System.out.println("Room " + room.roomId + " deleted");
                break;
            }
        }

        try {
            client.socket.close();

            //terminate the thread of the client
            Thread.currentThread().join();
        } catch (Exception ex) {
            System.out.println("Error in disconnecting client");
        }
    }


    /**
     * Method to broadcast a message to all the clients
     *
     * @param message    message to broadcast
     * @param senderId   id of the sender
     * @param senderName name of the sender
     */
    public void broadcastMessage(String message, String senderId, String senderName, List<Client> clients) {

        for (Client client : clients) {
            //send the message to all the clients except the sender
            if (client.clientId.equals(senderId)) continue;

            try {
                Map<String, String> map = new HashMap<>();
                map.put(Server.KEY_TYPE, Server.METHOD_SEND_MSG);
                map.put(Server.KEY_MESSAGE, message);
                map.put(Server.KEY_USER_NAME, senderName);

                client.writer.write(map.toString());
                client.writer.newLine();
                client.writer.flush();
            } catch (Exception e) {
                System.out.println("Error broadcasting message");
            }
        }
    }


    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            //create a new server socket
            serverSocket = new ServerSocket(8080);
            System.out.println("Server running on port 8080");

            //create a new server
            Server server = new Server(serverSocket);
            server.startServer();
        } catch (Exception e) {
            System.out.println("Error starting server on port 8080");
        }
    }
}
