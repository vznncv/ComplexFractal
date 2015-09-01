package local.complexfractal.model;

import local.complexfractal.util.Point2D;

/**
 * A {@code ComplexFractal} is base class for the fractals.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public abstract class ComplexFractal implements ComplexFractalChecker {
    /**
     * Radius of the bound. Bound has circle form.
     */
    private double criticalR;
    /**
     * Maximum number of the iteration, after that if point remains in circle, it includes in the fractal set.
     */
    private int maxIter;

    /**
     * Constructor.
     *
     * @param maxIter   maximum number of the iteration
     * @param criticalR radius of the bound
     */
    protected ComplexFractal(int maxIter, double criticalR) {
        setMaxIter(maxIter);
        setCriticalR(criticalR);
    }

    /**
     * Default constructor.
     * <p>
     * It sets maximum number of the iterations in {@code 1024} and radius of the bound in {@code 2.0}.
     */
    protected ComplexFractal() {
        this(1024, 2.0);
    }

    /**
     * Checks that point belongs the fractal. This method gets the coordinate of the point on complex plane and returns
     * 0, if it's point of the fractal, otherwise number of iterations when the point goes beyond the circle bound.
     *
     * @param p point
     * @return {@code 0} if the point belongs to the fractal, otherwise number of iterations
     * @throws NullPointerException if {@code p} is {@code null}
     */
    @Override
    abstract public int numberIter(Point2D p);

    /**
     * Gets radius of the bound.
     *
     * @return radius
     */
    public double getCriticalR() {
        return criticalR;
    }

    /**
     * Sets radius of the bound.
     *
     * @param criticalR radius
     */
    private void setCriticalR(double criticalR) {
        if (criticalR <= 0) {
            throw new IllegalArgumentException("criticalR is less or equal zero.");
        }
        this.criticalR = criticalR;
    }

    /**
     * Gets maximum number of the iterations.
     *
     * @return maximum number of the iterations
     */
    public int getMaxIter() {
        return maxIter;
    }

    /**
     * Sets maximum number of the iterations.
     *
     * @param maxIter maximum number of the iterations
     */
    private void setMaxIter(int maxIter) {
        if (maxIter <= 0) {
            throw new IllegalArgumentException("maxIter is less or equal zero.");
        }
        this.maxIter = maxIter;
    }

    /**
     * Tests that fractal checkers are equal.
     *
     * @param obj object for compare
     * @return true, if objects are equal, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComplexFractal)) {
            return false;
        }
        ComplexFractal rightArg = (ComplexFractal) obj;
        return criticalR == rightArg.criticalR && maxIter == rightArg.maxIter;
    }

}
