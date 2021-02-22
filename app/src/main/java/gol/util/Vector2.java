package gol.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.stuypulse.stuylib.math.Angle;

public class Vector2 {
    public double x, y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(double xy) {
        x = xy;
        y = xy;
    }

    public Vector2(String asString) {
        String[] coords = asString.replaceAll("\\)", "").replaceAll("\\(", "").replaceAll(" ", "").split(",");
        try {
            x = Double.parseDouble(coords[0]);
            y = Double.parseDouble(coords[1]);
        } catch (NullPointerException e) {
            System.out.println("Incorrect string given");
            x = 0;
            y = 0;
        }
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Vector2 add(Vector2 a) {
        return new Vector2(this.x+a.x, this.y+a.y);
    }

    public Vector2 sub(Vector2 a) {
        return new Vector2(this.x-a.x, this.y-a.y);
    }

    public Vector2 add(Vector2Int a) {
        return new Vector2(this.x+a.x, this.y+a.y);
    }

    public Vector2 sub(Vector2Int a) {
        return new Vector2(this.x-a.x, this.y-a.y);
    }

    public Vector2 add(double a) {
        return new Vector2(this.x+a, this.y+a);
    }

    public Vector2 sub(double a) {
        return new Vector2(this.x-a, this.y-a);
    }
    
    public Vector2 mul(double a) {
        return new Vector2(this.x*a, this.y*a);
    }

    public Vector2 div(double a) {
        return new Vector2(this.x/a, this.y/a);
    }

    public boolean within(Vector2 a, Vector2 b) {
        return this.x >= Math.min(a.x, b.x) && this.x <= Math.max(a.x, b.x)
            && this.y >= Math.min(a.y, b.y) && this.y <= Math.max(a.y, b.y);
    }

    public static HashSet<Vector2> overallWithin(Iterator<Vector2> data, Vector2 a, Vector2 b) {
        HashSet<Vector2> temp = new HashSet<Vector2>();
        while (data.hasNext())
            temp.add(data.next());

        return overallWithin(temp, a, b);
    }
    public static HashSet<Vector2> overallWithin(HashSet<Vector2> data, Vector2 a, Vector2 b) {
        HashSet<Vector2> output = new HashSet<Vector2>();

        for (Vector2 pos : data)
            if (pos.within(a, b))
                output.add(pos);
        
        return output;
    }

    public Vector2 floorToInterval(double interval) {
        return new Vector2(this.x - this.x%interval, this.y - this.y % interval);
    }

    public static Vector2 min(Vector2 a, Vector2 b) {
        return (a.x <= b.x && a.y <= b.y) ? a : b;
    }

    public static Vector2 max(Vector2 a, Vector2 b) {
        return (a.x >= b.x && a.y >= b.y) ? a : b;
    }

    public static Vector2 min(List<Vector2> data) {
        if (data.size() == 0)
            return null;
        
        Vector2 min = data.get(0);
        for (Vector2 i : data)
            if (Vector2.min(min, i).equals(i))
                min = i;
        
        return min;
    }

    public static Vector2 max(List<Vector2> data) {
        if (data.size() == 0)
            return null;
        
        Vector2 max = data.get(0);
        for (Vector2 i : data)
            if (Vector2.max(max, i).equals(i))
                max = i;
        
        return max;
    }

    public static Vector2 overallMin(HashSet<Vector2> data) {
        Vector2 min = new Vector2(Integer.MAX_VALUE);

        for (Vector2 i : data) {
            if (i.x < min.x)
                min.setX(i.x);
            if (i.y < min.y)
                min.setY(i.y);
        }

        return min;
    }

    public static Vector2 overallMax(HashSet<Vector2> data) {
        Vector2 max = new Vector2(Integer.MIN_VALUE);

        for (Vector2 i : data) {
            if (i.x > max.x)
                max.setX(i.x);
            if (i.y > max.y)
                max.setY(i.y);
        }

        return max;
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

    public Vector2Int round() {
        return new Vector2Int((int)Math.round(x), (int)Math.round(y));
    }

    public Vector2Int ceil() {
        return new Vector2Int((int)Math.ceil(x), (int)Math.ceil(y));
    }

    public Vector2Int floor() {
        return new Vector2Int((int)Math.floor(x), (int)Math.floor(y));
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
