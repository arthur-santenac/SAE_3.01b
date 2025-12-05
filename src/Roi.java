public class Roi extends Piece {
 
    public Roi(int joueur) {
        super(" ♔ ", joueur);
        if (joueur == 2) this.img = " ♚ ";
    }

}
