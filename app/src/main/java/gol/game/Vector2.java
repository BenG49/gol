package gol.game;

import com.stuypulse.stuylib.math.Angle;

public class Vector2 {
    public final double x, y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(double xy) {
        x = xy;
        y = xy;
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
            point.x * angle.cos() - point.y * angle.sin(),
            point.y * angle.cos() + point.x * angle.sin()
        );

        return origin.add(out);
    }

    public Vector2 round() {
        return new Vector2(Math.round(x), Math.round(y));
    }

    public Vector2 ceil() {
        return new Vector2(Math.ceil(x), Math.ceil(y));
    }

    public Vector2 floor() {
        return new Vector2(Math.floor(x), Math.floor(y));
    }

    public int hashCode() {
        int hash = 23;
        hash = (int)(hash * 31 + x);
        hash = (int)(hash * 31 + y);
        return hash;
    }

    public String toString() {
        return "("+x+","+y+")";
    }
}
