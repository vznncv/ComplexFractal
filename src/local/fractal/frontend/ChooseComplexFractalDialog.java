package local.fractal.frontend;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import local.fractal.model.*;
import local.fractal.util.BaseDialog;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * The {@code SinPaletteChoiceDialog} represents controller of the window for choosing complex fractal.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class ChooseComplexFractalDialog extends BaseDialog {
    // base settings of the fractal
    @FXML
    private TextField complexFractalParamMaxIter;
    @FXML
    private TextField complexFractalParamCriticalR;
    // settings of the Mandelbrot set
    private MandelbrotSetPropertyVersion mandelbrotSetProducer;
    @FXML
    private Node mandelbrotSetSettings;
    // settings of the Julia set
    private JuliaSetPropertyVersion juliaSetProducer;
    @FXML
    private Node juliaSetSettings;
    @FXML
    private TextField juliaSetParamC1;
    @FXML
    private TextField juliaSetParamC2;
    // setting of the complex fractal version 1
    private ComplexFractalVersion1PropertyVersion complexFractalVersion1Producer;
    @FXML
    private Node complexFractalV1Settings;
    @FXML
    private TextField complexFractalVersion1ParamN1;
    @FXML
    private TextField complexFractalVersion1ParamN2;

    // ChoiceBox for choosing the current fractal.
    @FXML
    private ChoiceBox<ComplexFractalPropertyVersion> choiceFractal;
    // Panel for display settings
    @FXML
    private BorderPane settingsPanel;

    // current fractal for drawing
    private ComplexFractal complexFractal;

    /**
     * Constructor.
     */
    private ChooseComplexFractalDialog() {
        // initialize fractal producers (mandelbrotSetProducer, juliaSetProducer, complexFractalVersion1Producer) the default values
        mandelbrotSetProducer = new MandelbrotSetPropertyVersion(new MandelbrotSet());
        juliaSetProducer = new JuliaSetPropertyVersion(new JuliaSet());
        complexFractalVersion1Producer = new ComplexFractalVersion1PropertyVersion(new ComplexFractalVersion1());
    }

    /**
     * Construct window for choosing complex fractal.
     *
     * @param stage          window for the dialog
     * @return controller of the window
     */
    public static ChooseComplexFractalDialog createWindow(Stage stage) {
        // load the graph scene
        FXMLLoader fxmlLoader = new FXMLLoader(ChooseComplexFractalDialog.class.getResource("ChooseComplexFractalDialog.fxml"));
        fxmlLoader.setControllerFactory(obj->new ChooseComplexFractalDialog());
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // get current controller
        ChooseComplexFractalDialog controller = fxmlLoader.getController();

        // create and set scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
        // set title of the window
        stage.setTitle("Choice of the palette");
        // set minimal size of the window
        stage.setMinWidth(400);
        stage.setMinHeight(400);
        // save current stage
        controller.setStage(stage);

        return controller;
    }

    /**
     * Gets default complex fractal.
     *
     * @return default complex fractal
     */
    public static ComplexFractal getDefaultComplexFractal() {
        return new MandelbrotSet();
    }

    /**
     * Determines that this dialog supports this type of the fractal.
     *
     * @param fractal complex fractal
     * @return true, if dialog support that type of the fractal, otherwise false
     */
    public static boolean isSupportComplexFractal(ComplexFractalChecker fractal) {
        if (fractal instanceof MandelbrotSet) {
            return true;
        } else if (fractal instanceof JuliaSet) {
            return true;
        } else if (fractal instanceof ComplexFractalVersion1) {
            return true;
        } else {
            throw new IllegalArgumentException("Unknown type of the complex fractal");
        }
    }

    /**
     * Gets complex fractal, that user chosen.
     *
     * @return complex fractal
     */
    public ComplexFractal getComplexFractal() {
        return complexFractal;
    }

    /**
     * Set {@code ComplexFractal} for displaying and editing.
     *
     * @param complexFractal complex fractal
     * @throws NullPointerException     if complexFractal is null
     * @throws IllegalArgumentException complexFractal is unsupported
     */
    public void setComplexFractal(ComplexFractal complexFractal) {
        this.complexFractal = Objects.requireNonNull(complexFractal);
        // determine corresponding fractal producer
        ComplexFractalPropertyVersion fractalProducer;
        if (complexFractal instanceof MandelbrotSet) {
            fractalProducer = mandelbrotSetProducer;
        } else if (complexFractal instanceof JuliaSet) {
            fractalProducer = juliaSetProducer;
        } else if (complexFractal instanceof ComplexFractalVersion1) {
            fractalProducer = complexFractalVersion1Producer;
        } else {
            throw new IllegalArgumentException("Unknown type of the complex fractal");
        }
        // update settings of current producer
        fractalProducer.setComplexFractalSettings(complexFractal);
        // show settings of the current fractal
        choiceFractal.setValue(fractalProducer);
    }

    /**
     * Gets {@code Note} with settings of {@code complexFractal}.
     *
     * @param complexFractal complex fractal
     * @return node with settings
     * @throws IllegalArgumentException if complexFractal has unknown type
     */
    private Node getFractalSettingsNode(ComplexFractalPropertyVersion complexFractal) {
        if (complexFractal instanceof MandelbrotSetPropertyVersion) {
            return mandelbrotSetSettings;
        } else if (complexFractal instanceof JuliaSetPropertyVersion) {
            return juliaSetSettings;
        } else if (complexFractal instanceof ComplexFractalVersion1PropertyVersion) {
            return complexFractalV1Settings;
        } else {
            throw new IllegalArgumentException("Unknown type of the complex fractal producer");
        }
    }

    /**
     * Initialize function, it will be invoked after the scene graph is loaded.
     */
    @FXML
    private void initialize() {
        // settings of the choice box
        choiceFractal.getItems().addAll(mandelbrotSetProducer, juliaSetProducer, complexFractalVersion1Producer);
        choiceFractal.setConverter(new StringConverter<ComplexFractalPropertyVersion>() {
            @Override
            public String toString(ComplexFractalPropertyVersion object) {
                return object.getDisplayName();
            }

            @Override
            public ComplexFractalPropertyVersion fromString(String string) {
                throw new UnsupportedOperationException("fromString operation isn't support");
            }
        });
        choiceFractal.valueProperty().addListener((obj, oldVal, newVal) -> settingsPanel.setCenter(getFractalSettingsNode(newVal)));
        setComplexFractal(getDefaultComplexFractal());
        // add validators to the settings of the base fractal properties
        TextFormatterUtil.setDoubleRange(complexFractalParamCriticalR, 0.0, Double.POSITIVE_INFINITY, getComplexFractal().getCriticalR());
        TextFormatterUtil.setIntegerRange(complexFractalParamMaxIter, 1, 32768, getComplexFractal().getMaxIter());
        // add validators to the settings of Julia set
        TextFormatterUtil.setComplexNumber(juliaSetParamC1, ComplexNumber.valueOf(juliaSetProducer.getC1()));
        TextFormatterUtil.setComplexNumber(juliaSetParamC2, ComplexNumber.valueOf(juliaSetProducer.getC2()));
        // add validators to the settings of complex fractal version 1
        TextFormatterUtil.setIntegerRange(complexFractalVersion1ParamN1, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.valueOf(complexFractalVersion1Producer.getN1()));
        TextFormatterUtil.setIntegerRange(complexFractalVersion1ParamN2, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.valueOf(complexFractalVersion1Producer.getN2()));
        // binding properties of the Mandelbrot set producer to text fields
        mandelbrotSetProducer.crititcalRProperty().bindBidirectional(complexFractalParamCriticalR.textProperty());
        mandelbrotSetProducer.maxIterProperty().bindBidirectional(complexFractalParamMaxIter.textProperty());
        // binding properties of the Julia set producer to text fields
        juliaSetProducer.crititcalRProperty().bindBidirectional(complexFractalParamCriticalR.textProperty());
        juliaSetProducer.maxIterProperty().bindBidirectional(complexFractalParamMaxIter.textProperty());
        juliaSetProducer.c1Property().bindBidirectional(juliaSetParamC1.textProperty());
        juliaSetProducer.c2Property().bindBidirectional(juliaSetParamC2.textProperty());
        // binding properties of the complex fractal version 1 producer to text fields
        complexFractalVersion1Producer.crititcalRProperty().bindBidirectional(complexFractalParamCriticalR.textProperty());
        complexFractalVersion1Producer.maxIterProperty().bindBidirectional(complexFractalParamMaxIter.textProperty());
        complexFractalVersion1Producer.n1Property().bindBidirectional(complexFractalVersion1ParamN1.textProperty());
        complexFractalVersion1Producer.n2Property().bindBidirectional(complexFractalVersion1ParamN2.textProperty());
    }

    /**
     * Saves current fractal and closes window.
     */
    @FXML
    private void saveComplexFractal() {
        // save palette
        setComplexFractal(choiceFractal.getValue().createComplexFractal());
        // close window
        closeWindow();
    }


    /**
     * Resets all fractal settings to default.
     */
    @FXML
    private void defaultFractals() {
        mandelbrotSetProducer.setComplexFractalSettings(new MandelbrotSet());
        juliaSetProducer.setComplexFractalSettings(new JuliaSet());
        complexFractalVersion1Producer.setComplexFractalSettings(new ComplexFractalVersion1());
        // show default fractal
        setComplexFractal(getDefaultComplexFractal());
    }


    /**
     * Closes window without saving the current fractal.
     */
    @FXML
    private void notSaveComplexFractal() {
        // close window
        closeWindow();
    }


}
