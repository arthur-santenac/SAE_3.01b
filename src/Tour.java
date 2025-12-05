public class Tour extends Piece {
    
    public Tour(int joueur) {
        super(" ♖ ", joueur);
        if (joueur == 2) this.img = " ♜ ";
    }

}
