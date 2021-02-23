package gol.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import java.util.ArrayList;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gol.display.shapes.*;
import gol.display.shapes.Text.ScreenPos;
import gol.game.schematic.JSON;
import gol.game.schematic.Schematic;
import gol.input.*;
import gol.util.*;

public class Board extends InputDisplay {

    private boolean betweenSteps;
    private Timer stepTimer;
    private TimerTask stepTimerTask;

    private boolean lastLeftMouse;
    private boolean lastRightMouse;
    private Vector2i selectA;
    private Vector2i selectB;

    private boolean promptingSave;
    private boolean displayKeybinds;
    private boolean placeSchem;
    private Schematic tempSchem;

    private List<HashSet<Vector2i>> ctrlZCells;
    private int ctrlZIndex;

    private List<Schematic> allSchematics;
    private BoardInput input;
    public GameAlg game;

    private static final int DEFAULT_WIDTH = 1000;
    private static final int OPTIMIZED_DRAW_INTERVAL = 50;
    private static final boolean OPTIMIZED_RENDER = true;
    private static final boolean USING_MENU = true;

    public Board() {
        this(new HashSet<Vector2i>(), 24, new KeyBinding());
    }
    public Board(HashSet<Vector2i> aliveCells) {
        this(aliveCells, 24, new KeyBinding());
    }
    public Board(Schematic schem) {
        this(schem.getData(), 24, new KeyBinding());
    }
    public Board(HashSet<Vector2i> aliveCells, int cellScreenLen, KeyBinding binding) {
        super(DEFAULT_WIDTH, DEFAULT_WIDTH, Color.BLACK);

        createMenu();

        game = new GameAlg(aliveCells);
        input = new BoardInput(this, binding, cellScreenLen);
        stepTimer = new Timer();
        ctrlZCells = new ArrayList<HashSet<Vector2i>>();
        allSchematics = new ArrayList<Schematic>();

        betweenSteps = false;
        lastLeftMouse = false;
        lastRightMouse = false;
        selectA = new Vector2i(0, 0);
        selectB = new Vector2i(0, 0);
        promptingSave = false;
        ctrlZIndex = 0;
    }

    public void run() {
        while (input.getRun()) {
            List<Shape> shapes = new ArrayList<Shape>();

            if (input.getRunOptimized()) {
                input.checkKeysOptimized();
                if (input.getStepAuto())
                    game.step();

                if (OPTIMIZED_RENDER && game.getStepCount() % OPTIMIZED_DRAW_INTERVAL == 0)
                    drawBoardOptimized(shapes);
            } else if (promptingSave) {
                drawBoard(shapes);
                schemSavePrompt(shapes);
            } else if (displayKeybinds) {
                drawBoard(shapes);
                drawKeybindings(shapes);
            } else if (placeSchem) {
                drawBoard(shapes);
                placeSchemDraw(shapes, tempSchem);
                input.checkKeysOptimized();
            } else {
                if (input.getStepAuto()) {
                    if (!betweenSteps) {
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
                }
                // laptop specs:
                // 8411 steps non optimized 30 secs full speed
                // 436730 steps optimized 30 secs

                drawBoard(shapes);
                input.checkKeys();
                checkMouseClicks();
            }

            if (input.getRunOptimized()) {
                if (OPTIMIZED_RENDER && game.getStepCount() % OPTIMIZED_DRAW_INTERVAL == 0)
                    draw(shapes);
            } else
                draw(shapes);
        }
    }

    private void checkMouseClicks() {
        int selectMode = input.getSelectMode();
        // LEFT CLICK
        if (getButtonPressed(1)) {
            // just clicked
            if (!lastLeftMouse) {
                if (selectMode == 0 && !input.getStepAuto())
                    ctrlZCells.add(new HashSet<Vector2i>());
                else if (selectMode == 1) {
                    selectA = getMouseGamePos();
                }
            }

            Vector2i mousePos = getMouseGamePos();

            // ADD CELL
            if (selectMode == 0) {
                if (!game.hasCell(mousePos) && !input.getStepAuto())
                    ctrlZCells.get(ctrlZIndex).add(mousePos);
                game.addCell(mousePos);
            }

            lastLeftMouse = true;
        } else {
            // just released
            if (lastLeftMouse) {
                if (selectMode == 0 && !input.getStepAuto())
                    ctrlZIndex++;
                else if (selectMode == 1) {
                    selectB = getMouseGamePos();
                    promptingSave = true;
                }

                lastLeftMouse = false;
            }
        }

        // RIGHT CLICK
        if (getButtonPressed(3)) {
            Vector2i mousePos = getMouseGamePos();

            // REMOVE CELL
            if (selectMode == 0 && game.hasCell(mousePos))
                game.removeCell(mousePos);

            lastRightMouse = true;
        } else if (lastRightMouse)
            lastRightMouse = false;
    }

    public void drawBoardOptimized(List<Shape> shapes) {
        final int CELL_WIDTH = (int) (input.getCellLen() * 0.95);

        Vector2d max = input.getScreenPos().add(new Vector2d(input.getScreenPos().x + WIDTH * input.getCellLen(),
                input.getScreenPos().y + HEIGHT * input.getCellLen()));
        Iterator<Vector2i> iterator = game.getIterator();

        while (iterator.hasNext()) {
            Vector2i pos = iterator.next();

            if (pos.x + CELL_WIDTH < input.getScreenPos().x || pos.x > max.x || pos.y + CELL_WIDTH < input.getScreenPos().y || pos.y > max.y)
                continue;

            projectToScreen(pos, shapes, 0);
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
        for (int y = -2; y < 3; y++)
            for (int x = -2; x < 3; x++)
                if (x == 0 || y == 0)
                    projectToScreen(new Vector2i(x, y), shapes, 1);

        // selection area
        if (input.getSelectMode() == 1 && getButtonPressed(1)) {
            int cellLen = input.getCellLen();
            Vector2d screenPos = input.getScreenPos();

            Vector2i drawPos = selectA.sub(screenPos).mul(cellLen).floor();
            Vector2d mouseScreenPos = new Vector2d(getMousePos(USING_MENU).x * WIDTH, (1 - getMousePos(USING_MENU).y) * HEIGHT);
            Vector2i size = mouseScreenPos.add(screenPos.div(cellLen)).sub(drawPos).ceil().floorToInterval(cellLen);

            if (size.x < 0) {
                drawPos.setX(drawPos.x + size.x - cellLen);
                size.setX(Math.abs(size.x)+input.getCellLen()*2);
            }
            if (size.y < 0) {
                drawPos.setY(drawPos.y + size.y - cellLen);
                size.setY(Math.abs(size.y)+input.getCellLen()*2);
            }

            shapes.add(new FillRect(drawPos, size, 0, new Color(1f, 1f, 1f, 0.5f)));
        }
        // mouse highlight
        else if (getMousePos(USING_MENU).x > 0 && getMousePos(USING_MENU).x < 1 && getMousePos(USING_MENU).y > 0 && getMousePos(USING_MENU).y < 1)
            projectToScreen(getMouseGamePos(), shapes, 2);

        // bottom left
        shapes.add(new Text(
                new String[] {
                        new Vector2i(WIDTH / input.getCellLen() / 2, HEIGHT / input.getCellLen() / 2)
                                .add(input.getScreenPos().floor()).toString(),
                        "Steps: " + game.getStepCount() },
                ScreenPos.BOT_LEFT, WIDTH, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
        // bottom right
        shapes.add(new Text("Speed: " + input.getSpeed0to10(), ScreenPos.BOT_RIGHT, WIDTH, Color.WHITE,
                new Font("Cascadia Code", Font.PLAIN, 20)));
    }

    private void projectToScreen(Vector2i pos, List<Shape> shapes, int highlight) {
        final int CELL_WIDTH = (int) (input.getCellLen() * 0.95);

        Vector2i drawPos = pos.sub(input.getScreenPos().floor()).mul(input.getCellLen());
        Color color;

        if (highlight == 1)
            color = new Color(1f, 1f, 1f, 0.25f);
        else if (highlight == 2)
            color = new Color(1f, 1f, 1f, 0.5f);
        else if (highlight == 3)
            color = new Color(1f, 1f, 1f, 0.75f);
        else
            color = Color.WHITE;

        shapes.add(new FillRect(drawPos, CELL_WIDTH, 0, color));
    }

    // TODO: optimize by having either cache or checking if zoom has changed
    // something weird with this rounding error
    public Vector2i getMouseGamePos() {
        double cellLen = input.getCellLen();
        Vector2d mouse = getMousePos(USING_MENU);

        return new Vector2d(mouse.x, 1-mouse.y).mul(HEIGHT/cellLen).add(input.getScreenPos()).floor();
    }

    private void schemSavePrompt(List<Shape> shapes) {
        // shapes.add(new FillRect(0, 0, WIDTH, HEIGHT, 0, new Color(0f, 0f, 0f, 0.5f)));
        shapes.add(new Text("Type "+input.keyBind.saveKey()+" to save, "+input.keyBind.cancelKey()+" to exit, "+input.keyBind.delete()+" to delete cells",
            ScreenPos.TOP_CENTER, WIDTH, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));

        int choice = input.checkSavePrompt();

        if (getButtonPressed(1) || getButtonPressed(3))
            promptingSave = false;

        if (choice != 0) {
            if (choice == 1) {
                Schematic temp = new Schematic(game.getIterator(), selectA, selectB);

                JSON.saveSchem(temp);
            } else if (choice == 2) {
                Iterator<Vector2i> iterator = game.getIterator();
                HashSet<Vector2i> toRemove = new HashSet<Vector2i>();

                while(iterator.hasNext()) {
                    Vector2i pos = iterator.next();
                    if (pos.within(selectA, selectB))
                        toRemove.add(pos);
                }

                for (Vector2i pos : toRemove)
                    game.removeCell(pos);
            }

            promptingSave = false;
        }
    }

    public void drawKeybindings(List<Shape> shapes) {
        shapes.add(new FillRect(0, 0, WIDTH, HEIGHT, 0, new Color(0f, 0f, 0f, 0.5f)));

        for (Shape i : input.getKeyGuide())
            shapes.add(i);
        
        if (input.checkKeybindPrompt())
            displayKeybinds = false;
    }

    public void placeSchemDraw(List<Shape> shapes, Schematic draw) {
        try { draw.getOrigin(); }
        catch(NullPointerException e) {
            placeSchem = false;
            return;
        }

        final int CELL_WIDTH = (int) (input.getCellLen()*0.95);
        final Vector2d max = input.getScreenPos().add(new Vector2d(
            input.getScreenPos().x + WIDTH * input.getCellLen(),
            input.getScreenPos().y + HEIGHT * input.getCellLen()));

        Vector2i offset = getMouseGamePos();
        for (Vector2i pos : draw.getData()) {
            Vector2i temp = offset.add(pos);;
            if (temp.x + CELL_WIDTH < input.getScreenPos().x || temp.x > max.x || temp.y + CELL_WIDTH < input.getScreenPos().y || temp.y > max.y)
                continue;

            projectToScreen(temp, shapes, 3);
        }

        // bounding box
        shapes.add(draw.getBoundingBox(2, Color.WHITE, input.getCellLen(), offset.sub(input.getScreenPos().floor())));

        // cancel text
        shapes.add(new Text("Press "+input.keyBind.cancelKey()+" to stop placement", ScreenPos.TOP_CENTER, WIDTH, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));

        // rotate schem
        if (input.placeSchemRotateCheck())
            Schematic.rotate90(tempSchem);

        // check for cancel key
        if (input.checkSavePrompt() == -1)
            placeSchem = false;

        if (getButtonPressed(1)) {
            for (Vector2i pos : draw.getData())
                game.addCell(pos.add(offset));

            draw.setOrigin(offset);
            allSchematics.add(draw);
        }
    }

    public void undo() {
        if (ctrlZIndex > 0) {
            for (Vector2i pos : ctrlZCells.get(ctrlZIndex-1))
                game.removeCell(pos);
        
            ctrlZCells.remove(ctrlZIndex-1);
            ctrlZIndex--;
        }
    }
    
    public void ctrlZClear() {
        ctrlZCells.clear();
        ctrlZIndex = 0;
    }

    public void clear() {
        allSchematics.clear();
        ctrlZClear();
        game.clearCells();
    }

    private void createMenu() {
        final JMenuBar menu = new JMenuBar();

        // menus
        JMenu fileMenu = new JMenu("File");
            JMenuItem lmao = new JMenuItem("Lmao did you expect any file functionality");
        JMenu helpMenu = new JMenu("Help");
            JMenuItem keybinds = new JMenuItem("Keybindings");
            keybinds.setActionCommand("displayKeybinds");
            keybinds.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { displayKeybinds = true; }
            });
        JMenu schem = new JMenu("Schematics");
            JMenuItem load = new JMenuItem("Load Schematic");
            load.setActionCommand("load");
            load.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tempSchem = JSON.loadSchem();
                    placeSchem = true;
                }
            });

        fileMenu.add(lmao);
        helpMenu.add(keybinds);
        schem.add(load);

        menu.add(fileMenu);
        menu.add(helpMenu);
        menu.add(schem);
        setJMenuBar(menu);
    }
}
