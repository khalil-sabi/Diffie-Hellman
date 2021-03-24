package sample;

public class DataWrapper {
    private Noeud voisin1;
    private Noeud voisin2;
    private int poids;
    public DataWrapper(Noeud v, Noeud v2, int p){
        voisin1 = v;
        voisin2 = v2;
        poids = p;
    }

    public Noeud getVoisin1() {
        return voisin1;
    }

    public Noeud getVoisin2() {
        return voisin2;
    }

    public int getPoids() {
        return poids;
    }
}
