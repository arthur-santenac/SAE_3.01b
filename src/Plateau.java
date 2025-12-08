import java.util.ArrayList;
import java.util.Collections;
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
        initPieces();
    }

    public void initPieces() {
        for (Case laCase : this.terrain.get(1)) laCase.setPieceCourante(new Pion(2));
        for (Case laCase : this.terrain.get(6)) laCase.setPieceCourante(new Pion(1));
        this.terrain.get(0).get(0).setPieceCourante(new Tour(2));
        this.terrain.get(0).get(1).setPieceCourante(new Cavalier(2));
        this.terrain.get(0).get(2).setPieceCourante(new Fou(2));
        this.terrain.get(0).get(3).setPieceCourante(new Dame(2));
        this.terrain.get(0).get(4).setPieceCourante(new Roi(2));
        this.terrain.get(0).get(5).setPieceCourante(new Fou(2));
        this.terrain.get(0).get(6).setPieceCourante(new Cavalier(2));
        this.terrain.get(0).get(7).setPieceCourante(new Tour(2));
        this.terrain.get(7).get(0).setPieceCourante(new Tour(1));
        this.terrain.get(7).get(1).setPieceCourante(new Cavalier(1));
        this.terrain.get(7).get(2).setPieceCourante(new Fou(1));
        this.terrain.get(7).get(3).setPieceCourante(new Roi(1));
        this.terrain.get(7).get(4).setPieceCourante(new Dame(1));
        this.terrain.get(7).get(5).setPieceCourante(new Fou(1));
        this.terrain.get(7).get(6).setPieceCourante(new Cavalier(1));
        this.terrain.get(7).get(7).setPieceCourante(new Tour(1));
    }

    public void placer(int ligne, int colonne, Piece piece){
        this.terrain.get(ligne).get(colonne).setPieceCourante(piece);
    }

    public List<List<Case>> getTerrain() {
        return this.terrain;
    }

    public void deplacer(Case caseDepart, Case caseArrive) {
        caseArrive.setPieceCourante(caseDepart.getPiece());
        caseDepart.setPieceCourante(null);
    }

    public void affichage(boolean inverser){
        List<List<Case>> copieTerrain;
        if (inverser) {
            copieTerrain = new ArrayList<>();
            for (List<Case> ligne : this.terrain) {
                List<Case> copieLigne = new ArrayList<>(ligne);
                Collections.reverse(copieLigne);
                copieTerrain.add(copieLigne);
            }
            Collections.reverse(copieTerrain);
        } else {
            copieTerrain = this.terrain;
        }
        String res = "     A   B   C   D   E   F   G   H\n";
        res += "   ┌───┬───┬───┬───┬───┬───┬───┬───┐\n";
        for(int i = 0; i<8; i++){
            res += " " + (8 - i) + " │";
            for(int j = 0; j<8; j++){
                Piece piece = copieTerrain.get(i).get(j).getPiece();
                if (piece == null) res += "   ";
                else res += piece.toString();
                res += "│";
            }
            res += "\n";
            if (i != 7) {
                res += "   ├───┼───┼───┼───┼───┼───┼───┼───┤\n";
            }
        }
        res += "   └───┴───┴───┴───┴───┴───┴───┴───┘";
        System.out.println(res);
    }
}

