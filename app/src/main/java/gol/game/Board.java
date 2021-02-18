package gol.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Color;
import java.awt.Font;

import gol.display.shapes.*;
import gol.game.schematic.Schematic;
import gol.input.InputDisplay;
import gol.input.KeyBinding;

public class Board extends InputDisplay {
    private Vector2 screenPos;
    private int cellScreenLen;

    private HashSet<Vector2> aliveCells;
    private int step;
    private boolean run;
    private int stepTime;
    private boolean stepAuto;

    private TimerTask task;
    private HashMap<String, Boolean> keysActive;
    private HashMap<String, Timer> cooldownTimers;

    private final KeyBinding binding;
    private final long KEY_COOLDOWN_MILLIS = 100L;

    public Board(HashSet<Vector2> aliveCells) { this(aliveCells, new Vector2(-10, -10), 25, new KeyBinding()); }
    public Board(Schematic schem) { this(schem.getData(), new Vector2(-10, -10), 25, new KeyBinding()); }
    public Board(HashSet<Vector2> aliveCells, Vector2 screenPos, int cellScreenLen, KeyBinding binding) {
        super(Color.BLACK);

        this.aliveCells = aliveCells;
        this.screenPos = screenPos;
        this.cellScreenLen = cellScreenLen;
        this.binding = binding;

        step = 0;
        run = true;
        stepAuto = false;
        stepTime = 1000;

        initKeyHashes();
    }

    public void run() {
        drawBoard();

        while (run) {
            checkKeys();
            if (stepAuto) {
                try {
                    Thread.sleep(stepTime);
                } catch (InterruptedException e) {}
                checkKeys();
                step();
            } else
                checkKeys();
        }
    }

    /* RULES:
    1. Live cell with <2 neighbors dies
    2. Live cell with 2-3 neighbors lives
    3. Dead cell with 3 neighbors is born
    4. Live cell with >3 neighbors dies */
    public void step() {
        HashSet<Vector2> next = (HashSet<Vector2>) aliveCells.clone();
        HashSet<Vector2> deadChecked = new HashSet<Vector2>();

        Iterator<Vector2> iterator = aliveCells.iterator();
        while (iterator.hasNext()) {
            Vector2 pos = iterator.next();
            int value = getNeighbors(pos);

            if (value > 3 || value < 2)
                next.remove(pos);
            
            for (int y = pos.y-1; y < pos.y+2; y++) {
                for (int x = pos.x-1; x < pos.x+2; x++) {
                    Vector2 temp = new Vector2(x, y);
                    if (!(x == pos.x && y == pos.y) && !deadChecked.contains(temp) && getNeighbors(temp) == 3) {
                        next.add(temp);
                    }
                    
                    deadChecked.add(temp);
                }
            }
        }

        aliveCells = next;
        step++;
        drawBoard();
    }

    public int getNeighbors(Vector2 pos) {
        int output = 0;

        for (int y = pos.y-1; y < pos.y+2; y++) {
            for (int x = pos.x-1; x < pos.x+2; x++) {
                if (!(x == pos.x && y == pos.y) && aliveCells.contains(new Vector2(x, y)))
                    output++;
            }
        }

        return output;
    }

    public void print(Vector2 min, Vector2 max) {
        for (int y = (int) min.y; y < max.y; y++) {
            for (int x = (int) min.x; x < max.x; x++) {
                if (aliveCells.contains(new Vector2(x, y)))
                    System.out.print("O");
                else
                    System.out.print(" ");
            }

            System.out.println();
        }
    }

    public void drawBoard() {
        Vector2 max = screenPos.add(new Vector2(WIDTH*cellScreenLen, HEIGHT*cellScreenLen));
        List<Shape> shapes = new ArrayList<Shape>();

        Iterator<Vector2> iterator = aliveCells.iterator();

        while (iterator.hasNext()) {
            Vector2 point = iterator.next();

            if (point.x < screenPos.x || point.x > max.x || point.y < screenPos.y || point.y > max.y)
                continue;
        
            Vector2 drawPos = (point.sub(screenPos)).mul(cellScreenLen);
            
            shapes.add(new FillRect(drawPos.x, drawPos.y, (int) (cellScreenLen*0.95), (int) (cellScreenLen*0.95),
                0, Color.WHITE));
        }

        shapes.add(new Text("Steps: "+step, 5, HEIGHT-10, Color.WHITE, new Font("Cascadia Code", Font.BOLD, 24)));

        Shape[] output = new Shape[shapes.size()];
        for (int i = 0; i < output.length; i++)
            output[i] = shapes.get(i);

        draw(output);
    }

    private void checkKeys() {
        if (hasKey(binding.toggleAutoKey()) && keysActive.get(binding.toggleAutoKey())) {
            stepAuto = !stepAuto;
            startTimer(binding.toggleAutoKey());
        }

        if (hasKey(binding.quitKey()) && keysActive.get(binding.quitKey())) {
            run = false;
            startTimer(binding.quitKey());
        }
        
        if (!stepAuto && hasKey(binding.singleStepKey()) && keysActive.get(binding.singleStepKey())) {
            step();
            startTimer(binding.singleStepKey());
        }
    }

    private void initKeyHashes() {
        keysActive = new HashMap<String, Boolean>();
        cooldownTimers = new HashMap<String, Timer>();

        hashPutInit(binding.toggleAutoKey());
        hashPutInit(binding.singleStepKey());
        hashPutInit(binding.quitKey());
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
