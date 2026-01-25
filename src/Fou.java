import java.util.ArrayList;
import java.util.List;

public class Fou extends Piece {

    public Fou(int numJoueur) {
        super("♗", numJoueur);
        if (numJoueur == 2)
            this.img = "♝";
    }

    public List<Case> casesPossibles(Plateau plateau, int posX, int posY) {
        List<Case> res = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
            if (posX + i >= 8 || posY + i >= 8)
                break;
            Case c = plateau.getTerrain().get(posX + i).get(posY + i);
            if (c.getPiece() == null) {
                res.add(c);
            } else {
                if (c.getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(c);
                }
                break;
            }
        }

        for (int i = 1; i < 8; i++) {
            if (posX + i >= 8 || posY - i < 0)
                break;
            Case c = plateau.getTerrain().get(posX + i).get(posY - i);
            if (c.getPiece() == null) {
                res.add(c);
            } else {
                if (c.getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(c);
                }
                break;
            }
        }

        for (int i = 1; i < 8; i++) {
            if (posX - i < 0 || posY + i >= 8)
                break;
            Case c = plateau.getTerrain().get(posX - i).get(posY + i);
            if (c.getPiece() == null) {
                res.add(c);
            } else {
                if (c.getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(c);
                }
                break;
            }
        }

        for (int i = 1; i < 8; i++) {
            if (posX - i < 0 || posY - i < 0)
                break;
            else {
                Case c = plateau.getTerrain().get(posX - i).get(posY - i);
                if (c.getPiece() == null) {
                    res.add(c);
                } else {
                    if (c.getPiece().getNumJoueur() != this.numJoueur) {
                        res.add(c);
                    }
                    break;
                }
            }
        }
        return res;
    }

}
