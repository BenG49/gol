package gol.util;

public class RectType {
    private Vector2 pos, size;

    public RectType(double xPos, double yPos, double sizeX, double sizeY) {
        this(new Vector2(xPos, yPos), new Vector2(sizeX, sizeY));
    }
    public RectType(Vector2Int pos, Vector2Int size) {
        this(pos.toVector2(), size.toVector2());
    }
    public RectType(Vector2 pos, Vector2 size)  {
        this.pos = pos;
        this.size = size;
    }

    public Vector2 getPos() {
        return pos;
    }

    public Vector2 getSize() {
        return size;
    }
}
