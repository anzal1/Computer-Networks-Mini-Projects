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

    // Constants
    public static final String METHOD_GET_ID = "get id";
    public static final String METHOD_NEW_USER = "new user";
    public static final String METHOD_SEND_MSG = "send message";

    public static final String KEY_TYPE = "type";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_MESSAGE = "message";

    ServerSocket serverSocket;

    // List of clients
    List<Client> clients;

    Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.clients = new ArrayList<>();
    }


    /**
     * Method to start the server
     */
    public void startServer() {
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
                clients.add(client);

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
        String senderId = map.get(KEY_USER_ID);
        String senderName = map.get(KEY_USER_NAME);

        //searching for the client
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).clientId.equals(senderId)) {
                clients.get(i).clientName = senderName;
                break;
            }
        }
        System.out.println("Client Connected: " + senderName);

        //broadcast the new client to all the clients
        broadcastMessage("joined the chat", senderId, senderName);
    }


    /**
     * Method to send a message to all the clients
     *
     * @param map map containing the message information
     */
    private void sendChat(Map<String, String> map) {
        String senderId = map.get(KEY_USER_ID);
        String senderName = map.get(KEY_USER_NAME);
        String msg = map.get(KEY_MESSAGE);

        //broadcast the message to all the clients
        broadcastMessage(msg, senderId, senderName);
    }


    /**
     * Method to disconnect a client
     *
     * @param client client to disconnect
     */
    public void disconnectClient(Client client) {
        //client disconnected before without entering a name
        if (client.clientName == null) {
            return;
        }

        System.out.println("Client Disconnected: " + client.clientName);

        //remove the client from the list
        clients.remove(client);

        //broadcast the client disconnection to all the clients
        broadcastMessage("left the chat", client.clientId, client.clientName);
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
    public void broadcastMessage(String message, String senderId, String senderName) {

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
