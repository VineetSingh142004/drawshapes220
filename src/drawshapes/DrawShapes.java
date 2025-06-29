package drawshapes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class DrawShapes extends JFrame {

    // Added two enums at the top for better organization
    public enum ShapeType {
        SQUARE, // basic square shape
        CIRCLE, // basic circle shape
        RECTANGLE // new shape I added for drawing rectangles
    }

    public enum OperationMode {
        DRAW, // default mode - for drawing new shapes
        MOVE, // lets me move shapes around
        RESIZE, // makes shapes bigger/smaller
        ROTATE  // new feature to rotate rectangles
    }

    // Main class variables - organized better
    private DrawShapesPanel shapePanel;
    private Scene scene;
    private ShapeType shapeType = ShapeType.SQUARE;  // default shape is square
    private Color color = Color.RED;                 // default color is red
    private Point startDrag;                         // for dragging shapes
    private OperationMode currentMode = OperationMode.DRAW;  // default mode is draw
    private Point lastDragPoint;                     // helps track mouse movement
    private Stack<Scene> undoStack = new Stack<>();  // for undo feature
    private Stack<Scene> redoStack = new Stack<>();  // for redo feature

    public DrawShapes(int width, int height) {
        // Set window title
        setTitle("Draw Shapes!");

        // Create new scene
        scene = new Scene();

        // Initialize first state for undo
        try {
            undoStack.push(scene.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        // Create canvas panel with specified size
        shapePanel = new DrawShapesPanel(width, height, scene);

        // Add panel to frame
        this.getContentPane().add(shapePanel, BorderLayout.CENTER);

        // Set window properties
        this.setResizable(false);
        this.setLocation(100, 100);

        // Important: Pack the frame to proper size
        this.pack();

        // Center window on screen
        this.setLocationRelativeTo(null);

        // Rest of your initialization code...
        initializeMouseListener();
        initializeKeyListener();
        initializeMenu();

        // Handle closing
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    /* Main mouse listener - handles all mouse operations
     * I added new features here:
     * - Mouse drag for moving shapes
     * - Mouse wheel for resizing and rotation
     * - Right click for selecting shapes
     */
    private void initializeMouseListener() {
        MouseAdapter a = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                System.out.printf("Mouse cliked at (%d, %d)\n", e.getX(), e.getY());

                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (currentMode == OperationMode.DRAW) {
                        // Deselect all shapes before creating a new one
                        deselectAllShapes();

                        // Create the shape first
                        IShape newShape = null;
                        if (shapeType == ShapeType.SQUARE) {
                            newShape = new Square(color, e.getX(), e.getY(), 100);
                        } else if (shapeType == ShapeType.CIRCLE) {
                            newShape = new Circle(color, e.getPoint(), 100);
                        } else if (shapeType == ShapeType.RECTANGLE) {
                            newShape = new Rectangle(e.getPoint(), 100, 200, color);
                        }

                        if (newShape != null) {
                            saveState(); // Save state before adding a new shape
                            scene.addShape(newShape);
                            repaint();
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    // apparently this is middle click
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // right-click for selection
                    Point p = e.getPoint();
                    System.out.printf("Right click is (%d, %d)\n", p.x, p.y);
                    List<IShape> selected = scene.select(p);
                    if (selected.size() > 0) {
                        for (IShape s : selected) {
                            s.setSelected(true);
                        }
                    } else {
                        deselectAllShapes();
                    }
                    System.out.printf("Select %d shapes\n", selected.size());
                    repaint();
                }
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
             */
            public void mousePressed(MouseEvent e) {
                System.out.printf("mouse pressed at (%d, %d)\n", e.getX(), e.getY());
                if (currentMode == OperationMode.MOVE) {
                    Point p = e.getPoint();
                    List<IShape> selectedShapes = scene.select(p);
                    if (selectedShapes.size() > 0) {
                        // Set these shapes as selected if they weren't already
                        for (IShape shape : selectedShapes) {
                            shape.setSelected(true);
                        }
                        lastDragPoint = p;
                    }
                } else {
                    // Original selection rectangle behavior
                    scene.startDrag(e.getPoint());
                }

            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
             */
            public void mouseReleased(MouseEvent e) {
                System.out.printf("mouse released at (%d, %d)\n", e.getX(), e.getY());
                lastDragPoint = null;
                scene.stopDrag();
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentMode == OperationMode.ROTATE && lastDragPoint != null) {
                    for (IShape shape : scene) {
                        if (shape.isSelected() && shape instanceof Rectangle) {
                            Rectangle rect = (Rectangle) shape;
                            Point center = rect.getAnchorPoint();

                            // Calculate angles from center to mouse positions
                            double lastAngle = Math.atan2(lastDragPoint.y - center.y, lastDragPoint.x - center.x);
                            double currentAngle = Math.atan2(e.getY() - center.y, e.getX() - center.x);

                            // Convert to degrees and get the difference
                            double deltaAngle = Math.toDegrees(currentAngle - lastAngle);

                            // Apply rotation
                            rect.rotate(deltaAngle);
                            saveState();
                            repaint();
                        }
                    }
                    lastDragPoint = e.getPoint();
                } else if (currentMode == OperationMode.MOVE && lastDragPoint != null) {
                    // Calculate movement delta
                    int dx = e.getX() - lastDragPoint.x;
                    int dy = e.getY() - lastDragPoint.y;

                    boolean anyShapeMoved = false;
                    for (IShape shape : scene) {
                        if (shape.isSelected()) {
                            anyShapeMoved = true;
                            Point currentAnchor = shape.getAnchorPoint();
                            shape.setAnchorPoint(new Point(
                                    currentAnchor.x + dx,
                                    currentAnchor.y + dy
                            ));
                        }
                    }

                    if (anyShapeMoved) {
                        saveState(); // Save state after moving shapes
                    }

                    lastDragPoint = e.getPoint();
                    repaint();
                } else {
                    // Original selection rectangle behavior
                    System.out.printf("mouse drag! (%d, %d)\n", e.getX(), e.getY());
                    scene.updateSelectRect(e.getPoint());
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (currentMode == OperationMode.RESIZE) {
                    saveState(); // Add this line
                    System.out.println("Resizing..."); // Debug output

                    // Make scaling more dramatic and inverse the direction
                    double scaleFactor = e.getWheelRotation() > 0 ? 0.8 : 1.2;

                    for (IShape shape : scene) {
                        if (shape.isSelected()) {
                            System.out.println("Found selected shape"); // Debug output

                            if (shape instanceof Rectangle) {
                                Rectangle rect = (Rectangle) shape;
                                if (shape instanceof Square) {
                                    int newSize = Math.max(20, (int) (rect.getWidth() * scaleFactor));
                                    ((Square) rect).setSize(newSize);
                                } else {
                                    int newWidth = Math.max(20, (int) (rect.getWidth() * scaleFactor));
                                    int newHeight = Math.max(20, (int) (rect.getHeight() * scaleFactor));
                                    rect.setWidth(newWidth);
                                    rect.setHeight(newHeight);
                                }
                            } else if (shape instanceof Circle) {
                                Circle circle = (Circle) shape;
                                int newRadius = Math.max(10, (int) (circle.getRadius() * scaleFactor));
                                circle.setRadius(newRadius);
                            }
                        }
                    }
                    repaint();
                } else if (currentMode == OperationMode.ROTATE) {
                    saveState();

                    // Rotate 15 degrees per wheel click
                    double rotationAmount = e.getWheelRotation() > 0 ? 15 : -15;

                    for (IShape shape : scene) {
                        if (shape.isSelected() && shape instanceof Rectangle) {
                            Rectangle rect = (Rectangle) shape;
                            rect.rotate(rotationAmount);
                        }
                    }
                    repaint();
                }
            }

        };

        // Add all required listeners
        shapePanel.addMouseListener(a);
        shapePanel.addMouseMotionListener(a);
        shapePanel.addMouseWheelListener(a); // Make sure this line is present
    }

    /* Menu initialization - I added many new menu items:
     * - New colors (green, yellow, black)
     * - Rectangle shape option
     * - Move/Resize/Rotate operations
     * - Save/Load file operations
     * - Undo/Redo/Clear operations
     */
    private void initializeMenu() {
        // menu bar
        JMenuBar menuBar = new JMenuBar();

        // file menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        // load
        JMenuItem loadItem = new JMenuItem("Load");
        fileMenu.add(loadItem);
        loadItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(".");
                int returnValue = jfc.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    try {
                        java.util.Scanner scanner = new java.util.Scanner(selectedFile);
                        // Clear existing shapes
                        scene = new Scene();

                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine().trim();
                            if (line.isEmpty()) {
                                continue;
                            }

                            String[] parts = line.split(" ");
                            String shapeType = parts[0];

                            switch (shapeType) {
                                case "SQUARE":
                                    int x = Integer.parseInt(parts[1]);
                                    int y = Integer.parseInt(parts[2]);
                                    int size = Integer.parseInt(parts[3]);
                                    Color color = Util.stringToColor(parts[4]);
                                    scene.addShape(new Square(color, x + size / 2, y + size / 2, size));
                                    break;
                                case "CIRCLE":
                                    x = Integer.parseInt(parts[1]);
                                    y = Integer.parseInt(parts[2]);
                                    int diameter = Integer.parseInt(parts[3]);
                                    color = Util.stringToColor(parts[4]);
                                    scene.addShape(new Circle(color, new Point(x + diameter / 2, y + diameter / 2), diameter));
                                    break;
                                case "RECTANGLE":
                                    x = Integer.parseInt(parts[1]);
                                    y = Integer.parseInt(parts[2]);
                                    int width = Integer.parseInt(parts[3]);
                                    int height = Integer.parseInt(parts[4]);
                                    color = Util.stringToColor(parts[5]);
                                    scene.addShape(new Rectangle(new Point(x + width / 2, y + height / 2), width, height, color));
                                    break;
                            }
                        }
                        scanner.close();
                        shapePanel.setScene(scene);
                        repaint();
                    } catch (Exception ex) {
                        System.err.println("Error loading file: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        });
        // save
        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(saveItem);
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(".");
                int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    try {
                        // If file doesn't end with .txt, add it
                        if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                            selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
                        }

                        java.io.PrintWriter writer = new java.io.PrintWriter(selectedFile);
                        writer.print(scene.toString());
                        writer.close();
                        System.out.println("Scene saved to " + selectedFile.getAbsolutePath());
                    } catch (Exception ex) {
                        System.err.println("Error saving file: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        });
        // undo
        JMenuItem undoItem = new JMenuItem("Undo");
        fileMenu.add(undoItem);
        undoItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });

        // redo
        JMenuItem redoItem = new JMenuItem("Redo");
        fileMenu.add(redoItem);
        redoItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });

        // clear canvas
        JMenuItem clearItem = new JMenuItem("Clear Canvas");
        fileMenu.add(clearItem);
        clearItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Save current state before clearing
                saveState();

                // Clear the scene
                scene = new Scene();
                shapePanel.setScene(scene);
                repaint();
            }
        });
        fileMenu.addSeparator();
        // edit
        JMenuItem itemExit = new JMenuItem("Exit");
        fileMenu.add(itemExit);
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                System.exit(0);
            }
        });

        // color menu
        JMenu colorMenu = new JMenu("Color");
        menuBar.add(colorMenu);

        // red color
        JMenuItem redColorItem = new JMenuItem("Red");
        colorMenu.add(redColorItem);
        redColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to red
                color = Color.RED;
            }
        });

        // blue color
        JMenuItem blueColorItem = new JMenuItem("Blue");
        colorMenu.add(blueColorItem);
        blueColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                color = Color.BLUE;
            }
        });

        // green color
        JMenuItem greenColorItem = new JMenuItem("Green");
        colorMenu.add(greenColorItem);
        greenColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                color = Color.GREEN;
            }
        });

        // yellow color
        JMenuItem yellowColorItem = new JMenuItem("Yellow");
        colorMenu.add(yellowColorItem);
        yellowColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                color = Color.YELLOW;
            }
        });

        // black color
        JMenuItem blackColorItem = new JMenuItem("Black");
        colorMenu.add(blackColorItem);
        blackColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = e.getActionCommand();
                System.out.println(text);
                color = Color.BLACK;
            }
        });

        // shape menu
        JMenu shapeMenu = new JMenu("Shape");
        menuBar.add(shapeMenu);

        // square
        JMenuItem squareItem = new JMenuItem("Square");
        shapeMenu.add(squareItem);
        squareItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Square");
                shapeType = ShapeType.SQUARE;
            }
        });

        // circle
        JMenuItem circleItem = new JMenuItem("Circle");
        shapeMenu.add(circleItem);
        circleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Circle");
                shapeType = ShapeType.CIRCLE;
            }
        });

        // rectangle
        JMenuItem rectangleItem = new JMenuItem("Rectangle");
        shapeMenu.add(rectangleItem);
        rectangleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Rectangle");
                shapeType = ShapeType.RECTANGLE;
            }
        });

        // operation mode menu
        JMenu operationModeMenu = new JMenu("Operation");
        menuBar.add(operationModeMenu);

        // draw option
        JMenuItem drawModeItem = new JMenuItem("Draw");
        operationModeMenu.add(drawModeItem);
        drawModeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Switching to draw mode");
                currentMode = OperationMode.DRAW;
                deselectAllShapes(); // Deselect all shapes when switching to draw mode
            }
        });

        // select option
        JMenuItem selectItem = new JMenuItem("Move");
        operationModeMenu.add(selectItem);
        selectItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Switching to move mode");
                currentMode = OperationMode.MOVE;
            }
        });

        // resize option
        JMenuItem resizeItem = new JMenuItem("Resize");
        operationModeMenu.add(resizeItem);
        resizeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Switching to resize mode");
                currentMode = OperationMode.RESIZE;
            }
        });

        // rotate option
        JMenuItem rotateItem = new JMenuItem("Rotate");
        operationModeMenu.add(rotateItem);
        rotateItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Switching to rotate mode");
                currentMode = OperationMode.ROTATE;
            }
        });

        // set the menu bar for this frame
        this.setJMenuBar(menuBar);
    }

    /**
     * Initialize the keyboard listener.
     */
    private void initializeKeyListener() {
        shapePanel.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                System.out.println("key typed: " + e.getKeyChar());
            }

            public void keyReleased(KeyEvent e) {
                // TODO: implement this method if you need it
            }

            public void keyTyped(KeyEvent e) {
                // TODO: implement this method if you need it
            }
        });
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        DrawShapes shapes = new DrawShapes(700, 600);
        shapes.setVisible(true);
    }

    // Helper function I made to deselect all shapes
    private void deselectAllShapes() {
        for (IShape s : scene) {
            s.setSelected(false);
        }
        repaint();
    }

    /* Save current state for undo/redo
     * I use this whenever something changes
     * Like when moving shapes or rotating them
     */
    private void saveState() {
        try {
            Scene currentState = scene.clone();
            if (undoStack.isEmpty() || !scenesEqual(currentState, undoStack.peek())) {
                undoStack.push(currentState);
                redoStack.clear();  // clear redo when new action happens
                while (undoStack.size() > 20) {  // keep only last 20 states
                    undoStack.remove(0);
                }
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    // Add these methods to the DrawShapes class
    private void undo() {
        if (!undoStack.isEmpty()) {
            try {
                redoStack.push(scene.clone());
                scene = undoStack.pop();
                shapePanel.setScene(scene);
                repaint();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            try {
                undoStack.push(scene.clone());
                scene = redoStack.pop();
                shapePanel.setScene(scene);
                repaint();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    // Add this helper method to compare scenes
    private boolean scenesEqual(Scene s1, Scene s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }

        Iterator<IShape> it1 = s1.iterator();
        Iterator<IShape> it2 = s2.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            IShape shape1 = it1.next();
            IShape shape2 = it2.next();

            if (!shapesEqual(shape1, shape2)) {
                return false;
            }
        }

        return !it1.hasNext() && !it2.hasNext();
    }

    private boolean shapesEqual(IShape s1, IShape s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        if (!s1.getClass().equals(s2.getClass())) {
            return false;
        }

        Point p1 = s1.getAnchorPoint();
        Point p2 = s2.getAnchorPoint();
        if (!p1.equals(p2)) {
            return false;
        }

        if (!s1.getColor().equals(s2.getColor())) {
            return false;
        }
        if (s1.isSelected() != s2.isSelected()) {
            return false;
        }

        if (s1 instanceof Square) {
            return ((Square) s1).getSize() == ((Square) s2).getSize();
        } else if (s1 instanceof Circle) {
            return ((Circle) s1).getRadius() == ((Circle) s2).getRadius();
        } else if (s1 instanceof Rectangle) {
            Rectangle r1 = (Rectangle) s1;
            Rectangle r2 = (Rectangle) s2;
            return r1.getWidth() == r2.getWidth()
                    && r1.getHeight() == r2.getHeight()
                    && r1.getRotation() == r2.getRotation();  // Add rotation check
        }
        return false;
    }
}
