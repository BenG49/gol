package gol;

import gol.game.Board;
import gol.game.Vector2Int;
import gol.game.schematic.Schematic;
import gol.game.schematic.Schematic.Pattern;

public class Main {

    public static void main(String[] args) {
        Board b = new Board(new Schematic(
            Schematic.getPattern(Pattern.GLIDER, 3),
            new Vector2Int(10, 10)
        ));
        b.run();
    }
}
