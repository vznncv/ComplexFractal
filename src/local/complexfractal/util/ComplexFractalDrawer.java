package local.complexfractal.util;

import javafx.beans.property.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import local.complexfractal.model.ComplexFractalChecker;

import java.util.Objects;
import java.util.stream.IntStream;

/**
 * The class {@code ComplexFractalDrawer} draws fractal on the {@link javafx.scene.image.WritableImage}.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class ComplexFractalDrawer {
    /**
     * Defines status of completing of the drawing (from 0 to 1).
     * <p>
     * This property can be used by threads not drawing the fractal. Use setter and getter for thread-safe operation.
     */
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(0.0);

    /**
     * Defines number row of the image which has been drawn (this number is great or equal zero).
     * <p>
     * This property can be used by threads not drawing the fractal. Use setter and getter for thread-safe operation.
     */
    private final ReadOnlyIntegerWrapper numberDrawnRows = new ReadOnlyIntegerWrapper(0);

    /**
     * Defines allowance for drawing the fractal. Uses for premature stopping drawing the fractal.
     * <p>
     * This property can be used by threads not drawing the fractal. Use setter and getter for thread-safe operation.
     */
    private final BooleanProperty permitWork = new SimpleBooleanProperty(true);

    /**
     * Mutex for setters and getters of the progress, numberDrawnRows and permitWork properties.
     */
    private final Object mutex = new Object();

    /**
     * Indicator of the drawing process of the fractal.
     */
    private volatile boolean work = false;


    /**
     * Image for drawing. When pixels are being drawn on image, the image is blocked with {@code synchronized}.
     */
    private volatile WritableImage image;


    /**
     * Default constructor.
     */
    public ComplexFractalDrawer() {
    }

    /**
     * Calculates the row of the point for fractal image.
     *
     * @param numLine   y coordinate of the row
     * @param lineWidth width of the row
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

        return IntStream
                // generate line of the points from (numLine, 0) to (numLine, lineWidth - 1)
                .range(0, lineWidth).parallel().mapToObj(colNum -> new Point2D(colNum, numLine))
                        // perform affine transform of the points
                .map(resTr::apply)
                        // calculate number of the iteration for points
                .mapToInt(fCh::numberIter)
                        // map number of the iteration to color
                .mapToObj(pl::numIterToColor)
                        // get result as array
                .toArray(Color[]::new);
    }

    /**
     * Draws the fractal on image. This method is used when there isn't necessary that other threads is observing the
     * process of the drawing.
     *
     * @param image image
     * @param resTr transform matrix for the points of the image
     * @param fCh   checker of the fractal
     * @param pl    palette
     */
    public static void drawFractal(WritableImage image, Point2DTransformer resTr, ComplexFractalChecker fCh, IterativePalette pl) {
        // size of the image
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();

        // draw the fractal
        PixelWriter pw = image.getPixelWriter();
        for (int i = 0; i < h; i++) {
            // calculate the line of the fractal
            Color[] colors = calculateLine(i, w, resTr, fCh, pl);
            // draw line
            for (int j = 0; j < w; j++) {
                pw.setColor(j, i, colors[j]);
            }
        }
    }

    /**
     * Create initial transform for image with {@code h} height and {@code w} width.
     * <p>
     * After this transform: <ul>
     * <li>origin of coordinate will be in the center of the image;</li>
     * <li>the axes have same scale;</li>
     * <li>the axes have such scale that points (1, 1), (1, -1), (-1, -1), (-1, -1) are on border of the image (square
     * in the center of the image with edge size 2 is guarantee placed on the image);</li>
     * <li>x axis is from left to right</li>
     * <li>y axis is from bottom to top.</li>
     * </ul>
     *
     * @param w width of the image
     * @param h height of the image
     * @return initial transform
     */
    public static Point2DTransformer calculateInitialTransform(double w, double h) {
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
            preTr = preTr.translation(-minR + centerX, -minR * (h / w) + centerY);
        } else {
            // scale coordinate
            double scale = 2 * minR / h;
            preTr = preTr.scale(scale, scale);
            // move the center of the coordinate
            preTr = preTr.translation(-minR * (w / h) + centerX, -minR + centerY);
        }
        // image has x axis from left to right and
        // y axis from top to bottom.
        // modify the direction of y coordinate from bottom to top
        preTr = preTr.scale(1, -1);

        return preTr;
    }


    public final double getProgress() {
        synchronized (mutex) {
            return progress.get();
        }
    }

    private void setProgress(double progress) {
        if (progress < 0.0 || progress > 1.0)
            throw new IllegalArgumentException("Illegal progress value");
        synchronized (mutex) {
            this.progress.set(progress);
        }
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    public final int getNumberDrawnRows() {
        synchronized (mutex) {
            return numberDrawnRows.get();
        }
    }

    private final void setNumberDrawnRows(int numberDrawnRows) {
        synchronized (mutex) {
            this.numberDrawnRows.set(numberDrawnRows);
        }
    }

    public ReadOnlyIntegerProperty numberDrawnRowsProperty() {
        return numberDrawnRows.getReadOnlyProperty();
    }

    public final boolean isPermitWork() {
        synchronized (mutex) {
            return permitWork.get();
        }
    }

    public final void setPermitWork(boolean permitWork) {
        synchronized (mutex) {
            this.permitWork.set(permitWork);
        }
    }

    public BooleanProperty permitWorkProperty() {
        return permitWork;
    }

    /**
     * Gets current image. When pixels are being drawn on image, the image is blocked with {@code synchronized}.
     *
     * @return image
     */
    public WritableImage getImage() {
        return image;
    }

    /**
     * Sets image for drawing.
     *
     * @param image image for drawing
     */
    public void setImage(WritableImage image) {
        this.image = Objects.requireNonNull(image);
    }

    /**
     * Draws the fractal. If new image will be set when the fractal is being drawn then method continues to draw on the
     * old image. It's possible to get status of the drawing progress in the other thread.
     *
     * @param resTr transform matrix for the points of the image
     * @param fCh   checker of the fractal
     * @param pl    palette
     */
    public void drawFractal(Point2DTransformer resTr, ComplexFractalChecker fCh, IterativePalette pl) {
        if (image == null)
            throw new IllegalStateException("image isn't set");
        if (work)
            throw new IllegalStateException("image is being drawing");

        // prepare for new drawing
        setProgress(0.0);
        setNumberDrawnRows(0);
        work = true;
        WritableImage currentImage = image;
        boolean continueDrawing = isPermitWork();

        // size of the image
        int w = (int) currentImage.getWidth();
        int h = (int) currentImage.getHeight();

        // draw the fractal
        // current line for drawing the fractal
        int i = 0;

        // draw the fractal
        while (i < h && continueDrawing) {
            // calculate the line of the fractal
            Color[] colors = calculateLine(i, w, resTr, fCh, pl);
            // draw line
            synchronized (currentImage) {
                PixelWriter pw = currentImage.getPixelWriter();
                for (int j = 0; j < w; j++) {
                    pw.setColor(j, i, colors[j]);
                }
            }
            // go to the next line
            i++;
            synchronized (mutex) {
                // update progress
                setProgress((double) i / (double) h);
                setNumberDrawnRows(i);

                continueDrawing = isPermitWork();
            }
        }
        // draw has been ended
        work = false;
        // reset progress if calculation has been canceled
        if (!isPermitWork()) {
            setProgress(0.0);
            setNumberDrawnRows(0);
        }
    }
}
