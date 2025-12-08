import java.util.ArrayList;
import java.util.List;

public class Fou extends Piece {
    
    public Fou(int numJoueur) {
        super("♗", numJoueur);
        if (numJoueur == 2) this.img = "♝";
    }

    public List<Case> casesPossibles(Plateau plateau, int posX, int posY) {
        List<Case> res = new ArrayList<>();
        for (int i = posX + 1; i < 8; i++) {
            if (plateau.getTerrain().get(i).get(i).getPiece() == null) {
                res.add(plateau.getTerrain().get(i).get(i));
            }
            else if(plateau.getTerrain().get(i).get(i).getPiece() != null){
                if (plateau.getTerrain().get(i).get(i).getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(plateau.getTerrain().get(i).get(i));
                }
                break;
            }
            else if (plateau.getTerrain().get(i).get(-i).getPiece() == null) {
                res.add(plateau.getTerrain().get(i).get(-i));
            }
            else {
                if (plateau.getTerrain().get(i).get(-i).getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(plateau.getTerrain().get(i).get(-i));
                }
                break;
            }
        }
        for (int i = posX - 1; i >= 0; i--) {
            if (plateau.getTerrain().get(i).get(i).getPiece() == null) {
                res.add(plateau.getTerrain().get(i).get(i));
            }
            else if(plateau.getTerrain().get(i).get(i).getPiece() != null){
                if (plateau.getTerrain().get(i).get(i).getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(plateau.getTerrain().get(i).get(i));
                }
                break;
            }
            else if (plateau.getTerrain().get(i).get(-i).getPiece() == null) {
                res.add(plateau.getTerrain().get(i).get(-i));
            }
            else {
                if (plateau.getTerrain().get(i).get(-i).getPiece().getNumJoueur() != this.numJoueur) {
                    res.add(plateau.getTerrain().get(i).get(-i));
                }
                break;
            }
        }
        return res;
    }

}
