package gol.util;

import com.stuypulse.stuylib.math.Angle;

public class Vector2Int {
    public int x, y;

    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2Int(int xy) {
        x = xy;
        y = xy;
    }

    public Vector2Int(String asString) {
        String[] coords = asString.replaceAll("\\)", "").replaceAll("\\(", "").replaceAll(" ", "").split(",");
        try {
            x = Integer.parseInt(coords[0]);
            y = Integer.parseInt(coords[1]);
        } catch (NullPointerException e) {
            System.out.println("Incorrect string given");
            x = 0;
            y = 0;
        }
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
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

    public Vector2Int add(int a) {
        return new Vector2Int(this.x+a, this.y+a);
    }

    public Vector2Int sub(int a) {
        return new Vector2Int(this.x-a, this.y-a);
    }

    public Vector2Int mul(int a) {
        return new Vector2Int(this.x*a, this.y*a);
    }

    public Vector2 div(int a) {
        return new Vector2(this.x/a, this.y/a);
    }

    public boolean within(Vector2Int a, Vector2Int b) {
        return this.x >= Math.min(a.x, b.x) && this.x <= Math.max(a.x, b.x)
            && this.y >= Math.min(a.y, b.y) && this.y <= Math.max(a.y, b.y);
    }

    public Vector2 toVector2() {
        return new Vector2(this.x, this.y);
    }

    public Vector2Int floorToInterval(int interval) {
        return new Vector2Int(this.x - this.x%interval, this.y - this.y % interval);
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

    public int hashCode() {
        int hash = 23;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        return hash;
    }

    public String JSONtoString() {
        return x+", "+y;
    }

    public String toString() {
        return "("+x+","+y+")";
    }
}

