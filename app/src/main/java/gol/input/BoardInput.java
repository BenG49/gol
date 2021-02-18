package gol.input;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import gol.game.Board;
import gol.game.Vector2;

public class BoardInput {
    private Board board;
    private KeyBinding keyBind;

    private TimerTask task;
    private HashMap<String, Boolean> keysActive;
    private HashMap<String, Timer> cooldownTimers;

    private final long KEY_COOLDOWN_MILLIS = 100L;
    private final float ZOOM_MULT = 1.5f;
    private final int MOVEMENT_SPEED = 50;

    public BoardInput(Board board, KeyBinding binding) {
        this.board = board;
        keyBind = binding;
        
        keysActive = new HashMap<String, Boolean>();
        cooldownTimers = new HashMap<String, Timer>();

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

        if (keyCanBePressed(keyBind.zoomOut())) {
            board.cellScreenLen /= ZOOM_MULT;
        }

        if (keyCanBePressed(keyBind.zoomIn())) {
            board.cellScreenLen *= ZOOM_MULT;
        }

        if (keyCanBePressed(keyBind.up())) {
            board.screenPos = board.screenPos.add(new Vector2(0, MOVEMENT_SPEED));
        }

        if (keyCanBePressed(keyBind.down())) {
            board.screenPos = board.screenPos.add(new Vector2(0, -MOVEMENT_SPEED));
        }

        if (keyCanBePressed(keyBind.left())) {
            board.screenPos = board.screenPos.add(new Vector2(MOVEMENT_SPEED, 0));
        }

        if (keyCanBePressed(keyBind.right())) {
            board.screenPos = board.screenPos.add(new Vector2(-MOVEMENT_SPEED, 0));
        }
    }

    private boolean keyCanBePressed(String key) {
        return keysActive.get(key) && board.hasKey(key);
    }

    private void hashPutInit(String key) {
        keysActive.put(key, true);
        cooldownTimers.put(key, new Timer(key));
    }

    private void startTimer(String key) {
        if (cooldownTimers.containsKey(key)) {
            keysActive.replace(key, false);

            task = new TimerTask() {
                public void run() {
                    keysActive.replace(Thread.currentThread().getName(), true);
                }
            };

            cooldownTimers.get(key).schedule(task, KEY_COOLDOWN_MILLIS);
        }
    }
}
