package gol.input;

import java.util.HashMap;

import gol.game.Board;
import gol.game.Vector2;

public class BoardInput {
    private Board b;
    private KeyBinding keyBind;
    private HashMap<String, Integer> keyCooldownTimer;

    // apparently this code runs 6x per millisecond on my laptop
    private final int KEY_COOLDOWN = 450*6;
    private final double ZOOM_MULT = 1.5;
    private final double MOVEMENT_MULT = 0.1;

    private double movementSpeed = 50;

    public BoardInput(Board board, KeyBinding binding) {
        this.b = board;
        keyBind = binding;
        
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
    }

    public void checkKeys() {
        movementSpeed = (b.WIDTH/b.cellScreenLen)*MOVEMENT_MULT;

        for (HashMap.Entry<String, Integer> mapElement : keyCooldownTimer.entrySet()) {
            if (mapElement.getValue() > 0)
                keyCooldownTimer.replace(mapElement.getKey(), mapElement.getValue()-1);
        }

        if (keyCanBePressed(keyBind.toggleAutoKey())) {
            b.stepAuto = !b.stepAuto;
            startTimer(keyBind.toggleAutoKey());
        }
        
        if (!b.stepAuto && keyCanBePressed(keyBind.singleStepKey())) {
            b.step();
            startTimer(keyBind.singleStepKey());
        }

        if (keyCanBePressed(keyBind.quitKey())) {
            b.run = false;
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
            b.screenPos = b.screenPos.add(new Vector2(0, -movementSpeed));
            startTimer(keyBind.up());
        }

        if (keyCanBePressed(keyBind.down())) {
            b.screenPos = b.screenPos.add(new Vector2(0, movementSpeed));
            startTimer(keyBind.down());
        }

        if (keyCanBePressed(keyBind.left())) {
            b.screenPos = b.screenPos.add(new Vector2(-movementSpeed, 0));
            startTimer(keyBind.left());
        }

        if (keyCanBePressed(keyBind.right())) {
            b.screenPos = b.screenPos.add(new Vector2(movementSpeed, 0));
            startTimer(keyBind.right());
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
        // TODO: fix zoom not working at 0,0
        if (zoomIn) {
            b.cellScreenLen *= ZOOM_MULT;
            b.screenPos = b.screenPos.div(ZOOM_MULT);
        } else {
            b.cellScreenLen /= ZOOM_MULT;
            b.screenPos = b.screenPos.mul(ZOOM_MULT);
        }

        if (b.cellScreenLen == 0)
            b.cellScreenLen = 1;
    }
}
