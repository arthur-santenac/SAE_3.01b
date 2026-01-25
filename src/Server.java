import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

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
            System.err.println("Erreur lecture JSON : " + e.getMessage());
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
            System.err.println("Erreur écriture JSON : " + e.getMessage());
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

    public void enleverConnecte(String identifiant) {
        this.listeConnectes.remove(identifiant);
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
        new Server().mainServer(5555);
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
    
    private SecretKeySpec secretKey;

    public Session(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public void envoyer(String message) {
        try {
            String messageChiffre = chiffrer(message);
            out.println(messageChiffre);
        } catch (Exception e) {
            System.err.println("Erreur d'envoi chiffré : " + e.getMessage());
        }
    }

    private String chiffrer(String messageClair) throws Exception {
        Cipher chiffreur = Cipher.getInstance("AES");
        chiffreur.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] octetsChiffres = chiffreur.doFinal(messageClair.getBytes());
        return Base64.getEncoder().encodeToString(octetsChiffres);
    }

    private String dechiffrer(String messageChiffre) throws Exception {
        byte[] octetsChiffres = Base64.getDecoder().decode(messageChiffre);
        Cipher dechiffreur = Cipher.getInstance("AES");
        dechiffreur.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] octetsClairs = dechiffreur.doFinal(octetsChiffres);
        return new String(octetsClairs);
    }

    public void afficherJeu(boolean inverser, boolean fini) {
        JeuEchec jeu = server.getListeJeuEnCours().get(identifiant);
        int id; if (inverser) id = 2; else id = 1;
        
        envoyer("\033[H\033[2J");
        out.flush();
        server.getListeConnectes().get(adversaire).envoyer("\033[H\033[2J");
        server.getListeConnectes().get(adversaire).getOut().flush();
        
        if (!fini) {
            envoyer("Vous êtes en partie avec le joueur " + this.adversaire);
            envoyer("Vous êtes le joueur " + id);
            server.getListeConnectes().get(adversaire).envoyer("Vous êtes en partie avec le joueur " + identifiant);
            server.getListeConnectes().get(adversaire).envoyer("Vous êtes le joueur " + (3 - id));
        }
        envoyer(jeu.affichage(inverser));
        server.getListeConnectes().get(adversaire).envoyer(jeu.affichage(!inverser));
        if (fini) {
            envoyer("Félicitation vous avez gagné !");
            server.getListeConnectes().get(adversaire).envoyer("Dommage vous avez perdu.");
        }
    }

    public PrintWriter getOut() {
        return out;
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

            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair myKeyPair = kpg.generateKeyPair();

            String ligneRecue = in.readLine();
            if (ligneRecue == null) return;
            String clientPublicKeyBase64 = ligneRecue.substring(5);
            out.println("OK"); 

            byte[] clientPublicKeyBytes = Base64.getDecoder().decode(clientPublicKeyBase64);
            KeyFactory kf = KeyFactory.getInstance("EC");
            PublicKey clientPublicKey = kf.generatePublic(new X509EncodedKeySpec(clientPublicKeyBytes));

            byte[] myPublicKeyBytes = myKeyPair.getPublic().getEncoded();
            String myPublicKeyBase64 = Base64.getEncoder().encodeToString(myPublicKeyBytes);
            
            out.println("sync " + myPublicKeyBase64);

            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(myKeyPair.getPrivate());
            ka.doPhase(clientPublicKey, true);
            byte[] sharedSecret = ka.generateSecret();

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = Arrays.copyOf(sha256.digest(sharedSecret), 16);
            secretKey = new SecretKeySpec(keyBytes, "AES");

            String line;
            while ((line = in.readLine()) != null) {
                line = dechiffrer(line).trim();
                
                String[] commande = line.split("\\s+");
                if (commande[0].equals("register")) {
                    if (commande.length == 3) {
                        String login = commande[1];
                        String mdp = commande[2];

                        if (login.length() < 3 || login.length() > 10) {
                            envoyer("ERR Le nomJoueur doit contenir entre 3 et 10 caracteres");
                        } else if (mdp.length() < 6) {
                            envoyer("ERR Le mot de passe doit contenir au moins 6 caracteres");
                        } else {
                            boolean succes = server.registerPlayer(login, mdp);
                            if (succes) {
                                envoyer("OK");
                            } else {
                                envoyer("ERR Le joueur " + login + " existe deja");
                            }
                        }

                    } else {
                        envoyer("ERR usage : register <numJoueur> <motDePasse>");
                    }
                }

                else if (commande[0].equals("connect")) {
                    if (commande.length == 3) {
                        String login = commande[1];
                        String mdp = commande[2];

                        if (server.isConnected(login)) {
                            envoyer("ERR Le joueur " + login + " est deja connecte");
                        } else if (server.verifieExist(login, mdp)) {
                            this.identifiant = login;
                            this.server.ajouterConnecte(identifiant, this);
                            envoyer("OK");
                        }

                        else {
                            envoyer("ERR Identifiant ou mot de passe incorrect");
                        }

                    } else {
                        envoyer("ERR usage : connect <numJoueur> <motDePasse>");
                    }
                } else if (commande[0].equals("play")) {
                    if (commande.length == 3) {
                        if (adversaire != null && server.getListeJeuEnCours().containsKey(identifiant)) {
                            envoyer(server.getListeJeuEnCours().get(identifiant).jouer(numJoueur, commande[1], commande[2]));
                            boolean fini = false; if (server.getListeJeuEnCours().get(identifiant).estFini()) fini = true;
                            if (numJoueur == 1) afficherJeu(false, fini); else afficherJeu(true, fini);
                            if (fini) {
                                server.getListeJeuEnCours().remove(identifiant); server.getListeJeuEnCours().remove(adversaire);
                            }
                        } else {
                            envoyer("ERR vous devez être en partie pour jouer");
                        }
                    } else {
                        envoyer("ERR usage : play <caseSource> <caseDestination>");
                    }
                } else if (commande[0].equals("leave")) {
                    if (commande.length == 1) {
                         if (this.adversaire != null && server.getListeConnectes().containsKey(this.adversaire)) {
                            server.getListeConnectes().get(this.adversaire).envoyer("INFO : Votre adversaire a abandonné. Vous avez gagné par forfait !");
                            server.getListeConnectes().get(this.adversaire).adversaire = null;
                        }
                        envoyer("OK Vous avez abandonné la partie.");
                        this.adversaire = null;
                    } else {
                        envoyer("ERR usage : leave");
                    }
                } else if (commande[0].equals("quit")) {
                    if (commande.length == 1) {
                         if (this.adversaire != null && server.getListeConnectes().containsKey(this.adversaire)) {
                            server.getListeConnectes().get(this.adversaire).envoyer("INFO: Votre adversaire a quitté. Vous avez gagné par forfait !");
                            server.getListeConnectes().get(this.adversaire).adversaire = null;
                        }
                        server.enleverConnecte(this.identifiant);
                        break;
                    } else {
                        envoyer("ERR usage : quit");
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
                                server.getListeConnectes().get(adversaire).envoyer(identifiant + " a envie de rejouer avec vous\nutilisez replay pour relancé une partie");
                            }
                        } else {
                            envoyer("ERR vous devez être en fin de partie pour rejouer");
                        }
                    } else {
                        envoyer("ERR usage : replay");
                    }
                } else if (commande[0].equals("new")) {
                    if (commande.length == 1) {
                        if(this.server.getListeAttente().isEmpty()){
                            this.server.ajouterJoueurAttente(identifiant, this);
                            envoyer("Vous êtes en file d'attente...");
                        }
                        else{
                            this.adversaire = this.server.getListeAttente().keySet().iterator().next();
                            if (this.adversaire.equals(this.identifiant)) {
                                envoyer("Vous êtes déjà dans la file d'attente.");
                            }
                            else {
                                Session sessionAdversaire = this.server.getListeAttente().get(this.adversaire);
                                this.server.getListeAttente().remove(this.adversaire);
                                JeuEchec nouveauJeu = new JeuEchec();
                                this.server.getListeJeuEnCours().put(this.identifiant, nouveauJeu);
                                this.server.getListeJeuEnCours().put(this.adversaire, nouveauJeu);
                                sessionAdversaire.setAdversaire(this.identifiant);
                                sessionAdversaire.setNumJoueur(1);
                                this.numJoueur = 2;
                                afficherJeu(true, false);
                            }
                        }
                    } else {
                        envoyer("ERR usage : new");
                    }
                } else if (commande[0].equals("ask")) {
                    if (commande.length == 2) {
                        if (identifiant != null) {
                            if (!identifiant.equals(commande[1])) {
                                if (server.isConnected(commande[1])){
                                    server.faireDemandes(this.identifiant, commande[1]);
                                    envoyer("OK");
                                    server.getListeConnectes().get(commande[1]).envoyer(this.identifiant + " veut jouer avec toi ! Utilise la commande 'accept " + identifiant + "' pour accepter.");
                                } else {
                                    envoyer("ERR ce joueur n'est pas connecté");
                                }
                            } else {
                                envoyer("ERR vous ne pouvez pas jouer contre vous même");
                            }
                        } else {
                            envoyer("ERR vous n'êtes pas connectés");
                        }
                    } else {
                        envoyer("ERR usage : ask <numJoueur>");
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
                            envoyer("ERR ce joueur ne vous a pas demandé en duel");
                        }
                    } else {
                        envoyer("ERR usage : accept <numJoueur>");
                    }
                } else if (commande[0].equals("players")) {
                    if (commande.length == 1) {
                        String res ="Liste des joueurs connectés :";
                        for(String id: this.server.getListeConnectes().keySet()){
                            if (!id.equals(identifiant)) {
                                res+= "\n" + id;
                            }
                        }
                        envoyer(res);
                    } else {
                        envoyer("ERR usage : players");
                    }
                } else if (commande[0].equals("help")) {
                    if (commande.length == 1) {
                        envoyer("- register <numJoueur> <motDePasse>\n- connect <numJoueur> <motDePasse>\n- play <caseSource> <caseDestination>\n- leave\n- quit\n- replay\n- new\n- ask <numJoueur>\n- accept <numJoueur>\n- players\n- save\n- list_games\n- load <idPartie>");
                    } else {
                        envoyer("ERR usage : help");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}