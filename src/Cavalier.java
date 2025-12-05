import java.util.ArrayList;
import java.util.List;

public class Cavalier extends Piece {
    
    public Cavalier(int numJoueur) {
        super("♘", numJoueur);
        if (numJoueur == 2) this.img = "♞";
    }

    public List<Case> casesPossibles() {
        List<Case> res = new ArrayList<>();
        return res;
    }

}
