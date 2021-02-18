package gol.input;

import java.util.HashMap;

import gol.game.Board;
import gol.game.Vector2;

public class BoardInput {
    private Board b;
    private KeyBinding keyBind;
    private HashMap<String, Integer> keyCooldownTimer;

    private final int KEY_COOLDOWN = 250;
    private final double ZOOM_MULT = 1.5;
    private final double MOVEMENT_MULT = 0.1;
    private final int STEP_TIME_INTERVAL = 25;

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
        stepTimeMillis = 400;
        
        keyCooldownTimer = new HashMap<String, Integer>();

        hashPutInit(keyBind.toggleAutoKey());
        hashPutInit(keyBind.singleStepKey());
        hashPutInit(keyBind.quitKey());
        hashPutInit(keyBind.zoomOut());
        hashPutInit(keyBind.zoomIn());
        hashPutInit(keyBind.up());
        hashPutInit(keyBind.down());
        hashPutInit(keyBind.left());
        hashPutInit(keyBind.right());
        hashPutInit(keyBind.speedUp());
        hashPutInit(keyBind.speedDown());
        hashPutInit(keyBind.toOrigin());
    }

    public void checkKeys() {
        movementSpeed = (b.WIDTH/cellScreenLen)*MOVEMENT_MULT;

        System.out.println(cellScreenLen+", "+b.WIDTH/cellScreenLen);

        for (HashMap.Entry<String, Integer> mapElement : keyCooldownTimer.entrySet()) {
            if (mapElement.getValue() > 0)
                keyCooldownTimer.replace(mapElement.getKey(), mapElement.getValue()-1);
        }

        if (keyCanBePressed(keyBind.toggleAutoKey())) {
            stepAuto = !stepAuto;
            startTimer(keyBind.toggleAutoKey());
        }
        
        if (!stepAuto && keyCanBePressed(keyBind.singleStepKey())) {
            b.step();
            startTimer(keyBind.singleStepKey());
        }

        if (keyCanBePressed(keyBind.quitKey())) {
            // run = false;
            startTimer(keyBind.quitKey());
        }

        if (keyCanBePressed(keyBind.zoomOut())) {
            zoom(false);
            startTimer(keyBind.zoomOut());
        }

        if (keyCanBePressed(keyBind.zoomIn())) {
            zoom(true);
            startTimer(keyBind.zoomIn());
        }

        if (keyCanBePressed(keyBind.up())) {
            screenPos = screenPos.add(new Vector2(0, -movementSpeed));
            startTimer(keyBind.up());
        }

        if (keyCanBePressed(keyBind.down())) {
            screenPos = screenPos.add(new Vector2(0, movementSpeed));
            startTimer(keyBind.down());
        }

        if (keyCanBePressed(keyBind.left())) {
            screenPos = screenPos.add(new Vector2(-movementSpeed, 0));
            startTimer(keyBind.left());
        }

        if (keyCanBePressed(keyBind.right())) {
            screenPos = screenPos.add(new Vector2(movementSpeed, 0));
            startTimer(keyBind.right());
        }

        if (keyCanBePressed(keyBind.speedUp())) {
            if (stepTimeMillis > 1)
                stepTimeMillis = Math.max(stepTimeMillis - STEP_TIME_INTERVAL, 1);
            startTimer(keyBind.speedUp());
        }

        if (keyCanBePressed(keyBind.speedDown())) {
            stepTimeMillis += STEP_TIME_INTERVAL;
            startTimer(keyBind.speedDown());
        }

        if (keyCanBePressed(keyBind.toOrigin())) {
            screenPos = new Vector2(-b.WIDTH/cellScreenLen/2, -b.HEIGHT/cellScreenLen/2);
            startTimer(keyBind.toOrigin());
        }
    }

    private boolean keyCanBePressed(String key) {
        return keyCooldownTimer.get(key) == 0 && b.hasKey(key);
    }

    private void hashPutInit(String key) {
        keyCooldownTimer.put(key, 0);
    }

    private void startTimer(String key) {
        keyCooldownTimer.replace(key, KEY_COOLDOWN);
    }

    private void zoom(boolean zoomIn) {
        final double squareCount = b.WIDTH/cellScreenLen;

        if (zoomIn) {
            cellScreenLen *= ZOOM_MULT;
            screenPos = screenPos.add(new Vector2((squareCount-squareCount/1.5)/2));
        } else {
            cellScreenLen /= ZOOM_MULT;
            screenPos = screenPos.sub(new Vector2((squareCount*1.5-squareCount)/2));
        }

        if (cellScreenLen == 0)
            cellScreenLen = 1;
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
}
