package local.fractal;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import local.fractal.model.ComplexFractal;
import local.fractal.model.ComplexFractalChecker;
import local.fractal.util.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
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
        // prepare and start drawing fractal
        (new Thread(this::prepareDrawingFractal)).start();
    }

    /**
     * Method returns true if thread drawing the fractal is working.
     *
     * @return thread is working or not
     */
    public boolean isWork() {
        return work;
    }

    /**
     * Set state of the drawing.
     *
     * @param work thread works or not
     */
    private void setWork(boolean work) {
        this.work = work;
    }

    /**
     * Need drawing stop?
     *
     * @return true if drawing need stop else false
     */
    private boolean isStopWork() {
        return stopWork;
    }

    /**
     * Set state of the stop work.
     *
     * @param stopWork state
     */
    private void setStopWork(boolean stopWork) {
        this.stopWork = stopWork;
    }

    /**
     * It's non blocking method stopping current drawing of the fractal in canvas.
     */
    public void stopDrawing() {
        setStopWork(true);
    }

    /**
     * This method prepared drawing fractal and start drawing.
     */
    private synchronized void prepareDrawingFractal() {
        // stop previous drawing
        if (isWork()) {
            stopDrawing();
            try {
                drawingThread.join();
            } catch (InterruptedException e) {
                System.err.println("Error waiting the end of the thread: " + e);
            }
        }
        // prepare and start new drawing
        // get current transformation
        final Point2DTransformer tr = new Point2DTransformer(getTransform());
        // get current fractal
        final ComplexFractalChecker fc = getFractal();
        // get current palette
        final IterativePalette pl = getPalette();
        // create and start thread for drawing
        drawingThread = new Thread(() -> drawFractal(tr, fc, pl));
        // start drawing
        setStopWork(false);
        setWork(true);
        drawingThread.start();
    }

    /**
     * Method for thread drawing fractal.
     */
    private void drawFractal(Point2DTransformer cTr, ComplexFractalChecker cFc, IterativePalette cPl) {
        // size of the canvas
        int w = canvas.widthProperty().intValue();
        int h = canvas.heightProperty().intValue();

        // graphic context for drawing points
        PixelWriter pw = canvas.getGraphicsContext2D().getPixelWriter();


        // function for generate line of the points
        UnaryOperator<Point2D> genLine = (p) -> new Point2D(p.getX() + 1, p.getY());
        // function for transformation points
        Function<Point2D, Pair<Point2D, Point2D>> toTransform = (p) -> new Pair<>(p, cTr.apply(p));

        // function for calculating number of iterations
        Function<Pair<Point2D, Point2D>, Pair<Point2D, Integer>> toCalc = (elem) ->
                new Pair<>(elem.getFirst(), cFc.numberIter(elem.getSecond().toComplex()));
        // function for to color point
        Function<Pair<Point2D, Integer>, Pair<Point2D, Color>> toColor = (elem) ->
                new Pair<>(elem.getFirst(), cPl.numIterToColor(elem.getSecond()));


        // function for draw point in the canvas
        Consumer<Pair<Point2D, Color>> drawPoint = (elem) -> {
            Point2D point = elem.getFirst();
            int xP = (int) point.getX();
            int yP = (int) point.getY();
            pw.setColor(xP, yP, elem.getSecond());
        };

        int y = 0;
        while (!isStopWork() && y < h) {
            // create list of points to draw
            List<Pair<Point2D, Color>> line = Stream
                    .iterate(new Point2D(0, y), genLine)
                    .limit(w)
                    .parallel()
                    .map(toTransform)
                    .map(toCalc)
                    .map(toColor)
                    .collect(Collectors.toList());
            // draw line
            Platform.runLater(() -> line.forEach(drawPoint));
            // go to the next line
            y++;
        }

        // thread stop working
        setWork(false);
        setStopWork(false);
    }

}
