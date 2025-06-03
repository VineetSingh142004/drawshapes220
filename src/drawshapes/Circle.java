package drawshapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class Circle extends AbstractShape {

    private int diameter;

    public Circle(Color color, Point center, int diameter) {
        super(new Point(center.x, center.y));
        setBoundingBox(center.x - diameter / 2, center.x + diameter / 2, center.y - diameter / 2, center.y + diameter / 2);
        this.color = color;
        this.diameter = diameter;
    }

    @Override
    public void draw(Graphics g) {
        if (isSelected()) {
            g.setColor(this.color.darker());
        } else {
            g.setColor(getColor());
        }
        g.fillOval((int) getAnchorPoint().getX() - diameter / 2,
                (int) getAnchorPoint().getY() - diameter / 2,
                diameter,
                diameter);
    }

    public String toString() {
        return String.format("CIRCLE %d %d %d %s %s",
                this.getAnchorPoint().x,
                this.getAnchorPoint().y,
                this.diameter,
                colorToString(this.getColor()),
                this.isSelected());
    }

    @Override
    public void setAnchorPoint(Point p) {
        // Calculate the offset
        int dx = p.x - this.anchorPoint.x;
        int dy = p.y - this.anchorPoint.y;

        // Update anchor point
        this.anchorPoint = p;

        // Update bounding box with the same offset
        setBoundingBox(
                boundingBox.getLeft() + dx,
                boundingBox.getRight() + dx,
                boundingBox.getTop() + dy,
                boundingBox.getBottom() + dy
        );
    }

}
