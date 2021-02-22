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
    private Vector2Int selectA;
    private Vector2Int selectB;

    private boolean promptingSave;
    private boolean displayKeybinds;
    private boolean placeSchem;
    private Schematic tempSchem;

    private BoardInput input;
    public GameAlg game;

    private static final int DEFAULT_WIDTH = 1000;
    private static final int OPTIMIZED_DRAW_INTERVAL = 10;
    private static final boolean USING_MENU = true;

    public Board(HashSet<Vector2Int> aliveCells) {
        this(aliveCells, 25, new KeyBinding());
    }

    public Board(Schematic schem) {
        this(schem.getData(), 25, new KeyBinding());
    }

    public Board(HashSet<Vector2Int> aliveCells, int cellScreenLen, KeyBinding binding) {
        super(DEFAULT_WIDTH, DEFAULT_WIDTH, Color.BLACK);

        createMenu();

        game = new GameAlg(aliveCells);
        input = new BoardInput(this, binding, cellScreenLen);
        stepTimer = new Timer();

        betweenSteps = false;
        lastLeftMouse = false;
        lastRightMouse = false;
        selectA = new Vector2Int(0, 0);
        selectB = new Vector2Int(0, 0);
        promptingSave = false;
    }

    public void run() {
        while (input.getRun()) {
            List<Shape> shapes = new ArrayList<Shape>();

            if (input.getRunOptimized()) {
                input.checkKeysOptimized();
                if (input.getStepAuto())
                    game.step();

                if (game.getStepCount() % OPTIMIZED_DRAW_INTERVAL == 0)
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
                // laptop specs:
                // 8411 steps non optimized 30 secs full speed
                // 436730 steps optimized 30 secs

                drawBoard(shapes);
                input.checkKeys();
                checkMouseClicks();
            }

            if (!(input.getRunOptimized() && game.getStepCount() % 10 > 0))
                draw(shapes);
        }
    }

    private void checkMouseClicks() {
        int selectMode = input.getSelectMode();
        // LEFT CLICK
        if (getButtonPressed(1)) {
            // just clicked
            if (!lastLeftMouse)
                if (selectMode == 1)
                    selectA = getMouseGamePos();

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
        final int CELL_WIDTH = (int) (input.getCellScreenLen() * 0.95);

        Vector2 max = input.getScreenPos().add(new Vector2(input.getScreenPos().x + WIDTH * input.getCellScreenLen(),
                input.getScreenPos().y + HEIGHT * input.getCellScreenLen()));
        Iterator<Vector2Int> iterator = game.getIterator();

        while (iterator.hasNext()) {
            Vector2Int pos = iterator.next();

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
                    projectToScreen(new Vector2Int(x, y), shapes, 1);

        // selection area
        if (input.getSelectMode() == 1 && getButtonPressed(1)) {
            int cellLen = input.getCellScreenLen();
            Vector2 screenPos = input.getScreenPos();

            Vector2Int drawPos = selectA.sub(screenPos).mul(cellLen).floor();
            Vector2 mouseScreenPos = new Vector2(getMousePos(USING_MENU).x * WIDTH, (1 - getMousePos(USING_MENU).y) * HEIGHT);
            // TODO: make this account for zoom
            Vector2Int size = mouseScreenPos.add(screenPos).sub(drawPos).ceil().floorToInterval(cellLen).add(cellLen);

            if (size.x < 0) {
                drawPos.setX(drawPos.x + size.x - cellLen);
                size.setX(Math.abs(size.x));
            }
            if (size.y < 0) {
                drawPos.setY(drawPos.y + size.y - cellLen);
                size.setY(Math.abs(size.y));
            }

            shapes.add(new FillRect(drawPos, size, 0, new Color(1f, 1f, 1f, 0.5f)));
        }
        // mouse highlight
        else if (getMousePos(USING_MENU).x > 0 && getMousePos(USING_MENU).x < 1 && getMousePos(USING_MENU).y > 0 && getMousePos(USING_MENU).y < 1)
            projectToScreen(getMouseGamePos(), shapes, 2);

        // bottom left
        shapes.add(new Text(
                new String[] {
                        new Vector2Int(WIDTH / input.getCellScreenLen() / 2, HEIGHT / input.getCellScreenLen() / 2)
                                .add(input.getScreenPos().floor()).toString(),
                        "Steps: " + game.getStepCount() },
                ScreenPos.BOT_LEFT, WIDTH, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
        // bottom right
        shapes.add(new Text("Speed: " + input.getSpeed0to10(), ScreenPos.BOT_RIGHT, WIDTH, Color.WHITE,
                new Font("Cascadia Code", Font.PLAIN, 20)));
    }

    private void projectToScreen(Vector2Int pos, List<Shape> shapes, int highlight) {
        final int CELL_WIDTH = (int) (input.getCellScreenLen() * 0.95);

        Vector2Int drawPos = pos.sub(input.getScreenPos()).mul(input.getCellScreenLen()).round();
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

    // TODO: optimize by having either cache or checking if mouse has moved
    // TODO: fix zoom not working with this
    public Vector2Int getMouseGamePos() {
        return new Vector2(getMousePos(USING_MENU).x, 1 - getMousePos(USING_MENU).y).mul(HEIGHT / input.getCellScreenLen())
                .add(input.getScreenPos()).floor();
    }

    private void schemSavePrompt(List<Shape> shapes) {
        shapes.add(new FillRect(0, 0, WIDTH, HEIGHT, 0, new Color(0f, 0f, 0f, 0.5f)));
        shapes.add(new Text("Type " + input.keyBind.saveKey() + " to save, " + input.keyBind.cancelKey() + " to exit",
            ScreenPos.TOP_CENTER, WIDTH, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));

        int choice = input.checkSavePrompt();

        if (getButtonPressed(1) || getButtonPressed(3))
            promptingSave = false;

        if (choice != 0) {
            if (input.checkSavePrompt() == 1) {
                Schematic temp = new Schematic(game.getIterator(), selectA, selectB);

                JSON.saveSchem(temp);
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
        final int CELL_WIDTH = (int) (input.getCellScreenLen()*0.95);
        final Vector2 max = input.getScreenPos().add(new Vector2(
            input.getScreenPos().x + WIDTH * input.getCellScreenLen(),
            input.getScreenPos().y + HEIGHT * input.getCellScreenLen()));

        Vector2Int offset = getMouseGamePos().sub(input.getScreenPos().floor()).sub(WIDTH/2/input.getCellScreenLen());
        for (Vector2Int pos : draw.getData()) {
            pos = pos.add(offset);
            if (pos.x + CELL_WIDTH < input.getScreenPos().x || pos.x > max.x || pos.y + CELL_WIDTH < input.getScreenPos().y || pos.y > max.y)
                continue;

            projectToScreen(pos, shapes, 3);
        }

        if (getButtonPressed(1)) {
            for (Vector2Int pos : draw.getData())
                game.addCell(pos.add(offset));
            placeSchem = false;
        }
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
                    // TODO: make it so that RectTypes of schematics are saved and linked
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
