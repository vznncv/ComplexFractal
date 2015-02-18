package local.fractal.model;


import org.apache.commons.math3.complex.Complex;

/**
 * A {@code MandelbrotSet} class checks belonging of point to Mandelbrot set.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class MandelbrotSet extends ComplexFractal {


    /**
     * Default constructor. It set maximum number of the iterations to 1024 and radius of the bound to 2;
     */
    public MandelbrotSet() {
        setMaxIter(1024);
        setCriticalR(2);
    }

    /**
     * Check that point belongs Mandelbrot set. If it's true then return 0, else number of iteration when point cross
     * bound (|z| >= rBound).
     *
     * @param z complex number
     * @return 0 if it's fractal point or number of the iteration
     */
    @Override
    public int numberIter(Complex z) {
        Complex zIter = Complex.ZERO;
        int maxIter = getMaxIter();
        int iter = 0;
        double r = getCriticalR();

        // test point
        while (iter < maxIter && zIter.abs() < r) {
            zIter = zIter.multiply(zIter).add(z);
            iter++;
        }
        if (zIter.abs() < r) {
            iter = 0;
        }

        return iter;
    }
}
