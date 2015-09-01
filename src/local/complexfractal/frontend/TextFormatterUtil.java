package local.complexfractal.frontend;

import javafx.scene.control.TextField;
import local.complexfractal.model.ComplexNumber;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * The {@code TextFormatterUtil} provides method for adding validation for the {@link  javafx.scene.control.TextField}
 * for to input the integer, double and complex number.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class TextFormatterUtil {
    /**
     * Adds validators (using {@code onFocus} and {@code onAction} events) for to input integer
     * number in the text field.
     *
     * @param textField    text field
     * @param min          minimum value of the number
     * @param max          maximum value of the number
     * @param defaultValue default value of the number
     * @throws NullPointerException     if textField is null
     * @throws IllegalArgumentException if min &gt; max or defaultValue &lt; min or defaultValue &gt; max
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

    /**
     * Adds validators (using {@code onFocus} and {@code onAction} events) for to input double number in the text
     * field.
     *
     * @param textField    text field
     * @param min          minimum value of the number
     * @param max          maximum value of the number
     * @param defaultValue default value of the number
     * @throws NullPointerException     if textField is null
     * @throws IllegalArgumentException if min &gt; max or defaultValue &lt; min or defaultValue &gt; max
     */
    public static void setDoubleRange(TextField textField, double min, double max, double defaultValue) {
        Objects.requireNonNull(textField);
        if (min > max)
            throw new IllegalArgumentException("min is less max");
        if (defaultValue < min)
            throw new IllegalArgumentException("defaultValue is less min");
        if (defaultValue > max)
            throw new IllegalArgumentException("defaultValue is great max");

        // set initial value
        textField.setText(String.valueOf(defaultValue));

        // check value range
        Function<String, String> checkText = (text) -> {
            if (textField.getText().isEmpty()) {
                // return default value
                return String.format(Locale.ENGLISH, "%f", defaultValue);
            }
            // check current value
            try {
                double currentValue = Double.valueOf(textField.getText());
                if (currentValue < min)
                    return String.format(Locale.ENGLISH, "%f", min);
                if (currentValue > max)
                    return String.format(Locale.ENGLISH, "%f", max);
            } catch (NumberFormatException e) {
                // return default value
                return String.format(Locale.ENGLISH, "%f", defaultValue);
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


    /**
     * Adds validators (using {@code onFocus} and {@code onAction} events) for to input {@link
     * local.complexfractal.model.ComplexNumber} in the text field.
     *
     * @param textField    text field
     * @param defaultValue default value of the complex number
     * @throws NullPointerException if textField or defaultValue is null
     */
    public static void setComplexNumber(TextField textField, ComplexNumber defaultValue) {
        Objects.requireNonNull(textField);
        Objects.requireNonNull(defaultValue);

        // set initial value
        textField.setText(String.valueOf(defaultValue));

        // check value range
        Function<String, String> checkText = (text) -> {
            if (textField.getText().isEmpty()) {
                // return default value
                return String.valueOf(defaultValue);
            }
            // check current value
            ComplexNumber complexNumber;
            try {
                complexNumber = ComplexNumber.valueOf(textField.getText());
            } catch (NumberFormatException e) {
                // return default value
                return String.valueOf(defaultValue);
            }
            // return value in standard view
            return String.valueOf(complexNumber);
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
