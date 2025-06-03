package drawshapes;

import java.awt.Color;
import java.awt.Point;

public class Square extends Rectangle {

    public Square(Color color, int centerX, int centerY, int length) {
        super(new Point(centerX, centerY), length, length, color);
    }

    public String toString() {
        return String.format("SQUARE %d %d %d %s %s",
                getAnchorPoint().x,
                getAnchorPoint().y,
                width,
                Util.colorToString(getColor()),
                selected);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.width = size;
        this.height = size;
        updateBoundingBox();
    }

    @Override
    public void updateBoundingBox() {
        setBoundingBox(
                anchorPoint.x,
                anchorPoint.x + width,
                anchorPoint.y,
                anchorPoint.y + height
        );
    }
}
