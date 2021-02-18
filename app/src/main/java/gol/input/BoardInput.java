package gol.input;

import java.util.HashMap;

import gol.game.Board;
import gol.game.Vector2;

public class BoardInput {
    private Board board;
    private KeyBinding keyBind;
    private HashMap<String, Integer> keyCooldownTimer;

    // apparently this code runs 6x per millisecond on my laptop
    private final int KEY_COOLDOWN = 450*6;
    private final float ZOOM_MULT = 1.5f;
    private final float MOVEMENT_MULT = 0.01f;

    private int movementSpeed = 50;

    public BoardInput(Board board, KeyBinding binding) {
        this.board = board;
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
        movementSpeed = (int)((board.WIDTH-board.cellScreenLen)*MOVEMENT_MULT);

        for (HashMap.Entry<String, Integer> mapElement : keyCooldownTimer.entrySet()) {
            if (mapElement.getValue() > 0)
                keyCooldownTimer.replace(mapElement.getKey(), mapElement.getValue()-1);
        }

        if (keyCanBePressed(keyBind.toggleAutoKey())) {
            board.stepAuto = !board.stepAuto;
            startTimer(keyBind.toggleAutoKey());
        }
        
        if (!board.stepAuto && keyCanBePressed(keyBind.singleStepKey())) {
            board.step();
            startTimer(keyBind.singleStepKey());
        }

        if (keyCanBePressed(keyBind.quitKey())) {
            board.run = false;
            startTimer(keyBind.quitKey());
        }

        // TODO: move screenPos so that zoom is centered on screen
        if (keyCanBePressed(keyBind.zoomOut())) {
            board.cellScreenLen /= ZOOM_MULT;
            startTimer(keyBind.zoomOut());
        }

        if (keyCanBePressed(keyBind.zoomIn())) {
            board.cellScreenLen *= ZOOM_MULT;
            startTimer(keyBind.zoomIn());
        }

        if (keyCanBePressed(keyBind.up())) {
            board.screenPos = board.screenPos.add(new Vector2(0, -movementSpeed));
            startTimer(keyBind.up());
        }

        if (keyCanBePressed(keyBind.down())) {
            board.screenPos = board.screenPos.add(new Vector2(0, movementSpeed));
            startTimer(keyBind.down());
        }

        if (keyCanBePressed(keyBind.left())) {
            board.screenPos = board.screenPos.add(new Vector2(-movementSpeed, 0));
            startTimer(keyBind.left());
        }

        if (keyCanBePressed(keyBind.right())) {
            board.screenPos = board.screenPos.add(new Vector2(movementSpeed, 0));
            startTimer(keyBind.right());
        }
    }

    private boolean keyCanBePressed(String key) {
        return keyCooldownTimer.get(key) == 0 && board.hasKey(key);
    }

    private void hashPutInit(String key) {
        keyCooldownTimer.put(key, 0);
    }

    private void startTimer(String key) {
        keyCooldownTimer.replace(key, KEY_COOLDOWN);
    }
}
