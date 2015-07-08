package local.fractal.frontend;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.Objects;
import java.util.function.Function;

/**
 * This class provide utils for validation of the {@code TextField}.
 */
public class TextFormatterUtil {

    /**
     * Add validators (using TextFormatter, onFocus and onAction event) for input integer number in the text field.
     *
     * @param textField    text field
     * @param min          minimum value of the number
     * @param max          maximum value of the number
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
        Function<String, String> checkText = (text) -> {
            if (textField.getText().isEmpty()) {
                // return default value
                return String.valueOf(defaultValue);
            }
            // check current value
            try {
                int currentValue = Integer.valueOf(textField.getText());
                if (currentValue < min)
                    return String.valueOf(min);
                if (currentValue > max)
                    return String.valueOf(max);
            } catch (NumberFormatException e) {
                // return default value
                return String.valueOf(defaultValue);
            }
            // return value without changing
            return text;
        };

        // add validate listeners
        textField.focusedProperty().addListener((obs, o, n) -> {
            if (!n) {
                textField.setText(checkText.apply(textField.getText()));
            }
        });
        textField.setOnAction(e -> textField.setText(checkText.apply(textField.getText())));
    }
}
