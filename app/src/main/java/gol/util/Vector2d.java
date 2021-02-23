package gol.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.stuypulse.stuylib.math.Angle;

public class Vector2d {
    public double x, y;

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2d(double xy) {
        x = xy;
        y = xy;
    }

    public Vector2d(String asString) {
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

    public Vector2d add(Vector2d a) {
        return new Vector2d(this.x+a.x, this.y+a.y);
    }

    public Vector2d sub(Vector2d a) {
        return new Vector2d(this.x-a.x, this.y-a.y);
    }

    public Vector2d add(Vector2i a) {
        return new Vector2d(this.x+a.x, this.y+a.y);
    }

    public Vector2d sub(Vector2i a) {
        return new Vector2d(this.x-a.x, this.y-a.y);
    }

    public Vector2d add(double a) {
        return new Vector2d(this.x+a, this.y+a);
    }

    public Vector2d sub(double a) {
        return new Vector2d(this.x-a, this.y-a);
    }
    
    public Vector2d mul(double a) {
        return new Vector2d(this.x*a, this.y*a);
    }

    public Vector2d div(double a) {
        return new Vector2d(this.x/a, this.y/a);
    }

    public boolean within(Vector2d a, Vector2d b) {
        return this.x >= Math.min(a.x, b.x) && this.x <= Math.max(a.x, b.x)
            && this.y >= Math.min(a.y, b.y) && this.y <= Math.max(a.y, b.y);
    }

    public static HashSet<Vector2d> overallWithin(Iterator<Vector2d> data, Vector2d a, Vector2d b) {
        HashSet<Vector2d> temp = new HashSet<Vector2d>();
        while (data.hasNext())
            temp.add(data.next());

        return overallWithin(temp, a, b);
    }
    public static HashSet<Vector2d> overallWithin(HashSet<Vector2d> data, Vector2d a, Vector2d b) {
        HashSet<Vector2d> output = new HashSet<Vector2d>();

        for (Vector2d pos : data)
            if (pos.within(a, b))
                output.add(pos);
        
        return output;
    }

    public Vector2d floorToInterval(double interval) {
        return new Vector2d(this.x - this.x%interval, this.y - this.y % interval);
    }

    public static Vector2d min(Vector2d a, Vector2d b) {
        return (a.x <= b.x && a.y <= b.y) ? a : b;
    }

    public static Vector2d max(Vector2d a, Vector2d b) {
        return (a.x >= b.x && a.y >= b.y) ? a : b;
    }

    public static Vector2d min(List<Vector2d> data) {
        if (data.size() == 0)
            return null;
        
        Vector2d min = data.get(0);
        for (Vector2d i : data)
            if (Vector2d.min(min, i).equals(i))
                min = i;
        
        return min;
    }

    public static Vector2d max(List<Vector2d> data) {
        if (data.size() == 0)
            return null;
        
        Vector2d max = data.get(0);
        for (Vector2d i : data)
            if (Vector2d.max(max, i).equals(i))
                max = i;
        
        return max;
    }

    public Vector2d abs() {
        return new Vector2d(Math.abs(this.x), Math.abs(this.y));
    }

    public static Vector2d overallMin(HashSet<Vector2d> data) {
        Vector2d min = new Vector2d(Integer.MAX_VALUE);

        for (Vector2d i : data) {
            if (i.x < min.x)
                min.setX(i.x);
            if (i.y < min.y)
                min.setY(i.y);
        }

        return min;
    }

    public static Vector2d overallMax(HashSet<Vector2d> data) {
        Vector2d max = new Vector2d(Integer.MIN_VALUE);

        for (Vector2d i : data) {
            if (i.x > max.x)
                max.setX(i.x);
            if (i.y > max.y)
                max.setY(i.y);
        }

        return max;
    }

    public boolean equals(Object a) {
        if (!(a instanceof Vector2d))
            return false;
        if (a == this)
            return true;

        Vector2d temp = (Vector2d) a;
        return this.x == temp.x && this.y == temp.y;
    }

    // thanks to Sam B from StuyLib for this method
    public Vector2d rotate(Angle angle, Vector2d origin) {
        Vector2d point = this.sub(origin);
        Vector2d out = new Vector2d(
            point.x * angle.cos() - point.y * angle.sin(),
            point.y * angle.cos() + point.x * angle.sin()
        );

        return origin.add(out);
    }

    public Vector2i round() {
        return new Vector2i((int)Math.round(x), (int)Math.round(y));
    }

    public Vector2i ceil() {
        return new Vector2i((int)Math.ceil(x), (int)Math.ceil(y));
    }

    public Vector2i floor() {
        return new Vector2i((int)Math.floor(x), (int)Math.floor(y));
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
