import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JeuEchec {
    
    Plateau plateau;
    Integer joueurActuel;
    Map<Character, Integer> significationLettre;

    public JeuEchec() {
        this.plateau = new Plateau();
        this.joueurActuel = 1;
        this.significationLettre = new HashMap<>();
        this.significationLettre.put('a', 0);
        this.significationLettre.put('b', 1);
        this.significationLettre.put('c', 2);
        this.significationLettre.put('d', 3);
        this.significationLettre.put('e', 4);
        this.significationLettre.put('f', 5);
        this.significationLettre.put('g', 6);
        this.significationLettre.put('h', 7);
    }

    public String affichage(boolean inverser) {
        String res = this.plateau.affichage(inverser);
        res += "\nAu joueur " + joueurActuel + " de jouer";
        return res; 
    }

    public String jouer(int numJoueur, String caseDep, String caseArr) {
        String coup = caseDep + caseArr;
        if (numJoueur == joueurActuel) {
            if (coup.matches("[a-h][1-8][a-h][1-8]")) {
                int jDep = this.significationLettre.get(coup.charAt(0));
                int iDep = Character.getNumericValue(coup.charAt(1));
                int jArr = this.significationLettre.get(coup.charAt(2));
                int iArr =  Character.getNumericValue(coup.charAt(3));
                Case caseDepart = this.plateau.getTerrain().get(8 - iDep).get(jDep);
                Case caseArrive = this.plateau.getTerrain().get(8 - iArr).get(jArr);
                if (caseDepart.getPiece() != null) {
                    if (caseDepart.getPiece().getNumJoueur() == this.joueurActuel) {
                        if (caseArrive.getPiece() == null || caseArrive.getPiece().getNumJoueur() != this.joueurActuel) {
                            if (caseDepart.getPiece().casesPossibles(this.plateau, 8 - iDep, jDep).contains(caseArrive)) {
                                if (coup.charAt(3) == '1' || coup.charAt(3) == '8') this.plateau.deplacer(caseDepart, caseArrive, true, numJoueur);
                                else this.plateau.deplacer(caseDepart, caseArrive, false, numJoueur);
                                this.joueurActuel = 3 - this.joueurActuel;
                            } else {
                                return "Déplacement illégal";
                            }
                        } else {
                            return "Vous ne pouvez pas manger vos propres pièces";
                        }
                    } else {
                        return "Cette pièce ne vous appartient pas";
                    }
                } else {
                    return "Il n'y a pas de pièce à cet endroit";
                }
            } else {
                return "Format invalide. Utilisez le bon format (ex: a2a4)";
            }
        } else {
            return "Ce n'est pas a vous de jouer";
        }
        return "";
    }

    public boolean estFini() {
        return !this.plateau.contientDeuxRoi();
    }

    public String promote(String caseSrc, String piece, int numJoueur) {
        if (numJoueur == joueurActuel) {
            if (caseSrc.matches("[a-h][1-8]")) {
                int jDep = this.significationLettre.get(caseSrc.charAt(0));
                int iDep = Character.getNumericValue(caseSrc.charAt(1));
                Case caseDepart = this.plateau.getTerrain().get(8 - iDep).get(jDep);
                if (caseDepart.getPiece() != null && caseDepart.getPiece() instanceof Pion) {
                    if (Arrays.asList("q", "r", "b", "k").contains(piece)) {
                        if (caseDepart.getPiece().getNumJoueur() == this.joueurActuel) {
                            caseDepart.getPiece().promoteP = piece;
                        } else {
                            return "Cette pièce ne vous appartient pas";
                        }
                    } else {
                        return "Vous pouvez seulement faire une promotion en q, r, b, ou k";
                    }
                } else {
                    return "Il n'y a pas de pièce à cet endroit ou ce n'est pas un pion";
                }
            } else {
                return "Format invalide. Utilisez le bon format (ex: a2)";
            }
        } else {
            return "Ce n'est pas a vous de jouer";
        }
        return "OK";
    }

}
