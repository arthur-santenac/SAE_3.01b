import java.util.List;

public abstract class Piece {
    protected String img;
    protected int numJoueur;

    public Piece(String img, int numJoueur) {
        this.img = img;
        this.numJoueur = numJoueur;
    }

    abstract List<Case> casesPossibles();
    
}
