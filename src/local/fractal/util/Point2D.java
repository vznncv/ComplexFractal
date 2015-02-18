package local.fractal.util;

import org.apache.commons.math3.complex.Complex;

/**
 * A {@code Point2D} represent the 2D point.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class Point2D {
    /**
     * x coordinate
     */
    private double x;
    /**
     * y coordinate
     */
    private double y;

    /**
     * Construct point (0,0).
     */
    public Point2D() {
        setX(0);
        setY(0);
    }

    /**
     * Construct point.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public Point2D(double x, double y) {
        setX(x);
        setY(y);
    }

    /**
     * Get x coordinate.
     *
     * @return x coordinate
     */
    public final double getX() {
        return x;
    }

    /**
     * set x coordinate
     *
     * @param x coordinate
     */
    public final void setX(double x) {
        this.x = x;
    }

    /**
     * Get y coordinate.
     *
     * @return y coordinate
     */
    public final double getY() {
        return y;
    }

    /**
     * Set y coordinate.
     *
     * @param y coordinate
     */
    public final void setY(double y) {
        this.y = y;
    }

    /**
     * Return representation point as complex number.
     *
     * @return complex number
     */
    public final Complex toComplex() {
        return new Complex(getX(), getY());
    }

    /**
     * Return string representation of the {@code Point2D}.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "(" + getX() + ", " + getY() + ")";
    }

}
