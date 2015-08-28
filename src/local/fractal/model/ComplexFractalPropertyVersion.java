package local.fractal.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

/**
 * The {@code ComplexFractalPropertyVersion} is helper class for creating {@link ComplexFractal}.
 * <p>
 * This class provides parameters of the {@code ComplexFractal} as JavaFX properties for binding for GUI elements.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public abstract class ComplexFractalPropertyVersion<T extends ComplexFractal> {
    /**
     * Defines radius of the bound for algorithm of the drawing the fractal.
     */
    private SimpleStringProperty criticalR = new SimpleStringProperty();
    /**
     * Defines maximum number of iterations for algorithm of the drawing the fractal.
     */
    private SimpleStringProperty maxIter = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    protected ComplexFractalPropertyVersion() {

    }

    final public String getCriticalR() {
        return criticalR.get();
    }

    final public void setCriticalR(String val) {
        criticalR.set(val);
    }

    public StringProperty criticalRProperty() {
        return criticalR;
    }

    final public String getMaxIter() {
        return maxIter.get();
    }

    final public void setMaxIter(String val) {
        maxIter.set(val);
    }

    public StringProperty maxIterProperty() {
        return maxIter;
    }

    /**
     * Sets {@code criticalR} and {@code maxIter} properties from {@code ComplexFractal}.
     *
     * @param val ComplexFractal object
     * @throws NullPointerException if val if null
     */
    protected void setBaseComplexFractalSettings(ComplexFractal val) {
        Objects.requireNonNull(val);
        setCriticalR(String.format("%f", val.getCriticalR()));
        setMaxIter(String.valueOf(val.getMaxIter()));
    }

    /**
     * Sets properties from {@code val}.
     *
     * @param val complex fractal (subclass of the ComplexFractal)
     * @throws NullPointerException if val if null
     */
    public abstract void setComplexFractalSettings(T val);

    /**
     * Creates complex fractal using current settings.
     *
     * @return complex fractal
     * @throws IllegalStateException if any of the properties is uncorrected
     */
    public abstract T createComplexFractal();


    /**
     * Gets name for displaying in the settings.
     *
     * @return displaying name
     */
    public abstract String getDisplayName();

    /**
     * Gets a criticalR as double.
     *
     * @return criticalR
     * @throws IllegalStateException if criticalR property isn't string of double number or less that zero
     */
    protected double parseCriticalR() {
        double criticalR;
        try {
            criticalR = Double.valueOf(getCriticalR());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("criticalR isn't double number", e);
        }
        if (criticalR < 0.0)
            throw new IllegalStateException("criticalR is less that zero");
        return criticalR;
    }

    /**
     * Gets a maxIter as int.
     *
     * @return maxIter
     * @throws IllegalStateException if maxIter property isn't string representing integer number or less that one
     */
    protected int parseMaxIter() {
        int maxIter;
        try {
            maxIter = Integer.valueOf(getMaxIter());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("maxIter isn't integer number", e);
        }
        if (maxIter < 1)
            throw new IllegalStateException("maxIter is less that one");
        return maxIter;
    }
}
