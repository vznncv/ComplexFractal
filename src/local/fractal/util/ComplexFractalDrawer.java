package local.fractal.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import local.fractal.model.ComplexFractalChecker;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * This class render fractal and save it in the writable image.
 */
public class ComplexFractalDrawer {

    /**
     * It's minimal size of the height and width of preview image of the fractal
     */
    private final int edgeImagePreview = 40;
    /**
     * Thread pool for drawing fractal.
     */
    final private ExecutorService singlePool;
    /**
     * image with fractal
     */
    private WritableImage image;
    /**
     * size of the image
     */
    private volatile int width;
    private volatile int height;
    /**
     * This transform move initial origin of the coordinate thus center of the image has
     * coordinate (centerX, centerY) and:
     * points (centerX - minR, centerY), (centerX + minR, centerY) lays on the edges of the image,
     * if image.getWidth() <= image.getHeight();
     * points (centerX, centerY - minR), (centerX, centerY + minR) lays on the edges of the image,
     * if image.getWidth() >= image.getHeight();
     * X axis is directed from left to right;
     * Y axis is directed  from bottom to top.
     */
    private volatile Point2DTransformer preTransform;
    /**
     * Current affine transform of the complex plane.
     * Apply after preTransform.
     */
    private volatile Point2DTransformer transform;
    /**
     * It's indicator that fractal is drawing.
     */
    private BooleanProperty work;
    /**
     * It's indicator that fractal is changed and image need to update.
     */
    private BooleanProperty changed;
    /**
     * Settings of drawing the fractal.
     */
    private volatile ComplexFractalChecker complexFractalChecker;
    private volatile IterativePalette iterativePalette;


    /**
     * Constructor.
     *
     * @param width                 width of the image of the fractal
     * @param height                height of the image of the fractal
     * @param complexFractalChecker object for checking point to belong the fractal
     * @param iterativePalette      palette for coloring the fractal and point which doesn't belong it.
     */
    public ComplexFractalDrawer(int width, int height, ComplexFractalChecker complexFractalChecker, IterativePalette iterativePalette) {
        // initialize property
        work = new SimpleBooleanProperty(false);
        changed = new SimpleBooleanProperty(false);

        // set fractal and palette
        setComplexFractalChecker(complexFractalChecker);
        setIterativePalette(iterativePalette);

        // initialize pool
        singlePool = Executors.newSingleThreadExecutor((task) -> {
            Thread t = new Thread(task);
            t.setDaemon(true);
            return t;
        });
        // initialize transform (when transform changed, image needs to be updated)
        transform = Point2DTransformer.CLEAR;

        // create image
        resizeImage(width, height);

        // when changed is set to the true, start redrawing the image
        changed.addListener((obj, oldVal, newVal) -> {
            if (newVal) {
                singlePool.execute(this::drawFractal);
            }
        });

        // start drawing of the fractal
        changed.set(false);
        changed.set(true);
    }

    /**
     * Calculate line of the point of the fractal.
     *
     * @param numLine   y coordinate of the line
     * @param lineWidth width of the line
     * @param resTr     transform matrix for the point
     * @param fCh       checker of the fractal
     * @param pl        palette
     * @return calculated colors
     */
    private static Color[] calculateLine(int numLine, int lineWidth, Point2DTransformer resTr, ComplexFractalChecker fCh, IterativePalette pl) {
        if (numLine < 0)
            throw new IllegalArgumentException("numLine < 0");
        if (lineWidth <= 0)
            throw new IllegalArgumentException("lineWidth <= 0");

        return Stream
                // generate line of the points from (numLine, 0) to (numLine, lineWidth - 1)
                .iterate(new Point2D(0, numLine), (p) -> new Point2D(p.getX() + 1, p.getY())).limit(lineWidth)
                        // create the parallel stream for increase of the performance
                .parallel()
                        // perform affine transform of the points
                .map(resTr::apply)
                        // calculate number of the iteration for points
                .mapToInt((c) -> fCh.numberIter(c.toComplex()))
                        // map number of the iteration to color
                .mapToObj(pl::numIterToColor)
                        // get result as array
                .toArray(Color[]::new);
    }

    /**
     * Bilinear interpolation of the image
     *
     * @param src source image
     * @param dst destination image
     */
    private static void bilinearInterpolation(WritableImage src, WritableImage dst) {
        // height and the width of the images
        int srcH = (int) src.getHeight();
        int srcW = (int) src.getWidth();
        int dstH = (int) dst.getHeight();
        int dstW = (int) dst.getWidth();
        // writers and readers of the images
        PixelWriter dstPw = dst.getPixelWriter();
        PixelReader srcPr = src.getPixelReader();

        // scaling ratios
        final float xScale = (srcW - 1.0f) / (dstW);
        final float yScale = (srcH - 1.0f) / (dstH);

        for (int i = 0; i < dstH; i++) {
            for (int j = 0; j < dstW; j++) {
                // index of the left-top point
                int xSrc = (int) (j * xScale);
                int ySrc = (int) (i * yScale);
                // ratio coefficient of left-point
                float ratioX = (j * xScale) - xSrc;
                float ratioY = (i * yScale) - ySrc;
                // get points
                // A   B
                //
                // C   D
                Color A = srcPr.getColor(xSrc, ySrc);
                Color B = srcPr.getColor(xSrc + 1, ySrc);
                Color C = srcPr.getColor(xSrc, ySrc + 1);
                Color D = srcPr.getColor(xSrc + 1, ySrc + 1);
                // weight coefficients of the points
                float rA = (1 - ratioX) * (1 - ratioY);
                float rB = ratioX * (1 - ratioY);
                float rC = (1 - ratioX) * ratioY;
                float rD = ratioX * ratioY;
                // calculate new color

                // version 1
                int r = (int) Math.round(255 *
                        (A.getRed() * rA + B.getRed() * rB + C.getRed() * rC + D.getRed() * rD));
                int g = (int) Math.round(255 *
                        (A.getGreen() * rA + B.getGreen() * rB + C.getGreen() * rC + D.getGreen() * rD));
                int b = (int) Math.round(255 *
                        (A.getBlue() * rA + B.getBlue() * rB + C.getBlue() * rC + D.getBlue() * rD));
                dstPw.setArgb(j, i, 0xFF << 24 | (r << 16) | (g << 8) | b);

                // version 2
                //dstPw.setColor(j, i, Color.color(
                //        A.getRed() * rA + B.getRed() * rB + C.getRed() * rC + D.getRed() * rD,
                //        A.getGreen() * rA + B.getGreen() * rB + C.getGreen() * rC + D.getGreen() * rD,
                //        A.getBlue() * rA + B.getBlue() * rB + C.getBlue() * rC + D.getBlue() * rD
                //));

                // version 1 is more quick than version 2 (~2 times)
            }
        }


    }

    /**
     * Create preliminary transform for image with {@code h} height and {@code w} width.
     * Origin of it coordinate will be in the center of the image.
     *
     * @param w width of the image
     * @param h height of the image
     */
    private static Point2DTransformer getInitialTransform(int w, int h) {
        // coordinate of the center of the image
        final double centerX = 0;
        final double centerY = 0;
        // maximum radius of the circle that may be placed on the image.
        final double minR = 1;

        if (h <= 0)
            throw new IllegalArgumentException("h <= 0");
        if (w <= 0)
            throw new IllegalArgumentException("w <= 0");

        Point2DTransformer preTr = Point2DTransformer.CLEAR;
        if (h > w) {
            // scale coordinate
            double scale = 2 * minR / w;
            preTr = preTr.scale(scale, scale);
            // move the center of the coordinate
            preTr = preTr.translation(-minR + centerX, -minR * ((double) h / (double) w) + centerY);
        } else {
            // scale coordinate
            double scale = 2 * minR / h;
            preTr = preTr.scale(scale, scale);
            // move the center of the coordinate
            preTr = preTr.translation(-minR * ((double) w / (double) h) + centerX, -minR + centerY);
        }
        // image has x axis from left to right and
        // y axis from top to bottom.
        // modify the direction of y coordinate from bottom to top
        preTr.scale(1, -1);

        return preTr;
    }

    /**
     * Get current image. Image may be incomplete. This image may be changed.
     * If it require that image isn't modified other threads, synchronize the object that return this image.
     *
     * @return image
     */
    public synchronized WritableImage getCurrentImage() {
        return image;
    }

    /**
     * Get completed image. If image isn't completed than thread will be blocked until image isn't completed.
     *
     * @return image
     */
    public WritableImage getCompleteImage() {
        throw new UnsupportedOperationException("Operation doesn't support yet.");
    }

    /**
     * Get width of the current image.
     *
     * @return width
     */
    public int getImageWidth() {
        return width;
    }

    /**
     * Get height of the current image.
     *
     * @return height
     */
    public int getImageHeight() {
        return height;
    }

    /**
     * Resize image.
     *
     * @param w new width of the image
     * @param h new height of the image
     */
    public synchronized void resizeImage(int w, int h) {
        // get new initial transform
        preTransform = getInitialTransform(w, h);
        // create new image
        image = new WritableImage(w, h);
        // save width and height of the image
        height = h;
        width = w;
        // redraw image
        changed.set(true);
    }

    /**
     * Get fractal checker.
     *
     * @return fractal checker
     */
    public ComplexFractalChecker getComplexFractalChecker() {
        return complexFractalChecker;
    }

    /**
     * Set fractal checker.
     *
     * @param complexFractalChecker fractal checker
     */
    public synchronized void setComplexFractalChecker(ComplexFractalChecker complexFractalChecker) {
        this.complexFractalChecker = Objects.requireNonNull(complexFractalChecker, "complexFractalChecker is null");
        changed.set(true);
    }

    /**
     * Get iterative palette.
     *
     * @return iterative palette
     */
    public IterativePalette getIterativePalette() {
        return iterativePalette;
    }

    /**
     * Set iterative palette.
     *
     * @param iterativePalette iterative palette
     */
    public synchronized void setIterativePalette(IterativePalette iterativePalette) {
        this.iterativePalette = Objects.requireNonNull(iterativePalette, "iterativePalette is null");
        changed.set(true);
    }

    /**
     * Get current transform of the axis.
     *
     * @return current transform
     */
    public Point2DTransformer getTransform() {
        return transform;
    }

    /**
     * Set current transform of the axis.
     *
     * @param transform new transform
     */
    public void setTransform(Point2DTransformer transform) {
        Objects.requireNonNull(transform, "transform is null");
        if (!this.transform.equals(transform)) {
            this.transform = transform;
            synchronized (this) {
                changed.set(true);
            }
        }
    }


    /**
     * Get coordinate of the center of the image.
     *
     * @return coordinate of the center of the image after transformation.
     */
    public Point2D getCenterCorrdinate() {
        return transform.apply(
                preTransform.apply(
                        new Point2D(getImageWidth() / 2.0, getImageHeight() / 2.0)
                ));
    }

    /**
     * Get resulting transform for image.
     *
     * @return resulting transformer
     */
    public Point2DTransformer getResultingTransform() {
        return preTransform.addAfter(transform);
    }

    /**
     * Get iterative palette.
     *
     * @return iterative palette
     */
    public synchronized ReadOnlyBooleanProperty workProperty() {
        return work;
    }

    /**
     * Start drawing fractal with current settings.
     */
    private void drawFractal() {
        // previous tread has ended because the setting is changed or
        // thread drew the fractal

        // current setting of the fractal
        ComplexFractalChecker cFrCh;
        IterativePalette itPl;
        Point2DTransformer tr;
        Point2DTransformer resTr;
        int w;
        int h;
        synchronized (this) {
            // get current setting of the fractal
            w = getImageWidth();
            h = getImageHeight();
            cFrCh = getComplexFractalChecker();
            itPl = getIterativePalette();
            tr = getTransform();
            resTr = getResultingTransform();
            // changes has accepted to processing
            changed.set(false);
            // start working
            work.set(true);
        }

        // draw preview of the fractal
        drawPreview(tr, cFrCh, itPl);

        // draw fractal
        drawFractalFull(w, h, resTr, cFrCh, itPl);

        synchronized (this) {
            // if thread draw fractal fully then working has been finished
            if (!changed.get()) {
                work.set(false);
            }
        }
    }

    /**
     * Draw preview of the fractal.
     *
     * @param tr  transform of the point (without preTransform)
     * @param fCh checker of the fractal
     * @param pl  palette
     */
    private synchronized void drawPreview(Point2DTransformer tr, ComplexFractalChecker fCh, IterativePalette pl) {
        // size of the main image
        int h = (int) image.getHeight();
        int w = (int) image.getWidth();
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
        PixelWriter pwPreview = previewImage.getPixelWriter();
        Point2DTransformer resTr = getInitialTransform(wPr, hPr).addAfter(tr);
        for (int i = 0; i < hPr; i++) {
            Color[] colors = calculateLine(i, wPr, resTr, fCh, pl);
            for (int j = 0; j < wPr; j++) {
                pwPreview.setColor(j, i, colors[j]);
            }
        }

        // rescale image
        synchronized (this) {
            bilinearInterpolation(previewImage, image);
        }
    }

    /**
     * Draw preview of the fractal. Drawing is interrupted, if fractal will be changed.
     *
     * @param w   width of the image
     * @param h   height of the image
     * @param tr  transform of the point (with preTransform)
     * @param fCh checker of the fractal
     * @param pl  palette
     */
    private void drawFractalFull(int w, int h, Point2DTransformer tr, ComplexFractalChecker fCh, IterativePalette pl) {
        // current line for drawing the fractal
        int i = 0;

        // draw the fractal
        while (i < h && !isChanged()) {
            // calculate the line of the fractal
            Color[] colors = calculateLine(i, w, tr, fCh, pl);
            // draw line
            synchronized (this) {
                if (!isChanged()) {
                    PixelWriter pw = image.getPixelWriter();
                    for (int j = 0; j < w; j++) {
                        pw.setColor(j, i, colors[j]);
                    }
                }
            }
            // go to the next line
            i++;
        }
    }


    /**
     * Is fractal changed?
     *
     * @return true, if fractal has been changed, else false
     */
    private synchronized boolean isChanged() {
        return changed.get();
    }

}
