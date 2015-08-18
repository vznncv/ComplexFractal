package local.fractal.util;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
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
 * This class {@code ComplexFractalCanvasDrawer} draws fractal in background thread on the canvas.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class ComplexFractalCanvasDrawer {
    /**
     * It's maximum value of the fps for animationTimer
     */
    private static final long maxFPS = 24;
    /**
     * It's minimal size of the height and width of preview image of the fractal
     */
    private final int edgeImagePreview = 80;
    /**
     * It's helper object for drawing the fractal to the buffer.
     */
    private ComplexFractalDrawer complexFractalDrawer = new ComplexFractalDrawer();
    /**
     * Settings of drawing the fractal.
     */
    private ComplexFractalChecker complexFractalChecker;
    private IterativePalette iterativePalette;
    /**
     * Buffer image with fractal.
     */
    private WritableImage imageBuffer;

    /**
     * Current affine transform of the complex plane.
     */
    private Point2DTransformer transform;
    /**
     * Canvas for drawing.
     */
    private Canvas canvas;
    /**
     * Timer for render flush current image of the fractal to the canvas.
     */
    private AnimationTimer animationTimer;
    /**
     * Thread pool for drawing fractal.
     */
    private ExecutorService singlePool = Executors.newSingleThreadExecutor((task) -> {
        Thread t = new Thread(task);
        t.setDaemon(true);
        return t;
    });
    /**
     * It's indicator of the drawing of the fractal.
     */
    private ReadOnlyBooleanWrapper work = new ReadOnlyBooleanWrapper(false);
    /**
     * It's helper object for update work property in javaFX thread.
     */
    private AtomicReference<Boolean> updateWorkValue = new AtomicReference<>(null);
    /**
     * It's indicator that fractal is changed and image need to update.
     */
    private BooleanProperty changed = new SimpleBooleanProperty(false);


    /**
     * {@code ComplexFractalCanvasDrawer} is helper class for drawing fractal on canvas.
     *
     * @param canvas  canvas for drawing
     * @param fractal complex fractal for drawing
     * @param palette palette for coloring fractal
     */
    public ComplexFractalCanvasDrawer(Canvas canvas, ComplexFractalChecker fractal, IterativePalette palette) {
        // store canvas
        this.canvas = Objects.requireNonNull(canvas, "canvas is null");
        // store the fractal settings
        setFractal(fractal);
        setPalette(palette);
        // initialize transform
        transform = Point2DTransformer.CLEAR;

        // initialize image buffer
        resizeImage();
        // resize fractal, when canvas will be resized
        this.canvas.heightProperty().addListener(e -> resizeImage());
        this.canvas.widthProperty().addListener(e -> resizeImage());

        // when changed is set to the true, start redrawing the image
        changed.addListener((obj, oldVal, newVal) -> {
            if (newVal) {
                // stop drawing in complexFractalDrawer
                complexFractalDrawer.setPermitWork(false);
                // redraw the fractal
                singlePool.execute(this::drawFractal);
            }
        });
        // start redrawing the fractal (after invocation method setFractal, setPalette, resizeImage has true value)
        changed.set(false);
        changed.set(true);


        // covert maxFPS to time in the nanosecond between the frames
        final long timeInterval = 1_000_000_000 / maxFPS;
        // create animation timer for drawing the fractal
        animationTimer = new AnimationTimer() {
            // time of the prevision starting of the timer
            private long prevFrame = System.nanoTime();
            // indicator that fractal is drawing by complexFractalDrawer2
            private boolean fractalIsComplete = false;

            @Override
            public void handle(long now) {
                // limit fps
                if (now > prevFrame + timeInterval) {
                    prevFrame = now;
                    boolean isWork = workProperty().get();
                    // draw the fractal
                    if (!fractalIsComplete || isWork) {
                        WritableImage im = getImageBuffer();
                        double h = complexFractalDrawer.getNumberDrawnRows();
                        double w = im.getWidth();

                        synchronized (im) {
                            canvas.getGraphicsContext2D().drawImage(im, 0, 0, w, h, 0, 0, w, h);
                        }
                    }
                    fractalIsComplete = !isWork;
                }
            }
        };

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
        this(canvas, fractal, new IterativePaletteSin());
    }


    /**
     * Get current palette.
     *
     * @return palette.
     */
    public synchronized IterativePalette getPalette() {
        return iterativePalette;
    }

    /**
     * Set new palette.
     *
     * @param iterativePalette palette
     */
    public synchronized void setPalette(IterativePalette iterativePalette) {
        Objects.requireNonNull(iterativePalette);
        if (this.iterativePalette == null || !this.iterativePalette.equals(iterativePalette)) {
            this.iterativePalette = iterativePalette;
            changed.set(true);
        }
    }

    /**
     * Get current complex fractal checker.
     *
     * @return complex fractal checker
     */
    public synchronized ComplexFractalChecker getFractal() {
        return complexFractalChecker;
    }

    /**
     * Set new complex fractal checker.
     *
     * @param complexFractalChecker complex fractal checker.
     */
    public synchronized void setFractal(ComplexFractalChecker complexFractalChecker) {
        Objects.requireNonNull(complexFractalChecker);
        if (this.complexFractalChecker == null || !this.complexFractalChecker.equals(complexFractalChecker)) {
            this.complexFractalChecker = complexFractalChecker;
            changed.set(true);
        }
    }

    /**
     * Get current transform of the axis.
     *
     * @return current transform
     */
    public synchronized Point2DTransformer getTransform() {
        return transform;
    }

    /**
     * Set current transform of the axis.
     *
     * @param transform new transform
     */
    public synchronized void setTransform(Point2DTransformer transform) {
        Objects.requireNonNull(transform);
        if (this.transform == null || !this.transform.equals(transform)) {
            this.transform = transform;
            changed.set(true);
        }
    }

    /**
     * Get image buffer
     *
     * @return image buffer
     */
    private synchronized WritableImage getImageBuffer() {
        return imageBuffer;
    }

    /**
     * Set new image buffer.
     *
     * @param imageBuffer image buffer
     */
    private synchronized void setImageBuffer(WritableImage imageBuffer) {
        this.imageBuffer = Objects.requireNonNull(imageBuffer);
        changed.set(true);
    }

    /**
     * Get width of current image buffer
     *
     * @return width
     */
    private synchronized int getImageBufferWidth() {
        return (int) getImageBuffer().getWidth();
    }

    /**
     * Get height of current image buffer
     *
     * @return height
     */
    private synchronized int getImageBufferHeigth() {
        return (int) getImageBuffer().getHeight();
    }

    /**
     * Resize buffer image to canvas size.
     */
    private void resizeImage() {
        // canvas can have zero size, because minimal size of the image limit 1 by 1
        int w = Math.max(1, (int) canvas.getWidth());
        int h = Math.max(1, (int) canvas.getHeight());
        setImageBuffer(new WritableImage(w, h));
    }

    /**
     * Check that image is changed.
     *
     * @return {@code true} if image (fractal) is changed otherwise {@code false}
     */
    private synchronized boolean isImageChanged() {
        return changed.get();
    }


    /**
     * Get status of the drawing.
     *
     * @return {@code true} if the fractal is drawing
     */
    final public boolean isWork() {
        return work.get();
    }

    /**
     * Get property indicated that fractal is drawing.
     *
     * @return property indicated calculating of the fractal
     */
    public ReadOnlyBooleanProperty workProperty() {
        return work.getReadOnlyProperty();
    }

    /**
     * This method can update work property in non javaFX thread.
     *
     * @param work value of the work property
     */
    private void updateWork(boolean work) {
        if (updateWorkValue.getAndSet(work) == null)
            Platform.runLater(() -> this.work.set(updateWorkValue.getAndSet(null)));
    }


    /**
     * Get resulting transform for image (with pixels coordinate);
     *
     * @return resulting transformer
     */
    private Point2DTransformer getResultingTransform() {
        return ImageUtils.calculateInitialTransform(getImageBufferWidth(), getImageBufferHeigth()).addAfter(getTransform());
    }


    /**
     * Perform translate of the fractal on canvas.
     *
     * @param dx x translate (in the pixels)
     * @param dy y translate (in the pixels)
     */
    public void translateImage(double dx, double dy) {
        Point2DTransformer resTr = getResultingTransform();
        Point2D p1 = resTr.apply(new Point2D(0, 0));
        Point2D p2 = resTr.apply(new Point2D(dx, dy));
        double dxFr = p1.getX() - p2.getX();
        double dyFr = p1.getY() - p2.getY();
        // perform translate
        setTransform(getTransform().translation(dxFr, dyFr));
    }

    /**
     * Set default scale of the image.
     */
    public void defaultScaleImage() {
        setTransform(Point2DTransformer.CLEAR);
    }

    /**
     * Change the scale of the image. The point with coordinate (x, y) doesn't move.
     *
     * @param xScale x scales
     * @param yScale y scales
     * @param x      x coordinate of the center scale at canvas
     * @param y      y coordinate of the center scale at canvas
     */
    public void scaleImage(double xScale, double yScale, double x, double y) {
        Point2D center = getResultingTransform().apply(new Point2D(x, y));
        setTransform(getTransform().scale(xScale, yScale, center));
    }

    /**
     * rotate image relative to the center of the image
     *
     * @param angle angle of rotate
     */
    public void rotateImage(double angle) {
        Point2D center = getResultingTransform().apply(new Point2D(getImageBufferWidth() / 2.0, getImageBufferHeigth() / 2.0));
        setTransform(getTransform().rotate(angle, center));
    }


    /**
     * Start drawing fractal with current settings. Drawing will interrupt if {@code changed} is set to true.
     */
    private void drawFractal() {
        // previous tread has ended because the setting is changed or
        // thread drew the fractal changed is set to true

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

        // draw preview of the fractal
        drawPreview(im, tr, cFrCh, itPl);

        // draw fractal
        drawFractalFull(im, tr, cFrCh, itPl);

        // if thread draw fractal fully then working has been finished
        if (!isImageChanged()) {
            updateWork(false);
        }
    }


    /**
     * Draw preview of the fractal with current setting. If {@code changed}  is set to true, than drawing is
     * interrupted.
     *
     * @param im  current image
     * @param tr  transform of the points
     * @param fCh checker of the fractal
     * @param pl  palette
     */
    private void drawPreview(WritableImage im, Point2DTransformer tr, ComplexFractalChecker fCh, IterativePalette pl) {
        // size of the main image
        int h = (int) im.getHeight();
        int w = (int) im.getWidth();
        // size of the preview image
        int hPr = edgeImagePreview;
        int wPr = edgeImagePreview;
        if (h > w) {
            hPr = (int) (hPr * ((double) h / (double) w));
        } else {
            wPr = (int) (wPr * ((double) w / (double) h));
        }

        // create the small preview image
        WritableImage previewImage = new WritableImage(wPr, hPr);
        ComplexFractalDrawer.drawFractal(previewImage, ImageUtils.calculateInitialTransform(wPr, hPr).addAfter(tr), fCh, pl);

        canvas.getGraphicsContext2D().drawImage(previewImage, 0, 0, w, h);

        //System.out.println("After canvas");

        /*
        // rescale image
        synchronized (im) {
            cachingBilinearInterpolation.interpolate(previewImage, im);
            //ImageUtils.bilinearInterpolation(previewImage, im);
        }*/
    }

    /**
     * Draw fractal completely. If {@code changed}  is set to true, than drawing is interrupted.
     *
     * @param im  current image
     * @param tr  transform of the point (with preTransform)
     * @param fCh checker of the fractal
     * @param pl  palette
     */
    private void drawFractalFull(WritableImage im, Point2DTransformer tr, ComplexFractalChecker fCh, IterativePalette pl) {
        // size of the main image
        int h = (int) im.getHeight();
        int w = (int) im.getWidth();

        complexFractalDrawer.setImage(im);
        complexFractalDrawer.drawFractal(ImageUtils.calculateInitialTransform(w, h).addAfter(tr), fCh, pl);
    }
}
