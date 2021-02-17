package gol.input;

import java.awt.Color;

import gol.display.Display;
import com.stuypulse.stuylib.util.chart.KeyTracker;

public class InputDisplay extends Display {
    private KeyTracker keyboard;

    public InputDisplay() { this(500, 500, Color.WHITE); }
    public InputDisplay(Color background) { this(500, 500, background); }
    public InputDisplay(int width, int height, Color background) {
        super(width, height, background);

        keyboard = new KeyTracker();
        addKeyListener(keyboard);
    }

    public boolean hasKey(String key) {
        return keyboard.hasKey(key);
    }
}