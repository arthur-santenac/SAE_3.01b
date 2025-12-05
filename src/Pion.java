public class Pion extends Piece {
    
    public Pion(int joueur) {
        super(" ♙ ", joueur);
        if (joueur == 2) this.img = " ♟ ";
    }

}
