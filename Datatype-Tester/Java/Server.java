import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    ServerSocket serverSocket;

    public static String getDatatype(String input) {
        //check for int
        if (input.matches("^[0-9]+$")) {
            return "int";
        }
        //check for float
        else if (input.matches("^[0-9]+\\.[0-9]+$")) {
            return "float";
        }
        //check for alphabets
        else if (input.matches("^[a-zA-Z]+$")) {
            return "alphabets";
        }
        //check for alphanumeric
        else if (input.matches("^[a-zA-Z0-9]+$")) {
            return "alphanumeric";
        }
        //check for special characters
        else if (input.matches("^[^a-zA-Z0-9]+$")) {
            return "special characters";
        }
        else {
            return "unknown";
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Server started");
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");

            new Thread(() -> {
                while (socket.isConnected()) {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        String line = reader.readLine();

                        //System.out.println("Client: " + line);
                       String datatype = getDatatype(line);
                        writer.write(datatype);
                        writer.newLine();
                        writer.flush();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            System.out.println("Error starting server");
        }
    }
}
