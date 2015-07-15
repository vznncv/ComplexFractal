package local.fractal.util;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import local.fractal.model.ComplexFractalChecker;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * The class {@code ComplexFractalDrawer} renders fractal on the {@code WritableImage}.
 */
public class ComplexFractalDrawer {
    /**
     * Indicator of the drawing.
     */
    private final ReadOnlyBooleanWrapper work = new ReadOnlyBooleanWrapper(false);
    /**
     * Indicator of the completing of the drawing (from 0 to 1).
     */
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(0.0);
    /**
     * Image for drawing. When pixels are being drawn on image, the image is blocked.
     */
    private volatile WritableImage image;
    /**
     * It's mark that allow draw the fractal.
     */
    private volatile boolean permitWork;


    /**
     * Default constructor.
     */
    public ComplexFractalDrawer() {
        setPermitWork(true);
    }


    /**
     * Calculate line of the point of the fractal for drawing.
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
     * Draw the fractal on image.
     * This method will return control after it draws the fractal.
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
        for (int i = 1; i < h; i++) {
            // calculate the line of the fractal
            Color[] colors = calculateLine(i, w, resTr, fCh, pl);
            // draw line
            for (int j = 0; j < w; j++) {
                pw.setColor(j, i, colors[j]);
            }
        }

    }


    /**
     * Get current image.
     * When pixels are being drawn on image, the image is blocked with {@code synchronized}.
     *
     * @return image
     */
    public WritableImage getImage() {
        return image;
    }

    /**
     * Set image for drawing.
     *
     * @param image image for drawing
     */
    public void setImage(WritableImage image) {
        this.image = Objects.requireNonNull(image);
    }

    /**
     * Draw the fractal.
     * If image will be set new image when fractal is drawing then method continues to draw on the old image.
     *
     * @param resTr transform matrix for the points of the image
     * @param fCh   checker of the fractal
     * @param pl    palette
     */
    public void drawFractal(Point2DTransformer resTr, ComplexFractalChecker fCh, IterativePalette pl) {
        if (image == null)
            throw new IllegalStateException("image isn't set");
        if (isWork())
            throw new IllegalStateException("image is being drawing");

        // prepare for new drawing
        setProgress(0.0);
        setWork(true);
        WritableImage currentImage = image;

        // size of the image
        int w = (int) currentImage.getWidth();
        int h = (int) currentImage.getHeight();

        // draw the fractal
        // current line for drawing the fractal
        int i = 0;

        // draw the fractal
        while (i < h && isPermitWork()) {
            // calculate the line of the fractal
            Color[] colors = calculateLine(i, w, resTr, fCh, pl);
            // draw line
            synchronized (currentImage) {
                if (isPermitWork()) {
                    PixelWriter pw = currentImage.getPixelWriter();
                    for (int j = 0; j < w; j++) {
                        pw.setColor(j, i, colors[j]);
                    }
                }
            }
            // go to the next line
            i++;
            // update the progress
            setProgress((double) i / (double) h);
        }
        // draw has been ended
        setWork(false);
        // reset progress if calculation has been canceled
        if (!isPermitWork()) {
            setProgress(0.0);
        }
    }


    /**
     * Get progress of the drawing the of fractal.
     *
     * @return status (from 0.0 to 1.0)
     */
    public final double getProgress() {
        synchronized (progress) {
            return progress.get();
        }
    }

    /**
     * Set progress of the drawing of the fractal.
     *
     * @param progress status (from 0.0 to 1.0)
     */
    private void setProgress(double progress) {
        if (progress < 0.0 || progress > 1.0)
            throw new IllegalArgumentException("Illegal progress value");
        synchronized (this.work) {
            this.progress.set(progress);
        }
    }

    /**
     * Get progress property. This property has range [0, 1], and show part of completed work.
     *
     * @return progress property
     */
    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    /**
     * Drawing is being.
     *
     * @return {@code true} if fractal is being drawn otherwise {@code false}
     */
    public final boolean isWork() {
        synchronized (work) {
            return work.get();
        }
    }

    /**
     * Set status of the work.
     *
     * @param work this value must be {@code true} if fractal is being drawn, otherwise {@code false}
     */
    private void setWork(boolean work) {
        synchronized (this.work) {
            this.work.set(work);
        }
    }

    /**
     * Get work property.
     *
     * @return work property
     */
    public ReadOnlyBooleanProperty workProperty() {
        return work.getReadOnlyProperty();
    }

    /**
     * This object can drawing the fractal?
     *
     * @return {@code true} if it can otherwise {@code false}
     */
    public boolean isPermitWork() {
        return permitWork;
    }

    /**
     * Forbid/allow work.
     *
     * @param permitWork {@code true} if work allow, otherwise {@code false}
     */
    public void setPermitWork(boolean permitWork) {
        this.permitWork = permitWork;
    }
}
