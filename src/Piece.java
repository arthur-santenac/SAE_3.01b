import java.util.List;

public abstract class Piece {
    protected String img;
    protected int numJoueur;
    public String promoteP = "";

    public Piece(String img, int numJoueur) {
        this.img = img;
        this.numJoueur = numJoueur;
    }

    abstract List<Case> casesPossibles(Plateau plateau, int posX, int posY);

    public int getNumJoueur() {
        return numJoueur;
    }
    
    @Override
    public String toString() {
        return " " + this.img + " ";
    }

}
