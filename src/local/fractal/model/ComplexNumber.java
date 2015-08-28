package local.fractal.model;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code ComplexNumber} is helper class for complex calculation. This class doesn't check case with NaN in the
 * real and image part of a complex number. Objects of this class are mutable.
 */
public final class ComplexNumber {
    // pattern for searching the complex number
    // accept complex number as these:
    // +23-0.6i
    // -434+3i+34-i
    // 32.32 - 3i
    // note support only numbers in the decimal format
    private static final Pattern complexNumberPatter = Pattern.compile(
            "(?<sign>[+-])|(?:(?<imag>(:?\\d+(?:\\.\\d+)?)?)i)|(?<real>\\d+(?:\\.\\d+)?)|(?<space>\\s+)");
    // real part of the complex number
    private double real;
    // imaginary part of the complex number
    private double imag;

    /**
     * Constructor.
     *
     * @param real real part of the number
     * @param imag imaginary part of the number
     */
    public ComplexNumber(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    /**
     * Default constructor for creating zero complex number.
     */
    public ComplexNumber() {
        this(0.0, 0.0);
    }

    /**
     * Copy constructor.
     *
     * @param val object for copying
     */
    public ComplexNumber(ComplexNumber val) {
        this(val.real, val.imag);
    }

    /**
     * Create complex number from string.
     *
     * @param val string with number
     * @throws NullPointerException  if val is null
     * @throws NumberFormatException if string doesn't represent complex number
     */
    public ComplexNumber(String val) {
        this(ComplexNumber.valueOf(val));
    }

    /**
     * Converts string to complex number.
     *
     * @param val string with number
     * @return null if it isn't complex number, otherwise ComplexNumber
     * @throws NullPointerException if val is null
     */
    public static ComplexNumber parseComplexNumber(String val) {
        Objects.requireNonNull(val);

        Matcher m = complexNumberPatter.matcher(val);
        double realPart = 0.0;
        double imagePart = 0.0;
        boolean hasRealOrImagPart = false;
        boolean prevSign = false;
        int index = 0;
        int sign = 1;

        // matcher find the follow type of substrings:
        // "  " - space
        // "+", "-" - sign
        // "34", "34.43" - real part without sign
        // "34i", "34.43i" - imaginary part without sign
        while (m.find()) {
            // it isn't complex number if have any foreign expression
            if (m.start() != index)
                return null;
            index = m.end();
            // skip spaces
            if (m.group("space") == null) {
                // some sings mustn't follow in sequence
                if (prevSign && m.group("sign") != null)
                    return null;

                if (m.group("sign") != null) {
                    prevSign = true;
                    sign = (m.group("sign").equals("-") ? -1 : +1);
                } else if (m.group("real") != null) {
                    prevSign = false;
                    hasRealOrImagPart = true;
                    realPart += sign * Double.parseDouble(m.group("real"));
                    // default sign
                    sign = 1;
                } else if (m.group("imag") != null) {
                    prevSign = false;
                    hasRealOrImagPart = true;
                    if (!m.group("imag").isEmpty()) {
                        imagePart += sign * Double.parseDouble(m.group("imag"));
                    } else {
                        // only i
                        imagePart += sign;
                    }
                    // default sign
                    sign = 1;
                }
            }
        }

        if (hasRealOrImagPart && val.length() == index) {
            return new ComplexNumber(realPart, imagePart);
        } else {
            // any imaginary or real part don't find
            return null;
        }
    }

    /**
     * Converts string to complex number.
     *
     * @param val string with number
     * @return ComplexNumber
     * @throws NullPointerException  if val is null
     * @throws NumberFormatException if string doesn't represent complex number
     */
    public static ComplexNumber valueOf(String val) {
        ComplexNumber res = parseComplexNumber(val);
        if (res == null)
            throw new NumberFormatException("\"" + val + "\" isn't complex number");
        return res;
    }

    /**
     * Checks that string represents complex number.
     *
     * @param val string with number
     * @return true if  string represents complex number, otherwise false
     * @throws NullPointerException if val is null
     */
    public static boolean isComplexNumber(String val) {
        return parseComplexNumber(val) != null;
    }


    /**
     * Adds number.
     * <p>
     * {@code result = l + r}, where {@code l} - this object
     *
     * @param r right argument
     * @return {@code result}
     */
    public ComplexNumber add(ComplexNumber r) {
        return new ComplexNumber(real, imag).addAndAsg(r);
    }

    /**
     * Subtracts number.
     * <p>
     * {@code result = l - r}, where {@code l} - this object
     *
     * @param r right argument
     * @return {@code result}
     */
    public ComplexNumber sub(ComplexNumber r) {
        return new ComplexNumber(real, imag).subAndAsg(r);
    }

    /**
     * Multiply by number.
     * <p>
     * {@code result = l * r}, where {@code l} - this object
     *
     * @param r right argument
     * @return {@code result}
     */
    public ComplexNumber mul(ComplexNumber r) {
        return new ComplexNumber(real, imag).mulAndAsg(r);
    }

    /**
     * Divide by number.
     * <p>
     * {@code result = l / r}, where {@code l} - this object
     *
     * @param r right argument
     * @return {@code result}
     */
    public ComplexNumber div(ComplexNumber r) {
        return new ComplexNumber(real, imag).divAndAsg(r);
    }

    /**
     * Raises to the power.
     * <p>
     * {@code result = l ^ n}, where {@code l} - this object
     *
     * @param n power
     * @return {@code result}
     */
    public ComplexNumber pow(int n) {
        return new ComplexNumber(real, imag).powAndAsg(n);
    }

    /**
     * Adds and assigns result to this object.
     * <p>
     * {@code l = l + r}, where {@code l} - this object
     *
     * @param r right argument
     * @return this object
     */
    public ComplexNumber addAndAsg(ComplexNumber r) {
        real += r.real;
        imag += r.imag;
        return this;
    }

    /**
     * Subtracts and assigns result to this object.
     * <p>
     * {@code l = l - r}, where {@code l} - this object
     *
     * @param r right argument
     * @return this object
     */
    public ComplexNumber subAndAsg(ComplexNumber r) {
        real -= r.real;
        imag -= r.imag;
        return this;
    }

    /**
     * Multiply and assigns result to this object.
     * <p>
     * {@code l = l * r}, where {@code l} - this object
     *
     * @param r right argument
     * @return this object
     */
    public ComplexNumber mulAndAsg(ComplexNumber r) {
        double rP = real * r.real - imag * r.imag;
        double iP = real * r.imag + imag * r.real;
        real = rP;
        imag = iP;
        return this;
    }

    /**
     * Divide and assigns result to this object.
     * <p>
     * {@code l = l / r}, where {@code l} - this object
     *
     * @param r right argument
     * @return this object
     */
    public ComplexNumber divAndAsg(ComplexNumber r) {
        double d = r.squareAbs();
        double rP = (real * r.real + imag * r.imag) / d;
        double iP = (-real * r.imag + imag * r.real) / d;
        real = rP;
        imag = iP;
        return this;
    }


    /**
     * Raises to the power and assigns result to this object.
     * <p>
     * {@code l = l ^ n}, where {@code l} - this object
     *
     * @param n power
     * @return this object
     */
    public ComplexNumber powAndAsg(int n) {
        double r = real;
        double i = imag;
        double resR = 1;
        double resI = 0;

        if (n != 0) {
            int pow = Math.abs(n);
            while (true) {
                if (pow % 2 == 1) {
                    double tmpR = resR * r - resI * i;
                    double tmpI = resR * i + resI * r;
                    resR = tmpR;
                    resI = tmpI;
                }

                pow >>>= 1;
                if (pow <= 0)
                    break;

                double tmpR = r * r - i * i;
                double tmpI = r * i + i * r;
                r = tmpR;
                i = tmpI;
            }

            if (n < 0) {
                double tmpD = resR * resR + resI * resI;
                resR = resR / tmpD;
                resI = -resI / tmpD;
            }
        }

        real = resR;
        imag = resI;
        return this;
    }

    /**
     * Assigns the value of the right argument.
     *
     * @param r right argument
     * @return this object
     */
    public ComplexNumber assign(ComplexNumber r) {
        real = r.real;
        imag = r.imag;
        return this;
    }

    /**
     * Calculates square of the absolute value of this number.
     *
     * @return square absolute value
     */
    public double squareAbs() {
        return real * real + imag * imag;
    }

    /**
     * Calculates absolute value of this number.
     *
     * @return absolute value
     */
    public double abs() {
        return Math.sqrt(squareAbs());
    }

    /**
     * Gets real part.
     *
     * @return real part
     */
    public double getReal() {
        return real;
    }

    /**
     * Sets real part.
     *
     * @param real real part
     */
    public void setReal(double real) {
        this.real = real;
    }

    /**
     * Gets imaginary part.
     *
     * @return imaginary part
     */
    public double getImag() {
        return imag;
    }

    /**
     * Sets imaginary part.
     *
     * @param imag imaginary part
     */
    public void setImag(double imag) {
        this.imag = imag;
    }

    /**
     * Gets string representation of the complex number.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        if (real != 0) {
            if (imag != 0)
                return String.format("%f %+fi", real, imag);
            else
                return String.format("%f", real);
        } else {
            if (imag != 0)
                return String.format("%fi", imag);
            else
                return "0.0";
        }
    }

    /**
     * Tests that complex number are equals.
     *
     * @param obj object for compare
     * @return true, if objects are equal, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComplexNumber))
            return false;
        ComplexNumber rightArg = (ComplexNumber) obj;
        return real == rightArg.real && imag == rightArg.imag;
    }

    /**
     * Gets independent copy of this number.
     *
     * @return copy of this number
     */
    public ComplexNumber copy() {
        return new ComplexNumber(this);
    }
}
