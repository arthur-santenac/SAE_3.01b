import java.io.*;
import java.net.*;
import java.util.*;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private final Object lock = new Object();
    private Map<String, Session> listeConnectes = new HashMap<>();
    private Map<String, String> listeDemande = new HashMap<>();

    private String Fichier_JOUEURS = "./sauvegarde.json";

    public boolean registerPlayer(String login, String password) {
        synchronized (lock) { 
            Map<String, String> players = chercherPlayers();
            
            if (players.containsKey(login)) {
                return false; 
            }

            players.put(login, password);
            savePlayers(players);
            return true; 
        }
    }

    private Map<String, String> chercherPlayers() {
        Map<String, String> map = new HashMap<>();
        File file = new File(Fichier_JOUEURS);
        if (!file.exists())
            return map;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);

            String content = sb.toString().trim();

            content = content.replace("{", "").replace("}", "").replace("\"", "");

            if (!content.isEmpty()) {
                String[] pairs = content.split(",");
                for (String pair : pairs) {
                    String[] entry = pair.split(":");
                    if (entry.length == 2) {
                        map.put(entry[0].trim(), entry[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture JSON: " + e.getMessage());
        }
        return map;
    }

private void savePlayers(Map<String, String> players) {

        try (FileWriter nvfichier = new FileWriter(Fichier_JOUEURS)) {
            nvfichier.write("{\n");

            int i = 0;
            for (Map.Entry<String, String> keyvalue : players.entrySet()) {
                nvfichier.write("  \"" + keyvalue.getKey() + "\": \"" + keyvalue.getValue() + "\"");

                if (i < players.size() - 1) {
                    nvfichier.write(",\n");
                } else {
                    nvfichier.write("\n");
                }
                i++;
            }
            nvfichier.write("}\n");
        } catch (IOException e) {
            System.err.println("Erreur écriture JSON: " + e.getMessage());
        }
    }

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

    public boolean isConnected(String identifiant) {
        return listeConnectes.containsKey(identifiant);
    }

    public Map<String, Session> getListeConnectes() {
        return listeConnectes;
    }

    public void faireDemandes(String demandeur, String cible) {
        listeDemande.put(demandeur, cible);
    }

    public Map<String, String> getListeDemande() {
        return listeDemande;
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

    public PrintWriter getOut() {
        return out;
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
                        String login = commande[1];
                        String mdp = commande[2];

                        boolean succes = server.registerPlayer(login, mdp);

                        if (succes) {
                            out.println("OK");
                        } else {
                            out.println("ERR Le joueur " + login + " existe deja");
                        }

                    } else {
                        out.println("ERR usage: register <numJoueur> <motDePasse>");
                    }
                }

                else if (commande[0].equals("connect")) {
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
                        if (server.isConnected(commande[1])){
                            server.faireDemandes(this.identifiant, commande[1]);
                            server.getListeConnectes().get(commande[1]).getOut().println(this.identifiant + " veut jouer avec toi ! Utilise la commande accept " + commande[1] + " pour accepter.");
                        }
                    } else {
                        out.println("ERR usage: ask <numJoueur>");
                    }
                } else if (commande[0].equals("accept")) {
                    if (commande.length == 2) {
                        if (server.getListeDemande().get(commande[1]).equals(identifiant)) {
                            out.println("La partie va commencer avec le joueur " + commande[1]);
                            server.getListeConnectes().get(commande[1]).getOut().println("La partie va commencer avec le joueur " + identifiant);
                        }
                    } else {
                        out.println("ERR usage: accept <numJoueur>");
                    }
                } else if (commande[0].equals("players")) {
                    if (commande.length == 1) {

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
                        out.println(
                                "- register <numJoueur> <motDePasse>\n- connect <numJoueur> <motDePasse>\n- play <caseSource> <caseDestination>\n- leave\n- quit\n- replay\n- new\n- ask <numJoueur>\n- accept <numJoueur>\n- players\n- save\n- list_games\n- load <idPartie>");
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
