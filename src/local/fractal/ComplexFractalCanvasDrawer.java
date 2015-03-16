package local.fractal;

import javafx.animation.AnimationTimer;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.canvas.Canvas;
import local.fractal.model.ComplexFractal;
import local.fractal.model.ComplexFractalChecker;
import local.fractal.util.ComplexFractalDrawer;
import local.fractal.util.IterativePalette;
import local.fractal.util.IterativePaletteV1;

import java.util.Objects;

/**
 * This class draws fractal in background thread.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class ComplexFractalCanvasDrawer {
    /**
     * It's maximum value of the fps for animationTimer
     */
    private static final long maxFPS = 24;
    /**
     * Object for render fractal to the inner buffer
     */
    private final ComplexFractalDrawer complexFractalDrawer;
    /**
     * Timer for render flush current image of the fractal to the canvas.
     */
    private AnimationTimer animationTimer;
    /**
     * Canvas for drawing.
     */
    private Canvas canvas;


    /**
     * {@code ComplexFractalDrawer} is helper class for drawing fractal.
     *
     * @param canvas  canvas for drawing
     * @param fractal complex fractal for drawing
     * @param palette palette for coloring fractal
     */
    public ComplexFractalCanvasDrawer(Canvas canvas, ComplexFractalChecker fractal, IterativePalette palette) {
        // store canvas
        this.canvas = Objects.requireNonNull(canvas, "canvas is null");

        // covert maxFPS to time in the nanosecond between the frames
        final long timeInterval = 1_000_000_000 / maxFPS;
        // create animation timer for drawing the fractal
        animationTimer = new AnimationTimer() {
            // time of the prevision starting of the timer
            private long prevFrame = System.nanoTime();
            // indicator that fractal is drawing by complexFractalDrawer
            private boolean fractalIsComplete = false;

            @Override
            public void handle(long now) {
                // limit fps
                if (now > prevFrame + timeInterval) {
                    prevFrame = now;
                    boolean isWork = complexFractalDrawer.workProperty().get();
                    // draw the fractal
                    if (!fractalIsComplete || isWork) {
                        synchronized (complexFractalDrawer) {
                            canvas.getGraphicsContext2D().drawImage(complexFractalDrawer.getCurrentImage(), 0, 0);
                        }
                    }
                    fractalIsComplete = !isWork;
                }
            }
        };
        // create complexFractalDrawer
        int wInit = Math.max(1, (int) canvas.getWidth());
        int hInit = Math.max(1, (int) canvas.getHeight());
        complexFractalDrawer = new ComplexFractalDrawer(wInit, hInit, fractal, palette);
        // resize fractal, when canvas will be resized
        InvalidationListener canvasResize = (cnv) -> {
            int w = Math.max(1, (int) canvas.getWidth());
            int h = Math.max(1, (int) canvas.getHeight());
            complexFractalDrawer.resizeImage(w, h);
        };
        this.canvas.heightProperty().addListener(canvasResize);
        this.canvas.widthProperty().addListener(canvasResize);
        // start the timer
        animationTimer.start();
    }

    /**
     * {@code ComplexFractalDrawer} is helper class for drawing fractal.
     *
     * @param canvas  canvas for drawing
     * @param fractal complex fractal for drawing
     */
    public ComplexFractalCanvasDrawer(Canvas canvas, ComplexFractal fractal) {
        this(canvas, fractal, new IterativePaletteV1());
    }

    /**
     * Get current palette.
     *
     * @return palette.
     */
    public IterativePalette getPalette() {
        return complexFractalDrawer.getIterativePalette();
    }

    /**
     * Set new palette.
     *
     * @param palette palette
     */
    public synchronized void setPalette(IterativePalette palette) {
        complexFractalDrawer.setIterativePalette(palette);
    }

    /**
     * Get current complex fractal checker.
     *
     * @return complex fractal checker
     */
    public synchronized ComplexFractalChecker getFractal() {
        return complexFractalDrawer.getComplexFractalChecker();
    }

    /**
     * Set new complex fractal checker.
     *
     * @param fractal complex fractal checker.
     */
    public synchronized void setFractal(ComplexFractalChecker fractal) {
        complexFractalDrawer.setComplexFractalChecker(fractal);
    }

    /**
     * Get property indicated that fractal is drawing.
     *
     * @return property indicated calculating of the fractal
     */
    public synchronized ReadOnlyBooleanProperty workProperty() {
        return complexFractalDrawer.workProperty();
    }
}
