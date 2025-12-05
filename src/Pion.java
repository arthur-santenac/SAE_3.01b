import java.util.List;

public class Pion extends Piece {
    
    public Pion(int numJoueur) {
        super("♙", numJoueur);
        if (numJoueur == 2) this.img = "♟";
    }

    public List<Case> casesPossibles() {
        
    }

}
