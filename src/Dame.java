import java.util.List;

public class Dame extends Piece {
    
    public Dame(int numJoueur) {
        super("♕", numJoueur);
        if (numJoueur == 2) this.img = "♛";
    }

    public List<Case> casesPossibles() {
        
    }

}
