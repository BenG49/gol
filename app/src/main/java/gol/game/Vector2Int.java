package gol.game;

import com.stuypulse.stuylib.math.Angle;

public class Vector2Int {
    public final int x, y;

    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2Int(int xy) {
        x = xy;
        y = xy;
    }

    public Vector2Int add(Vector2Int a) {
        return new Vector2Int(this.x+a.x, this.y+a.y);
    }

    public Vector2Int sub(Vector2Int a) {
        return new Vector2Int(this.x-a.x, this.y-a.y);
    }

    public Vector2 add(Vector2 a) {
        return new Vector2(this.x+a.x, this.y+a.y);
    }

    public Vector2 sub(Vector2 a) {
        return new Vector2(this.x-a.x, this.y-a.y);
    }

    public Vector2 mul(double a) {
        return new Vector2(this.x*a, this.y*a);
    }

    public Vector2 div(double a) {
        return new Vector2(this.x/a, this.y/a);
    }

    public boolean equals(Object a) {
        if (!(a instanceof Vector2Int))
            return false;
        if (a == this)
            return true;

        Vector2Int temp = (Vector2Int) a;
        return this.x == temp.x && this.y == temp.y;
    }

    // thanks to Sam B from StuyLib for this method
    public Vector2Int rotate(Angle angle, Vector2Int origin) {
        Vector2Int point = this.sub(origin);
        Vector2Int out = new Vector2Int(
            (int)(point.x * angle.cos()) - (int)(point.y * angle.sin()),
            (int)(point.y * angle.cos()) + (int)(point.x * angle.sin())
        );

        return origin.add(out);
    }

    // TODO: improve hashing
    public int hashCode() {
        int hash = 23;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        return hash;
    }

    public String toString() {
        return "("+x+","+y+")";
    }
}

