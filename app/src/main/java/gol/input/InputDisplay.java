package gol.input;

import java.awt.Color;

import gol.display.Display;
import gol.game.Vector2;

import com.stuypulse.stuylib.util.chart.KeyTracker;
import com.stuypulse.stuylib.util.chart.MouseTracker;

public class InputDisplay extends Display {
    private KeyTracker keyboard;
    private MouseTracker mouse;

    public InputDisplay() { this(500, 500, Color.WHITE); }
    public InputDisplay(Color background) { this(500, 500, background); }
    public InputDisplay(int width, int height, Color background) {
        super(width, height, background);

        keyboard = new KeyTracker();
        mouse = new MouseTracker(this);
        addKeyListener(keyboard);
    }

    public boolean hasKey(String key) {
        return keyboard.hasKey(key);
    }
    
    public Vector2 getMouse() {
        return new Vector2(mouse.getMouseX(), mouse.getMouseY());
    }
}