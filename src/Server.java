import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private final Object lock = new Object();
    private Map<String, Session> listeConnectes = new HashMap<>();

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

    public void ajouterConnecte(String identifiant, Session session) {
        this.listeConnectes.put(identifiant, session);
    }

    public Map<String, Session> getListeConnectes() {
        return listeConnectes;
    }

    public boolean isConnected(String identifiant) {
        return listeConnectes.containsKey(identifiant);
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
    private String identifiant = null;

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
                        out.println("ERR usage: register <numJoueur> <motDePasse>");
                    }
                } else if (commande[0].equals("connect")) {
                    if (commande.length == 3) {
                        this.identifiant = commande[1];
                        this.server.ajouterConnecte(identifiant, this);
                    } else {
                        out.println("ERR usage: connect <numJoueur> <motDePasse>");
                    }
                } else if (commande[0].equals("play")) {
                    if (commande.length == 3) {

                    } else {
                        out.println("ERR usage: play <caseSource> <caseDestination>");
                    }
                } else if (commande[0].equals("leave")) {
                    if (commande.length == 1) {

                    } else {
                        out.println("ERR usage: leave");
                    }
                } else if (commande[0].equals("quit")) {
                    if (commande.length == 1) {

                    } else {
                        out.println("ERR usage: quit");
                    }
                } else if (commande[0].equals("replay")) {
                    if (commande.length == 1) {

                    } else {
                        out.println("ERR usage: replay");
                    }
                } else if (commande[0].equals("new")) {
                    if (commande.length == 1) {

                    } else {
                        out.println("ERR usage: new");
                    }
                } else if (commande[0].equals("ask")) {
                    if (commande.length == 2) {

                    } else {
                        out.println("ERR usage: ask <numJoueur>");
                    }
                } else if (commande[0].equals("accept")) {
                    if (commande.length == 2) {

                    } else {
                        out.println("ERR usage: accept <numJoueur>");
                    }
                } else if (commande[0].equals("players")) {
                    if (commande.length == 1) {
                        System.out.println("Ok\n");
                        String joueursCo = "";
                        for(String id: this.server.getListeConnectes().keySet()){
                            joueursCo+= id+",";
                        }
                        String[] res = joueursCo.split(",");
                        for(String id :res){
                            System.out.println(id+"\n");
                        }
                        

                    } else {
                        out.println("ERR usage: players");
                    }
                } else if (commande[0].equals("save")) {
                    if (commande.length == 1) {

                    } else {
                        out.println("ERR usage: save");
                    }
                } else if (commande[0].equals("list_games")) {
                    if (commande.length == 1) {

                    } else {
                        out.println("ERR usage: list_games");
                    }
                } else if (commande[0].equals("load")) {
                    if (commande.length == 2) {

                    } else {
                        out.println("ERR usage: load <idPartie>");
                    }
                } else if (commande[0].equals("help")) {
                    if (commande.length == 1) {
                        out.println("- register <numJoueur> <motDePasse>\n- connect <numJoueur> <motDePasse>\n- play <caseSource> <caseDestination>\n- leave\n- quit\n- replay\n- new\n- ask <numJoueur>\n- accept <numJoueur>\n- players\n- save\n- list_games\n- load <idPartie>");
                    } else {
                        out.println("ERR usage: help");
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