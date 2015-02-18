package local.fractal.util;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Objects;

/**
 * The object of the class {@code Point2DTransformer} encapsulates
 * a 2-D affine geometric transformation.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class Point2DTransformer {
    /**
     * 3-by-3 matrix of the transformation
     */

    private RealMatrix transMat;

    /**
     * Default constructor.
     */
    public Point2DTransformer() {
        clear();
    }

    /**
     * Copy constructor.
     */
    public Point2DTransformer(Point2DTransformer obj) {
        Objects.requireNonNull(obj, "obj is null");
        transMat = obj.transMat.copy();
    }

    /**
     * Clear all transform.
     */
    public void clear() {
        // set tMat to identity matrix
        transMat = MatrixUtils.createRealIdentityMatrix(3);
    }

    /**
     * Apply current transform to the {@code point} and return its.
     *
     * @param point point for transformation
     * @return point after transformation
     */
    public Point2D apply(Point2D point) {
        // convert the point to vector 3-by-1
        RealMatrix x = MatrixUtils.createColumnRealMatrix(new double[]{point.getX(), point.getY(), 1});
        // multiple the transform matrix by vector "x"
        x = transMat.multiply(x);
        // return result
        return new Point2D(x.getEntry(0, 0) / x.getEntry(2, 0), x.getEntry(1, 0) / x.getEntry(2, 0));
    }


    /**
     * Scaling transformation.
     *
     * @param xScale x scaling
     * @param yScale y scaling
     * @return transformer after transformation
     */
    public Point2DTransformer scale(double xScale, double yScale) {
        double diag[] = {xScale, yScale, 1};
        transMat = MatrixUtils.createRealDiagonalMatrix(diag).multiply(transMat);
        return this;
    }

    /**
     * Translation transformation.
     *
     * @param xShift x shift
     * @param yShift y shift
     * @return transformer after transformation
     */
    public Point2DTransformer translation(double xShift, double yShift) {
        double mat[][] = {{1, 0, xShift}, {0, 1, yShift}, {0, 0, 1}};
        transMat = MatrixUtils.createRealMatrix(mat).multiply(transMat);
        return this;
    }

    /**
     * Rotation transformation.
     *
     * @param angle angle of the rotation
     * @return transformer after transformation
     */
    public Point2DTransformer rotate(double angle) {
        double mat[][] = {
                {Math.cos(angle), -Math.sin(angle), 0},
                {Math.sin(angle), Math.cos(angle), 0},
                {0, 0, 1}};
        transMat = MatrixUtils.createRealMatrix(mat).multiply(transMat);
        return this;
    }


    /**
     * Return string representation of the {@code Point2DTransformer}.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return transMat.toString();
    }
}
