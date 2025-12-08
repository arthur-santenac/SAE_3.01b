public class Case {

    private Piece pieceCourante;

    public Case(){
        this.pieceCourante = null;
    }

    public void setPieceCourante(Piece piece){
        this.pieceCourante = piece;
    }
    
    public Piece getPiece(){
        if (pieceCourante instanceof Piece){
            return this.pieceCourante;
        }
        return null;
    }
}
