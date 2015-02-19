package local.fractal.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.function.BiFunction;

public class Point2DTransformerJUnit4Test {

    private Point2DTransformer transformer;
    private BiFunction<Point2D, Point2D, Boolean> pointEq;

    @Before
    public void setUp() {
        transformer = new Point2DTransformer();

        pointEq = (f, s) -> {
            double exactness = 0.001;
            double dx = f.getX() - s.getX();
            double dy = f.getY() - s.getY();
            return Math.sqrt(dx * dx + dy * dy) < exactness;
        };
    }

    @Test
    public void testClear() {
        System.out.println("* Point2DTransformerJUnit4Test: testClear()");
        transformer.clear();

        Point2D p1 = new Point2D(1, 0);
        Point2D p2 = new Point2D(1, 1);
        Assert.assertTrue(pointEq.apply(p1, transformer.apply(p1)));
        Assert.assertTrue(pointEq.apply(p2, transformer.apply(p2)));
    }

    @Test
    public void testScale() {
        System.out.println("* Point2DTransformerJUnit4Test: testScale()");
        transformer.scale(2, 1);

        Assert.assertTrue(pointEq.apply(new Point2D(2, 0), transformer.apply(new Point2D(1, 0))));
        Assert.assertTrue(pointEq.apply(new Point2D(-1, 4), transformer.apply(new Point2D(-0.5, 4))));

        transformer.clear();
        transformer.scale(1, -0.5);

        Assert.assertTrue(pointEq.apply(new Point2D(1, 0), transformer.apply(new Point2D(1, 0))));
        Assert.assertTrue(pointEq.apply(new Point2D(-0.5, -2), transformer.apply(new Point2D(-0.5, 4))));
    }

    @Test
    public void testTranslation() {
        System.out.println("* Point2DTransformerJUnit4Test: testTranslation()");
        transformer.translation(2, 1);
        Assert.assertTrue(pointEq.apply(new Point2D(2, 1), transformer.apply(new Point2D(0, 0))));
        Assert.assertTrue(pointEq.apply(new Point2D(0, 2), transformer.apply(new Point2D(-2, 1))));
    }

    @Test
    public void testRotate() {
        System.out.println("* Point2DTransformerJUnit4Test: testRotate()");
        transformer.rotate(Math.PI);
        Assert.assertTrue(pointEq.apply(new Point2D(-2, -1), transformer.apply(new Point2D(2, 1))));
        Assert.assertTrue(pointEq.apply(new Point2D(0, 0), transformer.apply(new Point2D(0, 0))));

        transformer.clear();
        transformer.rotate(Math.PI / 2);
        Assert.assertTrue(pointEq.apply(new Point2D(-1, 2), transformer.apply(new Point2D(2, 1))));
        Assert.assertTrue(pointEq.apply(new Point2D(0, 0), transformer.apply(new Point2D(0, 0))));
    }

    @Test
    public void checkComposition() {
        System.out.println("* Point2DTransformerJUnit4Test: checkComposition()");

        transformer.translation(2, 0);
        transformer.rotate(-Math.PI / 2);
        transformer.scale(1, 0.5);
        transformer.translation(1, 0);
        Assert.assertTrue(pointEq.apply(new Point2D(1, -1), transformer.apply(new Point2D(0, 0))));
        Assert.assertTrue(pointEq.apply(new Point2D(0, -0.5), transformer.apply(new Point2D(-1, -1))));
        Assert.assertTrue(pointEq.apply(new Point2D(3, -1), transformer.apply(new Point2D(0, 2))));

    }
}