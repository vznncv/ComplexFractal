package local.fractal.util;

import javafx.scene.paint.Color;

/**
 * The interface {@code IterativePalette} must convert value getting from {@link local.fractal.model.ComplexFractalChecker#numberIter}
 * to {@link javafx.scene.paint.Color}.
 *
 * @author Kochin Konstantin Alexandrovich
 */
@FunctionalInterface
public interface IterativePalette {
    /**
     * Coverts number of the iteration to {@code Color}.
     * <p>
     * If number of the iteration is zero then it's point of the fractal else it isn't point of the fractal.
     *
     * @param numIter number of the iteration
     * @return color of the point
     */
    Color numIterToColor(int numIter);
}
