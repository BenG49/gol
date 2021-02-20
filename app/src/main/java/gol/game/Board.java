package gol.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Color;
import java.awt.Font;

import gol.display.shapes.*;
import gol.display.shapes.Text.ScreenPos;
import gol.display.ui.*;
import gol.game.schematic.Schematic;
import gol.input.*;
import gol.util.*;

public class Board extends InputDisplay {

    private boolean betweenSteps;
    private Timer stepTimer;
    private TimerTask stepTimerTask;

    private boolean lastLeftMouse;
    private boolean lastRightMouse;
    private Vector2Int selectA;
    private Vector2Int selectB;
    private boolean promptingSave;

    private BoardInput input;
    public GameAlg game;
    private List<Schematic> schematics;
    
    private List<TextBoxElement> text;

    private static final int DEFAULT_WIDTH = 1000;
    private static final int OPTIMIZED_DRAW_INTERVAL = 10;

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
        selectA = new Vector2Int(0, 0);
        selectB = new Vector2Int(0, 0);
        promptingSave = false;

        text = new ArrayList<TextBoxElement>(Arrays.asList(
            new TextBoxElement(new RectType(0, HEIGHT/2, WIDTH/2, HEIGHT/2), this),
            new TextBoxElement(new RectType(WIDTH/2, HEIGHT/2, WIDTH/2, HEIGHT/2), this)
        ));
    }

    public void run() {
        while (input.getRun()) {
            List<Shape> outputShapes = new ArrayList<Shape>();

            if (input.getRunOptimized()) {
                input.checkKeysOptimized();
                if (input.getStepAuto())
                    game.step();

                if (game.getStepCount() % OPTIMIZED_DRAW_INTERVAL == 0)
                    drawBoardOptimized(outputShapes);
            } else if (promptingSave) {
                // drawBoard is so inefficient that its hard to get keypresses in
                drawBoard(outputShapes);
                schemSavePrompt(outputShapes);
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
                // 8411 steps non optimized 30 secs full speed
                // 436730 steps optimized 30 secs

                drawBoard(outputShapes);
                input.checkKeys();
                checkMouseClicks();
            }

            if (!(input.getRunOptimized() && game.getStepCount() % 10 > 0))
                draw(outputShapes);
        }
    }

    private void checkMouseClicks() {
        int selectMode = input.getSelectMode();
        // LEFT CLICK
        if (getButtonPressed(1)) {
            // just clicked
            if (!lastLeftMouse) {
                if (selectMode == 1)
                    selectA = getMouseGamePos();
            }

            Vector2Int mousePos = getMouseGamePos();

            // ADD CELL
            if (selectMode == 0)
                game.addCell(mousePos);

            lastLeftMouse = true;
        } else {
            // just released
            if (lastLeftMouse) {
                if (selectMode == 1) {
                    selectB = getMouseGamePos();
                    promptingSave = true;
                }

                lastLeftMouse = false;
            }
        }

        // RIGHT CLICK
        if (getButtonPressed(3)) {
            Vector2Int mousePos = getMouseGamePos();

            // REMOVE CELL
            if (selectMode == 0 && game.hasCell(mousePos))
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

        // pause indicator
        if (!input.getStepAuto()) {
            shapes.add(new FillRect(10, 10, 12, 40, 0, Color.WHITE));
            shapes.add(new FillRect(30, 10, 12, 40, 0, Color.WHITE));
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

        // selection area
        if (input.getSelectMode() == 1 && getButtonPressed(1)) {
            int cellLen = input.getCellScreenLen();
            Vector2 screenPos = input.getScreenPos();

            Vector2Int drawPos = selectA.sub(screenPos).mul(cellLen).floor();
            Vector2 mouseScreenPos = new Vector2(getMouse().x*WIDTH, (1-getMouse().y)*HEIGHT);
            // TODO: make this account for zoom
            Vector2Int size = roundToCellLen(mouseScreenPos.add(screenPos).sub(drawPos).ceil());

            if (size.x < 0) {
                drawPos.setX(drawPos.x+size.x-cellLen);
                size.setX(Math.abs(size.x));
            }
            if (size.y < 0) {
                drawPos.setY(drawPos.y+size.y-cellLen);
                size.setY(Math.abs(size.y));
            }

            shapes.add(new FillRect(drawPos, size, 0, new Color(1f, 1f, 1f, 0.5f)));
        }
        // mouse highlight
        else if (getMouse().x > 0 && getMouse().x < 1 && getMouse().y > 0 && getMouse().y < 1)
            projectToScreen(getMouseGamePos(), shapes, true);
        
        
        // bottom left
        text.get(0).setContents(new Text(new String[] {
                new Vector2Int(WIDTH/input.getCellScreenLen()/2, HEIGHT/input.getCellScreenLen()/2).add(input.getScreenPos().floor()).toString(),
                "Steps: "+game.getStepCount()
            }, ScreenPos.BOT_LEFT, WIDTH, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
        // bottom right
        shapes.add(new Text(new String[] {
            "Speed: "+input.getSpeed0to10()
        }, ScreenPos.BOT_RIGHT, WIDTH, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
    }

    private void projectToScreen(Vector2Int pos, List<Shape> shapes, boolean highlight) {
        final int CELL_WIDTH = (int) (input.getCellScreenLen()*0.95);

        Vector2Int drawPos = pos.sub(input.getScreenPos()).mul(input.getCellScreenLen()).round();
        Color color;

        if (highlight)
            color = new Color(1f, 1f, 1f, 0.25f);
        else
            color = Color.WHITE;

        shapes.add(new FillRect(drawPos, CELL_WIDTH, 0, color));
    }

    // TODO: optimize by having either cache or checking if mouse has moved
    public Vector2Int getMouseGamePos() {
        Vector2Int mousePos = new Vector2(getMouse().x, 1-getMouse().y).mul(HEIGHT/input.getCellScreenLen()).add(input.getScreenPos()).floor();
        return new Vector2Int(mousePos.x, mousePos.y);
    }

    private Vector2Int roundToCellLen(Vector2Int pos) {
        int cellLen = input.getCellScreenLen();
        return new Vector2Int(pos.x - pos.x % cellLen, pos.y - pos.y % cellLen).add((int)(cellLen*0.95));
    }

    private void schemSavePrompt(List<Shape> shapes) {
        shapes.add(new Text("Type "+input.keyBind.saveKey()+" to save, "+input.keyBind.cancelKey()+" to exit",
                            new Vector2Int(100, HEIGHT/2-50), Color.WHITE, new Font("Cascadia Code", Font.BOLD, 24)));

        int choice = input.checkSavePrompt();

        if (getButtonPressed(1) || getButtonPressed(3))
            promptingSave = false;

        if (choice != 0) {
            if (input.checkSavePrompt() == 1) {
                Schematic temp = new Schematic(game.getIterator(), selectA, selectB);
                try {
                    schematics.add(temp);
                } catch(NullPointerException e) {
                    schematics = new ArrayList<Schematic>();
                    schematics.add(temp);
                }
            }

            promptingSave = false;
        }
    }
}
