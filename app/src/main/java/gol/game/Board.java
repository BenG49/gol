package gol.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import gol.display.shapes.FillRect;
import gol.display.shapes.Shape;
import gol.display.shapes.Text;
import gol.game.schematic.Schematic;
import gol.input.InputDisplay;

public class Board extends InputDisplay {

    private Vector2 screenPos;
    private int cellScreenLen;
    private int step;
    private HashSet<Vector2> aliveCells;

    private boolean run;
    private int stepTime;
    private boolean stepAuto;
    private int[] cooldownTimers;

    private final int KEY_COOLDOWN = 2_500_000;
    private final int KEY_COUNT = 3;

    public Board(HashSet<Vector2> aliveCells) { this(aliveCells, new Vector2(-10, -10), 25); }
    public Board(Schematic schem) { this(schem.getData(), new Vector2(-10, -10), 25); }
    public Board(HashSet<Vector2> aliveCells, Vector2 screenPos, int cellScreenLen) {
        super(Color.BLACK);

        this.aliveCells = aliveCells;
        this.screenPos = screenPos;
        this.cellScreenLen = cellScreenLen;

        step = 0;

        run = true;
        stepAuto = false;
        stepTime = 1000;
        cooldownTimers = new int[KEY_COUNT];
    }

    public void run() {
        drawBoard();

        while (run) {
            for (int i = 0; i < cooldownTimers.length; i++) {
                if (cooldownTimers[i] > 0)
                    cooldownTimers[i]--;
            }

            checkKeys();
            if (stepAuto) {
                try {
                    Thread.sleep(stepTime);

                    for (int i = 0; i < cooldownTimers.length; i++) {
                        if (cooldownTimers[i] > 0)
                            cooldownTimers[i] = 0;
                    }
                } catch (InterruptedException e) {}
                checkKeys();
                step();
            } else
                checkKeys();
        }
    }

    /* RULES:
    1. Live cell with <2 neighbors dies
    2. Live cell with 2-3 neighbors lives
    3. Dead cell with 3 neighbors is born
    4. Live cell with >4 neighbors dies */
    public void step() {
        HashSet<Vector2> next = (HashSet<Vector2>) aliveCells.clone();
        HashSet<Vector2> deadChecked = new HashSet<Vector2>();

        Iterator<Vector2> iterator = aliveCells.iterator();
        while (iterator.hasNext()) {
            Vector2 pos = iterator.next();
            int value = getNeighbors(pos);

            if (value > 4 || value < 2)
                next.remove(pos);
            
            for (int y = pos.y-1; y < pos.y+2; y++) {
                for (int x = pos.x-1; x < pos.x+2; x++) {
                    Vector2 temp = new Vector2(x, y);
                    if (!(x == pos.x && y == pos.y) && !deadChecked.contains(temp) && getNeighbors(temp) == 3) {
                        next.add(temp);
                    }
                    
                    deadChecked.add(temp);
                }
            }
        }

        aliveCells = next;
        step++;
        drawBoard();
    }

    public int getNeighbors(Vector2 pos) {
        int output = 0;

        for (int y = pos.y-1; y < pos.y+2; y++) {
            for (int x = pos.x-1; x < pos.x+2; x++) {
                if (!(x == pos.x && y == pos.y) && aliveCells.contains(new Vector2(x, y)))
                    output++;
            }
        }

        return output;
    }

    /*private static boolean withinCommonDist(Vector2 a, Vector2 b) {
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
    }*/

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
            
            shapes.add(new FillRect(drawPos.x, drawPos.y, (int) (cellScreenLen*0.95), (int) (cellScreenLen*0.95),
                0, Color.WHITE));
        }

        shapes.add(new Text("Steps: "+step, 5, HEIGHT-10, Color.WHITE, new Font("Cascadia Code", Font.BOLD, 24)));

        Shape[] output = new Shape[shapes.size()];
        for (int i = 0; i < output.length; i++)
            output[i] = shapes.get(i);

        draw(output);
    }

    private void checkKeys() {
        if (hasKey("Space") && cooldownTimers[0] == 0) {
            if (stepAuto) {
                stepAuto = false;
                cooldownTimers[0] = KEY_COOLDOWN;
            } else {
                stepAuto = true;
                cooldownTimers[0] = KEY_COOLDOWN;
            }
        }

        if (hasKey("q") || hasKey("Q") && cooldownTimers[1] == 0) {
            run = false;
            cooldownTimers[1] = KEY_COOLDOWN;
        }
        
        if (!stepAuto && (hasKey("e") || hasKey("E")) && cooldownTimers[2] == 0) {
            step();
            cooldownTimers[2] = KEY_COOLDOWN;
        }
    }
}
