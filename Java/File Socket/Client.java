import java.io.*;
import java.net.Socket;

public class Client {
    static Socket socket;

    public static void main(String[] args) {
        try {
            socket = new Socket("localhost", 8080);
            System.out.println("Connected to server");

            new Thread(() -> {
                    try {
                        //read file
                        DataInputStream dis = new DataInputStream(socket.getInputStream());
                        byte[] buffer = new byte[1];

                        //print file
                        while (dis.read(buffer) > 0) {
                            //progress
                            System.out.print(new String(buffer));
                        }

                        System.out.println("\n--- End of file ---");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            }).start();

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
