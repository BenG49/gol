package gol.game.schematic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.stuypulse.stuylib.math.Angle;

import java.util.ArrayList;
import java.util.Arrays;

import gol.util.Vector2Int;

public class Schematic {

    HashSet<Vector2Int> cells;
    List<Schematic> schematics;
    String path;

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
        this(constructor(allCells, selA, selB), new ArrayList<Schematic>(), Vector2Int.min(setToList(constructor(allCells, selA, selB))));
    }
    public Schematic(HashSet<Vector2Int> cells, Vector2Int origin) { this(cells, new ArrayList<Schematic>(), origin); }
    public Schematic(List<Schematic> schematics) { this(new HashSet<Vector2Int>(), schematics, new Vector2Int(0, 0)); }
    public Schematic(HashSet<Vector2Int> cells, List<Schematic> schematics, Vector2Int origin) {
        this.cells = cells;
        this.schematics = schematics;
        this.origin = origin;
    }

    private static HashSet<Vector2Int> constructor(Iterator<Vector2Int> allCells, Vector2Int selA, Vector2Int selB) {
        HashSet<Vector2Int> temp = new HashSet<Vector2Int>();

        while (allCells.hasNext()) {
            Vector2Int pos = allCells.next();
            if (!pos.within(selA, selB))
                continue;
            
            temp.add(pos);
        }

        return temp;
    }

    private static List<Vector2Int> setToList(HashSet<Vector2Int> set) {
        List<Vector2Int> temp = new ArrayList<Vector2Int>();
        temp.addAll(set);
        return temp;
    }

    public Vector2Int getOrigin() {
        return origin;
    }

    public HashSet<Vector2Int> getData() {
        HashSet<Vector2Int> output = new HashSet<Vector2Int>();

        for (Vector2Int pos : cells)
            output.add(pos.add(origin));

        for (Schematic schem : schematics) {
            HashSet<Vector2Int> temp = schem.getData();
            Vector2Int schemOrigin = schem.getOrigin();

            for (Vector2Int pos : temp)
                output.add(pos.add(schemOrigin));
        }

        return output;
    }

    public HashSet<String> getLinkedData() {
        HashSet<String> output = new HashSet<String>();

        for (Vector2Int pos : cells)
            output.add(pos.add(origin).JSONtoString());
        
        for (Schematic schem : schematics) {
            if (schem.getFilePath() != null)
                output.add(schem.getFilePath());
            else
                for (String pos : schem.getLinkedData())
                    output.add(pos);
        }

        return output;
    }

    public String getFilePath() {
        try {
            return path;
        } catch(NullPointerException e) {
            return null;
        }
    }

    public void setFilePath(String path) {
        this.path = path;
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
