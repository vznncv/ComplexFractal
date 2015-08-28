package local.fractal.model;

import local.fractal.util.Point2D;

/**
 * A {@code ComplexFractalChecker} is interface for checking that a point belongs to the fractal.
 *
 * @author Kochin Konstantin Alexandrovich
 */
@FunctionalInterface
public interface ComplexFractalChecker {
    /**
     * Checks that point belongs the fractal. This method gets the coordinate of the point on complex plane and returns
     * 0, if it's point of the fractal, otherwise some positive number (typically it's a number of iteration when the
     * point goes beyond the some bound).
     *
     * @param p point
     * @return {@code 0} if the point belongs to the fractal, otherwise some positive number
     * @throws NullPointerException if {@code p} is {@code null}
     */
    int numberIter(Point2D p);
}
