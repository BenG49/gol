package gol.display.ui;

import java.util.List;

import gol.display.shapes.Shape;
import gol.input.InputDisplay;
import gol.util.RectType;

import java.awt.Graphics2D;

public abstract class UIElement {
    public enum Element {TEXTBOX, MENUBAR};

    private RectType pos;
    private final InputDisplay input;

    public UIElement(RectType pos, InputDisplay input) {
        this.pos = pos;
        this.input = input;
    }

    public void setPos(RectType pos) {
        this.pos = pos;
    }

    public boolean containsMouse() {
        return input.getMouse().within(pos.getPos(), pos.getPos().add(pos.getSize()));
    }

    public boolean leftMouseClicked() {
        return containsMouse() && input.getButtonPressed(1);
    }

    public boolean rightMouseClicked() {
        return containsMouse() && input.getButtonPressed(3);
    }

    public abstract void draw(List<Shape> shapes);
    public abstract Element getElementType();
}
