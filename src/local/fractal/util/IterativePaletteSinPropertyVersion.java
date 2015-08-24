package local.fractal.util;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * The {@code IterativePaletteSinPropertyVersion} is helper class for creating {@link IterativePaletteSin}.
 * <p>
 * This class provides parameters of the {@code IterativePaletteSin} as JavaFX properties for binding for GUI elements.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class IterativePaletteSinPropertyVersion implements Observable {

    /**
     * Period of the red color.
     *
     * @defaultValue 0.0
     */
    private SimpleDoubleProperty perR = new SimpleDoubleProperty();
    /**
     * Period of the green color.
     *
     * @defaultValue 0.0
     */
    private SimpleDoubleProperty perG = new SimpleDoubleProperty();
    /**
     * Period of the blue color.
     *
     * @defaultValue 0.0
     */
    private SimpleDoubleProperty perB = new SimpleDoubleProperty();
    /**
     * Initial phase of the red color.
     *
     * @defaultValue 0.0
     */
    private SimpleDoubleProperty phi0R = new SimpleDoubleProperty();
    /**
     * Initial phase of the green color.
     *
     * @defaultValue 0.0
     */
    private SimpleDoubleProperty phi0G = new SimpleDoubleProperty();
    /**
     * Initial phase of the blue color.
     *
     * @defaultValue 0.0
     */
    private SimpleDoubleProperty phi0B = new SimpleDoubleProperty();
    /**
     * Defines color of the fractal.
     *
     * @defaultValue Color.BLACK
     */
    private SimpleObjectProperty<Color> fractalColor = new SimpleObjectProperty<>(Color.BLACK);
    /**
     * Default constructor.
     */
    public IterativePaletteSinPropertyVersion() {
    }
    /**
     * Constructor.
     * @param palette palette for getting starting settings.
     */
    public IterativePaletteSinPropertyVersion(IterativePaletteSin palette) {
        setPaletteSettings(palette);
    }

    /**
     * Converts phase to range [-PI/2; PI/2].
     *
     * @param phase phase in unlimited rang
     * @return phase in range [-PI/2; PI/2]
     */
    private static double correctPhase(double phase) {
        phase = phase % (2 * Math.PI);
        if (phase < Math.PI)
            phase += 2 * Math.PI;
        if (phase > Math.PI)
            phase -= 2 * Math.PI;
        return phase;
    }

    public final double getPerR() {
        return perR.get();
    }

    public final void setPerR(double perR) {
        this.perR.set(perR);
    }

    public DoubleProperty perRProperty() {
        return perR;
    }

    public final double getPerG() {
        return perG.get();
    }

    public final void setPerG(double perG) {
        this.perG.set(perG);
    }

    public DoubleProperty perGProperty() {
        return perG;
    }

    public final double getPerB() {
        return perB.get();
    }

    public final void setPerB(double perB) {
        this.perB.set(perB);
    }

    public DoubleProperty perBProperty() {
        return perB;
    }

    public final double getPhi0R() {
        return phi0R.get();
    }

    public final void setPhi0R(double phi0R) {
        this.phi0R.set(phi0R);
    }

    public DoubleProperty phi0RProperty() {
        return phi0R;
    }

    public final double getPhi0G() {
        return phi0G.get();
    }

    public final void setPhi0G(double phi0G) {
        this.phi0G.set(phi0G);
    }

    public DoubleProperty phi0GProperty() {
        return phi0G;
    }

    public final double getPhi0B() {
        return phi0B.get();
    }

    public final void setPhi0B(double phi0B) {
        this.phi0B.set(phi0B);
    }

    public DoubleProperty phi0BProperty() {
        return phi0B;
    }

    public final Color getFractalColor() {
        return fractalColor.get();
    }

    public final void setFractalColor(Color fractalColor) {
        this.fractalColor.set(fractalColor);
    }

    public SimpleObjectProperty<Color> fractalColorProperty() {
        return fractalColor;
    }

    /**
     * Applies setting from the {@code IterativePaletteSin}.
     *
     * @param pl palette
     */
    public void setPaletteSettings(IterativePaletteSin pl) {
        setPerR(pl.getPerR());
        setPerG(pl.getPerG());
        setPerB(pl.getPerB());
        // it's desirable that initial phases are limited with the range from -PI/2 to PI/2
        setPhi0R(correctPhase(pl.getPhi0R()));
        setPhi0G(correctPhase(pl.getPhi0G()));
        setPhi0B(correctPhase(pl.getPhi0B()));
        setFractalColor(pl.getFractalColor());
    }

    /**
     * Creates {@code IterativePaletteSin} using current settings.
     *
     * @return palette
     */
    public IterativePaletteSin createPalette() {
        return new IterativePaletteSin(getFractalColor(), getPerR(), getPerG(), getPerB(), getPhi0R(), getPhi0G(), getPhi0B());
    }


    /**
     * Adds a InvalidationListener which will be notified whenever the value of any properties of this object becomes
     * invalid.
     *
     * @param listener listener
     * @throws NullPointerException if listener is null
     */
    public void addListener(InvalidationListener listener) {
        Objects.requireNonNull(listener);
        InvalidationListener commonListener = obs -> listener.invalidated(this);
        perRProperty().addListener(commonListener);
        perGProperty().addListener(commonListener);
        perBProperty().addListener(commonListener);
        phi0RProperty().addListener(commonListener);
        phi0GProperty().addListener(commonListener);
        phi0BProperty().addListener(commonListener);
        fractalColorProperty().addListener(commonListener);
    }

    /**
     * Removes invalidation listener.
     *
     * @param listener listener
     * @throws NullPointerException if listener is null
     */
    @Override
    public void removeListener(InvalidationListener listener) {
        Objects.requireNonNull(listener);
        perRProperty().removeListener(listener);
        perGProperty().removeListener(listener);
        perBProperty().removeListener(listener);
        phi0RProperty().removeListener(listener);
        phi0GProperty().removeListener(listener);
        phi0BProperty().removeListener(listener);
        fractalColorProperty().removeListener(listener);
    }
}
