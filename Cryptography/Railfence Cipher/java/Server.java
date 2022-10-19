import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static ServerSocket serverSocket;
    static Socket socket;
    public static void main(String[] args) {
        try{
            serverSocket = new ServerSocket(8080);


        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
