package local.fractal.util;

import javafx.scene.paint.Color;

import java.util.Objects;
import java.util.function.DoubleFunction;

/**
 * Simple palette for fractal.
 * <p>
 * Created by konstantin on 12.02.15.
 */
public class IterativePaletteV1 implements IterativePalette {
    // function with next sketch
    //    /|\   _       _
    // 1.0 |   / \     / \
    //     |  /   \   /   \
    //     |_/     \_/     \_/
    //     0   255 511 767 1023
    private DoubleFunction<Double> wrapSin = (numIteration) ->
            Math.sin(numIteration * (1.0 / 255.0 * Math.PI / 2.0) - Math.PI / 2.0) / 2.0 + 0.5;
    // constant of increase for colors
    private double mulR;
    private double mulG;
    private double mulB;
    // color of the Fractal
    private Color fractalColor;

    /**
     * Create palette with given parameters.
     *
     * @param fractalColor color of the fractal
     * @param mulR         multiple constant for red color
     * @param mulG         multiple constant for greed color
     * @param mulB         multiple constant for blue color
     */
    public IterativePaletteV1(Color fractalColor, double mulR, double mulG, double mulB) {
        this.fractalColor = Objects.requireNonNull(fractalColor, "fractalColor is null");
        this.mulR = mulR;
        this.mulB = mulB;
        this.mulG = mulG;
    }

    /**
     * Create default palette.
     */
    public IterativePaletteV1() {
        this(Color.BLACK, 6.8, 4.9, 11.1);
    }

    /**
     * Covert number of the iteration to Color.
     *
     * @param numIter number of the iteration
     * @return color
     */
    @Override
    public Color numIterToColor(int numIter) {
        // return color
        return (numIter == 0 ? fractalColor : Color.color(
                wrapSin.apply(numIter * mulR),
                wrapSin.apply(numIter * mulG),
                wrapSin.apply(numIter * mulB)));
    }
}
