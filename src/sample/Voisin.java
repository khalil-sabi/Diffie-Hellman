package sample;


class Voisin
{
    public final Vertex autre;
    public final int poids;
    public Voisin(Vertex autre, int poids) {
        this.autre = autre;
        this.poids = poids;
    }
}