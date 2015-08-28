package local.fractal.model;

import local.fractal.util.Point2D;

import java.util.Objects;

/**
 * A {@code MandelbrotSet} class checks belonging of a point to Mandelbrot set.
 * The objects of this class are immutable.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public final class MandelbrotSet extends ComplexFractal {

    /**
     * Constructor.
     *
     * @param maxIter   maximum number of the iteration
     * @param criticalR radius of the bound
     */
    public MandelbrotSet(int maxIter, double criticalR) {
        super(maxIter, criticalR);
    }


    /**
     * Default constructor.
     * <p>
     * {@code matIter = 1024},
     * <p>
     * {@code criticalR = 2.0}
     */
    public MandelbrotSet() {
        super(1024, 2.0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int numberIter(Point2D p) {
        Objects.requireNonNull(p, "p is null");

        int maxIter = getMaxIter();
        // 1 - special case for point outside of the bound
        int iter = 1;
        double r2 = getCriticalR() * getCriticalR();
        ComplexNumber startP = new ComplexNumber(p.getX(), p.getY());
        ComplexNumber currentP = new ComplexNumber(0.0, 0.0);

        // test point
        while (iter < maxIter && currentP.squareAbs() < r2) {
            currentP.mulAndAsg(currentP).addAndAsg(startP);
            iter++;
        }
        if (currentP.squareAbs() < r2) {
            iter = 0;
        }
        return iter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof MandelbrotSet && super.equals(obj);
    }
}
