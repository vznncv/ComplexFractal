package local.fractal.model;


import org.apache.commons.math3.complex.Complex;

/**
 * A {@code ComplexFractalChecker} is functional interface for checking this is point of fractal.
 *
 * @author Kochin Konstantin Alexandrovich
 */
@FunctionalInterface
public interface ComplexFractalChecker {
    /**
     * Method {@code numberIter} gets the complex number and returned 0, if it's point of the fractal or number of
     * iteration when the point crosses some bound.
     *
     * @param z complex number
     * @return 0 if it's fractal point or positive number in other case
     */
    int numberIter(Complex z);
}
