package drawshapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class Rectangle extends AbstractShape {

    protected int width;
    protected int height;

    public Rectangle(Point clicked, int width, int height, Color color) {
        super(new Point(clicked.x - width / 2, clicked.y - height / 2));
        setBoundingBox(clicked.x - width / 2, clicked.x + width / 2, clicked.y - height / 2, clicked.y + height / 2);
        this.color = color;
        this.width = width;
        this.height = height;
    }

    public Rectangle(int left, int right, int top, int bottom) {
        super(new Point(left, top));
        setBoundingBox(left, right, top, bottom);
        this.color = Color.BLUE;
        this.width = right - left;
        this.height = bottom - top;
    }

    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#draw(java.awt.Graphics)
     */
    @Override
    public void draw(Graphics g) {
        if (isSelected()) {
            g.setColor(color.darker());
        } else {
            g.setColor(getColor());
        }
        g.fillRect(getAnchorPoint().x, getAnchorPoint().y, width, height);
    }

    public String toString() {
        return String.format("RECTANGLE %d %d %d %d %s %s",
                getAnchorPoint().x,
                getAnchorPoint().y,
                width,
                height,
                colorToString(getColor()),
                selected);
    }


    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#setAnchorPoint(java.awt.Point)
     */
    @Override
    public void setAnchorPoint(Point p) {
        // Calculate the offset from old position to new
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
