package gol.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Font;

import gol.display.shapes.*;
import gol.game.schematic.Schematic;
import gol.input.BoardInput;
import gol.input.InputDisplay;
import gol.input.KeyBinding;

public class Board extends InputDisplay {

    private HashSet<Vector2Int> aliveCells;
    private int stepCount;
    private boolean betweenSteps;
    private Timer stepTimer;
    private TimerTask stepTimerTask;

    private BoardInput input;

    private static final int DEFAULT_WIDTH = 700;

    public Board(HashSet<Vector2Int> aliveCells) { this(aliveCells, 25, new KeyBinding()); }
    public Board(Schematic schem) { this(schem.getData(), 25, new KeyBinding()); }
    public Board(HashSet<Vector2Int> aliveCells, int cellScreenLen, KeyBinding binding) {
        super(DEFAULT_WIDTH, DEFAULT_WIDTH, Color.BLACK);

        this.aliveCells = aliveCells;

        input = new BoardInput(this, binding, cellScreenLen);
        stepTimer = new Timer();

        stepCount = 0;
        betweenSteps = false;
    }

    public void run() {
        while (input.getRun()) {
            input.checkKeys();
            if (input.getStepAuto()) {
                if (!betweenSteps) {
                    step();

                    if (input.getStepTimeMillis() > 1) {
                        betweenSteps = true;
                        stepTimerTask = new TimerTask() {
                            public void run() {
                                betweenSteps = false;
                            }
                        };

                        stepTimer.schedule(stepTimerTask, input.getStepTimeMillis());
                    }
                }
            }
            drawBoard();
            input.checkKeys();
            checkMouseClicks();
        }
    }

    /* RULES:
    1. Live cell with <2 neighbors dies
    2. Live cell with 2-3 neighbors lives
    3. Dead cell with 3 neighbors is born
    4. Live cell with >3 neighbors dies */
    public void step() {
        HashSet<Vector2Int> next = (HashSet<Vector2Int>) aliveCells.clone();
        HashSet<Vector2Int> deadChecked = new HashSet<Vector2Int>();

        Iterator<Vector2Int> iterator = aliveCells.iterator();
        while (iterator.hasNext()) {
            Vector2Int pos = iterator.next();
            int value = getNeighbors(pos);

            if (value > 3 || value < 2)
                next.remove(pos);
            
            for (int y = pos.y-1; y < pos.y+2; y++) {
                for (int x = pos.x-1; x < pos.x+2; x++) {
                    Vector2Int temp = new Vector2Int(x, y);
                    if (deadChecked.contains(temp) || (x == pos.x && y == pos.y))
                        continue;

                    deadChecked.add(temp);

                    if (getNeighbors(temp) == 3)
                        next.add(temp);
                }
            }
        }

        aliveCells = next;
        stepCount++;
    }

    public int getNeighbors(Vector2Int pos) {
        int output = 0;

        for (int y = pos.y-1; y < pos.y+2; y++) {
            for (int x = pos.x-1; x < pos.x+2; x++) {
                if (!(x == pos.x && y == pos.y) && aliveCells.contains(new Vector2Int(x, y)))
                    output++;
            }
        }

        return output;
    }

    private void checkMouseClicks() {
        // LEFT CLICK -> ADD CELL
        if (getButtonPressed(1)) {
            Vector2Int mousePos = getMouseGamePos();

            if (!aliveCells.contains(mousePos))
                aliveCells.add(mousePos);
        }

        // RIGHT CLICK -> REMOVE CELL
        if (getButtonPressed(3)) {
            Vector2Int mousePos = getMouseGamePos();

            if (aliveCells.contains(mousePos))
                aliveCells.remove(mousePos);
        }
    }

    public void drawBoard() {
        final int CELL_WIDTH = (int) (input.getCellScreenLen()*0.95);

        Vector2 max = input.getScreenPos().add(new Vector2(
            input.getScreenPos().x+WIDTH*input.getCellScreenLen(),
            input.getScreenPos().y+HEIGHT*input.getCellScreenLen()
        ));
        List<Shape> shapes = new ArrayList<Shape>();
        Iterator<Vector2Int> iterator = aliveCells.iterator();

        while (iterator.hasNext()) {
            Vector2Int pos = iterator.next();

            if (pos.x+CELL_WIDTH < input.getScreenPos().x || pos.x > max.x || pos.y+CELL_WIDTH < input.getScreenPos().y || pos.y > max.y)
                continue;
        
            projectToScreen(pos, shapes, false);
        }

        // cross around 0,0
        projectToScreen(new Vector2Int(1, 0), shapes, true);
        projectToScreen(new Vector2Int(0, 1), shapes, true);
        projectToScreen(new Vector2Int(-1, 0), shapes, true);
        projectToScreen(new Vector2Int(0, -1), shapes, true);
        projectToScreen(new Vector2Int(2, 0), shapes, true);
        projectToScreen(new Vector2Int(0, 2), shapes, true);
        projectToScreen(new Vector2Int(-2, 0), shapes, true);
        projectToScreen(new Vector2Int(0, -2), shapes, true);

        // mouse highlight
        if (getMouse().x > 0 && getMouse().x < 1 && getMouse().y > 0 && getMouse().y < 1)
            projectToScreen(getMouseGamePos(), shapes, true);
        
        shapes.add(new Text("Steps: "+stepCount,             5, HEIGHT-10,        Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
        shapes.add(new Text("Speed: "+input.getSpeed0to10(), WIDTH-115, HEIGHT-10, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
        shapes.add(new Text(new Vector2Int(
            WIDTH/input.getCellScreenLen()/2,
            HEIGHT/input.getCellScreenLen()/2).add(input.getScreenPos().floor()).toString(),
            5, HEIGHT-35, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));

        Shape[] output = new Shape[shapes.size()];
        for (int i = 0; i < output.length; i++)
            output[i] = shapes.get(i);

        draw(output);
    }

    private void projectToScreen(Vector2Int pos, List<Shape> shapes, boolean highlight) {
        final int CELL_WIDTH = (int) (input.getCellScreenLen()*0.95);

        Vector2 drawPos =  pos.sub(input.getScreenPos()).mul(input.getCellScreenLen());
        Color color;

        if (highlight)
            color = new Color(1f, 1f, 1f, 0.25f);
        else
            color = Color.WHITE;

        shapes.add(new FillRect((int)drawPos.round().x, (int)drawPos.round().y,
            CELL_WIDTH, CELL_WIDTH, 0, color));
    }

    // TODO: optimize by having either cache or checking if mouse has moved
    public Vector2Int getMouseGamePos() {
        Vector2 mousePos = new Vector2(getMouse().x, 1-getMouse().y).mul(HEIGHT).div(input.getCellScreenLen()).add(input.getScreenPos()).floor();
        return new Vector2Int((int)mousePos.x, (int)mousePos.y);
    }

    public void clearCells() {
        aliveCells = new HashSet<Vector2Int>();
    }
}
