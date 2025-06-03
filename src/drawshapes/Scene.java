package drawshapes;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Color;  // Add this import
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A scene of shapes. Uses the Model-View-Controller (MVC) design pattern,
 * though note that model knows something about the view, as the draw() method
 * both in Scene and in Shape uses the Graphics object. That's kind of sloppy,
 * but it also helps keep things simple.
 *
 * This class allows us to talk about a "scene" of shapes, rather than
 * individual shapes, and to apply operations to collections of shapes.
 *
 * @author jspacco
 *
 */
public class Scene implements Iterable<IShape>, Cloneable {

    private List<IShape> shapeList = new LinkedList<IShape>();
    private SelectionRectangle selectRect;
    private boolean isDrag;
    private Point startDrag;

    public void updateSelectRect(Point drag) {
        for (IShape s : this) {
            s.setSelected(false);
        }
        if (drag.x > startDrag.x) {
            if (drag.y > startDrag.y) {
                // top-left to bottom-right
                selectRect = new SelectionRectangle(startDrag.x, drag.x, startDrag.y, drag.y);
            } else {
                // bottom-left to top-right
                selectRect = new SelectionRectangle(startDrag.x, drag.x, drag.y, startDrag.y);
            }
        } else {
            if (drag.y > startDrag.y) {
                // top-right to bottom-left
                selectRect = new SelectionRectangle(drag.x, startDrag.x, startDrag.y, drag.y);
            } else {
                // bottom-left to top-right
                selectRect = new SelectionRectangle(drag.x, startDrag.x, drag.y, startDrag.y);
            }
        }
        List<IShape> selectedShapes = this.select(selectRect);
        for (IShape s : selectedShapes) {
            s.setSelected(true);
        }
    }

    public void stopDrag() {
        this.isDrag = false;
    }

    public void startDrag(Point p) {
        this.isDrag = true;
        this.startDrag = p;
    }

    /**
     * Draw all the shapes in the scene using the given Graphics object.
     *
     * @param g
     */
    public void draw(Graphics g) {
        for (IShape s : shapeList) {
            if (s != null) {
                s.draw(g);
            }
        }
        if (isDrag) {
            selectRect.draw(g);
        }
    }

    /**
     * Get an iterator that can iterate through all the shapes in the scene.
     */
    public Iterator<IShape> iterator() {
        return shapeList.iterator();
    }

    /**
     * Return a list of shapes that contain the given point.
     *
     * @param point The point
     * @return A list of shapes that contain the given point.
     */
    public List<IShape> select(Point point) {
        List<IShape> selected = new LinkedList<IShape>();
        for (IShape s : shapeList) {
            if (s.contains(point)) {
                selected.add(s);
            }
        }
        return selected;
    }

    /**
     * Return a list of shapes in the scene that intersect the given shape.
     *
     * @param s The shape
     * @return A list of shapes intersecting the given shape.
     */
    public List<IShape> select(IShape shape) {
        List<IShape> selected = new LinkedList<IShape>();
        for (IShape s : shapeList) {
            if (s.intersects(shape)) {
                selected.add(s);
            }
        }
        return selected;
    }

    /**
     * Add a shape to the scene. It will be rendered next time the draw() method
     * is invoked.
     *
     * @param s
     */
    public void addShape(IShape s) {
        shapeList.add(s);
    }

    /**
     * Remove a list of shapes from the given scene.
     *
     * @param shapesToRemove
     */
    public void removeShapes(Collection<IShape> shapesToRemove) {
        shapeList.removeAll(shapesToRemove);
    }

    @Override
    public String toString() {
        String shapeText = "";
        for (IShape s : shapeList) {
            shapeText += s.toString() + "\n";
        }
        return shapeText;
    }

    @Override
    public Scene clone() throws CloneNotSupportedException {
        Scene cloned = (Scene) super.clone();
        cloned.shapeList = new LinkedList<>();

        for (IShape shape : this.shapeList) {
            if (shape instanceof Square) {
                Square sq = (Square) shape;
                Point anchorPoint = new Point(sq.getAnchorPoint()); // Create new Point object
                Square newSquare = new Square(
                        new Color(sq.getColor().getRGB()),
                        anchorPoint.x,
                        anchorPoint.y,
                        sq.getSize()
                );
                newSquare.setSelected(sq.isSelected());
                cloned.shapeList.add(newSquare);
            } else if (shape instanceof Circle) {
                Circle c = (Circle) shape;
                Point anchorPoint = new Point(c.getAnchorPoint()); // Create new Point object
                Circle newCircle = new Circle(
                        new Color(c.getColor().getRGB()),
                        anchorPoint,
                        c.getRadius() * 2
                );
                newCircle.setSelected(c.isSelected());
                cloned.shapeList.add(newCircle);
            } else if (shape instanceof Rectangle && !(shape instanceof Square)) {
                Rectangle r = (Rectangle) shape;
                Point anchorPoint = new Point(r.getAnchorPoint()); // Create new Point object
                Rectangle newRect = new Rectangle(
                        anchorPoint,
                        r.getWidth(),
                        r.getHeight(),
                        new Color(r.getColor().getRGB())
                );
                newRect.setSelected(r.isSelected());
                cloned.shapeList.add(newRect);
            }
        }

        // Clone drag state
        if (this.startDrag != null) {
            cloned.startDrag = new Point(this.startDrag);
        }
        cloned.isDrag = this.isDrag;

        return cloned;
    }
}
