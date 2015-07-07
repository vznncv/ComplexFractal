package local.fractal.frontend;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.Objects;

/**
 * This class provide utils for validation of the {@code TextField}.
 */
public class TextFormatterUtil {

    /**
     * Add validators (using TextFormatter and onFocus event) for input integer number in the text field.
     * @param textField text field
     * @param min minimum value of the number
     * @param max maximum value of the number
     * @param defaultValue default value of the number
     */
    public static void setIntegerRange(TextField textField, int min, int max, int defaultValue) {
        Objects.requireNonNull(textField);
        if (min > max)
            throw new IllegalArgumentException("min is less max");
        if (defaultValue < min)
            throw new IllegalArgumentException("defaultValue is less min");
        if (defaultValue > max)
            throw new IllegalArgumentException("defaultValue is great max");

        // set initial value
        textField.setText(String.valueOf(defaultValue));

        // allow enter only digits 0-9
        textField.setTextFormatter(new TextFormatter<Integer>(change -> {
            // pass empty string
            if (change.getControlNewText().isEmpty())
                return change;
            // if new value isn't integer, that reject changes
            try {
                Integer.valueOf(change.getControlNewText());
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        }));

        // check value range
        textField.focusedProperty().addListener((obs, oldValF, newValF) -> {
            if (!newValF) {
                if (textField.getText().isEmpty()) {
                    // set default value
                    textField.setText(String.valueOf(defaultValue));
                }
                // check current value
                int currentValue = Integer.valueOf(textField.getText());
                if (currentValue < min)
                    textField.setText(String.valueOf(min));
                if (currentValue > max)
                    textField.setText(String.valueOf(max));
            }
        });

    }


    /*
        extends TextFormatter<Integer> {
    public IntegerTextFormatter(@NamedArg("min") int min, @NamedArg("max") int max, @NamedArg("initValue") int initValue) {
        super(new IntegerStringConverter(),
                initValue,
                change -> {
                    // pass empty string
                    if (change.getControlNewText().isEmpty())
                        return change;
                    // if new value isn't integer, that reject changes
                    try {
                        Integer newVal = Integer.valueOf(change.getControlNewText());
                        if (newVal > max)
                            change.
                        return change;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
        );
    }*/
}
