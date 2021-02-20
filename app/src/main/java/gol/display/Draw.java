package gol.display;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import gol.display.shapes.Shape;

public class Draw extends JPanel {
    private final List<Shape> shapes;
    
    public Draw(Shape[] shapes, Color background) {
        this(Arrays.asList(shapes), background);
    }

    public Draw(List<Shape> shapes, Color background) {
        this.shapes = shapes;

        setPreferredSize(new Dimension(1, 1));
        setBackground(background);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        for (Shape i : shapes) {
            g2 = i.draw(g2);
        }
    }
}
