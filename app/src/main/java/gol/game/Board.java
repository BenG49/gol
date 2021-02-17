package gol.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import gol.display.shapes.FillRect;
import gol.display.shapes.Shape;
import gol.input.InputDisplay;

public class Board extends InputDisplay {

    private Vector2 screenPos;
    private int cellScreenLen;

    private HashSet<Vector2> aliveCells;

    public Board(HashSet<Vector2> aliveCells) { this(aliveCells, new Vector2(-10, -10), 25); }
    public Board(HashSet<Vector2> aliveCells, Vector2 screenPos, int zoom) {
        super(Color.BLACK);

        this.aliveCells = aliveCells;
        this.screenPos = screenPos;
        this.cellScreenLen = zoom;
    }

    /* RULES:
    1. Live cell with <2 neighbors dies
    2. Live cell with 2-3 neighbors lives
    3. Dead cell with 3 neighbors is born
    4. Live cell with >4 neighbors dies */
    public void step() {
        HashSet<Vector2> next = (HashSet<Vector2>) aliveCells.clone();
        HashMap<Vector2, Vector2> commonCells = new HashMap<Vector2, Vector2>();

        Iterator<Vector2> iterator = aliveCells.iterator();
        while (iterator.hasNext()) {
            Vector2 pos = iterator.next();
            int value = getNeighbors(pos);

            if (value > 4 || value < 2)
                next.remove(pos);
            
            Iterator<Vector2> nestedIterator = aliveCells.iterator();
            while(nestedIterator.hasNext()) {
                Vector2 nestedPos = nestedIterator.next();
                /* IF
                temp and nestedTemp are within common distance and
                the mapping doesn't already exist */
                if (withinCommonDist(pos, nestedPos) && !alreadyExists(commonCells, pos, nestedPos)) {
                    // so that there's only one value per key
                    if (commonCells.containsKey(pos))
                        commonCells.put(nestedPos, pos);
                    else
                        commonCells.put(pos, nestedPos);
                }
            }
        }

        for (HashMap.Entry<Vector2, Vector2> m : commonCells.entrySet()) {
            // A -> B (m)
            // B -> C

            /* IF
            B -> C exists
            withinCommonDist(A, C)*/
            if (commonCells.containsKey(m.getValue()) && withinCommonDist(m.getKey(), commonCells.get(m.getValue()))) {
                List<Vector2> points = getCommonCells(m.getKey(), m.getValue(), commonCells.get(m.getValue()));

                for (Vector2 i : points)
                    next.add(i);
            }
        }

        aliveCells = (HashSet<Vector2>) next.clone();
        drawBoard();
    }

    public int getNeighbors(Vector2 pos) {
        if (!aliveCells.contains(pos))
            return -1;

        int output = 0;

        for (int y = pos.y-1; y < pos.y+2; y++) {
            for (int x = pos.x-1; x < pos.x+2; x++) {
                if (!(x == 0 && y == 0) && aliveCells.contains(new Vector2(x, y)))
                    output++;
            }
        }

        return output;
    }

    private static boolean withinCommonDist(Vector2 a, Vector2 b) {
        if (a == b)
            return false;

        return Math.abs(a.x-b.x) < 3 && Math.abs(a.y-b.y) < 3;
    }

    private static List<Vector2> getCommonCells(Vector2 a, Vector2 b, Vector2 c) {
        List<Vector2> output = new ArrayList<Vector2>();

        for (int y = a.y-1; y < a.y+2; y++) {
            for (int x = a.x-1; x < a.x+2; x++) {
                if (!(x == 0 && y == 0) && Math.abs(x-b.x) < 2 && Math.abs(y-b.y) < 2 && Math.abs(x-c.x) < 2 && Math.abs(y-c.y) < 2)
                    output.add(new Vector2(x, y));
            }
        }

        return output;
    }

    private static boolean alreadyExists(HashMap<Vector2, Vector2> map, Vector2 a, Vector2 b) {
        if (map.containsKey(a) && map.get(a) == b)
            return true;
        if (map.containsKey(b) && map.get(b) == a)
            return true;
        
        return false;
    }

    public void print(Vector2 min, Vector2 max) {
        for (int y = (int) min.y; y < max.y; y++) {
            for (int x = (int) min.x; x < max.x; x++) {
                if (aliveCells.contains(new Vector2(x, y)))
                    System.out.print("O");
                else
                    System.out.print(" ");
            }

            System.out.println();
        }
    }

    public void drawBoard() {
        Vector2 max = screenPos.add(new Vector2(WIDTH*cellScreenLen, HEIGHT*cellScreenLen));
        List<Shape> shapes = new ArrayList<Shape>();

        Iterator<Vector2> iterator = aliveCells.iterator();

        while (iterator.hasNext()) {
            Vector2 point = iterator.next();

            if (point.x < screenPos.x || point.x > max.x || point.y < screenPos.y || point.y > max.y)
                continue;
        
            Vector2 drawPos = (point.sub(screenPos)).mul(cellScreenLen);
            
            shapes.add(new FillRect(
                drawPos.x,
                drawPos.y,
                cellScreenLen,
                cellScreenLen,
                0,
                Color.WHITE
            ));
        }

        Shape[] output = new Shape[shapes.size()];
        for (int i = 0; i < output.length; i++)
            output[i] = shapes.get(i);

        draw(output);
    }
}
