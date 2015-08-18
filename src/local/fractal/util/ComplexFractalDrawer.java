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
import java.util.stream.IntStream;

/**
 * The class {@code ComplexFractalDrawer} draws fractal on the {@code WritableImage}.
 */
public class ComplexFractalDrawer {
    /**
     * Defines static of drawing (true if the fractal is being drawing, otherwise false).
     */
    private final ReadOnlyBooleanWrapper work = new ReadOnlyBooleanWrapper(false);
    /**
     * Defines status of completing of the drawing (from 0 to 1).
     */
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(0.0);

    /**
     * Number row of the image which has been drawn (this number is great or equal zero).
     */
    private volatile int numberDrawnRows = 0;
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
     * Calculates the row of the point of fractal image.
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
     * Draws the fractal on image.
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


    public final double getProgress() {
        synchronized (progress) {
            return progress.get();
        }
    }

    private void setProgress(double progress) {
        if (progress < 0.0 || progress > 1.0)
            throw new IllegalArgumentException("Illegal progress value");
        synchronized (this.progress) {
            this.progress.set(progress);
        }
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    public final boolean isWork() {
        synchronized (work) {
            return work.get();
        }
    }

    private void setWork(boolean work) {
        synchronized (this.work) {
            this.work.set(work);
        }
    }

    public ReadOnlyBooleanProperty workProperty() {
        return work.getReadOnlyProperty();
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

    /**
     * Gets number rows that has been drawn.
     *
     * @return number of the rows.
     */
    public int getNumberDrawnRows() {
        return numberDrawnRows;
    }

    /**
     * Draws the fractal. If image will be set new image when fractal is drawing then method continues to draw on the
     * old image. It's possible to get status of drawing image in the other thread.
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
        numberDrawnRows = 0;
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
            numberDrawnRows++;
        }
        // draw has been ended
        setWork(false);
        // reset progress if calculation has been canceled
        if (!isPermitWork()) {
            setProgress(0.0);
        }
    }
}
