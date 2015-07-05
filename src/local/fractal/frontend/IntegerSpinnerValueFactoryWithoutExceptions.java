package local.fractal.frontend;

import javafx.beans.NamedArg;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.converter.IntegerStringConverter;

/**
 * A {@link javafx.scene.control.SpinnerValueFactory} implementation designed to iterate through integer values.
 * <p>
 * <p>Note that the default {@link #converterProperty() converter} isn't throw {@link NumberFormatException}
 * as the default {@link #converterProperty() converter} in the {@link SpinnerValueFactory.IntegerSpinnerValueFactory}
 * and return the old value in those cases.
 */
public class IntegerSpinnerValueFactoryWithoutExceptions extends SpinnerValueFactory.IntegerSpinnerValueFactory {

    {
        // set new default converter
        setConverter(new IntegerStringConverter() {
            /**
             * {@inheritDoc}
             *
             * @param value
             */
            @Override
            public Integer fromString(String value) {
                try {
                    // try convert string
                    return super.fromString(value);
                } catch (NumberFormatException e) {
                    Integer oldValue = IntegerSpinnerValueFactoryWithoutExceptions.this.getValue();
                    // it's not integer, invalidate the property and return the old value
                    IntegerSpinnerValueFactoryWithoutExceptions.this.setValue(oldValue - 1); // this value must be difference from oldValue
                    return oldValue;
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public IntegerSpinnerValueFactoryWithoutExceptions(@NamedArg("min") int min, @NamedArg("max") int max) {
        super(min, max);
    }

    /**
     * {@inheritDoc}
     */
    public IntegerSpinnerValueFactoryWithoutExceptions(@NamedArg("min") int min, @NamedArg("max") int max, @NamedArg("initialValue") int initialValue) {
        super(min, max, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    public IntegerSpinnerValueFactoryWithoutExceptions(@NamedArg("min") int min, @NamedArg("max") int max, @NamedArg("initialValue") int initialValue, @NamedArg("amountToStepBy") int amountToStepBy) {
        super(min, max, initialValue, amountToStepBy);
    }
}
