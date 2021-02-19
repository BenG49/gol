package gol.display;

import javax.swing.JFrame;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import gol.display.shapes.Shape;

public class Display extends JFrame {
    public final int WIDTH;
    public final int HEIGHT;

    private Color background;

    protected Draw currentDraw;

    public Display() { this(500, 500, Color.WHITE); }
    public Display(Color background) { this(500, 500, background); }
    public Display(int width, int height, Color background) {
        super("GOL");

        WIDTH = width;
        HEIGHT = height;
        this.background = background;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);

        getContentPane().setBackground(background);
    }

    public void draw(Shape[] shapes) {
        draw(Arrays.asList(shapes));
    }

    public void draw(List<Shape> shapes) {
        try {
            remove(currentDraw);
        // using general exception to catch NullPointer and "AWT-EventQueue-0" errors
        } catch(Exception e) {}

        Draw d = new Draw(shapes, background);
        currentDraw = d;
       
        add(d);
        revalidate();
    }

    public void setInternalBackground(Color background) {
        this.background = background;

        getContentPane().setBackground(background);
    }
}