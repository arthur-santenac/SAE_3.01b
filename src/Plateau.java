import java.util.ArrayList;
import java.util.List;



public class Plateau{
    private List<List<Case>> terrain;

    public Plateau(){
        this.terrain = new ArrayList<>();
        for (int i = 0; i<8; i++){
            for(int j = 0; j<8; j++){
                this.terrain.get(i).set(j, new Case());
            }

        }
    }
}