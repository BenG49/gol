package gol;

import java.util.HashSet;

import gol.game.Board;
import gol.game.Vector2;

public class Main {
    public static void main(String[] args) {
        HashSet<Vector2> in = new HashSet<Vector2>();
        in.add(new Vector2(0, -1));
        in.add(new Vector2(0, 0));
        in.add(new Vector2(0, 1));
        Board b = new Board(in);

        b.print(new Vector2(-2, -2), new Vector2(2, 2));
        for (int i = 0; i < 50; i++)
            b.step();
    }
}
