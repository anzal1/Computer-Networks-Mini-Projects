import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {

    //variables for the client
    static Socket socket;
    static String clientId;
    static String roomId;
    static String clientName;
    static BufferedReader reader;
    static BufferedWriter writer;


    /**
     * Method to send a message to the server
     */
    public static void sendMessageToServer() {
        try {
            //get message from console to send
            Scanner scanner = new Scanner(System.in);
            String msg;
            while (socket.isConnected()) {
                msg = scanner.nextLine();
                Map<String, String> payload = new HashMap<>();
                payload.put(Server.KEY_MESSAGE, msg);
                payload.put(Server.KEY_USER_ID, clientId);
                payload.put(Server.KEY_USER_NAME, clientName);
                payload.put(Server.KEY_ROOM_ID, String.valueOf(roomId));
                payload.put(Server.KEY_TYPE, Server.METHOD_SEND_MSG);

                writer.write(payload.toString());
                writer.newLine();
                writer.flush();
            }
        } catch (Exception e) {
            System.out.println("Error sending message to server");
        }
    }


    /**
     * Method to read messages from the server
     */
    public static void listenFromServer() {

        while (socket.isConnected()) {
            try {
                //read data from server
                String data = reader.readLine();

                //parse the data to a map
                Map<String, String> payload = Utils.messageToMap(data);

                //if method get id
                if (payload.get(Server.KEY_TYPE).equals(Server.METHOD_GET_ID)) {
                    getClientIdFromServer(payload);
                }
                //if method send message
                else if (payload.get(Server.KEY_TYPE).equals(Server.METHOD_SEND_MSG)) {
                    getMessageFromServer(payload);
                }
                //if method create room
                else if (payload.get(Server.KEY_TYPE).equals(Server.METHOD_CREATE_ROOM)) {
                    getRoomIdFromServer(payload);
                }
                //if method join room
                else if (payload.get(Server.KEY_TYPE).equals(Server.METHOD_JOIN_ROOM)) {
                    joinRoomFromServer(payload);
                }
            } catch (Exception e) {
                System.out.println("Server Disconnected");
                System.exit(0);
            }
        }

    }


    /**
     * Method to get id from the server
     *
     * @param payload payload to get the id from
     */
    private static void getClientIdFromServer(Map<String, String> payload) {
        //get the id from the payload
        clientId = payload.get(Server.KEY_USER_ID);

        //now show the menu
        showChoices();
    }


    /**
     * Method for getting a message from the server
     *
     * @param payload payload to get the message from
     */
    private static void getMessageFromServer(Map<String, String> payload) {
        String sender = payload.get(Server.KEY_USER_NAME);
        String msg = payload.get(Server.KEY_MESSAGE);
        System.out.println(sender + ": " + msg);
    }


    /**
     * Method to get room id from the server
     *
     * @param payload payload to get the room id from
     */
    public static void getRoomIdFromServer(Map<String, String> payload) {
        //get the room id from the payload
        roomId = payload.get(Server.KEY_ROOM_ID);
        System.out.println("Room " + roomId + " created");

        //now get the name of the user
        getName();

        //start sending messages to the server
        new Thread(Client::sendMessageToServer).start();
        System.out.println("Entered in the chat room");
    }


    /**
     * Method to join a room from the server
     *
     * @param payload payload to get the room id from
     */
    public static void joinRoomFromServer(Map<String, String> payload) {
        //get message from the payload
        String msg = payload.get(Server.KEY_MESSAGE);
        roomId = payload.get(Server.KEY_ROOM_ID);

        //if room exists
        if (msg.equals("fail")) {
            System.out.println("Room not found");

            //show choices again
            showChoices();

            // don't go further after showChoices recursion call returns
            return;
        } else {
            System.out.println("Room " + roomId + " joined");
        }

        //now get the name of the user
        getName();

        //start sending messages to the server
        new Thread(Client::sendMessageToServer).start();

        System.out.println("Entered in the chat room");
    }

    /**
     * Method to send a new user to the server
     */
    public static void getName() {
        //get name from console
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your name");
        clientName = scanner.nextLine();

        try {
            //send the name to the server
            Map<String, String> payload = new HashMap<>();
            payload.put(Server.KEY_USER_ID, clientId);
            payload.put(Server.KEY_USER_NAME, clientName);
            payload.put(Server.KEY_ROOM_ID, String.valueOf(roomId));
            payload.put(Server.KEY_TYPE, Server.METHOD_NEW_USER);
            writer.write(payload.toString());
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            System.out.println("Error sending name to server");
        }
    }


    /**
     * Method to show the choices to the user
     */
    public static void showChoices() {
        System.out.println("1. Create Room");
        System.out.println("2. Join Room");
        System.out.println("3. Exit");

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                createRoom();
                break;
            case 2:
                joinRoom();
                break;
            case 3:
                System.exit(0);
                break;
            default: {
                System.out.println("Invalid choice");
                showChoices();
            }
        }

    }


    /**
     * Method to create a room
     */
    public static void createRoom() {
        try {
            //send req to the server to create a room
            Map<String, String> payload = new HashMap<>();
            payload.put(Server.KEY_TYPE, Server.METHOD_CREATE_ROOM);
            writer.write(payload.toString());
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            System.out.println("Error sending request to create room");
        }
    }


    /**
     * Method to join a room
     */
    public static void joinRoom() {
        //get room id from console
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter room id: ");
        String roomId = scanner.nextLine();
        try {
            //send req to the server to join a room
            Map<String, String> payload = new HashMap<>();
            payload.put(Server.KEY_TYPE, Server.METHOD_JOIN_ROOM);
            payload.put(Server.KEY_ROOM_ID, roomId);
            writer.write(payload.toString());
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            System.out.println("Error sending request to join room");
        }
    }

    /**
     * Main method to start the client
     */
    public static void main(String[] args) {
        Socket socket;
        try {
            //connect to the server
            socket = new Socket("localhost", 8080);
            System.out.println("Connected to server");
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Client.socket = socket;
            Client.reader = reader;
            Client.writer = writer;

            //start listening to the server
            listenFromServer();
        } catch (Exception e) {
            System.out.println("Error connecting to server");
        }
    }
}
