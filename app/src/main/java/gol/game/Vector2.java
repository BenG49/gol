package gol.game;

import com.stuypulse.stuylib.math.Angle;

public class Vector2 {
    public int x, y;

    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 add(Vector2 a) {
        return new Vector2(this.x+a.x, this.y+a.y);
    }

    public Vector2 sub(Vector2 a) {
        return new Vector2(this.x-a.x, this.y-a.y);
    }

    public Vector2 mul(int a) {
        return new Vector2(this.x*a, this.y*a);
    }

    public Vector2 div(int a) {
        return new Vector2((int)(this.x/a), (int)(this.y/a));
    }

    public boolean equals(Object a) {
        if (!(a instanceof Vector2))
            return false;
        if (a == this)
            return true;

        Vector2 temp = (Vector2) a;
        return this.x == temp.x && this.y == temp.y;
    }

    // thanks to Sam B from StuyLib for this method
    public Vector2 rotate(Angle angle, Vector2 origin) {
        Vector2 point = this.sub(origin);
        Vector2 out = new Vector2(
            (int) (point.x * angle.cos()) - (int)(point.y * angle.sin()),
            (int) (point.y * angle.cos()) + (int)(point.x * angle.sin())
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
