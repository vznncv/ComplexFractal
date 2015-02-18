package local.fractal;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import local.fractal.model.ComplexFractal;
import local.fractal.model.ComplexFractalChecker;
import local.fractal.util.IterativePalette;
import local.fractal.util.IterativePaletteV1;
import local.fractal.util.Point2D;
import local.fractal.util.Point2DTransformer;

import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * This class draws fractal in background thread.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class ComplexFractalDrawer {
    /**
     * Object for scaling, rotating, translating the plane of the fractal.
     */
    private Point2DTransformer transform;
    /**
     * Canvas for drawing.
     */
    private Canvas canvas;
    /**
     * Palette for to color the fractal.
     */
    private IterativePalette palette;
    /**
     * Object for creating the fractal.
     */
    private ComplexFractalChecker fractal;

    /**
     * Thread for drawing.
     */
    private Thread drawingThread;
    /**
     * It indicates that the thread for drawing need stop.
     */
    private volatile boolean stopWork;
    /**
     * it indicates that the thread for drawing is working.
     */
    private volatile boolean work;
    private LinkedList<Runnable> taskToDraw;

    /**
     * {@code ComplexFractalDrawer} is helper class for drawing fractal.
     *
     * @param canvas  canvas for drawing
     * @param fractal complex fractal for drawing
     * @param palette palette for coloring fractal
     */
    public ComplexFractalDrawer(Canvas canvas, ComplexFractalChecker fractal, IterativePalette palette) {
        setCanvas(canvas);
        setFractal(fractal);
        setPalette(palette);
        stopWork = false;
        work = false;
    }

    /**
     * {@code ComplexFractalDrawer} is helper class for drawing fractal.
     *
     * @param canvas  canvas for drawing
     * @param fractal complex fractal for drawing
     */
    public ComplexFractalDrawer(Canvas canvas, ComplexFractal fractal) {
        this(canvas, fractal, new IterativePaletteV1());
    }

    /**
     * Store canvas, set the origin of coordinates in center to the canvas and
     * add scaling that point (1, 0) or (0,1) are at bound of the canvas.
     *
     * @param canvas canvas
     */
    private synchronized void setCanvas(Canvas canvas) {
        this.canvas = Objects.requireNonNull(canvas, "canvas is null");
        // correct initializing transform
        transform = new Point2DTransformer() {
            @Override
            public void clear() {
                super.clear();
                double w = canvas.getWidth();
                double h = canvas.getHeight();
                // translation the origin of coordinates to center of the canvas
                translation(-w / 2.0, -h / 2.0);
                // scaling that point (1, 0) or (0,1) will be at bound of the canvas
                double scaleXY = 2.0 / Math.min(w, h);
                scale(scaleXY, scaleXY);
            }
        };
        // initialize transform
        transform.clear();
    }

    /**
     * Get current transform for changing.
     *
     * @return current transform
     */
    public synchronized Point2DTransformer getTransform() {
        return transform;
    }

    /**
     * Get current palette.
     *
     * @return palette.
     */
    public synchronized IterativePalette getPalette() {
        return palette;
    }

    /**
     * Set new palette.
     *
     * @param palette palette
     */
    public synchronized void setPalette(IterativePalette palette) {
        this.palette = Objects.requireNonNull(palette, "palette is null");
    }

    /**
     * Get current complex fractal checker.
     *
     * @return complex fractal checker
     */
    public synchronized ComplexFractalChecker getFractal() {
        return fractal;
    }

    /**
     * Set new complex fractal checker.
     *
     * @param fractal complex fractal checker.
     */
    public synchronized void setFractal(ComplexFractalChecker fractal) {
        this.fractal = Objects.requireNonNull(fractal, "fractal is null");
    }

    /**
     * It's non blocking method starts drawing of the fractal in canvas.
     */
    public synchronized void draw() {
        new Thread(() -> {
            synchronized (this) {
                // stop previous drawing
                if (isWork()) {
                    stopDrawing();
                    try {
                        drawingThread.join();
                    } catch (InterruptedException e) {
                        System.err.println("Error waiting the end of the thread: " + e);
                    }
                }
                // get current transformation
                final Point2DTransformer tr = new Point2DTransformer(getTransform());
                // get current fractal
                final ComplexFractalChecker fc = getFractal();
                // get current palette
                final IterativePalette pl = getPalette();
                // create and start thread for drawing
                drawingThread = new Thread(() -> drawFractal(tr, fc, pl));
                // start drawing
                work = true;
                drawingThread.start();
            }
        }).start();
    }

    /**
     * Method returns true if thread drawing the fractal is working.
     *
     * @return thread is working or not
     */
    public synchronized boolean isWork() {
        return work;
    }

    /**
     * It's non blocking method stopping current drawing of the fractal in canvas.
     */
    public synchronized void stopDrawing() {
        if (isWork()) {
            stopWork = true;
        }
    }

    /**
     * Method for thread drawing fractal.
     */
    private void drawFractal(Point2DTransformer currentTransform, ComplexFractalChecker currentFractal, IterativePalette currentPalette) {
        // size of the canvas
        int w = canvas.widthProperty().intValue();
        int h = canvas.heightProperty().intValue();

        // graphic context for drawing points
        PixelWriter pw = canvas.getGraphicsContext2D().getPixelWriter();

        // function for generate line of the points
        UnaryOperator<Point2D> genLine = (p) -> {
            return new Point2D(p.getX() + 1, p.getY());
        };
        // function for transformation points
        Function<Point2D, PointAndPoint> toTransform = (p) -> {
            return new PointAndPoint(p, currentTransform.apply(p));
        };
        // function for calculating number of iterations
        Function<PointAndPoint, PointAndInt> toCalc = (elem) -> {
            int i = currentFractal.numberIter(elem.p2.toComplex());
            return new PointAndInt(elem.p1, i);
        };
        // function for to color point
        Function<PointAndInt, PointAndColor> toColor = (elem) -> {
            return new PointAndColor(elem.p, currentPalette.numIterToColor(elem.i));
        };
        // function for draw point in the canvas
        Consumer<PointAndColor> drawPoint = (elem) -> {
            Point2D point = elem.p;
            int xP = (int) point.getX();
            int yP = (int) point.getY();
            pw.setColor(xP, yP, elem.c);
        };

        /*
        Stream<Point2D> points = Stream.empty();
        for (int y = 0; y < h; y++) {
            points = Stream.concat(points, Stream.iterate(new Point2D(0, y), genLine).limit(w));
        }
        Stream<PointAndColor> lineForDrawing = points.parallel() // create line with points
                .map(toTransform) // transform points
                .map(toCalc) // calculate number of iterations;
                .map(toColor) // change color
                .sequential();

        Platform.runLater(()->lineForDrawing.forEach(drawPoint));
*/


        // for grouping line do draw
        taskToDraw = new LinkedList<>();
        AnimationTimer tm = new AnimationTimer() {
            @Override
            public void handle(long now) {
//                System.out.println("Task Start: " + System.currentTimeMillis());
                synchronized (taskToDraw) {
                    int maxLine = 5;
                    int i = 0;
                    while (!taskToDraw.isEmpty() && i < maxLine) {
                        taskToDraw.remove().run();
                        i++;
                    }

//                    taskToDraw.forEach((elem)->elem.run());
//                    taskToDraw.clear();
                    System.out.println("Lines were: " + taskToDraw.size() + " (max: " + h + ")");
                }
//                System.out.println("Task Start: " + System.currentTimeMillis());
//                System.out.println("Time: " + now);
            }
        };
        Platform.runLater(() -> {
            tm.start();
        });


        int j = 0;

        // draw fractal
        int y = 0;
        while (!stopWork && y < h) {
            Stream<PointAndColor> lineForDrawing = Stream
                    .iterate(new Point2D(0, y), genLine).limit(w).parallel() // create line with points
                    .map(toTransform) // transform points
                    .map(toCalc) // calculate number of iterations;
                    .map(toColor) // change color
                    .sequential();

            synchronized (taskToDraw) {
                taskToDraw.add(() -> lineForDrawing.forEach(drawPoint));
            }
            /*
            taskToDraw.add(() -> lineForDrawing.forEach(drawPoint));
            j++;

            if (j > 100) {
                final LinkedList<Runnable> task = taskToDraw;
                // show line
                Platform.runLater(() -> {

                    task.forEach((elem)->elem.run());
                });
                taskToDraw = new LinkedList<>();
                j = 0;
            }*/

            // go to next line
            y++;
        }

        //tm.stop();*/
        boolean b = true;
        while (b) {
            synchronized (taskToDraw) {
                b = !taskToDraw.isEmpty();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        tm.stop();


        // thread stop working
        work = false;
        stopWork = false;
    }

    /**
     * There are helpers classes for drawing fractals, using streams.
     */
    private static class PointAndPoint {
        public Point2D p1;
        public Point2D p2;

        public PointAndPoint(Point2D p1, Point2D p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }

    private static class PointAndInt {
        public Point2D p;
        public int i;

        public PointAndInt(Point2D p, int i) {
            this.p = p;
            this.i = i;
        }
    }

    private static class PointAndColor {
        public Point2D p;
        public Color c;

        public PointAndColor(Point2D p, Color c) {
            this.p = p;
            this.c = c;
        }
    }


}
