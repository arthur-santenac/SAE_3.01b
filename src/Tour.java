import java.util.List;

public class Tour extends Piece {
    
    public Tour(int numJoueur) {
        super("♖", numJoueur);
        if (numJoueur == 2) this.img = "♜";
    }

    public List<Case> casesPossibles() {
        
    }

}
