public class Executable {
    public static void main(String[] args) {
        Plateau p = new Plateau();
        p.placer(0, 0, new Cavalier(0));
        p.affichage();
    }
}
