public class Dame extends Piece {
    
    public Dame(int joueur) {
        super(" ♕ ", joueur);
        if (joueur == 2) this.img = " ♛ ";
    }

}
