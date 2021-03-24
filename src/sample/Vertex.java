package sample;

import java.util.ArrayList;

class Vertex implements Comparable<Vertex>
{
    public final Noeud c;
    public ArrayList<Voisin> adjacents;
    public int minDistance = Integer.MAX_VALUE;
    public Vertex DernierVisite;
    public Vertex(Noeud c) {
        this.c = c;
        adjacents = new ArrayList<>();
    }
    public Noeud getConnected() {
        return c;
    }
    public int compareTo(Vertex other)
    {
        return Integer.compare(minDistance, other.minDistance);
    }

}