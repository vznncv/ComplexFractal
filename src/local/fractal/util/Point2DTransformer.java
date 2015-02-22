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
     * Create from matrix of the transformation.
     * @param transMat matrix of the transformation
     */
    private Point2DTransformer(RealMatrix transMat) {
        Objects.requireNonNull(transMat);
        if (transMat.getColumnDimension() != 3 && transMat.getRowDimension() != 3) {
            throw new IllegalArgumentException("transMat doesn't have size 3 by 3");
        }
        this.transMat = transMat;
    }

    /**
     * Return new matrix as result multiplication l and r transformation.
     *
     * @param l transformation
     * @param r transformation
     * @return transformation
     */
    public static Point2DTransformer mul(Point2DTransformer l, Point2DTransformer r) {
        return new Point2DTransformer(l.transMat.multiply(r.transMat));
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
