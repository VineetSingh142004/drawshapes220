package drawshapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

public class Rectangle extends AbstractShape {

    protected int width;
    protected int height;
    private double rotation = 0.0;  // Store rotation in degrees

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
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        // Apply rotation
        g2d.rotate(Math.toRadians(rotation), getAnchorPoint().x + width / 2.0, getAnchorPoint().y + height / 2.0);

        if (isSelected()) {
            g.setColor(color.darker());
        } else {
            g.setColor(getColor());
        }
        g.fillRect(getAnchorPoint().x, getAnchorPoint().y, width, height);

        // Restore original transform
        g2d.setTransform(oldTransform);
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
        updateBoundingBox();
    }

    public void setHeight(int height) {
        this.height = height;
        updateBoundingBox();
    }

    private void updateBoundingBox() {
        setBoundingBox(
                anchorPoint.x,
                anchorPoint.x + width,
                anchorPoint.y,
                anchorPoint.y + height
        );
    }

    // Add getter method for rotation
    public double getRotation() {
        return rotation;
    }

    // Add rotation method if not already present
    public void rotate(double degrees) {
        this.rotation = (this.rotation + degrees) % 360;
        if (this.rotation < 0) {
            this.rotation += 360;
        }
    }

    // Make sure these are added to the clone method
    @Override
    public Rectangle clone() {
        Rectangle cloned = new Rectangle(
                new Point(anchorPoint),
                width,
                height,
                new Color(color.getRGB())
        );
        cloned.setSelected(selected);
        cloned.rotation = this.rotation;  // Copy rotation value
        return cloned;
    }
}
