package gol.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.stuypulse.stuylib.math.Angle;

public class Vector2i {
    public int x, y;

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i(int xy) {
        x = xy;
        y = xy;
    }

    public Vector2i(String asString) {
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

    public Vector2i add(Vector2i a) {
        return new Vector2i(this.x+a.x, this.y+a.y);
    }

    public Vector2i sub(Vector2i a) {
        return new Vector2i(this.x-a.x, this.y-a.y);
    }

    public Vector2d add(Vector2d a) {
        return new Vector2d(this.x+a.x, this.y+a.y);
    }

    public Vector2d sub(Vector2d a) {
        return new Vector2d(this.x-a.x, this.y-a.y);
    }

    public Vector2i add(int a) {
        return new Vector2i(this.x+a, this.y+a);
    }

    public Vector2i sub(int a) {
        return new Vector2i(this.x-a, this.y-a);
    }

    public Vector2i mul(int a) {
        return new Vector2i(this.x*a, this.y*a);
    }

    public Vector2d div(int a) {
        return new Vector2d(this.x/a, this.y/a);
    }

    public boolean within(Vector2i a, Vector2i b) {
        return this.x >= Math.min(a.x, b.x) && this.x <= Math.max(a.x, b.x)
            && this.y >= Math.min(a.y, b.y) && this.y <= Math.max(a.y, b.y);
    }

    public static HashSet<Vector2i> overallWithin(Iterator<Vector2i> data, Vector2i a, Vector2i b) {
        HashSet<Vector2i> temp = new HashSet<Vector2i>();
        while (data.hasNext())
            temp.add(data.next());

        return overallWithin(temp, a, b);
    }
    public static HashSet<Vector2i> overallWithin(HashSet<Vector2i> data, Vector2i a, Vector2i b) {
        HashSet<Vector2i> output = new HashSet<Vector2i>();

        for (Vector2i pos : data)
            if (pos.within(a, b))
                output.add(pos);
        
        return output;
    }

    public Vector2d toVector2() {
        return new Vector2d(this.x, this.y);
    }

    public Vector2i floorToInterval(int interval) {
        return new Vector2i(this.x - this.x%interval, this.y - this.y % interval);
    }

    public static Vector2i min(Vector2i a, Vector2i b) {
        return (a.x <= b.x && a.y <= b.y) ? a : b;
    }

    public static Vector2i max(Vector2i a, Vector2i b) {
        return (a.x >= b.x && a.y >= b.y) ? a : b;
    }

    public static Vector2i min(List<Vector2i> data) {
        if (data.size() == 0)
            return null;
        
        Vector2i min = data.get(0);
        for (Vector2i i : data)
            if (Vector2i.min(min, i).equals(i))
                min = i;
        
        return min;
    }

    public static Vector2i max(List<Vector2i> data) {
        if (data.size() == 0)
            return null;
        
        Vector2i max = data.get(0);
        for (Vector2i i : data)
            if (Vector2i.max(max, i).equals(i))
                max = i;
        
        return max;
    }

    public Vector2i abs() {
        return new Vector2i(Math.abs(this.x), Math.abs(this.y));
    }

    public static Vector2i overallMin(HashSet<Vector2i> data) {
        Vector2i min = new Vector2i(Integer.MAX_VALUE);

        for (Vector2i i : data) {
            if (i.x < min.x)
                min.setX(i.x);
            if (i.y < min.y)
                min.setY(i.y);
        }

        return min;
    }

    public static Vector2i overallMax(HashSet<Vector2i> data) {
        Vector2i max = new Vector2i(Integer.MIN_VALUE);

        for (Vector2i i : data) {
            if (i.x > max.x)
                max.setX(i.x);
            if (i.y > max.y)
                max.setY(i.y);
        }

        return max;
    }

    public boolean equals(Object a) {
        if (!(a instanceof Vector2i))
            return false;
        if (a == this)
            return true;

        Vector2i temp = (Vector2i) a;
        return this.x == temp.x && this.y == temp.y;
    }

    // thanks to Sam B from StuyLib for this method
    public Vector2i rotate(Angle angle, Vector2i origin) {
        Vector2i point = this.sub(origin);
        Vector2i out = new Vector2i(
            (int)(point.x * angle.cos()) - (int)(point.y * angle.sin()),
            (int)(point.y * angle.cos()) + (int)(point.x * angle.sin())
        );

        return origin.add(out);
    }

    public int hashCode() {
        // 15k steps 30 seconds
        // long hash = 0xDEADBEEFDEADBEEFL;

        // hash ^= ((long)x) << 32;
        // hash ^= ((long)y) << 0;
        // hash ^= hash << 13;
        // hash ^= hash >> 7;
        // hash ^= hash << 17;
        // hash ^= ((long)y) << 32;
        // hash ^= ((long)x) << 0;
        // hash ^= hash << 13;
        // hash ^= hash >> 7;
        // hash ^= hash << 17;

        // return (int)hash;

        // 17k steps 30 seconds
        int tX = x;
        int tY = y;
        tX ^= tX << 13;
        tY ^= tY << 13;
        tX ^= tX >> 17;
        tY ^= tY >> 7;
        tX ^= tX << 5;
        tY ^= tY << 17;
        return tX ^ tY;
    }

    public String JSONtoString() {
        return x+", "+y;
    }

    public String toString() {
        return "("+x+","+y+")";
    }
}

