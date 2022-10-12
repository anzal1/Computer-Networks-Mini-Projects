import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {

        try {
            Socket socket = new Socket("localhost", 5000);
            System.out.println("Connected to server");
            new Thread(() -> {
                while (socket.isConnected()) {
                    try {
                        Scanner scanner = new Scanner(System.in);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        String line = scanner.nextLine();
                        writer.write(line);
                        writer.newLine();
                        writer.flush();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            System.out.println("Error connecting to server");
        }

    }
}
