package local.fractal.model;

import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

/**
 * The {@code ComplexFractalVersion1PropertyVersion} is helper class for creating {@link ComplexFractalVersion1}.
 * <p>
 * This class provides parameters of the {@code ComplexFractalVersion1} as JavaFX properties for binding for GUI
 * elements.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class ComplexFractalVersion1PropertyVersion extends ComplexFractalPropertyVersion<ComplexFractalVersion1> {
    /**
     * Defines the power n1.
     */
    private SimpleStringProperty n1 = new SimpleStringProperty();
    /**
     * Defines the power n2.
     */
    private SimpleStringProperty n2 = new SimpleStringProperty();

    /**
     * Default constructor. All property contains empty strings.
     */
    public ComplexFractalVersion1PropertyVersion() {
    }

    /**
     * Constructor.
     *
     * @param fractal fractal for getting initial values of the properties
     */
    public ComplexFractalVersion1PropertyVersion(ComplexFractalVersion1 fractal) {
        setComplexFractalSettings(fractal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return "Complex fractal. Version 1";
    }


    public String getN1() {
        return n1.get();
    }

    public void setN1(String n1) {
        this.n1.set(n1);
    }

    public SimpleStringProperty n1Property() {
        return n1;
    }

    public String getN2() {
        return n2.get();
    }

    public void setN2(String n2) {
        this.n2.set(n2);
    }

    public SimpleStringProperty n2Property() {
        return n2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setComplexFractalSettings(ComplexFractalVersion1 val) {
        Objects.requireNonNull(val);
        setBaseComplexFractalSettings(val);
        setN1(String.valueOf(val.getN1()));
        setN2(String.valueOf(val.getN2()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComplexFractalVersion1 createComplexFractal() {
        try {
            return new ComplexFractalVersion1(Integer.parseInt(getN1()), Integer.parseInt(getN2()), parseMaxIter(), parseCriticalR());
        } catch (IllegalStateException | NumberFormatException e) {
            throw new IllegalStateException("Uncorrected state of the fractal property.", e);
        }
    }
}
