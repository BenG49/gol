package gol.display.shapes;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gol.util.RectType;
import gol.util.Vector2;
import gol.util.Vector2Int;

public class Text extends Shape {
    private final List<String> text;
    private final int x, y;
    private final Color c;
    private final Font font;

    private boolean usePreset;
    private ScreenPos preset;
    private RectType centerDimension;

    private static final int PADDING = 10;

    public enum ScreenPos {
        TOP_LEFT(-1, -1), TOP_CENTER(0, -1), TOP_RIGHT(1, -1),
        MID_LEFT(-1, 0),  MID_CENTER(0, 0),  MID_RIGHT(1, 0),
        BOT_LEFT(-1, 1),  BOT_CENTER(0, 1),  BOT_RIGHT(1, 1);

        int presetX;
        int presetY;

        ScreenPos(int presetX, int presetY) {
            this.presetX = presetX;
            this.presetY = presetY;
        }

        // thanks to https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-java
        public Vector2Int getXY(RectType dim, FontMetrics metrics, String text) {
            Vector2Int pos = dim.getPos().round();
            Vector2Int size = dim.getSize().round();

            int outputX, outputY;

            if (presetX == -1)
                outputX = PADDING;
            else if (presetX == 1)
                outputX = (size.x-metrics.stringWidth(text) - PADDING);
            else
                outputX = (size.x-metrics.stringWidth(text)) / 2;
            
            if (presetY == -1)
                outputY = metrics.getAscent() + PADDING;
            else if (presetY == 1)
                outputY = size.y - PADDING;
            else
                outputY = ((size.y-metrics.getHeight()) / 2) + metrics.getAscent();

            return new Vector2Int(outputX+pos.x, outputY+pos.y);
        }

        public int getBaseHeight(int y, FontMetrics metrics, int lineCount) {
            lineCount--;

            if (presetY == -1)
                return y;
            else if (presetY == 1)
                return y-lineCount*(metrics.getHeight()+PADDING);
            else
                return y-lineCount/2*(metrics.getHeight()+PADDING);
        }
    };
    public Text(String text, ScreenPos preset, int widthHeight, Color c, Font font) {
        this(text, preset, new RectType(new Vector2(0), new Vector2(widthHeight)), c, font);
    }
    public Text(String text, ScreenPos preset, RectType centerDimension, Color c, Font font) {
        this(new ArrayList<String>(Arrays.asList(text)), preset, centerDimension, c, font);
    }
    public Text(String[] text, ScreenPos preset, int widthHeight, Color c, Font font) {
        this(Arrays.asList(text), preset, new RectType(new Vector2(0), new Vector2(widthHeight)), c, font);
    }
    public Text(String[] text, ScreenPos preset, RectType centerDimension, Color c, Font font) {
        this(Arrays.asList(text), preset, centerDimension, c, font);
    }
    public Text(List<String> text, ScreenPos preset, RectType centerDimension, Color c, Font font) {
        this (text, 0, 0, c, font);
        this.preset = preset;
        this.centerDimension = centerDimension;

        usePreset = true;
    }

    public Text(String text, Vector2Int pos, Color c, Font font) {
        this(text, pos.x, pos.y, c, font);
    }
    public Text(String text, int x, int y, Color c, Font font) {
        this(new ArrayList<String>(Arrays.asList(text)), x, y, c, font);
    }
    public Text(String[] text, int x, int y, Color c, Font font) {
        this(Arrays.asList(text), x, y, c, font);
    }
    public Text(List<String> text, int x, int y, Color c, Font font) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.c = c;
        this.font = font;

        usePreset = false;
    }

    public ShapeType getShapeType() {
        return ShapeType.TEXT;
    }

    public Graphics2D draw(Graphics2D g) {
        g.setFont(font);
        g.setColor(c);
        int fontHeight = g.getFontMetrics(font).getAscent();
        if (!usePreset) {
            int baseHeight = y;
            for (int i = 0; i < text.size(); i++)
                g.drawString(text.get(i), x, baseHeight+i*(fontHeight+PADDING));
        }
        else {
            FontMetrics metrics = g.getFontMetrics(font);
            for (int i = 0; i < text.size(); i++) {
                Vector2Int pos = preset.getXY(centerDimension, metrics, text.get(i));
                int baseHeight = preset.getBaseHeight(pos.y, metrics, text.size());
                g.drawString(text.get(i), pos.x, baseHeight+i*(g.getFontMetrics(font).getAscent()+PADDING));
            }
        }

        return g;
    }
}
