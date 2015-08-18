package local.fractal.util;

/**
 * A {@code Point2D} represents the 2D point.
 *
 * @author Kochin Konstantin Alexandrovich
 */
final public class Point2D {
    /**
     * x coordinate
     */
    private double x;
    /**
     * y coordinate
     */
    private double y;

    /**
     * Create the point with coordinate (0,0).
     */
    public Point2D() {
        setX(0);
        setY(0);
    }

    /**
     * Create the point.
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
    public double getX() {
        return x;
    }

    /**
     * set x coordinate
     *
     * @param x coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Get y coordinate.
     *
     * @return y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Set y coordinate.
     *
     * @param y coordinate
     */
    public void setY(double y) {
        this.y = y;
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
