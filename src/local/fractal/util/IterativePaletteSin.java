package local.fractal.util;

import javafx.scene.paint.Color;

import java.util.Objects;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

/**
 * Simple sinusoidal palette for fractal.
 * <p>
 * This palette calculating color with next formula: <br/>
 * if numIter is great than zero: {@code color =  sin(((numIter - 1) / period) * 2 * PI + initialPhase) / 2 + 0.5},<br/>
 * else: {@code color = fractalColor}.
 * where:<br/>
 * <ul>
 * <li>color - component of the color (red, green of blue);</li>
 * <li>numIter - number of the iterations;</li>
 * <li>period - period of sinusoid for color;</li>
 * <li>initialPhase - initial phase of the sinusoid.</li>
 * </ul>
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
     * Create palette.
     *
     * @param fractalColor color of the fractal
     * @param perR         period of the red color in iterations
     * @param perG         period of the green color in iterations
     * @param perB         period of the blue color in iterations
     * @param phi0R        initial phase of the red color
     * @param phi0G        initial phase of the green color
     * @param phi0B        initial phase of the blue color
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
     * Create default palette.
     */
    public IterativePaletteSin() {
        this(Color.BLACK, 1024 / 6.8, 1024 / 4.9, 1024 / 11.1,
                -PI / 2 + PI / 20, -PI / 2 + PI / 20, -PI / 2 + PI / 10);
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
                sin((numIter - 1) * 2 * PI / perR + phi0R) / 2.0 + 0.5,
                sin((numIter - 1) * 2 * PI / perG + phi0G) / 2.0 + 0.5,
                sin((numIter - 1) * 2 * PI / perB + phi0B) / 2.0 + 0.5));
    }

    /**
     * Compare two palette.
     * @param obj object for comparison
     * @return {@code true} if palette same, else {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof IterativePaletteSin)){
            return false;
        }
        IterativePaletteSin r = (IterativePaletteSin) obj;
        return r.perR == perR && r.perG == perG && r.perB == perB &&
                r.phi0R == phi0R && r.phi0G == phi0G && r.phi0B == phi0B &&
                r.fractalColor.equals(fractalColor);
    }


    /**
     * Get period of the red color in the iterations
     *
     * @return period
     */
    public double getPerR() {
        return perR;
    }

    /**
     * Get period of the green color in the iterations
     *
     * @return period
     */
    public double getPerG() {
        return perG;
    }

    /**
     * Get period of the blue in the iterations
     *
     * @return period
     */
    public double getPerB() {
        return perB;
    }

    /**
     * Get initial phase of the red color.
     *
     * @return initial phase
     */
    public double getPhi0R() {
        return phi0R;
    }

    /**
     * Get initial phase of the green color.
     *
     * @return initial phase
     */
    public double getPhi0G() {
        return phi0G;
    }

    /**
     * Get initial phase of the blue color.
     *
     * @return initial phase
     */
    public double getPhi0B() {
        return phi0B;
    }

    /**
     * Get color of the fractal.
     *
     * @return color of the fractal
     */
    public Color getFractalColor() {
        return fractalColor;
    }
}
