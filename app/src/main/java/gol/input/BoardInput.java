package gol.input;

import java.awt.Font;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import bglib.display.shapes.Shape;
import bglib.display.shapes.AlignText;
import bglib.display.shapes.AlignText.Alignment;
import bglib.input.*;

import gol.game.Board;
import gol.game.BoardUI;

import bglib.util.RectType;
import bglib.util.Vector2d;

public class BoardInput {
    private Board b;

    public KeyBinding keyBind;

    // multiplier for zooming, higher is faster zooming
    private final double ZOOM_MULT = 1.5;
    // multiplier for movementSpeed
    private final double MOVEMENT_MULT = 0.1;
    // slowest step time
    private final int MAX_STEP_TIME = 300;
    // interval to increase stepTime by
    private final int STEP_TIME_INTERVAL = (int)(MAX_STEP_TIME/10);
    // zoom min
    public final int CELL_LEN_MIN = 2;
    // zoom max
    public final int CELL_LEN_MAX = 500;

    private int stepTimeMillis;
    private boolean stepAuto;
    private boolean run;
    private Vector2d screenPos;
    private int cellScreenLen;
    private int selectMode;
    private boolean runOptimized;

    private final int KEY_REPEAT_SLOW_MOD = 300;

    public BoardInput(Board b, KeyBinding binding, int cellScreenLen) {
        this(b, binding, new Vector2d(-b.WIDTH/cellScreenLen/2, -b.HEIGHT/cellScreenLen/2), cellScreenLen);
    }
    public BoardInput(Board b, KeyBinding binding, Vector2d screenPos, int cellScreenLen) {
        this.b = b;
        this.screenPos = screenPos;
        this.cellScreenLen = cellScreenLen;
        keyBind = binding;

        run = true;
        stepAuto = false;
        stepTimeMillis = STEP_TIME_INTERVAL*5;
        selectMode = 0;
        runOptimized = false;

        b.setTrackerEvents(keyEvents());

        BoardUI.setCellLen(cellScreenLen);
        BoardUI.setScreenPos(screenPos);
    }

    private HashMap<String, InputEvent> keyEvents() {
        HashMap<String, InputEvent> output = new HashMap<String, InputEvent>();

        output.put(keyBind.toggleAutoKey(), new InputEvent() {
            @Override public void run() { stepAuto = !stepAuto; }
            @Override public EventType getEventType() {
                return new EventType(EventTime.PRESS); }});
            
        output.put(keyBind.singleStepKey(), new InputEvent() {
            @Override public void run() { if (!stepAuto) {
                    b.game.step(); b.ctrlZClear(); }}
            @Override public EventType getEventType() {
                return new EventType(EventTime.PRESS); }});

        output.put(keyBind.zoomOut(), new InputEvent() {
            @Override public void run() { zoom(false); }
            @Override public EventType getEventType() {
                return new EventType(EventTime.REPEAT, KEY_REPEAT_SLOW_MOD); }});

        output.put(keyBind.zoomIn(), new InputEvent() {
            @Override public void run() { zoom(true); }
            @Override public EventType getEventType() {
                return new EventType(EventTime.REPEAT, KEY_REPEAT_SLOW_MOD); }});
        
        output.put(keyBind.up(), new InputEvent() {
            @Override public void run() { move("up"); }
            @Override public EventType getEventType() {
                return new EventType(EventTime.REPEAT); }});
        
        output.put(keyBind.down(), new InputEvent() {
            @Override public void run() { move("down"); }
            @Override public EventType getEventType() {
                return new EventType(EventTime.REPEAT); }});
        
        output.put(keyBind.left(), new InputEvent() {
            @Override public void run() { move("left"); }
            @Override public EventType getEventType() {
                return new EventType(EventTime.REPEAT); }});
        
        output.put(keyBind.right(), new InputEvent() {
            @Override public void run() { move("right"); }
            @Override public EventType getEventType() {
                return new EventType(EventTime.REPEAT); }});
            
        output.put(keyBind.toOrigin(), new InputEvent() {
            @Override public void run() {
                screenPos = new Vector2d(-b.WIDTH/cellScreenLen/2, -b.HEIGHT/cellScreenLen/2); }
            @Override public EventType getEventType() {
                return new EventType(EventTime.REPEAT); }});
            
        output.put(keyBind.speedUp(), new InputEvent() {
            @Override public void run() { if (stepTimeMillis > 2)
                stepTimeMillis = Math.max(stepTimeMillis - STEP_TIME_INTERVAL, 2); }
            @Override public EventType getEventType() {
                return new EventType(EventTime.REPEAT, KEY_REPEAT_SLOW_MOD); }});
            
        output.put(keyBind.speedDown(), new InputEvent() {
            @Override public void run() { if (stepTimeMillis < STEP_TIME_INTERVAL)
                stepTimeMillis += STEP_TIME_INTERVAL; }
            @Override public EventType getEventType() {
                return new EventType(EventTime.REPEAT, KEY_REPEAT_SLOW_MOD); }});

        output.put(keyBind.clear(), new InputEvent() {
            @Override public void run() { b.clear(); }
            @Override public EventType getEventType() {
                return new EventType(EventTime.PRESS); }});

        output.put(keyBind.mode1(), new InputEvent() {
            @Override public void run() { selectMode = 0; }
            @Override public EventType getEventType() {
                return new EventType(EventTime.PRESS); }});

        output.put(keyBind.mode2(), new InputEvent() {
            @Override public void run() { selectMode = 1; }
            @Override public EventType getEventType() {
                return new EventType(EventTime.PRESS); }});

        output.put(keyBind.reset(), new InputEvent() {
            @Override public void run() { b.game.resetSteps(); }
            @Override public EventType getEventType() {
                return new EventType(EventTime.PRESS); }});
        
        return output;
    }

    public int checkSavePrompt() {
        if (b.hasKey(keyBind.delete()))
            return 2;
        if (b.hasKey(keyBind.saveKey()))
            return 1;
        if (b.hasKey(keyBind.cancelKey()))
            return -1;
        return 0;
    }

    public boolean checkKeybindPrompt() {
        return b.hasKey(keyBind.cancelKey());
    }

    public int placeSchemCheck() {
        if (b.hasKey(keyBind.rotate()))
            return 1;
        if (b.hasKey(keyBind.mirror()))
            return 2;
        else
            return 0;
    }

    private void move(String dir) {
        double xDelta = 0, yDelta = 0;
        double movementSpeed = (b.WIDTH/cellScreenLen)*MOVEMENT_MULT;

        if (dir == "up")
            yDelta = -movementSpeed;
        else if (dir == "down")
            yDelta = movementSpeed;
        else if (dir == "left")
            xDelta = -movementSpeed;
        else if (dir == "right")
            xDelta = movementSpeed;
        
        screenPos = screenPos.add(new Vector2d(xDelta, yDelta));
        BoardUI.setScreenPos(screenPos);
    }

    private void zoom(boolean zoomIn) {
        final Vector2d screenCenter = screenPos.add(new Vector2d(b.WIDTH/2/cellScreenLen, b.HEIGHT/2/cellScreenLen));

        if (zoomIn)
            cellScreenLen = (int)Math.round(cellScreenLen*ZOOM_MULT);
        else
            cellScreenLen = (int)Math.round(cellScreenLen/ZOOM_MULT);

        if (cellScreenLen < CELL_LEN_MIN)
            cellScreenLen = CELL_LEN_MIN;
        else if (cellScreenLen > CELL_LEN_MAX)
            cellScreenLen = CELL_LEN_MAX;
        else
            screenPos = screenCenter.sub(new Vector2d(b.WIDTH/cellScreenLen/2, b.HEIGHT/cellScreenLen/2));
        
        BoardUI.setCellLen(cellScreenLen);
        BoardUI.setScreenPos(screenPos);
    }

    public void setSelMode(int mode) { 
        final int SEL_MODE_COUNT = 2;
        if (mode < SEL_MODE_COUNT)
            this.selectMode = mode;
    }

    // get methods
    public Shape[] getKeyGuide() {
        final int CENTER_RATIO = (int)(b.WIDTH*0.3);
        final int BOTTOM_RATIO = (int)(b.HEIGHT*0.9);
        final Font font = new Font("Cascadia Code", Font.PLAIN, 20);

        return new AlignText[] {new AlignText(new String[] {
                keyBind.toggleAutoKey()+                    ":",
                keyBind.up()+","+keyBind.left()+","+keyBind.down()+","+keyBind.right()+":",
                keyBind.zoomIn()+","+keyBind.zoomOut()+     ":",
                keyBind.speedUp()+","+keyBind.speedDown()+  ":",
                keyBind.singleStepKey()+                    ":",
                keyBind.toOrigin()+                         ":",
                keyBind.clear()+                            ":",
                keyBind.toggleOptimized()+                  ":",
                keyBind.mode1()+","+keyBind.mode2()+        ":",
                keyBind.reset()+                            ":",
                keyBind.undo()+                             ":",
                "Esc:"
            }, Alignment.TOP_RIGHT, new RectType(0, 0, CENTER_RATIO, BOTTOM_RATIO), Color.WHITE, font, false),

            new AlignText(new String[] {
                "toggle pause and play",
                "move around",
                "zoom in and out, respectively",
                "raise and lower the step speed (max 10)",
                "single step when in single step mode",
                "jump back to the origin if you get lost",
                "clear the board",
                "toggle optimized mode for running simulations fast",
                "switch to selection mode 1 and 2",
                "reset the sim back to step 0",
                "undo the last set of cells drawn",
                "close the simulation"
            }, Alignment.TOP_LEFT, new RectType(CENTER_RATIO, 0, b.WIDTH-CENTER_RATIO, BOTTOM_RATIO), Color.WHITE, font, false),

            new AlignText("Press "+keyBind.cancelKey()+" to exit.", Alignment.BOT_CENTER, new RectType(0, BOTTOM_RATIO, b.WIDTH, b.HEIGHT-BOTTOM_RATIO), Color.WHITE, font, false)
        };
    }

    public boolean getStepAuto() {
        return stepAuto;
    }

    public boolean getRun() {
        return run;
    }

    public Vector2d getScreenPos() {
        return screenPos;
    }

    public int getCellLen() {
        return cellScreenLen;
    }
    
    public int getStepTimeMillis() {
        return stepTimeMillis;
    }

    public int getSelectMode() {
        return selectMode;
    }

    public boolean getRunOptimized() {
        return runOptimized;
    }

    public int getSpeed0to10() {
        return (int)Math.ceil(10*(MAX_STEP_TIME-stepTimeMillis)/(float)MAX_STEP_TIME);
    }
}
