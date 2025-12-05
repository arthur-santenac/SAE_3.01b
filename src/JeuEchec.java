public class JeuEchec {
    
    Plateau plateau;
    Integer joueurActuel;

    public JeuEchec() {
        this.plateau = new Plateau();
        this.joueurActuel = 1;
    }

    public void afficherPlateau() {
        if (joueurActuel == 1) this.plateau.affichage(false);
        else this.plateau.affichage(true);
    }

    public void lancerPartie() {

        while (true) {
            this.afficherPlateau();
            System.out.println("Au tour du joueur " + joueurActuel);
            System.out.println("Entrez le coup :");
            System.console().readLine();
            this.joueurActuel = 3 - this.joueurActuel;
        }
    }

}
