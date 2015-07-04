package local.fractal.util;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * The class {@code ImageUtils} contain some helper static methods for drawing.
 */
public class ImageUtils {
    /**
     * Create initial transform for image with {@code h} height and {@code w} width.
     * After this transform:
     * origin of coordinate will be in the center of the image;
     * the axis will be same scale;
     * the axes have minimal limits, but points (0, 1), (0, 1), (0, -1), (-1, 0) are contained with image;
     * x axis is from left to right;
     * y axis is from bottom to top.
     *
     * @param w width of the image
     * @param h height of the image
     */
    public static Point2DTransformer calculateInitialTransform(int w, int h) {
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
     * Bilinear interpolation of the image
     *
     * @param src source image
     * @param dst destination image
     */
    public static void bilinearInterpolation(WritableImage src, WritableImage dst) {
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

                // version 1 is quicker than version 2
            }
        }
    }

}
