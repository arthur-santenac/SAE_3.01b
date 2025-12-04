public class Fou extends Piece {
    
    public Fou(int joueur) {
        super("♗", joueur);
        if (joueur == 2) this.img = "♝";
    }

}
