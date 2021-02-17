package gol;

import gol.game.Board;
import gol.game.schematic.Schematic;
import gol.game.schematic.Schematic.Pattern;

public class Main {

    public static void main(String[] args) {
        Board b = new Board(Schematic.getPattern(Pattern.GLIDER));
        b.run();
    }
}
