import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            // Create a server socket
            serverSocket = new ServerSocket(1234);
            System.out.println("Server is running...");

            // Listen for a connection request
            Socket socket = serverSocket.accept();

            // Separate Thread for reading from the client
            new Thread(() -> {
                //infinite loop to keep reading from the client
                while (socket.isConnected()) {
                    try {
                        // Create an input stream to receive data from the client
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String str = bufferedReader.readLine();

                        // Display to the console
                        System.out.println("Client: " + str);
                    } catch (IOException e) {
                        System.out.println("Client Disconnected");

                        // Close the socket
                        closeSocket(socket);
                    }
                }
            }).start();

            // Separate Thread for writing to the client
            new Thread(() -> {
                //infinite loop to keep writing to the client
                while (socket.isConnected()) {
                    try {
                        // Create an input stream to receive data from the console
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

                        // Create an output stream to send data to the client
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        bufferedWriter.write(bufferedReader.readLine());
                        bufferedWriter.newLine();

                        // Flush the stream, it is important to do this
                        bufferedWriter.flush();
                    } catch (IOException e) {
                        System.out.println("Error Writing To Client");

                        // Close the socket
                        closeSocket(socket);
                    }
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Error Creating Server Socket");
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
