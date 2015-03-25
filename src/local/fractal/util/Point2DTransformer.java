package local.fractal.util;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

/**
 * {@code Point2DTransformer} is class for 2-D affine transforms for the objects of class {@link Point2D}.
 * Objects of this class is immutable.
 *
 * @author Kochin Konstantin Alexandrovich
 */
final public class Point2DTransformer {
    /**
     * {@code Point2DTransformer} with identity matrix
     */
    public final static Point2DTransformer CLEAR = new Point2DTransformer(new double[]{
            1, 0, 0,
            0, 1, 0,
            0, 0, 1
    });

    /**
     * Transform matrix.
     */
    private final double[] trMatrix;


    /**
     * Constructor
     *
     * @param trMartix transform matrix
     */
    private Point2DTransformer(double[] trMartix) {
        Objects.requireNonNull(trMartix);
        if (trMartix.length != 9)
            new IllegalArgumentException("trMartix isn't matrix 3 by 3");
        this.trMatrix = trMartix;
    }

    /**
     * Calculate the matrix multiplication {@code res} = {@code l} * {code r}.
     *
     * @param l left argument
     * @param r right argument
     * @return result of the multiplication
     */
    private static double[] matrixMul(double[] l, double[] r) {
        Objects.requireNonNull(l);
        Objects.requireNonNull(r);
        if (l.length != 9)
            new IllegalArgumentException("l isn't matrix 3 by 3");
        if (r.length != 9)
            new IllegalArgumentException("r isn't matrix 3 by 3");

        // calculate result of the matrix multiplication
        double[] res = new double[9];
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                double a = 0;
                for (int k = 0; k < 3; k++)
                    a += l[i * 3 + k] * r[k * 3 + j];
                res[i * 3 + j] = a;
            }
        return res;
    }

    /**
     * Apply current transform to the {@code point} and return its.
     *
     * @param point point for transformation
     * @return point after transformation
     */
    public Point2D apply(Point2D point) {
        double[] oldP = {point.getX(), point.getY(), 1};
        double[] newP = new double[3];

        for (int i = 0; i < 3; i++) {
            double sum = 0;
            for (int j = 0; j < 3; j++)
                sum += trMatrix[i * 3 + j] * oldP[j];
            newP[i] = sum;
        }

        return new Point2D(newP[0] / newP[2], newP[1] / newP[2]);
    }

    /**
     * Translation transformation.
     *
     * @param xShift x shift
     * @param yShift y shift
     * @return transformer after transformation
     */
    public Point2DTransformer translation(double xShift, double yShift) {
        double[] translationMat = {
                1, 0, xShift,
                0, 1, yShift,
                0, 0, 1
        };
        return new Point2DTransformer(matrixMul(translationMat, trMatrix));
    }

    /**
     * Scaling transformation.
     *
     * @param xScale x scaling
     * @param yScale y scaling
     * @return transformer after transformation
     */
    public Point2DTransformer scale(double xScale, double yScale) {
        return scale(xScale, yScale, new Point2D(0, 0));
    }

    /**
     * Scaling transformation.
     *
     * @param xScale x scaling
     * @param yScale y scaling
     * @param fixP   point which mustn't move after scale transform
     * @return transformer after transformation
     */
    public Point2DTransformer scale(double xScale, double yScale, Point2D fixP) {
        double[] scaleMat = {
                xScale, 0, fixP.getX() * (1 - xScale),
                0, yScale, fixP.getY() * (1 - yScale),
                0, 0, 1
        };
        return new Point2DTransformer(matrixMul(scaleMat, trMatrix));
    }

    /**
     * Rotation transformation.
     *
     * @param angle angle of the rotation
     * @return transformer after transformation
     */
    public Point2DTransformer rotate(double angle) {
        return rotate(angle, new Point2D(0, 0));
    }

    /**
     * Rotation transformation.
     *
     * @param angle angle of the rotation
     * @param fixP  center of the rotate
     * @return transformer after transformation
     */
    public Point2DTransformer rotate(double angle, Point2D fixP) {
        double cA = Math.cos(angle);
        double sA = Math.sin(angle);
        double xF = fixP.getX();
        double yF = fixP.getY();
        double rotateMat[] = {
                cA, -sA, xF - cA * xF + sA * yF,
                sA, cA, yF - sA * xF - cA * yF,
                0, 0, 1
        };
        return new Point2DTransformer(matrixMul(rotateMat, trMatrix));
    }

    /**
     * Add transform {@code after} after this transform.
     *
     * @param after adding transform
     * @return resulting transform
     */
    public Point2DTransformer addAfter(Point2DTransformer after) {
        Objects.requireNonNull(after);
        return new Point2DTransformer(matrixMul(after.trMatrix, trMatrix));
    }

    /**
     * Get transformer with identity matrix
     *
     * @return {@code Point2DTransformer} with identity matrix
     */
    public Point2DTransformer clear() {
        return CLEAR;
    }

    /**
     * Return string representation of the {@code Point2DTransformer}.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##");
        StringBuilder str = new StringBuilder();
        str.append("transform matrix [ ");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++)
                str.append(df.format(trMatrix[3 * i + j])).append(j != 2 ? ", " : "");
            str.append((i != 2 ? "; " : ""));
        }
        str.append("]");

        return str.toString();
    }

    /**
     * Compare transformers.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if transformers are same; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point2DTransformer) {
            return Arrays.equals(trMatrix, ((Point2DTransformer) obj).trMatrix);
        }
        return false;
    }
}
