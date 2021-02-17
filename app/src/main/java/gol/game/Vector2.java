package gol.game;

public class Vector2 {
    public int x, y;

    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object a) {
        if (!(a instanceof Vector2))
            return false;
        if (a == this)
            return true;

        Vector2 temp = (Vector2) a;
        return this.x == temp.x && this.y == temp.y;
    }

    // TODO: make it so each has a unique value
    public int hashCode() {
        return (int) Math.pow(x, y);
    }

    public String toString() {
        return "("+x+","+y+")";
    }
}
