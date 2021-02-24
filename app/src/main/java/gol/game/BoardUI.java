package gol.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Color;
import java.awt.Font;

import gol.display.shapes.*;
import gol.display.shapes.Text.ScreenPos;
import gol.game.schematic.JSON;
import gol.game.schematic.Schematic;
import gol.util.RectType;
import gol.util.Vector2d;
import gol.util.Vector2i;

public class BoardUI {

    static int cellLen;
    static int drawWidth;
    static Vector2d screenPos;

    public static final boolean USING_MENU = true;

    public static void drawOptimized(List<Shape> shapes, Board board) {
        final Vector2d max = board.input.getScreenPos().add(board.WIDTH/cellLen);

        Iterator<Vector2i> iterator = board.game.getIterator();
        while (iterator.hasNext()) {
            Vector2i pos = iterator.next();

            // off screen
            if (pos.x + drawWidth < screenPos.x || pos.x > max.x || pos.y + drawWidth < screenPos.y || pos.y > max.y)
                continue;

            projectToScreen(shapes, pos, 0, board);
        }

        // pause indicator
        if (!board.input.getStepAuto()) {
            shapes.add(new FillRect(10, 10, 12, 40, 0, Color.WHITE));
            shapes.add(new FillRect(30, 10, 12, 40, 0, Color.WHITE));
        }
    }

    public static void drawBoard(List<Shape> shapes, Board board) {
        drawOptimized(shapes, board);

        final Vector2d mousePos = board.getMousePos(USING_MENU);

        // cross around 0,0
        for (int y = -2; y < 3; y++)
            for (int x = -2; x < 3; x++)
                if (x == 0 || y == 0)
                    projectToScreen(shapes, new Vector2i(x, y), 1, board);

        // selection area
        if (board.input.getSelectMode() == 1 && board.getButtonPressed(1)) {
            Vector2i drawPos = board.selectA.sub(screenPos).mul(cellLen).floor();
            Vector2d mouseScreenPos = mousePos.mul(board.WIDTH);
            Vector2i size = mouseScreenPos.add(screenPos.div(cellLen)).sub(drawPos).ceil().floorToInterval(cellLen);

            shapes.add(new FillRect(drawPos, size, 0, new Color(1f, 1f, 1f, 0.5f)));
        }
        // mouse highlight
        else if (mousePos.x > 0 && mousePos.x < 1 && mousePos.y > 0 && mousePos.y < 1)
            projectToScreen(shapes, board.getMouseGamePos(), 2, board);

        // bottom left
        shapes.add(new Text(
                new String[] {
                        new Vector2i(board.WIDTH / cellLen / 2, board.HEIGHT / cellLen / 2).add(screenPos.floor()).toString(),
                        "Steps: " + board.game.getStepCount()
                    },
                ScreenPos.BOT_LEFT, board.WIDTH, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));
        // bottom right
        shapes.add(new Text("Speed: " + board.input.getSpeed0to10(), ScreenPos.BOT_RIGHT, board.WIDTH, Color.WHITE,
                new Font("Cascadia Code", Font.PLAIN, 20)));
    }

    public static void projectToScreen(List<Shape> shapes, Vector2i pos, int highlight, Board board) {
        Vector2i drawPos = pos.sub(board.input.getScreenPos().floor()).mul(cellLen);
        Color color;

        if (highlight == 1)
            color = new Color(1f, 1f, 1f, 0.25f);
        else if (highlight == 2)
            color = new Color(1f, 1f, 1f, 0.5f);
        else if (highlight == 3)
            color = new Color(1f, 1f, 1f, 0.75f);
        else
            color = Color.WHITE;

        shapes.add(new FillRect(drawPos, drawWidth, 0, color));
    }

    public static void placeSchemDraw(List<Shape> shapes, Schematic draw, Board board) {
        try { draw.getOrigin(); }
        catch(NullPointerException e) {
            board.placeSchem = false;
            return;
        }

        final Vector2d max = screenPos.add(board.WIDTH/cellLen);

        Vector2i offset = board.getMouseGamePos();
        for (Vector2i pos : draw.getData()) {
            Vector2i temp = offset.add(pos);;
            if (temp.x + drawWidth < screenPos.x || temp.x > max.x || temp.y + drawWidth < screenPos.y || temp.y > max.y)
                continue;

            projectToScreen(shapes, temp, 3, board);
        }

        // bounding box
        shapes.add(new Rect(draw.getBoundingBox(cellLen, offset.sub(screenPos).ceil()), 2, Color.WHITE));

        // bounding box of every schematic
        for (Schematic s : board.allSchematics.keySet()) {
            // not on the step where the schematic was placed
            if (board.allSchematics.get(s) != board.game.getStepCount())
                continue;

            RectType dim = s.getBoundingBox(cellLen, screenPos.mul(-1).ceil());
            // not on screen
            if (dim.getPos().x + dim.getSize().x < 0 || dim.getPos().x > board.WIDTH ||
                dim.getPos().y + dim.getSize().y < 0 || dim.getPos().y > board.HEIGHT)
                continue;

            shapes.add(new Rect(dim, 2, Color.WHITE));
        }

        // cancel text
        shapes.add(new Text("Press "+board.input.keyBind.cancelKey()+" to stop placement", ScreenPos.TOP_CENTER, board.WIDTH, Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20)));

        int key = board.input.placeSchemCheck();
        // rotate schem
        if (key == 1)
            Schematic.rotate90(board.tempSchem);
        // mirror schem left/right
        else if (key == 2)
            Schematic.mirrorX(board.tempSchem);

        // check for cancel key
        if (board.input.checkSavePrompt() == -1)
            board.placeSchem = false;

        if (board.getButtonPressed(1)) {
            if (!board.lastLeftMouse) {
                HashSet<Vector2i> temp = new HashSet<Vector2i>();
                for (Vector2i pos : draw.getData()) {
                    Vector2i newPos = pos.add(offset);
                    board.game.addCell(newPos);
                    temp.add(newPos);
                }

                Schematic toAddList = new Schematic(draw);
                toAddList.setOrigin(Vector2i.overallMin(temp));
                board.allSchematics.put(toAddList, board.game.getStepCount());
            }

            board.lastLeftMouse = true;
        } else
            board.lastLeftMouse = false;
    }

    private static void drawGrid(List<Shape> shapes) {

    }

    public static void drawKeybindings(List<Shape> shapes, Board board) {
        shapes.add(new FillRect(0, 0, board.WIDTH, board.HEIGHT, 0, new Color(0f, 0f, 0f, 0.5f)));

        for (Shape i : board.input.getKeyGuide())
            shapes.add(i);
        
        if (board.input.checkKeybindPrompt())
            board.displayKeybinds = false;
    }

    public static void createMenu(Board board) {
        final JMenuBar menu = new JMenuBar();

        // menus
        JMenu fileMenu = new JMenu("File");
            JMenuItem lmao = new JMenuItem("Lmao did you expect any file functionality");
        JMenu helpMenu = new JMenu("Help");
            JMenuItem keybinds = new JMenuItem("Keybindings");
            keybinds.setActionCommand("displayKeybinds");
            keybinds.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { board.displayKeybinds = true; }
            });
        JMenu schem = new JMenu("Schematics");
            JMenuItem load = new JMenuItem("Load Schematic");
            load.setActionCommand("load");
            load.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    board.tempSchem = JSON.loadSchem();
                    board.placeSchem = true;
                }
            });

        fileMenu.add(lmao);
        helpMenu.add(keybinds);
        schem.add(load);

        menu.add(fileMenu);
        menu.add(helpMenu);
        menu.add(schem);
        board.setJMenuBar(menu);
    }

    public static void setCellLen(int cellLen) {
        BoardUI.cellLen = cellLen;
        BoardUI.drawWidth = (int)(cellLen*0.95);
    }

    public static void setScreenPos(Vector2d screenPos) {
        BoardUI.screenPos = screenPos;
    }
}