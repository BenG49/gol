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

    private boolean betweenSteps;
    private Timer stepTimer;
    private TimerTask stepTimerTask;

    private boolean lastLeftMouse;
    private boolean lastRightMouse;
    private Vector2Int selectA;
    private Vector2Int selectB;

    private BoardInput input;
    public GameAlg game;

    private static final int DEFAULT_WIDTH = 1000;

    public Board(HashSet<Vector2Int> aliveCells) { this(aliveCells, 25, new KeyBinding()); }
    public Board(Schematic schem) { this(schem.getData(), 25, new KeyBinding()); }
    public Board(HashSet<Vector2Int> aliveCells, int cellScreenLen, KeyBinding binding) {
        super(DEFAULT_WIDTH, DEFAULT_WIDTH, Color.BLACK);

        game = new GameAlg(aliveCells);
        input = new BoardInput(this, binding, cellScreenLen);
        stepTimer = new Timer();

        betweenSteps = false;
        lastLeftMouse = false;
        lastRightMouse = false;
    }

    public void run() {
        while (input.getRun()) {
            List<Shape> outputShapes = new ArrayList<Shape>();

            if (input.getRunOptimized()) {
                input.checkKeysOptimized();
                if (input.getStepAuto())
                    game.step();

                drawBoardOptimized(outputShapes);
            } else {
            if (input.getStepAuto()) {
                if (!betweenSteps) {
                    game.step();

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
            drawBoard(outputShapes);
            input.checkKeys();
            checkMouseClicks();
            }

            draw(outputShapes);
        }
    }

    private void checkMouseClicks() {
        // LEFT CLICK
        if (getButtonPressed(1)) {
            // just clicked
            if (!lastLeftMouse)
                selectA = getMouseGamePos();

            Vector2Int mousePos = getMouseGamePos();

            // ADD CELL
            if (!game.hasCell(mousePos))
                game.addCell(mousePos);

            lastLeftMouse = true;
        } else {
            // just released
            if (lastLeftMouse) {
                selectB = getMouseGamePos();

                lastLeftMouse = false;
            }
        }

        // RIGHT CLICK
        if (getButtonPressed(3)) {
            Vector2Int mousePos = getMouseGamePos();

            // REMOVE CELL
            if (game.hasCell(mousePos))
                game.removeCell(mousePos);

            lastRightMouse = true;
        } else if (lastRightMouse)
            lastRightMouse = false;
    }

    public void drawBoardOptimized(List<Shape> shapes) {
        final int CELL_WIDTH = (int) (input.getCellScreenLen()*0.95);

        Vector2 max = input.getScreenPos().add(new Vector2(
            input.getScreenPos().x+WIDTH*input.getCellScreenLen(),
            input.getScreenPos().y+HEIGHT*input.getCellScreenLen()
        ));
        Iterator<Vector2Int> iterator = game.getIterator();

        while (iterator.hasNext()) {
            Vector2Int pos = iterator.next();

            if (pos.x+CELL_WIDTH < input.getScreenPos().x || pos.x > max.x || pos.y+CELL_WIDTH < input.getScreenPos().y || pos.y > max.y)
                continue;
        
            projectToScreen(pos, shapes, false);
        }
    }

    public void drawBoard(List<Shape> shapes) {
        drawBoardOptimized(shapes);

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
        
        shapes.add(new Text("Steps: "+game.getStepCount(),   5, HEIGHT-10,         Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
        shapes.add(new Text("Speed: "+input.getSpeed0to10(), WIDTH-115, HEIGHT-10, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
        shapes.add(new Text(new Vector2Int(
            WIDTH/input.getCellScreenLen()/2,
            HEIGHT/input.getCellScreenLen()/2).add(input.getScreenPos().floor()).toString(),
            5, HEIGHT-35, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
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
        Vector2 mousePos = new Vector2(getMouse().x, 1-getMouse().y).mul(HEIGHT/input.getCellScreenLen()).add(input.getScreenPos()).floor();
        return new Vector2Int((int)mousePos.x, (int)mousePos.y);
    }
}
