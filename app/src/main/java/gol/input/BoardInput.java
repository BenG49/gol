package gol.input;

import java.util.HashMap;
import java.util.HashSet;

import gol.game.Board;
import gol.game.Vector2;

public class BoardInput {
    private Board b;
    private KeyBinding keyBind;
    private HashSet<String> lastKeyPressed;
    private HashMap<String, Integer> keyRepeat;

    // multiplier for zooming, higher is faster zooming
    private final double ZOOM_MULT = 1.5;
    // multiplier for movementSpeed
    private final double MOVEMENT_MULT = 0.1;
    // slowest step time
    private final int MAX_STEP_TIME = 750;
    // interval to increase stepTime by
    private final int STEP_TIME_INTERVAL = (int)(MAX_STEP_TIME/10);
    
    // <--THESE VALUES ARE COMPLETELY DEPENDENT ON PROGRAM SPEED-->
    // amount of time to wait until spamming
    private final int KEY_REPEAT_CUTOFF = 500;
    // interval to wait between keys to activate when spamming
    private final int KEY_REPEAT_MODULO = 40;

    private int stepTimeMillis;
    private boolean stepAuto;
    private boolean run;
    private Vector2 screenPos;
    private int cellScreenLen;

    private double movementSpeed = 50;

    public BoardInput(Board b, KeyBinding binding, int cellScreenLen) {
        this(b, binding, new Vector2(-b.WIDTH/cellScreenLen/2, -b.HEIGHT/cellScreenLen/2), cellScreenLen);
    }
    public BoardInput(Board b, KeyBinding binding, Vector2 screenPos, int cellScreenLen) {
        this.b = b;
        this.screenPos = screenPos;
        this.cellScreenLen = cellScreenLen;
        keyBind = binding;

        run = true;
        stepAuto = false;
        stepTimeMillis = STEP_TIME_INTERVAL*5;

        lastKeyPressed = new HashSet<String>();
        keyRepeat = new HashMap<String, Integer>();
    }

    public void checkKeys() {

        movementSpeed = (b.WIDTH/cellScreenLen)*MOVEMENT_MULT;

        // KEYS
        if (keyCanBePressed(keyBind.toggleAutoKey()))
            stepAuto = !stepAuto;
        
        if (!stepAuto && keyCanBePressed(keyBind.singleStepKey()))
            b.game.step();

        if (keyCanBePressed(keyBind.quitKey()))
            ;// run = false; // COMMENTED FOR NOW BECAUSE IT'S ANNOYING

        if (keyCanBePressed(keyBind.zoomOut()))
            zoom(false);

        if (keyCanBePressed(keyBind.zoomIn()))
            zoom(true);

        if (keyCanBePressed(keyBind.up()))
            screenPos = screenPos.add(new Vector2(0, -movementSpeed));

        if (keyCanBePressed(keyBind.down()))
            screenPos = screenPos.add(new Vector2(0, movementSpeed));

        if (keyCanBePressed(keyBind.left()))
            screenPos = screenPos.add(new Vector2(-movementSpeed, 0));

        if (keyCanBePressed(keyBind.right()))
            screenPos = screenPos.add(new Vector2(movementSpeed, 0));

        if (keyCanBePressed(keyBind.speedUp()) && stepTimeMillis > 2)
            stepTimeMillis = Math.max(stepTimeMillis - STEP_TIME_INTERVAL, 2);

        if (keyCanBePressed(keyBind.speedDown()) && stepTimeMillis < MAX_STEP_TIME)
            stepTimeMillis += STEP_TIME_INTERVAL;

        if (keyCanBePressed(keyBind.toOrigin()))
            screenPos = new Vector2(-b.WIDTH/cellScreenLen/2, -b.HEIGHT/cellScreenLen/2);

        if (keyCanBePressed(keyBind.clear()))
            b.game.clearCells();
    }

    private boolean keyCanBePressed(String key) {
        // key held down
        if (b.hasKey(key)) {
            if (!keyRepeat.containsKey(key))
                keyRepeat.put(key, 0);
            
            Integer temp = keyRepeat.get(key);
            keyRepeat.replace(key, ++temp);
            
            if (temp >= KEY_REPEAT_CUTOFF && temp % KEY_REPEAT_MODULO == 0)
                return true;
        }

        // key initial press
        if (!lastKeyPressed.contains(key) && b.hasKey(key)) {
            lastKeyPressed.add(key);
            return true;
        }

        // key release
        if (lastKeyPressed.contains(key) && !b.hasKey(key)) {
            lastKeyPressed.remove(key);
            keyRepeat.remove(key);
        }

        return false;
    }

    private void zoom(boolean zoomIn) {
        final Vector2 screenCenter = screenPos.add(new Vector2(b.WIDTH/2/cellScreenLen, b.HEIGHT/2/cellScreenLen));

        if (zoomIn)
            cellScreenLen = (int)Math.round(cellScreenLen*ZOOM_MULT);
        else
            cellScreenLen = (int)Math.round(cellScreenLen/ZOOM_MULT);

        screenPos = screenCenter.sub(new Vector2(b.WIDTH/cellScreenLen/2, b.HEIGHT/cellScreenLen/2));

        if (cellScreenLen < 2)
            cellScreenLen = 2;
    }

    // get methods
    public boolean getStepAuto() {
        return stepAuto;
    }

    public boolean getRun() {
        return run;
    }

    public Vector2 getScreenPos() {
        return screenPos;
    }

    public int getCellScreenLen() {
        return cellScreenLen;
    }
    
    public int getStepTimeMillis() {
        return stepTimeMillis;
    }

    public int getSpeed0to10() {
        int value = (int)Math.ceil(10*(MAX_STEP_TIME-stepTimeMillis)/(float)MAX_STEP_TIME);
        return value;
    }
}
