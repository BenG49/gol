package gol.game;

import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Color;
import java.awt.Font;

import bglib.display.shapes.AlignText.Alignment;
import bglib.display.shapes.*;
import bglib.util.*;

import gol.game.schematic.*;

public class BoardUI {

    static int cellLen;
    static int drawWidth;
    static Vector2d screenPos;

    public static final boolean USING_MENU = true;

    public static void drawOptimized(Board board) {
        final Vector2d max = board.input.getScreenPos().add(board.getDSize().div(cellLen));

        Iterator<Vector2i> iterator = board.game.getIterator();
        while (iterator.hasNext()) {
            Vector2i pos = iterator.next();

            // off screen
            if (pos.x + drawWidth < screenPos.x || pos.x > max.x || pos.y + drawWidth < screenPos.y || pos.y > max.y)
                continue;

            projectToScreen(pos, 0, board);
        }

        // pause indicator
        if (!board.input.getStepAuto()) {
            board.frameAdd(new FillRect(new RectType(10, 10, 12, 40), 0, Color.WHITE));
            board.frameAdd(new FillRect(new RectType(30, 10, 12, 40), 0, Color.WHITE));
        }
    }

    public static void drawBoard(Board board) {
        drawOptimized(board);

        final Vector2d mousePos = board.getMousePos(USING_MENU);

        // cross around 0,0
        for (int y = -2; y < 3; y++)
            for (int x = -2; x < 3; x++)
                if (x == 0 || y == 0)
                    projectToScreen(new Vector2i(x, y), 1, board);

        // selection area
        if (board.input.getSelectMode() == 1 && board.getButtonPressed(1)) {
            // Vector2i drawPos = board.selectA.sub(screenPos).mul(cellLen).floor();
            Vector2i drawPos = posToDraw(board.selectA);
            Vector2d mouseScreenPos = mousePos;
            mouseScreenPos.setX(mousePos.x*board.WIDTH);
            mouseScreenPos.setY(mousePos.y*board.HEIGHT);
            Vector2i size = mouseScreenPos.add(screenPos.div(cellLen)).sub(drawPos).ceil().floorToInterval(cellLen);

            board.frameAdd(new FillRect(new RectType(drawPos, size), 0, new Color(1f, 1f, 1f, 0.5f)));
        }
        // mouse highlight
        else if (mousePos.x > 0 && mousePos.x < 1 && mousePos.y > 0 && mousePos.y < 1)
            projectToScreen(board.getMouseGamePos(), 2, board);

        // bottom left
        board.frameAdd(new AlignText(
                new String[] {
                        board.getDSize().div(cellLen).div(2).add(screenPos.floor()).toString(),
                        "Steps: " + board.game.getStepCount()
                    },
                Alignment.BOT_LEFT, board.getDimensions(), Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20), false));
        // bottom right
        board.frameAdd(new AlignText("Speed: " + board.input.getSpeed0to10(), Alignment.BOT_RIGHT, board.getDimensions(), Color.WHITE,
                new Font("Cascadia Code", Font.PLAIN, 20), false));
            
        drawGrid(10, board);
    }

    public static void projectToScreen(Vector2i pos, int highlight, Board board) {
        Color color;

        if (highlight == 1)
            color = new Color(1f, 1f, 1f, 0.25f);
        else if (highlight == 2)
            color = new Color(1f, 1f, 1f, 0.5f);
        else if (highlight == 3)
            color = new Color(1f, 1f, 1f, 0.75f);
        else
            color = Color.WHITE;

        board.frameAdd(new FillRect(new RectType(posToDraw(pos), new Vector2i(drawWidth)), 0, color));
    }

    // IF INTERVAL SET TO 0, DRAW GRID NORMALLY
    private static void drawGrid(int interval, Board board) {
        // final Vector2i startPos = screenPos.sub(screenPos.mod((double)interval)).floor();
        final Vector2i startPos = screenPos.floorToInterval(interval).floor();
        final Vector2i endPos = screenPos.add(board.getDSize().div(cellLen)).floorToInterval(interval).add(interval).floor();
        final Color COLOR = new Color(1f, 1f, 1f, 0.2f);

        System.out.println(posToDraw(startPos)+", "+posToDraw(endPos));

        for (int x = startPos.x; x < endPos.x; x++) {
            int width = 1;
            if (x % interval == 0)
                width = 2;
            board.frameAdd(new Line(
                new Vector2i(startPos.x+x*cellLen+board.getDSize().x/2, 0),
                new Vector2i(startPos.x+x*cellLen+board.getDSize().x/2, board.getDSize().y),
                // posToDraw(new Vector2i(startPos.x+x*cellLen, 0)),
                // posToDraw(new Vector2i(startPos.x+x*cellLen, board.size.y/cellLen)),
                COLOR, width
            ));
        }

        for (int y = startPos.y; y < endPos.y; y++) {
            int width = 1;
            if (y % interval == 0)
                width = 2;
            board.frameAdd(new Line(
                new Vector2i(0,                  startPos.y+y*cellLen+board.getDSize().x/2),
                new Vector2i(board.getDSize().y, startPos.y+y*cellLen+board.getDSize().x/2),
                COLOR, width
            ));
        }
    }

    public static void placeSchemDraw(Schematic draw, Board board) {
        try { draw.getOrigin(); }
        catch(NullPointerException e) {
            board.placeSchem = false;
            return;
        }

        final Vector2d max = screenPos.add(board.getDSize().div(cellLen));

        Vector2i offset = board.getMouseGamePos();
        for (Vector2i pos : draw.getData()) {
            Vector2i temp = offset.add(pos);;
            if (temp.x + drawWidth < screenPos.x || temp.x > max.x || temp.y + drawWidth < screenPos.y || temp.y > max.y)
                continue;

            projectToScreen(temp, 3, board);
        }

        // bounding box
        RectType box = draw.getBoundingBox(cellLen, offset.sub(screenPos).ceil());
        // box.setPos(box.getPos().add(new Vector2i(cellLen, -cellLen)));
        board.frameAdd(new Rect(box, 2, Color.WHITE));

        // bounding box of every schematic
        for (Schematic s : board.allSchematics.keySet()) {
            // not on the step where the schematic was placed
            if (board.allSchematics.get(s) != board.game.getStepCount())
                continue;

            RectType dim = s.getBoundingBox(cellLen, screenPos.ceil().add(board.getDSize().div(cellLen).sub(1).floor()));
            // not on screen
            if (dim.getPos().x + dim.getSize().x < 0 || dim.getPos().x > board.WIDTH ||
                dim.getPos().y + dim.getSize().y < 0 || dim.getPos().y > board.HEIGHT)
                continue;

            board.frameAdd(new Rect(dim, 2, Color.WHITE));
        }

        // cancel text
        board.frameAdd(new AlignText("Press "+board.input.keyBind.cancelKey()+" to stop placement", Alignment.TOP_CENTER, board.getDimensions(),
                Color.WHITE, new Font("Cascadia Code", Font.PLAIN, 20), false));

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

    public static void drawKeybindings(Board board) {
        board.frameAdd(new FillRect(board.getDimensions(), 0, new Color(0f, 0f, 0f, 0.5f)));

        for (Shape i : board.input.getKeyGuide())
            board.frameAdd(i);
        
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

    public static Vector2i posToDraw(Vector2i pos) {
        return pos.sub(screenPos.floor()).mul(cellLen);
    }

    public static void setCellLen(int cellLen) {
        BoardUI.cellLen = cellLen;
        BoardUI.drawWidth = (int)(cellLen*0.95);
    }

    public static void setScreenPos(Vector2d screenPos) {
        BoardUI.screenPos = screenPos;
    }
}
