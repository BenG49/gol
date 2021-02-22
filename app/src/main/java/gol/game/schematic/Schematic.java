package gol.game.schematic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.awt.Color;

import com.stuypulse.stuylib.math.Angle;

import gol.util.Vector2Int;
import gol.display.shapes.Rect;

public class Schematic {

    public static HashMap<String, Schematic> filePathLUT = new HashMap<String, Schematic>();
    private HashSet<Vector2Int> cells;
    private List<Schematic> schematics;
    private String path;

    // just used for multiple schematics
    private Vector2Int origin;

    public enum Pattern {
        BLINKER (Arrays.asList(
            new Vector2Int(0, -1),
            new Vector2Int(0, 0),
            new Vector2Int(0, 1)
        )),
        GLIDER (Arrays.asList(
            new Vector2Int(0, 0),
            new Vector2Int(0, 1),
            new Vector2Int(0, 2),
            new Vector2Int(1, 2),
            new Vector2Int(2, 1)
        )),
        BEACON (Arrays.asList(
            new Vector2Int(0, 2),
            new Vector2Int(0, 3),
            new Vector2Int(1, 2),
            new Vector2Int(1, 3),
            new Vector2Int(2, 0),
            new Vector2Int(2, 1),
            new Vector2Int(3, 0),
            new Vector2Int(3, 1)
        ));

        List<Vector2Int> cells;

        Pattern(List<Vector2Int> cells) {
            this.cells = cells;
        }

        public List<Vector2Int> getArray() {
            return cells;
        }
    };

    public Schematic(Iterator<Vector2Int> allCells, Vector2Int selA, Vector2Int selB) { this(Vector2Int.overallWithin(allCells, selA, selB)); }
    public Schematic(HashSet<Vector2Int> cells, Vector2Int origin) { this(cells, new ArrayList<Schematic>(), origin); }
    public Schematic(List<Schematic> schematics) { this(new HashSet<Vector2Int>(), schematics, new Vector2Int(0, 0)); }
    public Schematic(HashSet<Vector2Int> cells) { this(cells, new ArrayList<Schematic>(), Vector2Int.overallMin(cells)); }
    public Schematic(HashSet<Vector2Int> cells, List<Schematic> schematics, Vector2Int origin) {
        this.cells = new HashSet<Vector2Int>(cells);
        this.schematics = schematics;
        this.origin = origin;
    }

    public Vector2Int getOrigin() {
        return origin;
    }

    public HashSet<Vector2Int> getData() {
        HashSet<Vector2Int> output = new HashSet<Vector2Int>();
        
        if (origin.equals(new Vector2Int(0)))
            output = cells;

        for (Vector2Int pos : cells)
            output.add(pos.sub(origin));
            
        if (schematics.size() == 0)
            return output;

        for (Schematic schem : schematics) {
            Vector2Int schemOrigin = schem.getOrigin();

            for (Vector2Int pos : schem.getData())
                output.add(pos.add(schemOrigin));
        }

        return output;
    }

    public HashSet<String> getLinkedData() {
        HashSet<String> output = new HashSet<String>();

        for (Vector2Int pos : cells)
            output.add(pos.sub(origin).JSONtoString());
        
        if (schematics.size() == 0)
            return output;
        
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

    public Rect getBoundingBox(int border, Color color, int cellLen, Vector2Int offset) {
        Vector2Int min = Vector2Int.overallMin(cells);
        Vector2Int max = Vector2Int.overallMax(cells);
        return new Rect(offset.add(min).mul(cellLen), max.add(min).abs().add(1).mul(cellLen), border, color);
    }

    public void setFilePath(String path) {
        this.path = path;
        filePathLUT.put(path, this);
    }

    public static List<Vector2Int> rotate90(List<Vector2Int> in) {
        List<Vector2Int> out = new ArrayList<Vector2Int>();

        for (Vector2Int i : in)
            out.add(i.rotate(Angle.k90deg, new Vector2Int(0)));

        return out;
    }

    public static void rotate90(Schematic in) {
        HashSet<Vector2Int> data = in.getData();
        HashSet<Vector2Int> set = new HashSet<Vector2Int>();

        for (Vector2Int pos : data)
            set.add(pos.rotate(Angle.k90deg, new Vector2Int(0)));
        
        in.cells = set;
    }

    public static HashSet<Vector2Int> getPattern(Pattern pattern, int rotation) {
        List<Vector2Int> temp = pattern.getArray();
        for (int i = 0; i < rotation; i++)
            temp = rotate90(temp);

        return new HashSet<Vector2Int>(temp);
    }

    public static Schematic parseFile(HashSet<String> contents) {
        HashSet<Vector2Int> outputCells = new HashSet<Vector2Int>();
        List<Schematic> outputSchems = new ArrayList<Schematic>();

        for (String index : contents) {
            if (index.contains(".json")) {
                if (filePathLUT.containsKey(index))
                    outputSchems.add(filePathLUT.get(index));
                else
                    outputSchems.add(parseFile(JSON.JSONRead(index)));
            } else
                outputCells.add(new Vector2Int(index));
        }

        if (outputSchems.size() == 0)
            return new Schematic(outputCells);

        return new Schematic(outputCells, outputSchems, Vector2Int.overallMin(outputCells));
    }
}
