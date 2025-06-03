package drawshapes;

import java.awt.Color;

/**
 * Utilities class containing methods to convert strings to colors, colors to
 * strings, write to files, and so on.
 *
 * This class contains static utility methods. It doesn't make sene to create
 * instances of this class.
 */
public class Util {

    // private constructor
    private Util() {
    }

    public static String colorToString(Color color) {
        // Compare RGB values instead of color objects
        if (color.getRGB() == Color.RED.getRGB()) {
            return "RED";
        } else if (color.getRGB() == Color.BLUE.getRGB()) {
            return "BLUE";
        } else if (color.getRGB() == Color.GREEN.getRGB()) {
            return "GREEN";
        } else if (color.getRGB() == Color.YELLOW.getRGB()) {
            return "YELLOW";
        } else if (color.getRGB() == Color.BLACK.getRGB()) {
            return "BLACK";
        }
        // If no match, return RED as default
        return "RED";  // Default color instead of throwing exception
    }

    public static Color stringToColor(String color) {
        switch (color.toUpperCase()) {
            case "RED":
                return Color.RED;
            case "BLUE":
                return Color.BLUE;
            case "GREEN":
                return Color.GREEN;
            case "YELLOW":
                return Color.YELLOW;
            case "BLACK":
                return Color.BLACK;
            default:
                return Color.RED;  // Default color
        }
    }
}
