import java.util.ArrayList;
import java.util.List;

public class Pion extends Piece {

    private boolean dejaJouer;
    
    public Pion(int numJoueur) {
        super("♙", numJoueur);
        if (numJoueur == 2) this.img = "♟";
        this.dejaJouer = false;
    }

    public List<Case> casesPossibles(Plateau plateau, int posX, int posY) {
        int ajout;
        if (numJoueur == 2) ajout = 1;
        else ajout = -1;
        List<Case> res = new ArrayList<>();
        if (posX < 7) {
            if (plateau.getTerrain().get(posX + ajout).get(posY).getPiece() == null) {
                res.add(plateau.getTerrain().get(posX + ajout).get(posY));
                if (!this.dejaJouer && plateau.getTerrain().get(posX + ajout * 2).get(posY).getPiece() == null) {
                    res.add(plateau.getTerrain().get(posX + ajout * 2).get(posY));
                }
            }
        }
        if (posY < 7) {
            if (plateau.getTerrain().get(posX + ajout).get(posY + 1).getPiece() instanceof Piece) res.add(plateau.getTerrain().get(posX + ajout).get(posY + 1));
        }
        if (posY > 0) {

            if (plateau.getTerrain().get(posX + ajout).get(posY - 1).getPiece() instanceof Piece) res.add(plateau.getTerrain().get(posX + ajout).get(posY - 1));
        }
        this.dejaJouer = true;
        return res;
    }

}
