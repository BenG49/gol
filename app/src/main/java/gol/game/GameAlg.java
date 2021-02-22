package gol.game;

import java.util.HashSet;
import java.util.Iterator;

import gol.game.schematic.Schematic;
import gol.util.Vector2Int;

public class GameAlg {

    private HashSet<Vector2Int> aliveCells;
    private HashSet<Vector2Int> step0Cells;
    private HashSet<Schematic> schematics;
    private int stepCount;

    public GameAlg(HashSet<Vector2Int> aliveCells) {
        this.aliveCells = aliveCells;

        stepCount = 0;
        step0Cells = new HashSet<Vector2Int>();
    }

    /* RULES:
    1. Live cell with <2 neighbors dies
    2. Live cell with 2-3 neighbors lives
    3. Dead cell with 3 neighbors is born
    4. Live cell with >3 neighbors dies */
    public void step() {
        HashSet<Vector2Int> next = new HashSet<Vector2Int>(aliveCells);
        HashSet<Vector2Int> deadChecked = new HashSet<Vector2Int>();

        Iterator<Vector2Int> iterator = aliveCells.iterator();
        while (iterator.hasNext()) {
            Vector2Int pos = iterator.next();
            int value = getNeighbors(pos);

            if (value > 3 || value < 2)
                next.remove(pos);
            
            for (int y = pos.y-1; y < pos.y+2; y++) {
                for (int x = pos.x-1; x < pos.x+2; x++) {
                    Vector2Int temp = new Vector2Int(x, y);
                    if (deadChecked.contains(temp) || (x == pos.x && y == pos.y))
                        continue;

                    deadChecked.add(temp);

                    if (getNeighbors(temp) == 3)
                        next.add(temp);
                }
            }
        }

        if (stepCount == 0 && !step0Cells.equals(aliveCells))
            step0Cells = new HashSet<Vector2Int>(aliveCells);
        aliveCells = next;
        stepCount++;
    }

    public int getStepCount() {
        return stepCount;
    }

    public Iterator<Vector2Int> getIterator() {
        return aliveCells.iterator();
    }

    private int getNeighbors(Vector2Int pos) {
        int output = 0;

        for (int y = pos.y-1; y < pos.y+2; y++) {
            for (int x = pos.x-1; x < pos.x+2; x++) {
                if (!(x == pos.x && y == pos.y) && aliveCells.contains(new Vector2Int(x, y)))
                    output++;
            }
        }

        return output;
    }

    public void clearCells() {
        aliveCells = new HashSet<Vector2Int>();
    }

    public void resetSteps() {
        stepCount = 0;
        aliveCells = (HashSet<Vector2Int>) step0Cells.clone();
    }

    public void addCell(Vector2Int pos) {
        aliveCells.add(pos);
    }

    public void removeCell(Vector2Int pos) {
        aliveCells.remove(pos);
    }

    public boolean hasCell(Vector2Int pos) {
        return aliveCells.contains(pos);
    }

    public void addSchematic(Schematic schem) {
        schematics.add(schem);

        for (Vector2Int pos : schem.getData())
            aliveCells.add(pos);
    }

}
