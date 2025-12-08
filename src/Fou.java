import java.util.ArrayList;
import java.util.List;

public class Fou extends Piece {
    
    public Fou(int numJoueur) {
        super("♗", numJoueur);
        if (numJoueur == 2) this.img = "♝";
    }

    public List<Case> casesPossibles(Plateau plateau, int posX, int posY) {
        List<Case> res = new ArrayList<>();
        while (posX < 7 && posY < 7 ){
            
        }
        return res;
    }

}
