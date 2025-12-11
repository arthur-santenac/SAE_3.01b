import java.io.*;
import java.net.*;

public class Server {

    private final Object lock = new Object();

    public void mainServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Session session = new Session(this, clientSocket);
                    session.start();
                } catch (IOException e) {
                    System.err.println("Erreur de connexion : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server().mainServer(5557);
    }
}

class Session extends Thread {
    private final Server server;
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Session(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                System.out.println(line);
            }
        } catch (IOException e) {

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}