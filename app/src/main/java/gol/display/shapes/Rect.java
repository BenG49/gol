package gol.display.shapes;

import java.awt.*;

import gol.util.RectType;
import gol.util.Vector2i;

public class Rect extends Shape {
    private final int x, y, width, height, border;
    private final Color c;

    public Rect(RectType rect, int border, Color c) {
        this(rect.getPos().floor(), rect.getSize().floor(), border, c);
    }
    public Rect(Vector2i pos, Vector2i size, int border, Color c) {
        this(pos.x, pos.y, size.x, size.y, border, c);
    }
    public Rect(Vector2i pos, int width, int height, int border, Color c) {
        this(pos.x, pos.y, width, height, border, c);
    }
    public Rect(Vector2i pos, int size, int border, Color c) {
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

    public int[] getDimensions() {
        return new int[] {x, y, width, height };
    }
}
