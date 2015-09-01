package local.complexfractal.util;

import javafx.scene.paint.Color;

import java.util.Objects;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

/**
 * The {@code IterativePaletteSin} represents simple sinusoidal palette for fractal.
 * <p>
 * This class is immutable.
 * <p>
 * This palette calculates color with next formula: <br>
 * if {@code numIter} is greater than zero: {@code color =  sin(((numIter - 1) / period) * 2 * PI + initialPhase) / 2 +
 * 0.5} for each channel,<br>
 * else: {@code color = fractalColor}.
 * where:<br>
 * <ul>
 * <li>color - component of the color (red, green of blue);</li>
 * <li>numIter - number of the iterations;</li>
 * <li>period - period of sinusoid for color;</li>
 * <li>initialPhase - initial phase of the sinusoid.</li>
 * </ul>
 *
 * @author Kochin Konstantin Alexandrovich
 */
final public class IterativePaletteSin implements IterativePalette {
    // period of the colors
    private double perR;
    private double perG;
    private double perB;
    // initial phase of the colors
    private double phi0R;
    private double phi0G;
    private double phi0B;
    // color of the Fractal
    private Color fractalColor;


    /**
     * Constructor.
     *
     * @param fractalColor color of the fractal
     * @param perR         period of the red color in iterations
     * @param perG         period of the green color in iterations
     * @param perB         period of the blue color in iterations
     * @param phi0R        initial phase of the red color
     * @param phi0G        initial phase of the green color
     * @param phi0B        initial phase of the blue color
     * @throws NullPointerException if {@code} fractalColor is {@code null}
     */
    public IterativePaletteSin(Color fractalColor, double perR, double perG, double perB, double phi0R, double phi0G, double phi0B) {
        this.fractalColor = Objects.requireNonNull(fractalColor, "fractalColor is null");
        this.perR = perR;
        this.perG = perG;
        this.perB = perB;
        this.phi0R = phi0R;
        this.phi0G = phi0G;
        this.phi0B = phi0B;
    }

    /**
     * Constructor with default parameters.
     */
    public IterativePaletteSin() {
        this(Color.BLACK, 1024 / 6.8, 1024 / 4.9, 1024 / 11.1,
                -PI / 2 + PI / 20, -PI / 2 + PI / 20, -PI / 2 + PI / 10);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color numIterToColor(int numIter) {
        // return color
        return (numIter == 0 ? fractalColor : Color.color(
                sin((numIter - 1) * 2 * PI / perR + phi0R) / 2.0 + 0.5,
                sin((numIter - 1) * 2 * PI / perG + phi0G) / 2.0 + 0.5,
                sin((numIter - 1) * 2 * PI / perB + phi0B) / 2.0 + 0.5));
    }

    /**
     * Compares two palette.
     *
     * @param obj object for comparison
     * @return {@code true} if palette same, else {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof IterativePaletteSin)) {
            return false;
        }
        IterativePaletteSin r = (IterativePaletteSin) obj;
        return r.perR == perR && r.perG == perG && r.perB == perB &&
                r.phi0R == phi0R && r.phi0G == phi0G && r.phi0B == phi0B &&
                r.fractalColor.equals(fractalColor);
    }


    /**
     * Gets period of the red color in the iterations
     *
     * @return period
     */
    public double getPerR() {
        return perR;
    }

    /**
     * Gets period of the green color in the iterations
     *
     * @return period
     */
    public double getPerG() {
        return perG;
    }

    /**
     * Gets period of the blue in the iterations
     *
     * @return period
     */
    public double getPerB() {
        return perB;
    }

    /**
     * Gets initial phase of the red color.
     *
     * @return initial phase
     */
    public double getPhi0R() {
        return phi0R;
    }

    /**
     * Gets initial phase of the green color.
     *
     * @return initial phase
     */
    public double getPhi0G() {
        return phi0G;
    }

    /**
     * Gets initial phase of the blue color.
     *
     * @return initial phase
     */
    public double getPhi0B() {
        return phi0B;
    }

    /**
     * Gets color of the fractal.
     *
     * @return color of the fractal
     */
    public Color getFractalColor() {
        return fractalColor;
    }
}
