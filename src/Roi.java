import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Roi extends Piece {
 
    public Roi(int numJoueur) {
        super("♔", numJoueur);
        if (numJoueur == 2) this.img = "♚";
    }

    public List<Case> casesPossibles(Plateau plateau, int posX, int posY) {
        List<Case> res = new ArrayList<>();
        for (int i : Arrays.asList(-1, 0, 1)) {
            for (int j : Arrays.asList(-1, 0, 1)) {
                if (!(i == 0 && j == 0) && posX + i >= 0 && posX + i < 8 && posY + j >= 0 && posY + j < 8) {
                    res.add(plateau.getTerrain().get(posX + i).get(posY + j));
                }
            }
        }
        return res;
    }

}
