package gol.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import com.stuypulse.stuylib.math.Vector2D;

import edu.wpi.first.wpiutil.math.Vector;

public class Board {

    private HashSet<Vector2D> aliveCells;

    public Board (HashSet<Vector2D> aliveCells) {
        this.aliveCells = aliveCells;
    }

    /* RULES:
    1. Live cell with <2 neighbors dies
    2. Live cell with 2-3 neighbors lives
    3. Dead cell with 3 neighbors is born
    4. Live cell with >4 neighbors dies */
    public void step() {
        HashSet<Vector2D> next = aliveCells;
        List<List<Vector2D>> commonDistCells = new ArrayList<List<Vector2D>>();

        Iterator<Vector2D> pos = aliveCells.iterator();
        while (pos.hasNext()) {
            Vector2D temp = pos.next();
            int value = getNeighbors(temp);

            if (value > 4 || value < 2)
                next.remove(temp);
            
            Iterator<Vector2D> nestedPos = aliveCells.iterator();
            while(nestedPos.hasNext()) {
                // check withinCommonDist() on temp, if list size is already 2 check common dist for other members
            }
            // n*(n - 1)/2
        }

        aliveCells = next;
    }

    private int getNeighbors(Vector2D pos) {
        int output = 0;

        for (int y = (int)pos.y-1; y < pos.y+2; y++) {
            for (int x = (int)pos.x-1; x < pos.x+2; x++) {
                if (aliveCells.contains(new Vector2D(x, y)))
                    output++;
            }
        }

        return output;
    }

    private boolean withinCommonDist(Vector2D a, Vector2D b) {
        return Math.abs(a.x-b.x) < 3 && Math.abs(a.y-b.y) < 3;
    }

    private List<Vector2D> getCommonCells(Vector2D a, Vector2D b) {
        // if (!withinCommonDist(a, b))
        //     return new ArrayList<Vector2D>();

        List<Vector2D> output = new ArrayList<Vector2D>();

        for (int y = (int)a.y-1; y < (int)a.y+2; y++) {
            for (int x = (int)a.x-1; x < (int)a.x+2; x++) {
                if (!(x == 0 && y == 0) && Math.abs(x-b.x) < 2 && Math.abs(y-b.y) < 2)
                    output.add(new Vector2D(x, y));
            }
        }

        return output;
    }
}
