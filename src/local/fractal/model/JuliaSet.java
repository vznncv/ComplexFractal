package local.fractal.model;

import local.fractal.util.Point2D;

import java.util.Objects;

/**
 * A {@code JuliaSet} class checks belonging of a point to Julia set with the function:
 * <p>
 * {@code F(z) = z^2 + z * c1 + c2},
 * <p>
 * where: {@code c1} and  {@code c2} parameters ( @code c1} and  {@code c2} are complex number).
 * <p>
 * The objects of this class are immutable.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public final class JuliaSet extends ComplexFractal {
    private ComplexNumber c1;
    private ComplexNumber c2;

    /**
     * Constructor.
     *
     * @param c1        coefficient c1
     * @param c2        coefficient c2
     * @param maxIter   maximum number of the iteration
     * @param criticalR radius of the bound
     */
    public JuliaSet(ComplexNumber c1, ComplexNumber c2, int maxIter, double criticalR) {
        super(maxIter, criticalR);
        setC1(c1);
        setC2(c2);
    }

    /**
     * Default constructor.
     * <p>
     * {@code F(z) = z^2 + z * 0 - 0.8 + 0.2i}
     * <p>
     * matIter = 1024,
     * <p>
     * criticalR = 2.0
     */
    public JuliaSet() {
        this(new ComplexNumber(0.0, 0.0), new ComplexNumber(-0.8, 0.2), 1024, 2.0);
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
        ComplexNumber z = new ComplexNumber(p.getX(), p.getY());
        ComplexNumber zPow2 = new ComplexNumber();
        ComplexNumber zPow1 = new ComplexNumber();

        // test point
        while (iter < maxIter && z.squareAbs() < r2) {
            zPow2.assign(z).mulAndAsg(z);
            zPow1.assign(z).mulAndAsg(c1);
            z.assign(c2).addAndAsg(zPow1).addAndAsg(zPow2);
            iter++;
        }
        if (z.squareAbs() < r2) {
            iter = 0;
        }
        return iter;
    }

    /**
     * Gets copy of the coefficient c1.
     *
     * @return c1 copy of the coefficient
     */
    public ComplexNumber getC1() {
        return c1.copy();
    }

    /**
     * Sets copy of the coefficient c1.
     *
     * @param c1 coefficient
     * @throws NullPointerException if c1 is null
     */
    private void setC1(ComplexNumber c1) {
        this.c1 = Objects.requireNonNull(c1).copy();
    }

    /**
     * Gets copy of the coefficient c2.
     *
     * @return copy of the coefficient
     */
    public ComplexNumber getC2() {
        return c2.copy();
    }

    /**
     * Sets copy of the coefficient c2.
     *
     * @param c2 coefficient
     * @throws NullPointerException if c2 is null
     */
    private void setC2(ComplexNumber c2) {
        this.c2 = Objects.requireNonNull(c2).copy();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JuliaSet))
            return false;
        JuliaSet rightArg = (JuliaSet) obj;
        return super.equals(obj) && c1.equals(rightArg.c1) && c2.equals(rightArg.c2);
    }
}
