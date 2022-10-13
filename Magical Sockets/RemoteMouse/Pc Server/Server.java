import java.awt.*;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Objects;

public class Server {
    ServerSocket serverSocket;
    static int x;
    static int y;

    Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                new Thread(() -> {
                    while (socket.isConnected()) {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String message = reader.readLine();
                            controlMouse(message);
                        } catch (Exception e) {
                            disconnectClient(socket);
                            break;
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            System.out.println("Server closed");
        }
    }

    public static void controlMouse(String message) {
//        System.out.println(message);
        String[] coords = message.split(" ");
        String action = coords[0];
        x = MouseInfo.getPointerInfo().getLocation().x;
        y = MouseInfo.getPointerInfo().getLocation().y;
        int dx = Integer.parseInt(coords[1]);
        int dy = Integer.parseInt(coords[2]);

        try {
            Robot robot = new Robot();
            if (Objects.equals(action, "move")) {
                robot.mouseMove(x + dx, y + dy);
            } else if (Objects.equals(action, "leftClick")) {
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            } else if (Objects.equals(action, "rightClick")) {
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            } else if (Objects.equals(action, "up")) {
                if (x + dx >= 0 && x + dx <= 1920) x += dx;
                if (y + dy >= 0 && y + dy <= 1080) y += dy;
            }

        } catch (Exception e) {
            System.out.println("Server closed");
        }

    }

    public static void disconnectClient(Socket socket) {
        System.out.println("Client disconnected");
        try {
            socket.close();
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            Server server = new Server(serverSocket);


            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server is running on " + ip);

            server.startServer();

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
