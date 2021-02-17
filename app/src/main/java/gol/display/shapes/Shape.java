package gol.display.shapes;

import java.awt.Graphics2D;

public abstract class Shape {
    public enum ShapeType { RECTANGLE, TEXT, FILL_RECTANGLE };

    public abstract ShapeType getShapeType();
    public abstract Graphics2D draw(Graphics2D g);
}
