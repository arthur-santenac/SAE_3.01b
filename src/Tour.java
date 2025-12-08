import java.util.ArrayList;
import java.util.List;

public class Tour extends Piece {
    
    public Tour(int numJoueur) {
        super("♖", numJoueur);
        if (numJoueur == 2) this.img = "♜";
    }

    public List<Case> casesPossibles(Plateau plateau, int posX, int posY) {
        List<Case> res = new ArrayList<>();
        for (int i = posX + 1; i < 8; i++) {
            if (plateau.getTerrain().get(i).get(posY).getPiece() == null) {
                res.add(plateau.getTerrain().get(i).get(posY));
            } 
            else {
                if (plateau.getTerrain().get(i).get(posY).getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(plateau.getTerrain().get(i).get(posY));
                }
                break;
            }
        }
        for (int i = posX - 1; i >= 0; i--) {
            if (plateau.getTerrain().get(i).get(posY).getPiece() == null) {
                res.add(plateau.getTerrain().get(i).get(posY));
            } 
            else {
                if (plateau.getTerrain().get(i).get(posY).getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(plateau.getTerrain().get(i).get(posY));
                }
                break;
            }
        }
        for (int i = posY + 1; i < 8; i++) {
            if (plateau.getTerrain().get(posX).get(i).getPiece() == null) {
                res.add(plateau.getTerrain().get(posX).get(i));
            } 
            else {
                if (plateau.getTerrain().get(posX).get(i).getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(plateau.getTerrain().get(posX).get(i));
                }
                break;
            }
        }
        for (int i = posY - 1; i >= 0; i--) {
            if (plateau.getTerrain().get(posX).get(i).getPiece() == null) {
                res.add(plateau.getTerrain().get(posX).get(i));
            } 
            else {
                if (plateau.getTerrain().get(posX).get(i).getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(plateau.getTerrain().get(posX).get(i));
                }
                break;
            }
        }
        return res;
    }

}
