package local.fractal.util;

import javafx.scene.paint.Color;

/**
 * Interface for palette.
 *
 * @author Kochin Konstantin Alexandrovich
 */
@FunctionalInterface
public interface IterativePalette {
    /**
     * Covert number of the iteration to Color.
     * <p>
     * If number of the iteration is zero then it's point of the fractal else it isn't point of the fractal.
     *
     * @param numIter number of the iteration
     * @return color of the point
     */
    Color numIterToColor(int numIter);
}
