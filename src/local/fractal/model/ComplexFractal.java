package local.fractal.model;

import local.fractal.util.Point2D;

/**
 * A {@code ComplexFractal} is base class for Mandelbrot set and Julia set
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
     * Checks that point belongs the fractal. This method gets the coordinate of the point on plane and returns 0, if
     * it's point of the fractal, otherwise number of iterations when the point goes beyond the circle bound.
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
    public void setCriticalR(double criticalR) {
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
    public void setMaxIter(int maxIter) {
        if (maxIter <= 0) {
            throw new IllegalArgumentException("maxIter is less or equal zero.");
        }
        this.maxIter = maxIter;
    }

    /**
     * Helper class for complex calculation. This class doesn't check case with NaN in the real and image part of a
     * complex number. This class is mutable.
     */
    protected static final class ComplexNumber {
        // real part of the complex number
        private double real;
        // imaginary part of the complex number
        private double imag;

        /**
         * Constructor.
         *
         * @param real real part of the number
         * @param imag imaginary part of the number
         */
        public ComplexNumber(double real, double imag) {
            this.real = real;
            this.imag = imag;
        }

        /**
         * Default constructor for creating zero complex number.
         */
        public ComplexNumber() {
            this(0.0, 0.0);
        }

        /**
         * Adding.
         * <p>
         * {@code result = l + r}, where {@code l} - this object
         *
         * @param r right argument
         * @return {@code result}
         */
        public ComplexNumber add(ComplexNumber r) {
            return new ComplexNumber(real, imag).addAndAsg(r);
        }

        /**
         * Subtracting.
         * <p>
         * {@code result = l - r}, where {@code l} - this object
         *
         * @param r right argument
         * @return {@code result}
         */
        public ComplexNumber sub(ComplexNumber r) {
            return new ComplexNumber(real, imag).subAndAsg(r);
        }

        /**
         * Multiplying.
         * <p>
         * {@code result = l * r}, where {@code l} - this object
         *
         * @param r right argument
         * @return {@code result}
         */
        public ComplexNumber mul(ComplexNumber r) {
            return new ComplexNumber(real, imag).mulAndAsg(r);
        }

        /**
         * Dividing.
         * <p>
         * {@code result = l / r}, where {@code l} - this object
         *
         * @param r right argument
         * @return {@code result}
         */
        public ComplexNumber div(ComplexNumber r) {
            return new ComplexNumber(real, imag).divAndAsg(r);
        }


        /**
         * Adding and assigning.
         * <p>
         * {@code l = l + r}, where {@code l} - this object
         *
         * @param r right argument
         * @return this object
         */
        public ComplexNumber addAndAsg(ComplexNumber r) {
            real += r.real;
            imag += r.imag;
            return this;
        }

        /**
         * Subtracting and assigning.
         * <p>
         * {@code l = l - r}, where {@code l} - this object
         *
         * @param r right argument
         * @return this object
         */
        public ComplexNumber subAndAsg(ComplexNumber r) {
            real -= r.real;
            imag -= r.imag;
            return this;
        }

        /**
         * Multiplying and assigning.
         * <p>
         * {@code l = l * r}, where {@code l} - this object
         *
         * @param r right argument
         * @return this object
         */
        public ComplexNumber mulAndAsg(ComplexNumber r) {
            double rP = real * r.real - imag * r.imag;
            double iP = real * r.imag + imag * r.real;
            real = rP;
            imag = iP;
            return this;
        }

        /**
         * Dividing and assigning.
         * <p>
         * {@code l = l / r}, where {@code l} - this object
         *
         * @param r right argument
         * @return this object
         */
        public ComplexNumber divAndAsg(ComplexNumber r) {
            double d = r.squareAbs();
            double rP = (real * r.real + imag * r.imag) / d;
            double iP = (real * r.imag - imag * r.real) / d;
            real = rP;
            imag = iP;
            return this;
        }

        /**
         * Calculates square absolute value of this number.
         *
         * @return square absolute value
         */
        public double squareAbs() {
            return real * real + imag * imag;
        }

        /**
         * Calculates absolute value of this number.
         *
         * @return absolute value
         */
        public double abs() {
            return Math.sqrt(squareAbs());
        }

        /**
         * Gets real part.
         *
         * @return real part
         */
        public double getReal() {
            return real;
        }

        /**
         * Gets imaginary part.
         *
         * @return imaginary part
         */
        public double getImage() {
            return imag;
        }
    }
}
