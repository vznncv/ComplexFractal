package local.fractal;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class draws fractal in background thread.
 * (use close method to shutdown all background thread)
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class ComplexFractalDrawer implements AutoCloseable {
    /**
     * Object for scaling, rotating, translating the plane of the fractal.
     */
    private Point2DTransformer transform = new Point2DTransformer();
    /**
     * This transform move initial origin of the coordinate to the center of the canvas
     * which that points (1,0) and (0,1) is in canvas. (need when window resizes).
     */
    private Point2DTransformer initAxisTransform = new Point2DTransformer();

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
     * Indicator of the process of drawing.
     */
    private BooleanProperty work = new SimpleBooleanProperty(false);
    /**
     * Indicate that fractal has been changed and it require the redrawing.
     */
    private BooleanProperty changed = new SimpleBooleanProperty(false);
    /**
     * Allow/disallow calculation and drawing the fractal.
     */
    private boolean active = true;


    /**
     * {@code ComplexFractalDrawer} is helper class for drawing fractal.
     *
     * @param canvas  canvas for drawing
     * @param fractal complex fractal for drawing
     * @param palette palette for coloring fractal
     */
    public ComplexFractalDrawer(Canvas canvas, ComplexFractalChecker fractal, IterativePalette palette) {
        // start redrawing when changed property set to the true
        changed.addListener((obs) -> {
            if (((ReadOnlyBooleanProperty) obs).get()) {
                startDraw();
            }
        });
        // store palette, fractal and canvas
        setFractal(fractal);
        setPalette(palette);
        setCanvas(canvas);
        // start drawing the fractal
        changed.set(true);
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
        // initializing initAxisTransform
        updateCanvasTransformation();
        // redrawing fractal when canvas is resized
        InvalidationListener canvasResize = (obs) -> {
            synchronized (this) {
                updateCanvasTransformation();
                changed.set(true);
            }
        };
        this.canvas.heightProperty().addListener(canvasResize);
        this.canvas.widthProperty().addListener(canvasResize);
    }

    /**
     * Update initAxisTransform when canvas is resized.
     */
    private synchronized void updateCanvasTransformation() {
        initAxisTransform.clear();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        // translation the origin of coordinates to center of the canvas
        initAxisTransform.translation(-w / 2.0, -h / 2.0);
        // scaling that point (1, 0) or (0,1) will be at bound of the canvas
        double scaleXY = 2.0 / Math.min(w, h);
        initAxisTransform.scale(scaleXY, scaleXY);
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
     * Get property indicated that fractal is drawing.
     *
     * @return property indicated calculating of the fractal
     */
    public synchronized ReadOnlyBooleanProperty workProperty() {
        return work;
    }


    /**
     * Return true if thread may continue the calculation of the fractal.
     *
     * @return true if thread may continue the calculation
     */
    private synchronized boolean continueWork() {
        return !changed.get() && active;
    }

    /**
     * It's non blocking method starts drawing of the fractal in canvas.
     */
    public synchronized void startDraw() {
        // create thread for drawing if it isn't work
        if (!work.get()) {
            drawingThread = new Thread(this::runDraw);
            work.set(true);
            drawingThread.start();
        }
    }

    // start point of the drawing thread
    private void runDraw() {
        // need draw fractal?
        Supplier<Boolean> continueThread = () -> {
            synchronized (this) {
                if (active && changed.get()) {
                    // start drawing of the fractal
                    changed.set(false);
                    return true;
                } else {
                    // end thread
                    work.set(false);
                    return false;
                }
            }
        };

        while (continueThread.get()) {
            Point2DTransformer tr;
            ComplexFractalChecker fc;
            IterativePalette pl;
            int w;
            int h;
            PixelWriter pw;

            // prepare task for drawing fractal
            synchronized (this) {
                // get current transformation
                tr = Point2DTransformer.mul(initAxisTransform, getTransform());
                // get current fractal
                fc = getFractal();
                // get current palette
                pl = getPalette();
                // current size of the canvas
                w = canvas.widthProperty().intValue();
                h = canvas.heightProperty().intValue();
                // pixel writer for canvas
                pw = canvas.getGraphicsContext2D().getPixelWriter();
            }
            // start drawing
            if (w >= 1 && h >= 1) {
                drawFractal(w, h, pw, tr, fc, pl);
            }
        }
    }

    /**
     * Method for thread drawing fractal.
     *
     * @param w   current width of the canvas
     * @param h   current height of the canvas
     * @param pw  current pixel writer of the canvas
     * @param cTr current transformation for point of the canvas
     * @param cFc current complex fractal checker
     * @param cPl current iterative palette
     */
    private void drawFractal(int w, int h, PixelWriter pw, Point2DTransformer cTr, ComplexFractalChecker cFc, IterativePalette cPl) {
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
        while (continueWork() && y < h) {
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
    }


    /**
     * Stop any drawing (background thread are ended).
     *
     * @throws InterruptedException
     */
    @Override
    public synchronized void close() throws InterruptedException {
        active = false;
        // wait while thread of the drawing is ended
        if (drawingThread != null) {
            drawingThread.join();
        }
    }
}
