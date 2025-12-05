import java.util.ArrayList;
import java.util.List;

public class Pion extends Piece {
    
    public Pion(int numJoueur) {
        super(" ♙ ", numJoueur);
        if (numJoueur == 2) this.img = " ♟ ";
    }

    public List<Case> casesPossibles() {
        List<Case> res = new ArrayList<>();
        return res;
    }

    @Override
    public String toString() {
        return " " + this.img + " ";
    }

}
