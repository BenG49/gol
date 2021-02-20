package gol.display.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gol.display.shapes.*;
import gol.input.InputDisplay;
import gol.util.RectType;

public class TextBoxElement extends UIElement {

    private List<Text> contents;
    private FillRect fill;
    private Rect border;

    private static final Color DEFAULT_COLOR = new Color(0f, 0f, 0f, 0f);

    public TextBoxElement(RectType pos, InputDisplay input) {
        this(pos, input, new ArrayList<Text>());
    }
    public TextBoxElement(RectType pos, InputDisplay input, Text contents) {
        this(pos, input, new ArrayList<Text>(Arrays.asList(contents)));
    }
    public TextBoxElement(RectType pos, InputDisplay input, List<Text> contents) {
        this(pos, input, contents, new Rect(0, 0, 0, 0, 0, DEFAULT_COLOR), new FillRect(0, 0, 0, 0, 0, DEFAULT_COLOR));
    }
    public TextBoxElement(RectType pos, InputDisplay input, List<Text> contents, Rect border, FillRect fill) {
        super(pos, input);

        this.border = border;
        this.fill = fill;
    }

    public void setContents(List<Text> contents) {
        this.contents = contents;
    }
    public void setContents(Text contents) {
        setContents(new ArrayList<Text>(Arrays.asList(contents)));
    }

    @Override
    public void draw(List<Shape> shapes) {
        shapes.add(fill);
        shapes.add(border);
        for (Text i : contents)
            shapes.add(i);
    }

    @Override
    public Element getElementType() {
        return Element.TEXTBOX;
    }
}
