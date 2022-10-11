import java.io.*;
import java.net.Socket;

public class Client {

    static Socket socket;
    
    public static void main(String[] args) {
        try {
            socket = new Socket("localhost", 1234);
            System.out.println("Connected to Server");

            // Separate Thread for reading from the server
            new Thread(() -> {
                //infinite loop to keep reading from the server
                while (socket.isConnected()) {
                    try {
                        // Create an input stream to receive data from the server
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String str = bufferedReader.readLine();

                        // Display to the console
                        System.out.println("Server: " + str);
                    } catch (Exception e) {
                        System.out.println("Server Disconnected");

                        // Close the socket
                        closeSocket(socket);
                    }
                }
            }).start();

            // Separate Thread for writing to the server
            new Thread(() -> {
                //infinite loop to keep writing to the server
                while (socket.isConnected()) {
                    try {
                        // Create an input stream to receive data from the console
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

                        // Create an output stream to send data to the server
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        bufferedWriter.write(bufferedReader.readLine());
                        bufferedWriter.newLine();

                        // Flush the stream, it is important to do this
                        bufferedWriter.flush();
                    } catch (Exception e) {
                        System.out.println("Error Writing To Server");

                        // Close the socket
                        closeSocket(socket);
                    }
                }
            }).start();
        } catch (IOException e) {
            System.out.println("Error Connecting To Server");
        }
    }

    public static void closeSocket(Socket socket) {
        try {
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Error Closing Socket");
        }
    }
}
