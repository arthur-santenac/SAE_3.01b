import java.util.ArrayList;
import java.util.List;

public class Cavalier extends Piece {

    public Cavalier(int numJoueur) {
        super("♘", numJoueur);
        if (numJoueur == 2)
            this.img = "♞";
    }

    public List<Case> casesPossibles(Plateau plateau, int posX, int posY) {
        List<Case> res = new ArrayList<>();

        int[][] sauts = {
                { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 },
                { 1, -2 }, { 1, 2 }, { 2, -1 }, { 2, 1 }
        };

        for (int[] saut : sauts) {
            int nouvX = posX + saut[0];
            int nouvY = posY + saut[1];
            if (nouvX >= 0 && nouvX < 8 && nouvY >= 0 && nouvY < 8) {
                res.add(plateau.getTerrain().get(nouvX).get(nouvY));
            }
        }
        return res;
    }

}
