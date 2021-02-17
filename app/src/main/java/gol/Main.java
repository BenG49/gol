package gol;

import java.util.HashSet;

import gol.game.Board;
import gol.input.InputDisplay;

import com.stuypulse.stuylib.math.Vector2D;

public class Main {
    public static void main(String[] args) {
        InputDisplay d = new InputDisplay();

        HashSet<Vector2D> in = new HashSet<Vector2D>();
        in.add(new Vector2D(0, 0));
        Board b = new Board(in);

        b.step();
    }
}
