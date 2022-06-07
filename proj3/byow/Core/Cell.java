package byow.Core;

public class Cell implements Comparable<Cell>{
    public Point pos;
    public double weight;

    public Cell(Point pos,double weight) {
        this.pos = pos;
        this.weight = weight;
    }

    @Override
    public int compareTo(Cell o) {
        return (int)(this.weight  - o.weight);
    }
}
