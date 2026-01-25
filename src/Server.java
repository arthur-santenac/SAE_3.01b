import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Server {

    private final Object lock = new Object();
    private Map<String, Session> listeConnectes = new HashMap<>();
    private Map<String, String> listeDemande = new HashMap<>();
    private Map<String, JeuEchec> listeJeuEnCours = new HashMap<>();
    private Map<String, Session> listeJoueurAttente = new HashMap<>();

    private Map<String, PartieInfo> parties = new HashMap<>();
    private Map<String, String> joueurEnJeu = new HashMap<>();

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

    public Map<String, PartieInfo> getParties() {
        return parties;
    }

    public Map<String, String> getJoueurEnJeu() {
        return joueurEnJeu;
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

    public String recupererHistorique(String pseudoCible) {
        File file = new File("./parties.json");
        if (!file.exists()) {
            return "Aucune partie enregistrée.";
        }

        StringBuilder resultatFinal = new StringBuilder();
        resultatFinal.append("--- Vos Parties ---\n");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            String id = "Inconnu";
            String pBlanc = "";
            String pNoir = "";
            String date = "";
            String statut = "";

            boolean dansUnePartie = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("{")) {
                    dansUnePartie = true;

                    id = "Inconnu";
                    pBlanc = "";
                    pNoir = "";
                    date = "";
                    statut = "";
                }

                if (dansUnePartie) {
                    if (line.startsWith("\"id\":")) {
                        id = extraireValeurJson(line);
                    } else if (line.startsWith("\"pseudoBlanc\":")) {
                        pBlanc = extraireValeurJson(line);
                    } else if (line.startsWith("\"pseudoNoir\":")) {
                        pNoir = extraireValeurJson(line);
                    } else if (line.startsWith("\"date\":")) {
                        date = extraireValeurJson(line);
                    } else if (line.startsWith("\"statut\":")) {
                        statut = extraireValeurJson(line);
                    } else if (line.startsWith("\"resultat\":")) {

                        statut = extraireValeurJson(line);
                    }
                }

                if (line.startsWith("}") || line.endsWith("}")) {
                    dansUnePartie = false;

                    if (pseudoCible.equals(pBlanc) || pseudoCible.equals(pNoir)) {
                        String adversaire = pseudoCible.equals(pBlanc) ? pNoir : pBlanc;
                        String couleur = pseudoCible.equals(pBlanc) ? "Blanc" : "Noir";

                        resultatFinal.append(String.format("ID: %s | VS: %s (%s) | Date: %s | Statut: %s\n",
                                id, adversaire, couleur, date, statut));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture historique : " + e.getMessage());
            return "ERR Impossible de lire l'historique.";
        }

        if (resultatFinal.toString().equals("--- Vos Parties ---\n")) {
            return "Vous n'avez joué aucune partie pour le moment.";
        }

        return resultatFinal.toString();
    }

    private String extraireValeurJson(String ligne) {
        int debut = ligne.indexOf(":");
        if (debut == -1)
            return "";

        String valeur = ligne.substring(debut + 1).trim();
        valeur = valeur.replace("\"", "").replace(",", "");
        return valeur;
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
        new Server().mainServer(5556);
    }

    public boolean sauvegarderPartie(PartieInfo partie) {
        File file = new File("./parties.json");
        List<String> toutesLesParties = new ArrayList<>();

        if (file.exists()) {
            toutesLesParties = lireBlocsJson(file);
        }

        String idRecherche = "\"id\": \"" + partie.getId() + "\"";
        toutesLesParties.removeIf(jsonBlock -> jsonBlock.contains(idRecherche));

        String nouvelleSauvegarde = genererJsonString(partie);
        toutesLesParties.add(nouvelleSauvegarde);

        try (FileWriter writer = new FileWriter(file, false)) {
            for (String jsonBlock : toutesLesParties) {
                writer.write(jsonBlock);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde partie: " + e.getMessage());
            return false;
        }
    }

    private String genererJsonString(PartieInfo partie) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"id\": \"").append(partie.getId()).append("\",\n");
        sb.append("  \"pseudoBlanc\": \"").append(partie.getPseudoBlanc()).append("\",\n");
        sb.append("  \"pseudoNoir\": \"").append(partie.getPseudoNoir()).append("\",\n");
        sb.append("  \"attributionCouleurs\": \"").append(partie.getAttributionCouleurs()).append("\",\n");
        sb.append("  \"date\": \"").append(partie.getDate()).append("\",\n");

        if ("En cours".equals(partie.getResultat())) {
            sb.append("  \"statut\": \"En cours\",\n");
            sb.append("  \"joueurAuTrait\": \"").append(partie.getJoueurAuTrait()).append("\",\n");
        } else {
            sb.append("  \"statut\": \"Terminée\",\n");
            sb.append("  \"resultat\": \"").append(partie.getResultat()).append("\",\n");
        }

        sb.append("  \"coups\": [");
        List<String> coups = partie.getListeCoups();
        for (int i = 0; i < coups.size(); i++) {
            sb.append("\"").append(coups.get(i)).append("\"");
            if (i < coups.size() - 1)
                sb.append(", ");
        }
        sb.append("]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private List<String> lireBlocsJson(File file) {
        List<String> blocs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder currentBlock = new StringBuilder();
            String line;
            int accoladeCount = 0;
            boolean dansUnBloc = false;

            while ((line = br.readLine()) != null) {
                currentBlock.append(line).append("\n");

                for (char c : line.toCharArray()) {
                    if (c == '{') {
                        accoladeCount++;
                        dansUnBloc = true;
                    } else if (c == '}') {
                        accoladeCount--;
                    }
                }

                if (dansUnBloc && accoladeCount == 0) {
                    blocs.add(currentBlock.toString());
                    currentBlock.setLength(0);
                    dansUnBloc = false;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture pour nettoyage : " + e.getMessage());
        }
        return blocs;
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
        int id;
        if (inverser)
            id = 2;
        else
            id = 1;

        envoyer("\033[H\033[2J");
        out.flush();
        if (server.getListeConnectes().containsKey(adversaire)) {
            server.getListeConnectes().get(adversaire).envoyer("\033[H\033[2J");
            server.getListeConnectes().get(adversaire).getOut().flush();
        }

        if (!fini) {
            envoyer("Vous êtes en partie avec le joueur " + this.adversaire);
            envoyer("Vous êtes le joueur " + id);
            if (server.getListeConnectes().containsKey(adversaire)) {
                server.getListeConnectes().get(adversaire).envoyer("Vous êtes en partie avec le joueur " + identifiant);
                server.getListeConnectes().get(adversaire).envoyer("Vous êtes le joueur " + (3 - id));
            }
        }
        envoyer(jeu.affichage(inverser));
        if (server.getListeConnectes().containsKey(adversaire)) {
            server.getListeConnectes().get(adversaire).envoyer(jeu.affichage(!inverser));
        }
        if (fini) {
            envoyer("Félicitation vous avez gagné !");
            if (server.getListeConnectes().containsKey(adversaire)) {
                server.getListeConnectes().get(adversaire).envoyer("Dommage vous avez perdu.");
            }
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

            String clientPublicKeyBase64 = in.readLine();
            byte[] clientPublicKeyBytes = Base64.getDecoder().decode(clientPublicKeyBase64);
            KeyFactory kf = KeyFactory.getInstance("EC");
            PublicKey clientPublicKey = kf.generatePublic(new X509EncodedKeySpec(clientPublicKeyBytes));

            byte[] myPublicKeyBytes = myKeyPair.getPublic().getEncoded();
            String myPublicKeyBase64 = Base64.getEncoder().encodeToString(myPublicKeyBytes);
            out.println(myPublicKeyBase64);

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

                            String resultatCoup = server.getListeJeuEnCours().get(identifiant).jouer(numJoueur,
                                    commande[1], commande[2]);
                            envoyer(resultatCoup);

                            if (!resultatCoup.startsWith("ERR")) {
                                String idPartie = server.getJoueurEnJeu().get(this.identifiant);
                                if (idPartie != null) {
                                    PartieInfo partie = server.getParties().get(idPartie);
                                    if (partie != null) {
                                        String coupJoue = commande[1] + commande[2];
                                        partie.ajouterCoup(coupJoue);
                                    }
                                }
                            }

                            boolean fini = false;
                            if (server.getListeJeuEnCours().get(identifiant).estFini())
                                fini = true;

                            if (numJoueur == 1)
                                afficherJeu(false, fini);
                            else
                                afficherJeu(true, fini);

                            if (fini) {
                                String idPartie = server.getJoueurEnJeu().get(this.identifiant);

                                if (idPartie != null) {
                                    PartieInfo partie = server.getParties().get(idPartie);

                                    if (partie != null) {

                                        String resultat = "Victoire de " + this.identifiant;
                                        partie.terminerPartie(resultat);

                                        server.sauvegarderPartie(partie);
                                    }

                                    server.getJoueurEnJeu().remove(this.identifiant);
                                    server.getJoueurEnJeu().remove(this.adversaire);
                                }

                                server.getListeJeuEnCours().remove(identifiant);
                                server.getListeJeuEnCours().remove(adversaire);
                            }
                        } else {
                            envoyer("ERR vous devez être en partie pour jouer");
                        }
                    } else {
                        envoyer("ERR usage : play <caseSource> <caseDestination>");
                    }
                } else if (commande[0].equals("leave")) {
                    if (commande.length == 1) {

                        String idPartie = server.getJoueurEnJeu().get(this.identifiant);
                        if (idPartie != null) {
                            PartieInfo partie = server.getParties().get(idPartie);
                            if (partie != null) {

                                String vainqueur = (this.adversaire != null) ? this.adversaire : "Adversaire";
                                partie.terminerPartie("Victoire par forfait de " + vainqueur);
                                server.sauvegarderPartie(partie);
                            }

                            server.getJoueurEnJeu().remove(this.identifiant);
                            if (this.adversaire != null)
                                server.getJoueurEnJeu().remove(this.adversaire);
                        }

                        if (this.adversaire != null && server.getListeConnectes().containsKey(this.adversaire)) {

                            server.getListeJeuEnCours().remove(this.adversaire);

                            server.getListeConnectes().get(this.adversaire).adversaire = null;
                        }

                        server.getListeJeuEnCours().remove(this.identifiant);
                        envoyer("OK Vous avez abandonné la partie.");
                        this.adversaire = null;
                    } else {
                        envoyer("ERR usage : leave");
                    }
                } else if (commande[0].equals("quit")) {
                    if (commande.length == 1) {
                        if (this.adversaire != null && server.getListeConnectes().containsKey(this.adversaire)) {
                            server.getListeConnectes().get(this.adversaire)
                                    .envoyer("INFO: Votre adversaire a quitté. Vous avez gagné par forfait !");
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
                            if (server.getListeDemande().containsKey(adversaire)
                                    && server.getListeDemande().get(adversaire).equals(identifiant)) {
                                server.getListeDemande().remove(adversaire);
                                JeuEchec jeu = new JeuEchec();
                                server.getListeJeuEnCours().put(identifiant, jeu);
                                server.getListeJeuEnCours().put(adversaire, jeu);
                                this.numJoueur = 1;
                                server.getListeConnectes().get(adversaire).setNumJoueur(2);
                                afficherJeu(false, false);
                            } else {
                                server.getListeDemande().put(this.identifiant, adversaire);
                                server.getListeConnectes().get(adversaire).envoyer(identifiant
                                        + " a envie de rejouer avec vous\nutilisez replay pour relancé une partie");
                            }
                        } else {
                            envoyer("ERR vous devez être en fin de partie pour rejouer");
                        }
                    } else {
                        envoyer("ERR usage : replay");
                    }
                } else if (commande[0].equals("new")) {
                    if (commande.length == 1) {
                        if (this.server.getListeAttente().isEmpty()) {
                            this.server.ajouterJoueurAttente(identifiant, this);
                            envoyer("Vous êtes en file d'attente...");
                        } else {
                            this.adversaire = this.server.getListeAttente().keySet().iterator().next();
                            if (this.adversaire.equals(this.identifiant)) {
                                envoyer("Vous êtes déjà dans la file d'attente.");
                            } else {
                                Session sessionAdversaire = this.server.getListeAttente().get(this.adversaire);
                                this.server.getListeAttente().remove(this.adversaire);

                                JeuEchec nouveauJeu = new JeuEchec();
                                this.server.getListeJeuEnCours().put(this.identifiant, nouveauJeu);
                                this.server.getListeJeuEnCours().put(this.adversaire, nouveauJeu);

                                sessionAdversaire.setAdversaire(this.identifiant);
                                sessionAdversaire.setNumJoueur(1);
                                this.numJoueur = 2;

                                PartieInfo nouvellePartie = new PartieInfo(adversaire, identifiant);
                                server.getParties().put(nouvellePartie.getId(), nouvellePartie);
                                server.getJoueurEnJeu().put(adversaire, nouvellePartie.getId());
                                server.getJoueurEnJeu().put(identifiant, nouvellePartie.getId());

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
                                if (server.isConnected(commande[1])) {
                                    server.faireDemandes(this.identifiant, commande[1]);
                                    envoyer("OK");
                                    server.getListeConnectes().get(commande[1])
                                            .envoyer(this.identifiant
                                                    + " veut jouer avec toi ! Utilise la commande 'accept "
                                                    + identifiant + "' pour accepter.");
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

                        String demandeur = commande[1];
                        String accepteur = this.identifiant;

                        if (server.getListeDemande().containsKey(demandeur)
                                && server.getListeDemande().get(demandeur).equals(accepteur)) {
                            server.getListeDemande().remove(demandeur);

                            this.adversaire = demandeur;
                            server.getListeConnectes().get(demandeur).setAdversaire(this.identifiant);

                            this.numJoueur = 1;
                            server.getListeConnectes().get(demandeur).setNumJoueur(2);

                            JeuEchec jeu = new JeuEchec();
                            server.getListeJeuEnCours().put(accepteur, jeu);
                            server.getListeJeuEnCours().put(demandeur, jeu);

                            PartieInfo nouvellePartie = new PartieInfo(accepteur, demandeur);
                            server.getParties().put(nouvellePartie.getId(), nouvellePartie);
                            server.getJoueurEnJeu().put(demandeur, nouvellePartie.getId());
                            server.getJoueurEnJeu().put(accepteur, nouvellePartie.getId());

                            envoyer("La partie commence ! ID: " + nouvellePartie.getId());
                            server.getListeConnectes().get(demandeur)
                                    .envoyer("La partie commence ! ID: " + nouvellePartie.getId());

                            afficherJeu(false, false);
                        } else {
                            envoyer("ERR ce joueur ne vous a pas demandé en duel");
                        }
                    } else {
                        envoyer("ERR usage : accept <numJoueur>");
                    }
                } else if (commande[0].equals("players")) {
                    if (commande.length == 1) {
                        String res = "Liste des joueurs connectés :";
                        for (String id : this.server.getListeConnectes().keySet()) {
                            if (!id.equals(identifiant)) {
                                res += "\n" + id;
                            }
                        }
                        envoyer(res);
                    } else {
                        envoyer("ERR usage : players");
                    }
                } else if (commande[0].equals("save")) {
                    if (commande.length == 1) {

                        String idPartie = server.getJoueurEnJeu().get(this.identifiant);

                        if (idPartie != null) {

                            PartieInfo laPartie = server.getParties().get(idPartie);

                            if (laPartie != null) {

                                boolean succes = server.sauvegarderPartie(laPartie);

                                if (succes) {
                                    envoyer("OK");
                                } else {
                                    envoyer("ERR Problème lors de l'écriture du fichier");
                                }
                            } else {
                                envoyer("ERR Partie introuvable (incohérence serveur)");
                            }
                        } else {
                            envoyer("ERR Vous n'êtes pas dans une partie active");
                        }
                    } else {
                        envoyer("ERR usage : save");
                    }
                } else if (commande[0].equals("list_games")) {
                    if (commande.length == 1) {

                        if (this.identifiant != null) {
                            String historique = server.recupererHistorique(this.identifiant);
                            envoyer(historique);
                        } else {
                            envoyer("ERR Vous devez être connecté pour voir vos parties.");
                        }
                    } else {
                        envoyer("ERR usage : list_games");
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