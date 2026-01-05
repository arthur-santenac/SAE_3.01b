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
        new Server().mainServer(5556);
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
                String[] commande = line.split("\\s+");
                if (commande[0].equals("register")) {
                    if (commande.length == 3) {

                    } else {
                        out.println("ERR usage: register <nom> <mdp>");
                    }
                } else if (commande[0].equals("connect")) {
                    if (commande.length == 3) {

                    } else {
                        out.println("ERR usage: connect <nom> <mdp>");
                    }
                } else if (commande[0].equals("play")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                } else if (commande[0].equals("leave")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                } else if (commande[0].equals("quit")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                } else if (commande[0].equals("replay")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                } else if (commande[0].equals("new")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                } else if (commande[0].equals("ask")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                } else if (commande[0].equals("accept")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                } else if (commande[0].equals("players")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                } else if (commande[0].equals("save")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                } else if (commande[0].equals("list_games")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                } else if (commande[0].equals("load")) {
                    if (commande.length == ) {

                    } else {
                        out.println("ERR usage:");
                    }
                }
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