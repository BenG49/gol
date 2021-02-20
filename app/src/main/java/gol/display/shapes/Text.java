package gol.display.shapes;

import java.awt.*;
import gol.game.Vector2Int;

public class Text extends Shape {
    private final String text;
    private final int x, y;
    private final Color c;
    private final Font font;

    public Text(String text, Vector2Int pos, Color c, Font font) {
        this(text, pos.x, pos.y, c, font);
    }
    public Text(String text, int x, int y, Color c, Font font) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.c = c;
        this.font = font;
    }

    public ShapeType getShapeType() {
        return ShapeType.TEXT;
    }

    public Graphics2D draw(Graphics2D g) {
        g.setFont(font);
        g.setColor(c);
        g.drawString(text, x, y);

        return g;
    }
}
