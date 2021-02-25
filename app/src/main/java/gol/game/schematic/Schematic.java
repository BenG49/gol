package gol.game.schematic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.stuypulse.stuylib.math.Angle;

import bglib.util.RectType;
import bglib.util.Vector2i;

public class Schematic {

    public static HashMap<String, Schematic> filePathLUT = new HashMap<String, Schematic>();
    private HashSet<Vector2i> cells;
    private List<Schematic> schematics;
    private String path;

    // just used for multiple schematics
    private Vector2i origin;

    public enum Pattern {
        BLINKER (Arrays.asList(
            new Vector2i(0, -1),
            Vector2i.ORIGIN,
            new Vector2i(0, 1)
        )),
        GLIDER (Arrays.asList(
            Vector2i.ORIGIN,
            new Vector2i(0, 1),
            new Vector2i(0, 2),
            new Vector2i(1, 2),
            new Vector2i(2, 1)
        )),
        BEACON (Arrays.asList(
            new Vector2i(0, 2),
            new Vector2i(0, 3),
            new Vector2i(1, 2),
            new Vector2i(1, 3),
            new Vector2i(2, 0),
            new Vector2i(2, 1),
            new Vector2i(3, 0),
            new Vector2i(3, 1)
        ));

        List<Vector2i> cells;

        Pattern(List<Vector2i> cells) {
            this.cells = cells;
        }

        public List<Vector2i> getArray() {
            return cells;
        }
    };

    public Schematic(Schematic copy) {
        this.cells = new HashSet<Vector2i>(copy.cells);
        this.schematics = new ArrayList<Schematic>(copy.schematics);
        this.origin = new Vector2i(copy.origin);
        try {
            this.path = copy.path;
            filePathLUT.put(path, this);
        } catch(NullPointerException e) {}
    }

    public Schematic(Iterator<Vector2i> allCells, Vector2i selA, Vector2i selB) { this(Vector2i.overallWithin(allCells, selA, selB)); }
    public Schematic(HashSet<Vector2i> cells, Vector2i origin) { this(cells, new ArrayList<Schematic>(), origin); }
    public Schematic(List<Schematic> schematics) { this(new HashSet<Vector2i>(), schematics, Vector2i.ORIGIN); }
    public Schematic(HashSet<Vector2i> cells) { this(cells, new ArrayList<Schematic>(), Vector2i.overallMin(cells)); }
    public Schematic(HashSet<Vector2i> cells, List<Schematic> schematics, Vector2i origin) {
        this.cells = new HashSet<Vector2i>(cells);
        this.schematics = schematics;
        this.origin = origin;
    }

    public Vector2i getOrigin() {
        return origin;
    }

    public HashSet<Vector2i> getData() {
        HashSet<Vector2i> output = new HashSet<Vector2i>();
        
        if (origin.equals(Vector2i.ORIGIN))
            output = cells;

        for (Vector2i pos : cells)
            output.add(pos.sub(origin));
            
        if (schematics.size() == 0)
            return output;

        for (Schematic schem : schematics) {
            Vector2i schemOrigin = schem.getOrigin();

            for (Vector2i pos : schem.getData())
                output.add(pos.add(schemOrigin));
        }

        return output;
    }

    public HashSet<String> getLinkedData() {
        HashSet<String> output = new HashSet<String>();

        for (Vector2i pos : cells)
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
        return path;
    }

    /**
     * 
     * @param cellLen current cell length, or zoom level
     * @param offset integer position in cells on the screen
     * @return RectType of the bounding box around the schematic
     */
    public RectType getBoundingBox(int cellLen, Vector2i offset) {
        Vector2i max = Vector2i.overallMax(cells);

        return new RectType(
            offset.mul(cellLen),
            max.sub(origin).abs().mul(cellLen).add((int)(cellLen*0.95))
        );
    }

    public void setFilePath(String path) {
        this.path = path;
        filePathLUT.put(path, this);
    }

    public void setOrigin(Vector2i origin) {
        this.origin = origin;
    }

    public static List<Vector2i> rotate90(List<Vector2i> in) {
        List<Vector2i> out = new ArrayList<Vector2i>();

        for (Vector2i i : in)
            out.add(i.rotate(Angle.k90deg, Vector2i.ORIGIN));

        return out;
    }

    public static void rotate90(Schematic in) {
        HashSet<Vector2i> data = in.getData();
        HashSet<Vector2i> output = new HashSet<Vector2i>();

        Vector2i center = Vector2i.avg(Vector2i.overallMax(data), in.origin);

        for (Vector2i pos : data)
            output.add(pos.rotate(Angle.k90deg, center));

        in.cells = output;
        in.origin = Vector2i.overallMin(output);
    }

    public static void mirrorX(Schematic in) {
        int minX = Vector2i.overallMin(in.getData()).x;
        int maxX = Vector2i.overallMax(in.getData()).x;
        int originToCenter = minX+(maxX-minX)/2;

        HashSet<Vector2i> output = new HashSet<Vector2i>();

        for (Vector2i pos : in.getData()) {
            pos.setX(-(pos.x-originToCenter)*2+pos.x);
            output.add(pos);
        }

        in.cells = output;
        in.origin = Vector2i.overallMin(output);
    }

    public static HashSet<Vector2i> getPattern(Pattern pattern, int rotation) {
        List<Vector2i> temp = pattern.getArray();
        for (int i = 0; i < rotation; i++)
            temp = rotate90(temp);

        return new HashSet<Vector2i>(temp);
    }

    public static Schematic parseFile(HashSet<String> contents) {
        HashSet<Vector2i> outputCells = new HashSet<Vector2i>();
        List<Schematic> outputSchems = new ArrayList<Schematic>();

        for (String index : contents) {
            if (index.contains(".json")) {
                if (filePathLUT.containsKey(index))
                    outputSchems.add(filePathLUT.get(index));
                else
                    outputSchems.add(parseFile(JSON.JSONRead(index)));
            } else
                outputCells.add(new Vector2i(index));
        }

        if (outputSchems.size() == 0)
            return new Schematic(outputCells);

        return new Schematic(outputCells, outputSchems, Vector2i.overallMin(outputCells));
    }
}
