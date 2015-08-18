package local.fractal.util;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * A class {@code CachingBilinearInterpolation} realizes bilinear interpolation with caching some calculation for
 * increasing the performance.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class CachingBilinearInterpolation {
    // Caching information for every pixel of a destination image.
    // Information stores in the one dimensional array row by row.
    private PointInfo cachingInfo[];
    // size of the last source image
    private int srcW = 0;
    private int srcH = 0;
    // size of the last destination image
    private int dstW = 0;
    private int dstH = 0;

    /**
     * Finds near neighbour on the left (down) on the axis of source image for pixels of the destination image.
     *
     * @param srcLen length (width of height) of source image (must be great than zero)
     * @param dstLen length (width of height) of destination image (must be great than zero)
     * @return indexes and distances for neighbour
     */
    private static Pair<int[], float[]> findNearNeighbour(int srcLen, int dstLen) {
        // distances to the near left (down) point
        float distances[] = new float[dstLen];
        // indexes of the near left (down) point
        int indexes[] = new int[dstLen];
        if (dstLen > 1) {
            float scale = (float) (srcLen - 1) / (float) (dstLen - 1);
            for (int i = 0; i < dstLen - 1; i++) {
                float x = i * scale;
                indexes[i] = (int) x;
                distances[i] = x - (float) Math.floor(x);
            }
            // special case for pixels of the right border for convenience
            // (don't use the most right (up) neighbour)
            if (srcLen > 1) {
                indexes[dstLen - 1] = srcLen - 2;
                distances[dstLen - 1] = 1;
            } else {
                indexes[dstLen - 1] = 0;
                distances[dstLen - 1] = 0;
            }
        } else {
            indexes[0] = (srcLen - 1) / 2;
            distances[0] = (srcLen - 1) / 2.0f - indexes[0];
        }

        return new Pair<>(indexes, distances);
    }

    /**
     * Performs bilinear interpolation.
     *
     * @param src source image
     * @param dst destination image
     */
    public synchronized void interpolate(WritableImage src, WritableImage dst) {
        // check caching information and update it if it's needed
        checkCachingInfo(src, dst);

        // writers and readers of the images
        PixelWriter dstPw = dst.getPixelWriter();
        PixelReader srcPr = src.getPixelReader();

        // mask for colors
        final int rMask = 0x00FF0000;
        final int gMask = 0x0000FF00;
        final int bMask = 0x000000FF;
        final int aMask = 0xFF000000;

        // case of source image is column
        if (srcH > 1 && srcW > 1) {
            // common case
            for (int i = 0; i < dstH; i++)
                for (int j = 0; j < dstW; j++) {
                    PointInfo pInf = cachingInfo[i * dstW + j];
                    int a = srcPr.getArgb(pInf.xA, pInf.yA);
                    int b = srcPr.getArgb(pInf.xA + 1, pInf.yA);
                    int c = srcPr.getArgb(pInf.xA, pInf.yA + 1);
                    int d = srcPr.getArgb(pInf.xA + 1, pInf.yA + 1);

                    int colR = (int) ((a & rMask) * pInf.rA + (b & rMask) * pInf.rB + (c & rMask) * pInf.rC + (d & rMask) * pInf.rD) & rMask;
                    int colG = (int) ((a & gMask) * pInf.rA + (b & gMask) * pInf.rB + (c & gMask) * pInf.rC + (d & gMask) * pInf.rD) & gMask;
                    int colB = (int) ((a & bMask) * pInf.rA + (b & bMask) * pInf.rB + (c & bMask) * pInf.rC + (d & bMask) * pInf.rD) & bMask;

                    dstPw.setArgb(j, i, aMask | colR | colG | colB);
                }
        } else if (srcH > 1 && srcW == 1) {
            // source image is column
            for (int i = 0; i < dstH; i++)
                for (int j = 0; j < dstW; j++) {
                    PointInfo pInf = cachingInfo[i * dstW + j];
                    int a = srcPr.getArgb(pInf.xA, pInf.yA);
                    int c = srcPr.getArgb(pInf.xA, pInf.yA + 1);

                    int colR = (int) ((a & rMask) * pInf.rA + (c & rMask) * pInf.rC) & rMask;
                    int colG = (int) ((a & gMask) * pInf.rA + (c & gMask) * pInf.rC) & gMask;
                    int colB = (int) ((a & bMask) * pInf.rA + (c & bMask) * pInf.rC) & bMask;

                    dstPw.setArgb(j, i, aMask | colR | colG | colB);
                }
        } else if (srcH == 1 && srcW > 1) {
            // source image is row
            for (int i = 0; i < dstH; i++)
                for (int j = 0; j < dstW; j++) {
                    PointInfo pInf = cachingInfo[i * dstW + j];
                    int a = srcPr.getArgb(pInf.xA, pInf.yA);
                    int b = srcPr.getArgb(pInf.xA + 1, pInf.yA);

                    int colR = (int) ((a & rMask) * pInf.rA + (b & rMask) * pInf.rB) & rMask;
                    int colG = (int) ((a & gMask) * pInf.rA + (b & gMask) * pInf.rB) & gMask;
                    int colB = (int) ((a & bMask) * pInf.rA + (b & bMask) * pInf.rB) & bMask;

                    dstPw.setArgb(j, i, aMask | colR | colG | colB);
                }
        } else if (srcH <= 1 && srcW <= 1) {
            // source image is empty or point
            int color = (srcH == 0 || srcW == 0 ? aMask : srcPr.getArgb(0, 0));
            for (int i = 0; i < dstH; i++)
                for (int j = 0; j < dstW; j++) {
                    dstPw.setArgb(j, i, color);
                }
        }


    }

    /**
     * Checks caching information and updates if it's needed
     *
     * @param src source image
     * @param dst destination image
     */
    private void checkCachingInfo(WritableImage src, WritableImage dst) {
        int srcWNew = (int) src.getWidth();
        int srcHNew = (int) src.getHeight();
        int dstWNew = (int) dst.getWidth();
        int dstHNew = (int) dst.getHeight();
        if (srcHNew < 0 || srcWNew < 0)
            throw new IllegalArgumentException("Uncorrected size of the source image");
        if (dstWNew < 0 || dstHNew < 0)
            throw new IllegalArgumentException("Uncorrected size of the destination image");

        boolean srcIsChanged = srcWNew != srcW || srcHNew != srcH;
        boolean dstIsChanged = dstWNew != dstW || dstHNew != dstH;

        if (dstIsChanged) {
            dstW = dstWNew;
            dstH = dstHNew;

            cachingInfo = new PointInfo[dstW * dstH];
            for (int index = 0; index < dstW * dstH; index++) {
                cachingInfo[index] = new PointInfo();
            }
        }

        if (dstIsChanged) {
            srcW = srcWNew;
            srcH = srcHNew;
        }

        // calculate coefficient if destination and source image aren't empty
        if ((dstIsChanged || srcIsChanged) && dstW > 0 && dstH > 0 && srcW > 0 && srcH > 0) {
            // data for interpolation along x and y axis
            Pair<int[], float[]> xNeighbours = findNearNeighbour(srcW, dstW);
            Pair<int[], float[]> yNeighbours = findNearNeighbour(srcH, dstH);

            // update caching data
            for (int i = 0; i < dstH; i++) {
                for (int j = 0; j < dstW; j++) {
                    PointInfo pInf = cachingInfo[i * dstW + j];
                    float dX = xNeighbours.second[j];
                    float dY = yNeighbours.second[i];
                    pInf.xA = xNeighbours.first[j];
                    pInf.yA = yNeighbours.first[i];
                    pInf.rA = (1 - dX) * (1 - dY);
                    pInf.rB = dX * (1 - dY);
                    pInf.rC = (1 - dX) * dY;
                    pInf.rD = dX * dY;
                }
            }
        }
    }

    /**
     * Helper class for to store caching information.
     */
    private final static class PointInfo {
        // /|\
        //  |  C    D
        //  |
        //  |     T
        //  |  A    B  \
        // -|-----------
        //  0          /
        // A, B, C, D - points of the source image
        // T - point of the destination image

        // coordinates of the A point
        int xA = 0;
        int yA = 0;
        // weight coefficients of the points of the source image
        float rA = 1.0f;
        float rB = 0.0f;
        float rC = 0.0f;
        float rD = 0.0f;
    }

    /**
     * Helper class for to return pair of the argument
     */
    private final static class Pair<F, S> {
        F first;
        S second;

        Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
}
