package gol.game;

import java.util.Iterator;
import java.util.List;

import java.awt.Color;
import java.awt.Font;

import gol.display.shapes.*;
import gol.display.shapes.Text.ScreenPos;
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
            Vector2d mouseScreenPos = new Vector2d(mousePos.x * board.WIDTH, (1 - mousePos.y) * board.HEIGHT);
            Vector2i size = mouseScreenPos.add(screenPos.div(cellLen)).sub(drawPos).ceil().floorToInterval(cellLen);

            if (size.x < 0) {
                drawPos.setX(drawPos.x + size.x - cellLen);
                size.setX(Math.abs(size.x)+cellLen*2);
            }
            if (size.y < 0) {
                drawPos.setY(drawPos.y + size.y - cellLen);
                size.setY(Math.abs(size.y)+cellLen*2);
            }

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

    public static void setCellLen(int cellLen) {
        BoardUI.cellLen = cellLen;
        BoardUI.drawWidth = (int)(cellLen*0.95);
    }

    public static void setScreenPos(Vector2d screenPos) {
        BoardUI.screenPos = screenPos;
    }
}
