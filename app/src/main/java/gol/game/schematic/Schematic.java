package gol.game.schematic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.stuypulse.stuylib.math.Angle;

import java.util.ArrayList;
import java.util.Arrays;

import gol.game.Vector2;

public class Schematic {

    HashSet<Vector2> cells;
    List<Schematic> schematics;

    // just used for multiple schematics
    Vector2 origin;

    public enum Pattern {
        BLINKER (Arrays.asList(new Vector2[] {
            new Vector2(0, -1),
            new Vector2(0, 0),
            new Vector2(0, 1)
        })),
        GLIDER (Arrays.asList(new Vector2[] {
            new Vector2(0, 0),
            new Vector2(0, 1),
            new Vector2(0, 2),
            new Vector2(1, 2),
            new Vector2(2, 1)
        })),
        BEACON (Arrays.asList(new Vector2[] {
            new Vector2(0, 2),
            new Vector2(0, 3),
            new Vector2(1, 2),
            new Vector2(1, 3),
            new Vector2(2, 0),
            new Vector2(2, 1),
            new Vector2(3, 0),
            new Vector2(3, 1)
        }));

        List<Vector2> cells;

        Pattern(List<Vector2> cells) {
            this.cells = cells;
        }

        public List<Vector2> getArray() {
            return cells;
        }
    };

    public Schematic(HashSet<Vector2> cells, Vector2 origin) {
        this.cells = cells;
        this.origin = origin;
    }
    public Schematic(List<Schematic> schematics) {
        this.schematics = schematics;
        origin = new Vector2(0, 0);
    }

    public Vector2 getOrigin() {
        return origin;
    }

    public HashSet<Vector2> getData() {
        HashSet<Vector2> output = cells;

        for (Schematic i : schematics) {
            HashSet<Vector2> temp = i.getData();
            Vector2 schemOrigin = i.getOrigin();

            Iterator<Vector2> iterator = temp.iterator();

            while (iterator.hasNext())
                output.add(iterator.next().add(schemOrigin));
        }

        return output;
    }

    public static List<Vector2> rotate90(List<Vector2> in) {
        List<Vector2> out = new ArrayList<Vector2>();

        for (Vector2 i : in)
            out.add(i.rotate(Angle.k90deg, new Vector2(0, 0)));

        return out;
    }

    public static HashSet<Vector2> getPattern(Pattern pattern, int rotation) {
        List<Vector2> temp = pattern.getArray();
        for (int i = 0; i < rotation; i++)
            temp = rotate90(temp);

        return new HashSet<Vector2>(temp);
    }
}
