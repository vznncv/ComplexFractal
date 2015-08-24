package local.fractal.model;

import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

/**
 * The {@code JuliaSetPropertyVersion} is helper class for creating {@link JuliaSet}.
 * <p>
 * This class provides parameters of the {@code JuliaSet} as JavaFX properties for binding for GUI elements.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class JuliaSetPropertyVersion extends ComplexFractalPropertyVersion<JuliaSet> {
    /**
     * Defines the coefficient c1.
     */
    private SimpleStringProperty c1 = new SimpleStringProperty();
    /**
     * Defines the coefficient c1.
     */
    private SimpleStringProperty c2 = new SimpleStringProperty();

    /**
     * Default constructor. All property contains empty strings.
     */
    public JuliaSetPropertyVersion() {
    }

    /**
     * Constructor.
     *
     * @param fractal fractal for getting initial values of the properties
     */
    public JuliaSetPropertyVersion(JuliaSet fractal) {
        setComplexFractalSettings(fractal);
    }

    public String getC1() {
        return c1.get();
    }

    public void setC1(String c1) {
        this.c1.set(c1);
    }

    public SimpleStringProperty c1Property() {
        return c1;
    }

    public String getC2() {
        return c2.get();
    }

    public void setC2(String c2) {
        this.c2.set(c2);
    }

    public SimpleStringProperty c2Property() {
        return c2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setComplexFractalSettings(JuliaSet val) {
        Objects.requireNonNull(val);
        setBaseComplexFractalSettings(val);
        setC1(val.getC1().toString());
        setC2(val.getC2().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JuliaSet createComplexFractal() {
        try {
            return new JuliaSet(ComplexNumber.valueOf(getC1()), ComplexNumber.valueOf(getC2()), parseMaxIter(), parseCriticalR());
        } catch (IllegalStateException | NumberFormatException e) {
            throw new IllegalStateException("Uncorrected state of the fractal property.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return "Julia set";
    }
}
