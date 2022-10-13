import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static ServerSocket serverSocket;
    static DataOutputStream dos;

    /**
     * Method for sending file
     */
    public static void sendFile() {
        new JFXPanel();
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                try {
                    FileInputStream fis = new FileInputStream(selectedFile);
                    byte[] buffer = new byte[1];

                    int prevProgress = 0;

                    System.out.print("\nProgress:-\n[");
                    //sending file in chunks
                    while (fis.read(buffer) > 0) {
                        dos.write(buffer);
                        dos.flush();

                        //progress
                        int progress = (int) ((fis.getChannel().position() * 70) / selectedFile.length());
                        if (progress != prevProgress) {
                            System.out.print("=");
                            prevProgress = progress;
                        }
                    }
                    System.out.println("]");

                    dos.close();
                    fis.close();
                    System.out.println("\nFile sent");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Main method
     * @param args command line arguments
     */
    public static void main(String[] args) {

        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server is running on port 8080");

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                dos = new DataOutputStream(socket.getOutputStream());


                new Thread(() -> {
                    try {
                        sendFile();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

    }
}
