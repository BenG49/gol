package gol.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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

    public static HashSet<Vector2Int> overallWithin(Iterator<Vector2Int> data, Vector2Int a, Vector2Int b) {
        HashSet<Vector2Int> temp = new HashSet<Vector2Int>();
        while (data.hasNext())
            temp.add(data.next());

        return overallWithin(temp, a, b);
    }
    public static HashSet<Vector2Int> overallWithin(HashSet<Vector2Int> data, Vector2Int a, Vector2Int b) {
        HashSet<Vector2Int> output = new HashSet<Vector2Int>();

        for (Vector2Int pos : data)
            if (pos.within(a, b))
                output.add(pos);
        
        return output;
    }

    public Vector2 toVector2() {
        return new Vector2(this.x, this.y);
    }

    public Vector2Int floorToInterval(int interval) {
        return new Vector2Int(this.x - this.x%interval, this.y - this.y % interval);
    }

    public static Vector2Int min(Vector2Int a, Vector2Int b) {
        return (a.x <= b.x && a.y <= b.y) ? a : b;
    }

    public static Vector2Int max(Vector2Int a, Vector2Int b) {
        return (a.x >= b.x && a.y >= b.y) ? a : b;
    }

    public static Vector2Int min(List<Vector2Int> data) {
        if (data.size() == 0)
            return null;
        
        Vector2Int min = data.get(0);
        for (Vector2Int i : data)
            if (Vector2Int.min(min, i).equals(i))
                min = i;
        
        return min;
    }

    public static Vector2Int max(List<Vector2Int> data) {
        if (data.size() == 0)
            return null;
        
        Vector2Int max = data.get(0);
        for (Vector2Int i : data)
            if (Vector2Int.max(max, i).equals(i))
                max = i;
        
        return max;
    }

    public Vector2Int abs() {
        return new Vector2Int(Math.abs(this.x), Math.abs(this.y));
    }

    public static Vector2Int overallMin(HashSet<Vector2Int> data) {
        Vector2Int min = new Vector2Int(Integer.MAX_VALUE);

        for (Vector2Int i : data) {
            if (i.x < min.x)
                min.setX(i.x);
            if (i.y < min.y)
                min.setY(i.y);
        }

        return min;
    }

    public static Vector2Int overallMax(HashSet<Vector2Int> data) {
        Vector2Int max = new Vector2Int(Integer.MIN_VALUE);

        for (Vector2Int i : data) {
            if (i.x > max.x)
                max.setX(i.x);
            if (i.y > max.y)
                max.setY(i.y);
        }

        return max;
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

