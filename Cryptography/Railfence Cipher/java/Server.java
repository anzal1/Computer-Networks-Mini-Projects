import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    static ServerSocket serverSocket;
    static Socket socket;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server started");
            socket = serverSocket.accept();
            System.out.println("Client connected");

            //separate thread for listening to client
            new Thread(() -> {
                while (socket.isConnected()) {

                    try {
                        //read message from client
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String message = reader.readLine();
                        System.out.println("\nEncrypted message: " + message);

                        System.out.println("Enter decryption key: ");
                        //read key from console
                        Scanner scanner = new Scanner(System.in);
                        int key = scanner.nextInt();

                        //decrypt message
                        message = railFenceCipher.decrypt(message, key);
                        System.out.println("Decrypted message: " + message);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
