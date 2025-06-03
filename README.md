# DrawShapes

## Basic shape drawing program for Java

## Class Hierarchy Explanation

The shape hierarchy starts with `IShape` interface at the top, which defines core methods like `draw()` and `isSelected()`. Then `AbstractShape` class implements basic functionality shared by all shapes. Individual shapes like `Square`, `Circle`, and `Rectangle` extend from `AbstractShape`. I made `Square` extend from `Rectangle` since a square is just a special rectangle with equal sides.

## How Shape Selection Works

When you click and drag, the program creates an invisible selection rectangle. It tracks mouse movement to update this rectangle's size. Every shape in the scene is checked to see if it intersects with this selection rectangle. If a shape intersects, it gets marked as "selected" by setting its selected flag to true, which makes it appear darker.

## My Journey Adding Features

I started by adding basic rectangle support since we only had squares and circles. This was pretty straightforward - I just created a `Rectangle` class that could have different width and height.

Then I wanted to make shapes more interactive, so I added:

1. Moving shapes by dragging them (this was tricky with rotation)
2. Resizing with mouse wheel (had to add minimum sizes to prevent tiny shapes)
3. Rotation for rectangles (the math for this gave me headaches)

The biggest challenge was rotation. I had to:

- Use Graphics2D for proper rotation around shape center
- Convert between degrees and radians (kept forgetting this!)
- Fix weird behavior when rotation went negative
- Make sure shapes still selected correctly when rotated

For undo/redo, I used two Stacks to store scene states. This worked well but I had to limit stack size to prevent memory issues.

Finally added save/load using simple text files. Each shape's properties get saved in a readable format like:

```
RECTANGLE 100 200 50 30 RED 45.0
```

Problems I faced:

- Shapes disappeared when rotated (forgot to reset Graphics2D transform)
- Memory leaks from unlimited undo stack (fixed with max size)
- Weird behavior when resizing too small (added minimum sizes)
- Selection not working on rotated shapes (had to update hit detection)


## Core Features Added

✓ Save scene to text file  
✓ Load scene from file  
✓ Undo/Redo support  
✓ Shape rotation  
✓ Multiple colors  
✓ Move and resize shapes
