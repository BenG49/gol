package gol.display.shapes;

import java.awt.*;
import gol.util.Vector2i;

public class FillRect extends Shape {
    private final int x, y, width, height, border;
    private final Color fill;

    public FillRect(Vector2i pos, Vector2i size, int border, Color background) {
        this(pos.x, pos.y, size.x, size.y, border, background);
    }
    public FillRect(Vector2i pos, int width, int height, int border, Color background) {
        this(pos.x, pos.y, width, height, border, background);
    }
    public FillRect(Vector2i pos, int size, int border, Color background) {
        this(pos.x, pos.y, size, size, border, background);
    }
    public FillRect(int x, int y, int width, int height, int border, Color fill) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.border = border;
        this.fill = fill;
    }

    public ShapeType getShapeType() {
        return ShapeType.FILL_RECTANGLE;
    }

    public Graphics2D draw(Graphics2D g) {
        g.setColor(fill);
        g.setStroke(new BasicStroke(border));
        g.fillRect(x, y, width, height);
        
        return g;
    }
}
