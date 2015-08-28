package local.fractal.util;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import local.fractal.model.ComplexFractal;
import local.fractal.model.ComplexFractalChecker;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The class {@code ComplexFractalCanvasDrawer} draws fractal in background thread on the canvas.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class ComplexFractalCanvasDrawer {
    /**
     * Minimal size of the side (height or width) for preview image of the fractal.
     */
    private final int edgeImagePreview = 80;
    /**
     * Preview image.
     */
    private WritableImage previewImage = new WritableImage(edgeImagePreview, edgeImagePreview);
    /**
     * Indicator of that the animation timer must draw preview image. The preview image is always drawn when any
     * settings of the fractal (size of the canvas, palette, fractal checker, affine transform) is changed.
     */
    private volatile boolean drawPreviewImage = false;


    /**
     * Helper object for drawing the fractal on the buffer.
     */
    private ComplexFractalDrawer complexFractalDrawer = new ComplexFractalDrawer();
    /**
     * Image buffer for the fractal.
     */
    private WritableImage imageBuffer;
    /**
     * Settings of drawing the fractal.
     */
    private ComplexFractalChecker complexFractalChecker;
    private IterativePalette iterativePalette;
    /**
     * Current affine transform on the complex plane. This transform applies to point of the image after transform got
     * from {@code ComplexFractalDrawer.calculateInitialTransform}.
     */
    private Point2DTransformer transform;


    /**
     * Canvas for drawing.
     */
    private Canvas canvas;
    /**
     * Thread pool for drawing fractal.
     */
    private ExecutorService singlePool = Executors.newSingleThreadExecutor((task) -> {
        Thread t = new Thread(task);
        t.setDaemon(true);
        return t;
    });
    /**
     * Defines that fractal is drawing in background thread. True, if fractal is drawing, otherwise false. This
     * property always updates in JavaFX thread.
     */
    private ReadOnlyBooleanWrapper work = new ReadOnlyBooleanWrapper(false);
    /**
     * Timer for drawing the fractal on the canvas. It draws preview image and completed part from {@code imageBuffer}.
     */
    private AnimationTimer animationTimer = new AnimationTimer() {
        // maximum value of the fps for animationTimer
        private final long maxFPS = 24;
        // covert maxFPS to time in the nanosecond between the frames
        private final long timeInterval = 1_000_000_000 / maxFPS;
        // time of the prevision starting of the timer
        private long prevFrame = System.nanoTime();
        // indicator that fractal is drawing by complexFractalDrawer
        private boolean fractalIsComplete = false;

        @Override
        public void handle(long now) {
            // limit fps
            if (now > prevFrame + timeInterval) {
                prevFrame = now;
                boolean isWork = isWork();
                // draw the fractal
                if (!fractalIsComplete || isWork) {
                    // size of the canvas
                    double cW = canvas.getWidth();
                    double cH = canvas.getHeight();

                    // draw preview image if it's needed
                    if (drawPreviewImage) {
                        drawPreviewImage = false;
                        canvas.getGraphicsContext2D().drawImage(getPreviewImage(), 0, 0, cW, cH);
                    }

                    // draw main image
                    WritableImage im = getImageBuffer();
                    double hReady = complexFractalDrawer.getNumberDrawnRows();
                    synchronized (im) {
                        canvas.getGraphicsContext2D().drawImage(im, 0, 0, cW, hReady, 0, 0, cW, hReady);
                    }
                }
                fractalIsComplete = !isWork;
            }
        }
    };
    /**
     * Helper object for update {@code work} property in javaFX thread.
     */
    private AtomicReference<Boolean> updateWorkValue = new AtomicReference<>(null);
    /**
     * Indicator that fractal is changed and image needs to update.
     */
    private BooleanProperty changed = new SimpleBooleanProperty(false);


    /**
     * Constructor.
     *
     * @param canvas  canvas for drawing
     * @param fractal complex fractal for drawing
     * @param palette palette for coloring fractal
     * @throws NullPointerException if {@code canvas}, {@code fractal} or {@code palette} is {@code null}
     */
    public ComplexFractalCanvasDrawer(Canvas canvas, ComplexFractalChecker fractal, IterativePalette palette) {
        // store canvas
        this.canvas = Objects.requireNonNull(canvas, "canvas is null");
        // store the fractal settings
        setFractal(fractal);
        setPalette(palette);
        // initialize transform
        setTransform(Point2DTransformer.CLEAR);


        // resize fractal, when canvas will be resized
        InvalidationListener resizeImage = e -> {
            // canvas can have zero size whereas WritableImage must have positive width and height
            int w = Math.max(1, (int) canvas.getWidth());
            int h = Math.max(1, (int) canvas.getHeight());
            setImageBuffer(new WritableImage(w, h));
        };
        this.canvas.heightProperty().addListener(resizeImage);
        this.canvas.widthProperty().addListener(resizeImage);
        // initialize image buffer
        resizeImage.invalidated(null);

        // when changed is set to the true, start redrawing the image
        changed.addListener((obj, oldVal, newVal) -> {
            if (newVal) {
                // stop drawing in complexFractalDrawer
                complexFractalDrawer.setPermitWork(false);
                // redraw the fractal
                singlePool.execute(this::drawFractal);
            }
        });
        // start redrawing the fractal (after invocation resizeImage.invalidated(null) has true value)
        changed.set(false);
        changed.set(true);

        // start the timer
        animationTimer.start();
    }

    /**
     * Constructor with default palette.
     *
     * @param canvas  canvas for drawing
     * @param fractal complex fractal for drawing
     */
    public ComplexFractalCanvasDrawer(Canvas canvas, ComplexFractal fractal) {
        this(canvas, fractal, new IterativePaletteSin());
    }


    /**
     * Gets current palette.
     *
     * @return palette.
     */
    public synchronized IterativePalette getPalette() {
        return iterativePalette;
    }

    /**
     * Sets new palette.
     *
     * @param iterativePalette palette
     * @throws NullPointerException if iterativePalette is {@code null}
     */
    public synchronized void setPalette(IterativePalette iterativePalette) {
        Objects.requireNonNull(iterativePalette);
        if (!iterativePalette.equals(this.iterativePalette)) {
            this.iterativePalette = iterativePalette;
            changed.set(true);
        }
    }

    /**
     * Gets current complex fractal checker.
     *
     * @return complex fractal checker
     */
    public synchronized ComplexFractalChecker getFractal() {
        return complexFractalChecker;
    }

    /**
     * Sets new complex fractal checker.
     *
     * @param complexFractalChecker complex fractal checker.
     * @throws NullPointerException if complexFractalChecker is {@code null}
     */
    public synchronized void setFractal(ComplexFractalChecker complexFractalChecker) {
        Objects.requireNonNull(complexFractalChecker);
        if (!complexFractalChecker.equals(this.complexFractalChecker)) {
            this.complexFractalChecker = complexFractalChecker;
            changed.set(true);
        }
    }

    /**
     * Gets current transform of the axis.
     *
     * @return current transform
     */
    public synchronized Point2DTransformer getTransform() {
        return transform;
    }

    /**
     * Sets current transform of the axis.
     *
     * @param transform new transform
     * @throws NullPointerException if transform is {@code null}
     */
    public synchronized void setTransform(Point2DTransformer transform) {
        Objects.requireNonNull(transform);
        if (!transform.equals(this.transform)) {
            this.transform = transform;
            changed.set(true);
        }
    }

    /**
     * Gets image buffer
     *
     * @return image buffer
     */
    private synchronized WritableImage getImageBuffer() {
        return imageBuffer;
    }

    /**
     * Sets new image buffer.
     *
     * @param imageBuffer image buffer
     * @throws NullPointerException if imageBuffer is {@code null}
     */
    private synchronized void setImageBuffer(WritableImage imageBuffer) {
        this.imageBuffer = Objects.requireNonNull(imageBuffer);
        changed.set(true);
    }

    /**
     * Gets preview image
     *
     * @return image buffer
     */
    private synchronized WritableImage getPreviewImage() {
        return previewImage;
    }

    /**
     * Sets preview image.
     *
     * @param previewImage image
     * @throws NullPointerException if previewImage is {@code null}
     */
    private synchronized void setPreviewImage(WritableImage previewImage) {
        this.previewImage = Objects.requireNonNull(previewImage);
    }


    final public boolean isWork() {
        return work.get();
    }

    public ReadOnlyBooleanProperty workProperty() {
        return work.getReadOnlyProperty();
    }

    /**
     * Updates {@code work} property in non javaFX thread. Listeners of this property updates in JavaFX thread.
     *
     * @param work value of the work property
     */
    private void updateWork(boolean work) {
        if (updateWorkValue.getAndSet(work) == null)
            Platform.runLater(() -> this.work.set(updateWorkValue.getAndSet(null)));
    }


    /**
     * Gets resulting transform for image.
     *
     * @return resulting transformer
     */
    private synchronized Point2DTransformer getResultingTransform() {
        int w = (int) getImageBuffer().getWidth();
        int h = (int) getImageBuffer().getHeight();
        return ComplexFractalDrawer.calculateInitialTransform(w, h).addAfter(getTransform());
    }


    /**
     * Performs translation of the fractal on the canvas.
     *
     * @param dx x translate (in the pixels)
     * @param dy y translate (in the pixels)
     */
    public synchronized void translateImage(double dx, double dy) {
        Point2DTransformer resTr = getResultingTransform();
        Point2D p1 = resTr.apply(new Point2D(0, 0));
        Point2D p2 = resTr.apply(new Point2D(dx, dy));
        double dxFr = p1.getX() - p2.getX();
        double dyFr = p1.getY() - p2.getY();
        // perform translate
        setTransform(getTransform().translation(dxFr, dyFr));
    }

    /**
     * Sets default scale of the image.
     */
    public synchronized void defaultScaleImage() {
        setTransform(Point2DTransformer.CLEAR);
    }

    /**
     * Changes the scale of the image. The point with coordinate (x, y) doesn't move.
     *
     * @param xScale x scales
     * @param yScale y scales
     * @param x      x coordinate of the center scale at canvas
     * @param y      y coordinate of the center scale at canvas
     */
    public synchronized void scaleImage(double xScale, double yScale, double x, double y) {
        Point2D center = getResultingTransform().apply(new Point2D(x, y));
        setTransform(getTransform().scale(xScale, yScale, center));
    }

    /**
     * Rotates image relative to the center of the image.
     *
     * @param angle angle of rotate
     */
    public synchronized void rotateImage(double angle) {
        int w = (int) getImageBuffer().getWidth();
        int h = (int) getImageBuffer().getHeight();
        Point2D center = getResultingTransform().apply(new Point2D(w / 2.0, h / 2.0));
        setTransform(getTransform().rotate(angle, center));
    }


    /**
     * Starts drawing fractal with current settings. Drawing will interrupt if {@code changed} is set to true.
     */
    private void drawFractal() {
        // setting of the fractal
        ComplexFractalChecker cFrCh;
        IterativePalette itPl;
        Point2DTransformer tr;
        // current setting of the image
        WritableImage im;

        synchronized (this) {
            // get current setting

            // current setting of the fractal
            cFrCh = getFractal();
            itPl = getPalette();
            tr = getTransform();
            // current setting of the image
            im = getImageBuffer();

            // changes has accepted to processing
            changed.set(false);
            complexFractalDrawer.setPermitWork(true);
            // start work
            updateWork(true);
        }

        // size of the main image
        int h = (int) im.getHeight();
        int w = (int) im.getWidth();
        // calculate size of the preview image
        double scale = (w > h ? (double) edgeImagePreview / (double) h : (double) edgeImagePreview / (double) w);
        int hPr = Math.max((int) (h * scale), 1);
        int wPr = Math.max((int) (w * scale), 1);

        // draw preview image of the fractal
        WritableImage prIm = getPreviewImage();
        if ((int) prIm.getWidth() != wPr || (int) prIm.getHeight() != hPr) {
            prIm = new WritableImage(wPr, hPr);
            setPreviewImage(prIm);
        }
        ComplexFractalDrawer.drawFractal(prIm, ComplexFractalDrawer.calculateInitialTransform(wPr, hPr).addAfter(tr), cFrCh, itPl);
        drawPreviewImage = true;

        // draw fractal
        complexFractalDrawer.setImage(im);
        complexFractalDrawer.drawFractal(ComplexFractalDrawer.calculateInitialTransform(w, h).addAfter(tr), cFrCh, itPl);

        // if thread has drawn fractal fully then working has been finished
        synchronized (this) {
            if (!changed.get())
                updateWork(false);
        }
    }
}
