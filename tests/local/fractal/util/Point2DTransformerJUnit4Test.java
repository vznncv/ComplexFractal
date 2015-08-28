package local.fractal.util;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * The {@code Point2DTransformerJUnit4Test} represents units test for class {@code Point2DTransformer}.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class Point2DTransformerJUnit4Test {

    /**
     * Compare coordinate of two points.
     *
     * @param p1        first point
     * @param p2        second point
     * @param exactness exactness of the compare
     * @return true if coordinates of the points equal with exactness {@code exactness}, otherwise false
     */
    private static boolean pointEq(Point2D p1, Point2D p2, double exactness) {
        return Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2) < exactness * exactness;
    }

    /**
     * Compare coordinate of two points with exactness 0.001.
     *
     * @param p1 first point
     * @param p2 second point
     * @return true if coordinates of the points equal with exactness 0.001, otherwise false
     */
    private static boolean pointEq(Point2D p1, Point2D p2) {
        return pointEq(p1, p2, 0.001);
    }

    @Test
    public void testMatrixMul() throws Exception {
        System.out.println("* Point2DTransformerJUnit4Test: testMatrixMul()");

        Method matrixMul = Point2DTransformer.class.getDeclaredMethod("matrixMul", double[].class, double[].class);
        matrixMul.setAccessible(true);

        double[] eye = {
                1, 0, 0,
                0, 1, 0,
                0, 0, 1
        };
        double[] a = {
                1, 1, 1,
                0, 0, 0,
                2, 0, 2,
        };
        double[] b = {
                0, 2, 1,
                2, 0, 0,
                1, 0, 2,
        };
        double[] abRes = {
                3, 2, 3,
                0, 0, 0,
                2, 4, 6,
        };
        double[] baRes = {
                2, 0, 2,
                2, 2, 2,
                5, 1, 5,
        };

        Assert.assertArrayEquals((double[]) matrixMul.invoke(null, a, eye), a, 0.001);
        Assert.assertArrayEquals((double[]) matrixMul.invoke(null, eye, a), a, 0.001);
        Assert.assertArrayEquals((double[]) matrixMul.invoke(null, a, b), abRes, 0.001);
        Assert.assertArrayEquals((double[]) matrixMul.invoke(null, b, a), baRes, 0.001);
    }

    @Test
    public void testTranslation() throws Exception {
        System.out.println("* Point2DTransformerJUnit4Test: testTranslation()");

        Point2DTransformer tr = Point2DTransformer.CLEAR;
        tr = tr.translation(-1, 1);
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(1, 1)),
                new Point2D(0, 2)
        ));
    }

    @Test
    public void testScale() throws Exception {
        System.out.println("* Point2DTransformerJUnit4Test: testScale()");

        Point2DTransformer tr = Point2DTransformer.CLEAR;
        tr = tr.scale(2, 0.5, new Point2D(1, 1));
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(1, 1)),
                new Point2D(1, 1)
        ));
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(0, 0)),
                new Point2D(-1, 0.5)
        ));
    }

    @Test
    public void testRotate() throws Exception {
        System.out.println("* Point2DTransformerJUnit4Test: testRotate()");

        Point2DTransformer tr = Point2DTransformer.CLEAR;
        tr = tr.rotate(Math.PI / 2, new Point2D(2, 2));
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(2, 2)),
                new Point2D(2, 2)
        ));
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(1, 1)),
                new Point2D(3, 1)
        ));
    }

    @Test
    public void complexTest() throws Exception {
        System.out.println("* Point2DTransformerJUnit4Test: complexTest()");

        Point2DTransformer tr = Point2DTransformer.CLEAR
                .translation(1, 1)
                .rotate(Math.PI, new Point2D(-1, -1))
                .scale(2, 2, new Point2D(1, 1));
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(0, 0)),
                new Point2D(-7, -7)
        ));
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(2, -2)),
                new Point2D(-11, -3)
        ));
    }


    @Test
    public void testAddAfter() throws Exception {
        System.out.println("* Point2DTransformerJUnit4Test: testAddAfter()");

        Point2DTransformer tr1 = Point2DTransformer.CLEAR.rotate(-Math.PI / 2, new Point2D(0, 1));
        Point2DTransformer tr2 = Point2DTransformer.CLEAR.translation(1, -1);
        Point2DTransformer tr = tr1.addAfter(tr2);
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(0, 0)),
                new Point2D(0, 0)
        ));
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(0, 1)),
                new Point2D(1, 0)
        ));
    }

    @Test
    public void testClear() throws Exception {
        System.out.println("* Point2DTransformerJUnit4Test: testClear()");

        Point2DTransformer tr = Point2DTransformer.CLEAR.rotate(10).translation(1, 1).scale(2, 0.5);
        tr = tr.clear();
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(2, 2)),
                new Point2D(2, 2)
        ));
        Assert.assertTrue(pointEq(
                tr.apply(new Point2D(10, 10)),
                new Point2D(10, 10)
        ));
    }

    @Test
    public void testEquals() throws Exception {
        System.out.println("* Point2DTransformerJUnit4Test: testEquals()");

        Point2DTransformer tr1 = Point2DTransformer.CLEAR.rotate(Math.PI / 2, new Point2D(0, 1));
        Point2DTransformer tr2 = Point2DTransformer.CLEAR.rotate(Math.PI / 2, new Point2D(0, 1));
        Assert.assertTrue(tr1.equals(tr2));
        Assert.assertTrue(tr1.equals(tr1));
    }
}