package local.fractal.model;

import local.fractal.util.Point2D;

import java.util.Objects;

/**
 * A {@code ComplexFractalVersion1} class checks belonging of a point to the fractal similar Mandelbrot set.
 * <p>
 * This fractal calculates as Mandelbrot set, but Mandelbrot set uses function: F(z) = z^2 + c, whereas this fractal
 * uses function: F(z) = z^n1 + z^n2 + c, where n1 and n2 integer parameters
 * <p>
 * The objects of this class are immutable.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public final class ComplexFractalVersion1 extends ComplexFractal {
    // parameters of the fractal
    private int n1;
    private int n2;

    /**
     * Constructor.
     *
     * @param n1        power n1
     * @param n2        power n2
     * @param maxIter   maximum number of the iteration
     * @param criticalR radius of the bound
     */
    public ComplexFractalVersion1(int n1, int n2, int maxIter, double criticalR) {
        super(maxIter, criticalR);
        setN1(n1);
        setN2(n2);
    }

    /**
     * Default constructor.
     * <p>
     * {@code F(z) = z^6 + z^1 + c}
     * <p>
     * {@code matIter = 1024},
     * <p>
     * {@code criticalR = 2.0}
     */
    public ComplexFractalVersion1() {
        this(6, 1, 1024, 2.0);
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
        ComplexNumber initP = new ComplexNumber(p.getX(), p.getY());
        ComplexNumber z = new ComplexNumber(0, 0);
        ComplexNumber powN1 = new ComplexNumber();
        ComplexNumber powN2 = new ComplexNumber();

        // test point
        z.assign(initP);
        while (iter < maxIter && z.squareAbs() < r2 && !Double.isNaN(z.getImag()) && !Double.isNaN(z.getReal())) {
            powN1.assign(z).powAndAsg(n1);
            powN2.assign(z).powAndAsg(n2);
            z.assign(initP).addAndAsg(powN1).addAndAsg(powN2);
            iter++;
        }
        if (z.squareAbs() < r2) {
            iter = 0;
        }
        return iter;
    }

    /**
     * Gets the power n1.
     *
     * @return power n1
     */
    public int getN1() {
        return n1;
    }

    /**
     * Sets the power n1.
     *
     * @param n1 power n1
     */
    private void setN1(int n1) {
        this.n1 = n1;
    }

    /**
     * Gets the power n2.
     *
     * @return power n2
     */
    public int getN2() {
        return n2;
    }

    /**
     * Sets the power n2.
     *
     * @param n2 power n2
     */
    private void setN2(int n2) {
        this.n2 = n2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComplexFractalVersion1))
            return false;
        ComplexFractalVersion1 rightArg = (ComplexFractalVersion1) obj;
        return super.equals(obj) && n1 == rightArg.n1 && n2 == rightArg.n2;
    }
}

