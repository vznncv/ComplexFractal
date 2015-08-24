package local.fractal.model;

import java.util.Objects;

/**
 * The {@code MandelbrotSetPropertyVersion} is helper class for creating {@link MandelbrotSet}.
 * <p>
 * This class provides parameters of the {@code MandelbrotSet} as JavaFX properties for binding for GUI elements.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class MandelbrotSetPropertyVersion extends ComplexFractalPropertyVersion<MandelbrotSet> {

    /**
     * Default constructor. All property contains empty strings.
     */
    public MandelbrotSetPropertyVersion() {
    }

    /**
     * Constructor.
     *
     * @param fractal fractal for getting initial values of the properties
     */
    public MandelbrotSetPropertyVersion(MandelbrotSet fractal) {
        setComplexFractalSettings(fractal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setComplexFractalSettings(MandelbrotSet val) {
        Objects.requireNonNull(val);
        setBaseComplexFractalSettings(val);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MandelbrotSet createComplexFractal() {
        return new MandelbrotSet(parseMaxIter(), parseCriticalR());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return "Mandelbrot set";
    }
}
