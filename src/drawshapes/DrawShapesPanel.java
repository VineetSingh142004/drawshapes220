package drawshapes;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * The Panel owned by the DrawShapes frame. This code doesn't need to be
 * changed.
 *
 * @author jspacco
 *
 */
@SuppressWarnings("serial")
public class DrawShapesPanel extends JPanel {

    private int width;
    private int height;
    private Scene scene;

    public DrawShapesPanel(int width, int height, Scene scene) {
        this.width = width;
        this.height = height;
        this.scene = scene;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        scene.draw(g);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getMinimumSize()
     */
    public Dimension getMinimumSize() {
        return new Dimension(width, height);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getMaximumSize()
     */
    public Dimension getMaximumSize() {
        return new Dimension(width, height);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#isFocusable()
     */
    public boolean isFocusable() {
        return true;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
