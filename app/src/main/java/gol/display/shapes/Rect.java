package gol.display.shapes;

import java.awt.*;
import gol.game.Vector2Int;

public class Rect extends Shape {
    private final int x, y, width, height, border;
    private final Color c;

    public Rect(Vector2Int pos, Vector2Int size, int border, Color c) {
        this(pos.x, pos.y, size.x, size.y, border, c);
    }
    public Rect(Vector2Int pos, int width, int height, int border, Color c) {
        this(pos.x, pos.y, width, height, border, c);
    }
    public Rect(Vector2Int pos, int size, int border, Color c) {
        this(pos.x, pos.y, size, size, border, c);
    }
    public Rect(int x, int y, int width, int height, int border, Color c) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.border = border;
        this.c = c;
    }

    public ShapeType getShapeType() {
        return ShapeType.RECTANGLE;
    }

    public Graphics2D draw(Graphics2D g) {
        g.setColor(c);
        g.setStroke(new BasicStroke(border));
        g.drawRect(x, y, width, height);

        return g;
    }
}
