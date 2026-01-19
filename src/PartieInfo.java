import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PartieInfo {
    private String id;
    private String pseudoBlanc;
    private String pseudoNoir;
    private String attributionCouleurs; // Ex: "Random" ou défini par le défi
    private String date;
    private String resultat; // "En cours", "1-0", "0-1", "1/2-1/2"
    private String joueurAuTrait; // Pseudo du joueur qui doit jouer
    private List<String> listeCoups;

    public PartieInfo(String pseudoBlanc, String pseudoNoir) {
        this.id = UUID.randomUUID().toString(); // Génère un ID unique
        this.pseudoBlanc = pseudoBlanc;
        this.pseudoNoir = pseudoNoir;
        this.attributionCouleurs = "Blanc: " + pseudoBlanc + ", Noir: " + pseudoNoir;
        this.date = new Date().toString();
        this.resultat = "En cours";
        this.joueurAuTrait = pseudoBlanc; // Les blancs commencent toujours
        this.listeCoups = new ArrayList<>();
    }

    public void ajouterCoup(String coup) {
        this.listeCoups.add(coup);
        changerTour();
    }

    private void changerTour() {
        if (this.joueurAuTrait.equals(pseudoBlanc)) {
            this.joueurAuTrait = pseudoNoir;
        } else {
            this.joueurAuTrait = pseudoBlanc;
        }
    }

    public void terminerPartie(String resultat) {
        this.resultat = resultat;
        this.joueurAuTrait = "Aucun"; // La partie est finie
    }

    // Getters utiles pour le serveur
    public String getId() { return id; }
    public String getJoueurAuTrait() { return joueurAuTrait; }
    public String getPseudoBlanc() { return pseudoBlanc; }
    public String getPseudoNoir() { return pseudoNoir; }
    public String getResultat() { return resultat; }
}