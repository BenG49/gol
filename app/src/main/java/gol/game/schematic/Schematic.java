package gol.game.schematic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.stuypulse.stuylib.math.Angle;

import java.util.ArrayList;
import java.util.Arrays;

import gol.game.Vector2Int;

public class Schematic {

    HashSet<Vector2Int> cells;
    List<Schematic> schematics;

    // just used for multiple schematics
    Vector2Int origin;

    public enum Pattern {
        BLINKER (Arrays.asList(new Vector2Int[] {
            new Vector2Int(0, -1),
            new Vector2Int(0, 0),
            new Vector2Int(0, 1)
        })),
        GLIDER (Arrays.asList(new Vector2Int[] {
            new Vector2Int(0, 0),
            new Vector2Int(0, 1),
            new Vector2Int(0, 2),
            new Vector2Int(1, 2),
            new Vector2Int(2, 1)
        })),
        BEACON (Arrays.asList(new Vector2Int[] {
            new Vector2Int(0, 2),
            new Vector2Int(0, 3),
            new Vector2Int(1, 2),
            new Vector2Int(1, 3),
            new Vector2Int(2, 0),
            new Vector2Int(2, 1),
            new Vector2Int(3, 0),
            new Vector2Int(3, 1)
        }));

        List<Vector2Int> cells;

        Pattern(List<Vector2Int> cells) {
            this.cells = cells;
        }

        public List<Vector2Int> getArray() {
            return cells;
        }
    };

    public Schematic(Iterator<Vector2Int> allCells, Vector2Int selA, Vector2Int selB) {
        cells = new HashSet<Vector2Int>();

        while (allCells.hasNext()) {
            Vector2Int temp = allCells.next();
            if (!temp.within(selA, selB))
                continue;
            
            cells.add(temp);
        }
    }
    public Schematic(HashSet<Vector2Int> cells, Vector2Int origin) {
        this.cells = cells;
        this.origin = origin;
    }
    public Schematic(List<Schematic> schematics) {
        this.schematics = schematics;
        origin = new Vector2Int(0, 0);
    }

    public Vector2Int getOrigin() {
        return origin;
    }

    public HashSet<Vector2Int> getData() {
        HashSet<Vector2Int> output = new HashSet<Vector2Int>();

        try {
            if (!origin.equals(new Vector2Int(0, 0))) {
                Iterator<Vector2Int> iterator = cells.iterator();
                while (iterator.hasNext())
                    output.add(iterator.next().add(origin));
            } else
                output = cells;
        } catch (NullPointerException e) {}

        try {
            for (Schematic i : schematics) {
                HashSet<Vector2Int> temp = i.getData();
                Vector2Int schemOrigin = i.getOrigin();

                Iterator<Vector2Int> iterator = temp.iterator();

                while (iterator.hasNext())
                    output.add(iterator.next().add(schemOrigin));
            }
        } catch (NullPointerException e) {}

        return output;
    }

    public static List<Vector2Int> rotate90(List<Vector2Int> in) {
        List<Vector2Int> out = new ArrayList<Vector2Int>();

        for (Vector2Int i : in)
            out.add(i.rotate(Angle.k90deg, new Vector2Int(0, 0)));

        return out;
    }

    public static HashSet<Vector2Int> getPattern(Pattern pattern, int rotation) {
        List<Vector2Int> temp = pattern.getArray();
        for (int i = 0; i < rotation; i++)
            temp = rotate90(temp);

        return new HashSet<Vector2Int>(temp);
    }
}
