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
            input.checkKeys();
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
            drawBoard();
            input.checkKeys();
            checkMouseClicks();
        }
    }

    private void checkMouseClicks() {
        // LEFT CLICK
        if (getButtonPressed(1)) {
            lastLeftMouse = true;

            Vector2Int mousePos = getMouseGamePos();

            // ADD CELL
            if (!game.hasCell(mousePos))
                game.addCell(mousePos);
        } else if (lastLeftMouse)
            lastLeftMouse = false;

        // RIGHT CLICK
        if (getButtonPressed(3)) {
            lastRightMouse = true;

            Vector2Int mousePos = getMouseGamePos();

            // REMOVE CELL
            if (game.hasCell(mousePos))
                game.removeCell(mousePos);
        } else if (lastRightMouse)
            lastRightMouse = false;
    }

    public void drawBoard() {
        final int CELL_WIDTH = (int) (input.getCellScreenLen()*0.95);

        Vector2 max = input.getScreenPos().add(new Vector2(
            input.getScreenPos().x+WIDTH*input.getCellScreenLen(),
            input.getScreenPos().y+HEIGHT*input.getCellScreenLen()
        ));
        List<Shape> shapes = new ArrayList<Shape>();
        Iterator<Vector2Int> iterator = game.getIterator();

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
        
        shapes.add(new Text("Steps: "+game.getStepCount(),   5, HEIGHT-10,         Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
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
}
