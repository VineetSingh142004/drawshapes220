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
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class DrawShapes extends JFrame {

    public enum ShapeType {
        SQUARE,
        CIRCLE,
        RECTANGLE
    }

    public enum OperationMode {
        DRAW,
        MOVE,
        RESIZE
    }

    private DrawShapesPanel shapePanel;
    private Scene scene;
    private ShapeType shapeType = ShapeType.SQUARE;
    private Color color = Color.RED;
    private Point startDrag;
    private OperationMode currentMode = OperationMode.DRAW;
    private Point lastDragPoint;

    public DrawShapes(int width, int height) {
        setTitle("Draw Shapes!");
        scene = new Scene();

        // create our canvas, add to this frame's content pane
        shapePanel = new DrawShapesPanel(width, height, scene);
        this.getContentPane().add(shapePanel, BorderLayout.CENTER);
        this.setResizable(false);
        this.pack();
        this.setLocation(100, 100);

        // Add key and mouse listeners to our canvas
        initializeMouseListener();
        initializeKeyListener();

        // initialize the menu options
        initializeMenu();

        // Handle closing the window.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private void initializeMouseListener() {
        MouseAdapter a = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                System.out.printf("Mouse cliked at (%d, %d)\n", e.getX(), e.getY());

                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (currentMode == OperationMode.DRAW) {
                        // Deselect all shapes before creating a new one
                        deselectAllShapes();

                        if (shapeType == ShapeType.SQUARE) {
                            scene.addShape(new Square(color,
                                    e.getX(),
                                    e.getY(),
                                    100));
                        } else if (shapeType == ShapeType.CIRCLE) {
                            scene.addShape(new Circle(color,
                                    e.getPoint(),
                                    100));
                        } else if (shapeType == ShapeType.RECTANGLE) {
                            scene.addShape(new Rectangle(
                                    e.getPoint(),
                                    100,
                                    200,
                                    color));
                        }
                        repaint();
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
                if (currentMode == OperationMode.MOVE && lastDragPoint != null) {
                    // Calculate movement delta
                    int dx = e.getX() - lastDragPoint.x;
                    int dy = e.getY() - lastDragPoint.y;

                    // Move all selected shapes
                    for (IShape shape : scene) {
                        if (shape.isSelected()) {
                            Point currentAnchor = shape.getAnchorPoint();
                            shape.setAnchorPoint(new Point(
                                    currentAnchor.x + dx,
                                    currentAnchor.y + dy
                            ));
                        }
                    }

                    // Update last drag point
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
                }
            }

        };

        // Add all required listeners
        shapePanel.addMouseListener(a);
        shapePanel.addMouseMotionListener(a);
        shapePanel.addMouseWheelListener(a); // Make sure this line is present
    }

    /**
     * Initialize the menu options
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

    // Add this method to the DrawShapes class
    private void deselectAllShapes() {
        for (IShape s : scene) {
            s.setSelected(false);
        }
        repaint();
    }
}
