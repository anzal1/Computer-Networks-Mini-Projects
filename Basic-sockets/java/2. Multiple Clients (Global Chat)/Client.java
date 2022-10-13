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
                    getIdFromServer(payload);
                }
                //if method send message
                else if (payload.get(Server.KEY_TYPE).equals(Server.METHOD_SEND_MSG)) {
                    getMessageFromServer(payload);
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
    private static void getIdFromServer(Map<String, String> payload) {
        clientId = payload.get(Server.KEY_USER_ID);

        getName();
        new Thread(Client::sendMessageToServer).start();

        System.out.println("Entered the global chat");
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
     * Method to send a new user to the server
     */
    public static void getName() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your name: ");
        clientName = scanner.nextLine();
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put(Server.KEY_USER_ID, clientId);
            payload.put(Server.KEY_USER_NAME, clientName);
            payload.put(Server.KEY_TYPE, Server.METHOD_NEW_USER);
            writer.write(payload.toString());
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Main method to start the client
     */
    public static void main(String[] args) {
        Socket socket;
        try {
            socket = new Socket("localhost", 8080);
            System.out.println("Connected to server");
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Client.socket = socket;
            Client.reader = reader;
            Client.writer = writer;
            listenFromServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
