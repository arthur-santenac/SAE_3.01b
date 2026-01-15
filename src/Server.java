import java.io.*;
import java.net.*;
import java.util.*;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private final Object lock = new Object();
    private Map<String, Session> listeConnectes = new HashMap<>();
    private Map<String, String> listeDemande = new HashMap<>();
    private Map<String, JeuEchec> listeJeuEnCours = new HashMap<>();
    private Map<String, Session> listeJoueurAttente = new HashMap<>();

    
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

    public boolean verifieExist(String login, String password) {
        synchronized (lock) {
            Map<String, String> players = chercherPlayers();
            if (players.containsKey(login)) {
                return players.get(login).equals(password);
            }
            return false;
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

    public Map<String, Session> getListeConnectes() {
        return listeConnectes;
    }

    public boolean isConnected(String identifiant) {
        return listeConnectes.containsKey(identifiant);
    }

    public void faireDemandes(String demandeur, String cible) {
        listeDemande.put(demandeur, cible);
    }

    public Map<String, String> getListeDemande() {
        return listeDemande;
    }

    public Map<String, JeuEchec> getListeJeuEnCours() {
        return listeJeuEnCours;
    }

    public void ajouterJoueurAttente(String identifiant, Session session) {
        this.listeJoueurAttente.put(identifiant, session);
    }

    public Map<String, Session> getListeAttente() {
        return this.listeJoueurAttente;
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
    private String adversaire = null;
    private int numJoueur = 0;

    public Session(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void afficherJeu(boolean inverser, boolean fini) {
        JeuEchec jeu = server.getListeJeuEnCours().get(identifiant);
        int id; if (inverser) id = 2; else id = 1;
        out.println("\033[H\033[2J");
        out.flush();
        server.getListeConnectes().get(adversaire).getOut().println("\033[H\033[2J");
        server.getListeConnectes().get(adversaire).getOut().flush();
        if (!fini) {
            out.println("Vous êtes en partie avec le joueur " + this.adversaire);
            out.println("Vous êtes le joueur " + id);
            server.getListeConnectes().get(adversaire).getOut().println("Vous êtes en partie avec le joueur " + identifiant);
            server.getListeConnectes().get(adversaire).getOut().println("Vous êtes le joueur " + (3 - id));
        }
        out.println(jeu.affichage(inverser));
        server.getListeConnectes().get(adversaire).getOut().println(jeu.affichage(!inverser));
        if (fini) {
            out.println("Félicitation vous avez gagné !");
            server.getListeConnectes().get(adversaire).getOut().println("Dommage vous avez perdu.");
        }
    }

    public void setAdversaire(String adversaire) {
        this.adversaire = adversaire;
    }

    public void setNumJoueur(int numJoueur) {
        this.numJoueur = numJoueur;
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

                        if (login.length() < 3 || login.length() > 10) {
                            out.println("ERR Le nomJoueur doit contenir entre 3 et 10 caracteres");
                        } else if (mdp.length() < 6) {
                            out.println("ERR Le mot de passe doit contenir au moins 6 caracteres");
                        } else {

                            boolean succes = server.registerPlayer(login, mdp);

                            if (succes) {
                                out.println("OK");
                            } else {
                                out.println("ERR Le joueur " + login + " existe deja");
                            }
                        }

                    } else {
                        out.println("ERR usage: register <numJoueur> <motDePasse>");
                    }
                }

                else if (commande[0].equals("connect")) {
                    if (commande.length == 3) {
                        String login = commande[1];
                        String mdp = commande[2];

                        if (server.isConnected(login)) {
                            out.println("ERR Le joueur " + login + " est deja connecte");
                        } else if (server.verifieExist(login, mdp)) {
                            this.identifiant = login;
                            this.server.ajouterConnecte(identifiant, this);
                            out.println("OK");
                        }

                        else {
                            out.println("ERR Identifiant ou mot de passe incorrect");
                        }

                    } else {
                        out.println("ERR usage: connect <numJoueur> <motDePasse>");
                    }
                } else if (commande[0].equals("play")) {
                    if (commande.length == 3) {
                        if (adversaire != null && server.getListeJeuEnCours().containsKey(identifiant)) {
                            out.println(server.getListeJeuEnCours().get(identifiant).jouer(numJoueur, commande[1], commande[2]));
                            boolean fini = false; if (server.getListeJeuEnCours().get(identifiant).estFini()) fini = true;
                            if (numJoueur == 1) afficherJeu(false, fini); else afficherJeu(true, fini);
                            if (fini) {
                                server.getListeJeuEnCours().remove(identifiant); server.getListeJeuEnCours().remove(adversaire);
                            }
                        } else {
                            out.println("ERR vous devez être en partie pour jouer");
                        }
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
                        if (adversaire != null && !server.getListeJeuEnCours().containsKey(identifiant)) {
                            if (server.getListeDemande().containsKey(adversaire) && server.getListeDemande().get(adversaire).equals(identifiant)) {
                                server.getListeDemande().remove(adversaire);
                                JeuEchec jeu = new JeuEchec();
                                server.getListeJeuEnCours().put(identifiant, jeu);
                                server.getListeJeuEnCours().put(adversaire, jeu);
                                this.numJoueur = 1;
                                server.getListeConnectes().get(adversaire).setNumJoueur(2);
                                afficherJeu(false, false);
                            } else {
                                server.getListeDemande().put(this.identifiant, adversaire);
                                server.getListeConnectes().get(adversaire).getOut().println(identifiant + " a envie de rejouer avec vous\nutilisez replay pour relancé une partie");
                            }
                        } else {
                            out.println("ERR vous devez être en fin de partie pour rejouer");
                        }
                    } else {
                        out.println("ERR usage: replay");
                    }
                } else if (commande[0].equals("new")) {
                    if (commande.length == 1) {
                        if(this.server.getListeAttente().isEmpty()){
                            this.server.ajouterJoueurAttente(identifiant, this);
                        }
                        else{
                            this.adversaire = this.server.getListeAttente().get()
                        }

                    } else {
                        out.println("ERR usage: new");
                    }
                } else if (commande[0].equals("ask")) {
                    if (commande.length == 2) {
                        if (identifiant != null) {
                            if (!identifiant.equals(commande[1])) {
                                if (server.isConnected(commande[1])){
                                    server.faireDemandes(this.identifiant, commande[1]);
                                    out.println("OK");
                                    server.getListeConnectes().get(commande[1]).getOut().println(this.identifiant + " veut jouer avec toi ! Utilise la commande 'accept " + identifiant + "' pour accepter.");
                                } else {
                                    out.println("ERR ce joueur n'est pas connecté");
                                }
                            } else {
                                out.println("ERR vous ne pouvez pas jouer contre vous même");
                            }
                        } else {
                            out.println("ERR vous n'êtes pas connectés");
                        }
                    } else {
                        out.println("ERR usage: ask <numJoueur>");
                    }
                } else if (commande[0].equals("accept")) {
                    if (commande.length == 2) {
                        if (server.getListeDemande().containsKey(commande[1]) && server.getListeDemande().get(commande[1]).equals(identifiant)) {
                            server.getListeDemande().remove(commande[1]);
                            this.adversaire = commande[1];
                            server.getListeConnectes().get(commande[1]).setAdversaire(this.identifiant);
                            JeuEchec jeu = new JeuEchec();
                            server.getListeJeuEnCours().put(identifiant, jeu);
                            server.getListeJeuEnCours().put(commande[1], jeu);
                            this.numJoueur = 1;
                            server.getListeConnectes().get(commande[1]).setNumJoueur(2);
                            afficherJeu(false, false);
                        } else {
                            out.println("ERR ce joueur ne vous a pas demandé en duel");
                        }
                    } else {
                        out.println("ERR usage: accept <numJoueur>");
                    }
                } else if (commande[0].equals("players")) {
                    if (commande.length == 1) {
                        String res ="Liste des joueurs connectés :";
                        for(String id: this.server.getListeConnectes().keySet()){
                            if (!id.equals(identifiant)) {
                                res+= "\n" + id;
                            }
                        }
                        out.println(res);
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
