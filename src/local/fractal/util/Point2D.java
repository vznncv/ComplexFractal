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
     * Creates the point with coordinate (0,0).
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
     * Gets x coordinate.
     *
     * @return x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Sets x coordinate.
     *
     * @param x coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Gets y coordinate.
     *
     * @return y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Sets y coordinate.
     *
     * @param y coordinate
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns string representation of the {@code Point2D}.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "(" + getX() + ", " + getY() + ")";
    }
}
