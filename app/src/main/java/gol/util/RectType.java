package gol.util;

public class RectType {
    private Vector2d pos, size;

    public RectType(double xPos, double yPos, double sizeX, double sizeY) {
        this(new Vector2d(xPos, yPos), new Vector2d(sizeX, sizeY));
    }
    public RectType(Vector2i pos, Vector2i size) {
        this(pos.toVector2(), size.toVector2());
    }
    public RectType(Vector2d pos, Vector2d size)  {
        this.pos = pos;
        this.size = size;
    }

    public Vector2d getPos() {
        return pos;
    }

    public Vector2d getSize() {
        return size;
    }
}
