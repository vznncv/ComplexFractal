package local.fractal.model;

import org.apache.commons.math3.complex.Complex;

/**
 * It's helper for Mandelbrot set and Julia set
 *
 * @author Kochin Konstantin Alexandrovich
 */
public abstract class ComplexFractal implements ComplexFractalChecker {
    /**
     * The bound is circle.
     * It's radius of this circle.
     */
    private double criticalR;
    /**
     * Maximum number of the iteration, after that if point remain in circle includes in the fractal set.
     */
    private int maxIter;

    /**
     * Calculate value of the complex polynomial {@code poly} in the point {@code x}.
     *
     * @param poly polynomial
     * @param x    point
     * @return value of the complex polynomial {@code poly} in the point {@code x}
     */
    protected static Complex polyEval(Complex[] poly, Complex x) {
        Complex xPow = Complex.ONE;
        Complex res = Complex.ZERO;
        for (int pow = 0; pow < poly.length; pow++) {
            res = res.add(poly[pow].multiply(xPow));
            xPow = xPow.multiply(x);
        }
        return res;
    }

    /**
     * Get radius of the bound.
     *
     * @return radius of the bound
     */
    public double getCriticalR() {
        return criticalR;
    }

    /**
     * Set radius of the bound.
     *
     * @param criticalR radius
     */
    public void setCriticalR(double criticalR) {
        if (criticalR <= 0) {
            throw new IllegalArgumentException("criticalR is less or equal zero.");
        }
        this.criticalR = criticalR;
    }

    /**
     * Get maximum number of the iterations.
     *
     * @return maximum number of the iterations
     */
    public int getMaxIter() {
        return maxIter;
    }

    /**
     * Set maximum number of the iterations.
     *
     * @param maxIter maximum number of the iterations
     */
    public void setMaxIter(int maxIter) {
        if (maxIter <= 0) {
            throw new IllegalArgumentException("maxIter is less or equal zero.");
        }
        this.maxIter = maxIter;
    }
}
