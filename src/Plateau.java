import java.util.ArrayList;
import java.util.List;



public class Plateau{
    private List<List<Case>> terrain;

    public Plateau(){
        this.terrain = new ArrayList<>();
        for (int i = 0; i<8; i++){
            List<Case> ligne = new ArrayList<>();
            for(int j = 0; j<8; j++){
                ligne.add(new Case());
            }
            this.terrain.add(ligne);
        }
    }

    public void placer(int ligne, int colonne, Piece piece){
        this.terrain.get(ligne).get(colonne).setPieceCourante(piece);
    }

    public void affichage(){
        String res = "┌─┬─┬─┬─┬─┬─┬─┬─┐\n";
        for(int i = 0; i<8; i++){

            for(int j = 0; j<8; j++){
                res += "│";
                //Piece piece = terrain.get(i).get(j).getPiece();
                //if (piece == null) System.out.println(" ");
                res += " ";
            }
            res += "\n";
        }
        System.out.println(res);
    }
}

