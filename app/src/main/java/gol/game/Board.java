package gol.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.*;

import bglib.input.InputDisplay;
import bglib.display.shapes.*;
import bglib.display.shapes.Shape.Conversion;
import bglib.display.shapes.AlignText.Alignment;
import bglib.util.*;

import gol.game.schematic.*;
import gol.input.*;

public class Board extends InputDisplay {

    // step intervals
    private boolean betweenSteps;
    private Timer stepTimer;
    private TimerTask stepTimerTask;

    // mouse flags
    protected boolean lastLeftMouse;
    private boolean lastRightMouse;

    // selection points
    protected Vector2i selectA;
    protected Vector2i selectB;

    // display modes
    private boolean promptingSel;
    protected boolean displayKeybinds;
    protected boolean placeSchem;
    protected Schematic tempSchem;

    // undo
    private List<HashSet<Vector2i>> undoCells;
    private int undoIndex;

    // mouse dragging sel points
    private Vector2i mouseDragA;
    private Vector2i mouseDragDelta;

    protected HashMap<Schematic, Integer> allSchematics;
    protected BoardInput input;
    public GameAlg game;

    private static final int DEFAULT_WIDTH = 1000;
    private static final int OPTIMIZED_DRAW_INTERVAL = 50;
    private static final boolean OPTIMIZED_RENDER = true;

    public final Conversion conversion;

    public Board() {
        this(new HashSet<Vector2i>(), 24, new KeyBinding());
    }
    public Board(HashSet<Vector2i> aliveCells) {
        this(aliveCells, 24, new KeyBinding());
    }
    public Board(HashSet<Vector2i> aliveCells, int cellScreenLen, KeyBinding binding) {
        super(DEFAULT_WIDTH, DEFAULT_WIDTH, Color.BLACK);

        BoardUI.createMenu(this);

        game = new GameAlg(aliveCells);
        input = new BoardInput(this, binding, cellScreenLen);
        stepTimer = new Timer();
        undoCells = new ArrayList<HashSet<Vector2i>>();
        allSchematics = new HashMap<Schematic, Integer>();
        conversion = (pos) -> (pos.sub(input.getScreenPos()).mul(input.getCellLen()).floor());

        betweenSteps = false;
        lastLeftMouse = false;
        lastRightMouse = false;
        selectA = Vector2i.ORIGIN;
        selectB = Vector2i.ORIGIN;
        mouseDragA = Vector2i.ORIGIN;
        mouseDragDelta = Vector2i.ORIGIN;
        promptingSel = false;
        undoIndex = 0;
    }

    public void run() {
        while (input.getRun()) {
            if (input.getRunOptimized()) {
                input.checkKeysOptimized();
                if (input.getStepAuto())
                    game.step();

                if (OPTIMIZED_RENDER && game.getStepCount() % OPTIMIZED_DRAW_INTERVAL == 0)
                    BoardUI.drawOptimized(this);
                    
            } else {
                if (promptingSel)
                    selectionPrompt();
                else if (displayKeybinds)
                    BoardUI.drawKeybindings(this);
                else if (placeSchem) {
                    BoardUI.placeSchemDraw(tempSchem, this);
                    input.checkKeysOptimized();
                } else {
                    if (input.getStepAuto() && !betweenSteps) {
                        game.step();
                        ctrlZClear();

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
                    // laptop specs:
                    // 8411 steps non optimized 30 secs full speed
                    // 436730 steps optimized 30 secs

                    input.checkKeys();
                    checkMouseClicks();
                }

                BoardUI.drawBoard(this);
            }

            if (input.getRunOptimized()) {
                if (OPTIMIZED_RENDER && game.getStepCount() % OPTIMIZED_DRAW_INTERVAL == 0)
                    draw(conversion);
            } else
                draw(conversion);
        }
    }

    private void checkMouseClicks() {
        final int selectMode = input.getSelectMode();
        final Vector2i mousePos = getMouseGamePos();

        // LEFT CLICK
        if (getButtonPressed(1)) {
            // just clicked
            if (!lastLeftMouse) {
                if (promptingSel)
                    promptingSel = false;
                else if (selectMode == 0 && !input.getStepAuto())
                    undoCells.add(new HashSet<Vector2i>());
                else if (selectMode == 1)
                    selectA = mousePos;

                lastLeftMouse = true;
            }

            // ADD CELL
            if (selectMode == 0 && !promptingSel) {
                if (!input.getStepAuto())
                    undoCells.get(undoIndex).add(mousePos);

                game.addCell(mousePos);
            }
        } else {
            // just released
            if (lastLeftMouse) {
                if (selectMode == 0 && !input.getStepAuto())
                    undoIndex++;
                else if (selectMode == 1) {
                    selectB = mousePos;
                    promptingSel = true;
                }

                lastLeftMouse = false;
            }
        }

        // RIGHT CLICK
        if (getButtonPressed(3)) {
            if (!lastRightMouse) {
                if (promptingSel)
                    mouseDragA = mousePos;

                lastRightMouse = true;
            }

            if (promptingSel)
                mouseDragDelta = mouseDragA.sub(mousePos);

            // REMOVE CELL
            else if (selectMode == 0 && game.hasCell(mousePos))
                game.removeCell(mousePos);
        } else {
            if (lastRightMouse) {
                if (promptingSel)
                    mouseDragA = null;

                lastRightMouse = false;
            }
        }
    }

    // something weird with this rounding error
    public Vector2i getMouseGamePos() {
        Vector2d temp = getMousePos(BoardUI.USING_MENU);
        temp.setX(temp.x*(WIDTH/input.getCellLen()));
        temp.setY(temp.y*(HEIGHT/input.getCellLen()));
        return temp.add(input.getScreenPos()).floor();
    }

    private void selectionPrompt() {
        frameAdd(new AlignText("Type "+input.keyBind.saveKey()+" to save, "+input.keyBind.cancelKey()+" to exit, "+input.keyBind.delete()+" to delete cells",
            Alignment.TOP_CENTER, new RectType(Vector2d.ORIGIN, getDSize().asVector2d()), Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20), false));

        int choice = input.checkSavePrompt();

        checkMouseClicks();

        if (!mouseDragDelta.equals(Vector2i.ORIGIN)) {
            Iterator<Vector2i> iterator = game.getIterator();
            List<Vector2i> toDraw = new ArrayList<Vector2i>();
            
            while (iterator.hasNext()) {
                Vector2i pos = iterator.next();

                if (pos.within(selectA, selectB))
                    toDraw.add(pos.sub(mouseDragDelta));
            }

            for (Vector2i pos : toDraw)
                BoardUI.projectToScreen(pos, 1, this);

            // mouse has been released
            try { mouseDragA.equals(Vector2i.ORIGIN); }
            catch (NullPointerException e) {
                Iterator<Vector2i> i = game.getIterator();
                List<Vector2i> toRemove = new ArrayList<Vector2i>();
                List<Vector2i> toReplace = new ArrayList<Vector2i>();
            
                while (i.hasNext()) {
                    Vector2i pos = i.next();

                    if (pos.within(selectA, selectB)) {
                        toRemove.add(pos);
                        toReplace.add(pos.sub(mouseDragDelta));
                    }
                }

                for (Vector2i pos : toRemove)
                    game.removeCell(pos);

                for (Vector2i pos : toReplace)
                    game.addCell(pos);

                promptingSel = false;
                mouseDragA = Vector2i.ORIGIN;
                mouseDragDelta = Vector2i.ORIGIN;
            }
        }

        if (choice != 0) {
            if (choice == 1) {
                Schematic temp = new Schematic(game.getIterator(), selectA, selectB);

                JSON.saveSchem(temp);
            } else if (choice == 2) {
                Iterator<Vector2i> iterator = game.getIterator();
                List<Vector2i> toRemove = new ArrayList<Vector2i>();

                while(iterator.hasNext()) {
                    Vector2i pos = iterator.next();
                    if (pos.within(selectA, selectB))
                        toRemove.add(pos);
                }

                for (Vector2i pos : toRemove)
                    game.removeCell(pos);
            }

            promptingSel = false;
        }
    }

    public void undo() {
        if (undoIndex > 0) {
            for (Vector2i pos : undoCells.get(undoIndex-1))
                game.removeCell(pos);
        
            undoCells.remove(undoIndex-1);
            undoIndex--;
        }
    }
    
    public void ctrlZClear() {
        undoCells.clear();
        undoIndex = 0;
    }

    public void clear() {
        allSchematics.clear();
        ctrlZClear();
        game.clearCells();
    }
}
