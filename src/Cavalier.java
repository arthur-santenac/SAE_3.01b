public class Cavalier extends Piece {
    
    public Cavalier(int joueur) {
        super(" ♘ ", joueur);
        if (joueur == 2) this.img = " ♞ ";
    }

}
