import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8080);
            System.out.println("Connected to server");

            //separate thread for sending messages to server
            new Thread(() -> {
                try {
                    while (socket.isConnected()) {
                        Scanner scanner = new Scanner(System.in);

                        //read message from console
                        String message = scanner.nextLine();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                        //encrypt message
                        message = railFenceCipher.encrypt(message, 3);
                        writer.write(message);
                        writer.newLine();
                        writer.flush();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
