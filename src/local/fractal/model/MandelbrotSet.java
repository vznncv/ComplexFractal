package local.fractal.model;

import local.fractal.util.Point2D;

import java.util.Objects;

/**
 * A {@code MandelbrotSet} class checks belonging of a point to Mandelbrot set.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class MandelbrotSet extends ComplexFractal {


    /**
     * Default constructor. It sets maximum number of the iterations in {@code 1024} and radius of the bound in {@code
     * 2}.
     */
    public MandelbrotSet() {
        setMaxIter(1024);
        setCriticalR(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int numberIter(Point2D p) {
        Objects.requireNonNull(p, "p is null");

        int maxIter = getMaxIter();
        int iter = 0;
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
}
