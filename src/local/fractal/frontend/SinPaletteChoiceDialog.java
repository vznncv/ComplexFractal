package local.fractal.frontend;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import local.fractal.util.BaseDialog;
import local.fractal.util.IterativePaletteSin;
import local.fractal.util.IterativePaletteSinProperty;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * It's controller of the window of the palette  dialog.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class SinPaletteChoiceDialog extends BaseDialog {
    // picker of the color of the fractal
    @FXML
    ColorPicker fractalColor;
    // canvas for display palette
    @FXML
    private Canvas canvasPalette;
    // chart
    @FXML
    private LineChart<Number, Number> lineChart;
    // current palette for storing the changes
    private IterativePaletteSinProperty currentPalette = new IterativePaletteSinProperty();
    // final palette
    private IterativePaletteSin palette;
    // limit of the iterations
    private int maxIter;

    // setting of the colors
    // red color
    @FXML
    private Slider redPhi0Slider;
    @FXML
    private Label redPhi0Label;
    @FXML
    private Slider redPeriodSlider;
    @FXML
    private Label redPeriodLabel;
    // green color
    @FXML
    private Slider greenPhi0Slider;
    @FXML
    private Label greenPhi0Label;
    @FXML
    private Slider greenPeriodSlider;
    @FXML
    private Label greenPeriodLabel;
    // blue color
    @FXML
    private Slider bluePhi0Slider;
    @FXML
    private Label bluePhi0Label;
    @FXML
    private Slider bluePeriodSlider;
    @FXML
    private Label bluePeriodLabel;


    /**
     * Constructor.
     *
     * @param palette palette for initial displaying
     */
    private SinPaletteChoiceDialog(IterativePaletteSin palette, int maxIter) {
        setPalette(palette);
        setMaxIter(maxIter);
    }

    /**
     * Construct window for choosing palette.
     *
     * @param stage               window of the choosing palette dialog
     * @param iterativePaletteSin palette for initial displaying
     * @param maxIter             limit iterations of the ComplexFractalChecker
     * @return controller of the window
     */
    public static SinPaletteChoiceDialog createWindow(Stage stage, IterativePaletteSin iterativePaletteSin, int maxIter) {
        // load the graph scene
        FXMLLoader fxmlLoader = new FXMLLoader(SinPaletteChoiceDialog.class.getResource("SinPaletteChoiceDialog.fxml"));
        fxmlLoader.setControllerFactory(c -> {
            if (c != SinPaletteChoiceDialog.class) {
                throw new IllegalArgumentException("It's wrong controller class (" + c + ") for the choosing palette dialog.");
            }
            return new SinPaletteChoiceDialog(iterativePaletteSin, maxIter);
        });
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // get current controller
        SinPaletteChoiceDialog controller = fxmlLoader.getController();

        // create and set scene
        stage.setScene(new Scene(root));
        // set title of the window
        stage.setTitle("Choice of the palette");
        // set minimal size of the window
        stage.setMinWidth(400);
        stage.setMinHeight(400);
        // save current stage
        controller.setStage(stage);
        // add stylesheets of the root to scene
        // it's necessary for correct style of the ColorPicker
        stage.getScene().getStylesheets().addAll(root.getStylesheets());

        return controller;
    }

    /**
     * Initialize function, it will be invoked after the scene graph is loaded.
     * Warning: LineChart is slow (initialize 3000 points approximately 2.5 second).
     */
    @FXML
    private void initialize() {
        // set initial value of the currentPalette
        currentPalette.setPaletteSettings(getPalette());
        // maximum number of iterations
        int limitIter = getMaxIter();


        // sliders and labels
        Slider colorsPerSlider[] = {redPeriodSlider, greenPeriodSlider, bluePeriodSlider};
        Label colorsPerLabel[] = {redPeriodLabel, greenPeriodLabel, bluePeriodLabel};
        Slider colorsPhi0Slider[] = {redPhi0Slider, greenPhi0Slider, bluePhi0Slider};
        Label colorsPhi0Label[] = {redPhi0Label, greenPhi0Label, bluePhi0Label};
        // property for bidirectional binding
        DoubleProperty colorsPerProp[] = {currentPalette.perRProperty(), currentPalette.perGProperty(), currentPalette.perBProperty()};
        DoubleProperty colorsPhi0Prop[] = {currentPalette.phi0RProperty(), currentPalette.phi0GProperty(), currentPalette.phi0BProperty()};
        // helper string converter of the phase
        NumberStringConverter phaseConverter = new NumberStringConverter(new DecimalFormat("0.00"));
        // helper string converter of the period
        NumberStringConverter periodConverter = new NumberStringConverter(new DecimalFormat("0.00"));
        // configure sliders and listeners for labels
        for (int i = 0; i < 3; i++) {
            // configure phase slider
            Slider phiSlider = colorsPhi0Slider[i];
            phiSlider.setMin(-Math.PI);
            phiSlider.setMax(Math.PI);
            phiSlider.valueProperty().bindBidirectional(colorsPhi0Prop[i]);
            // bind label to the slider
            colorsPhi0Label[i].textProperty().bindBidirectional(phiSlider.valueProperty(), phaseConverter);

            // configure period slider
            Slider periodSlider = colorsPerSlider[i];
            periodSlider.setMin(1.0);
            periodSlider.setMax(limitIter);
            periodSlider.valueProperty().bindBidirectional(colorsPerProp[i]);
            // bind label to slider value
            colorsPerLabel[i].textProperty().bindBidirectional(periodSlider.valueProperty(), periodConverter);
        }
        // configure color picker for choosing color of the fractal
        fractalColor.valueProperty().bindBidirectional(currentPalette.fractalColorProperty());


        // create data series for the line chart
        XYChart.Series<Number, Number> lines[] = new XYChart.Series[3];
        String lineNames[] = {"red", "green", "blue"};
        IterativePaletteSin pl = currentPalette.createPalette();
        IntToDoubleFunction calcsY[] = {
                x -> pl.numIterToColor(x).getRed(),
                x -> pl.numIterToColor(x).getGreen(),
                x -> pl.numIterToColor(x).getBlue()
        };
        for (int i = 0; i < 3; i++) {
            IntToDoubleFunction y = calcsY[i];
            lines[i] = new XYChart.Series<>(lineNames[i], FXCollections.observableArrayList(
                    IntStream.range(1, limitIter + 1).mapToObj(x -> new XYChart.Data<Number, Number>(x, y.applyAsDouble(x))).collect(Collectors.toList())
            ));
        }
        // add data to chart
        lineChart.getData().addAll(lines);
        // set limit for x axis
        ((NumberAxis) lineChart.getXAxis()).setUpperBound(limitIter);
        // calculate tick step
        double tick = Math.pow(10, Math.round(Math.log10(limitIter)) - 1);
        ((NumberAxis) lineChart.getXAxis()).setTickUnit(tick);
        // recalculate lines when current palette is updated
        currentPalette.addListener(obs -> {
            IterativePaletteSin newPalette = ((IterativePaletteSinProperty) obs).createPalette();
            lines[0].getData().stream().forEach(data -> data.setYValue(newPalette.numIterToColor(data.getXValue().intValue()).getRed()));
            lines[1].getData().stream().forEach(data -> data.setYValue(newPalette.numIterToColor(data.getXValue().intValue()).getGreen()));
            lines[2].getData().stream().forEach(data -> data.setYValue(newPalette.numIterToColor(data.getXValue().intValue()).getBlue()));
        });


        // update canvasPalette and fractalColor when currentPalette is changed
        currentPalette.addListener(obs -> updateCurrentPaletteCanvas());
        // update canvasPalette when it is resized
        // Platform#runLater is usage because the listener must be notified when sizes of the chart have been updated
        canvasPalette.widthProperty().addListener(obs -> Platform.runLater(this::updateCurrentPaletteCanvas));
    }


    /**
     * Update {@code canvasPalette} using {@code} current palette
     */
    private void updateCurrentPaletteCanvas() {
        // get chart content
        Region chartContent = (Region) lineChart.getChildrenUnmodifiable().stream()
                .filter(c -> c.getStyleClass().contains("chart-content")).findFirst().get();
        // get chart plot background
        Region chartPlotBackground = (Region) chartContent.getChildrenUnmodifiable().stream()
                .filter(c -> c.getStyleClass().contains("chart-plot-background")).findFirst().get();
        // width of the chart
        double chartWidth = lineChart.getWidth();
        // get size of the chart plot background
        double plotWidth = chartPlotBackground.getWidth();
        // get the width from left side of the chart to right side of the chart-plot-background
        double leftIndent = chartWidth - plotWidth - lineChart.getPadding().getRight() - chartContent.getPadding().getRight();

        // create new gradient
        IterativePaletteSin pl = currentPalette.createPalette();
        int maxI = getMaxIter();
        Stop[] stops = (maxI > 1 ?
                IntStream.range(1, maxI + 1).mapToObj(i -> new Stop((i - 1.0) / (maxI - 1.0), pl.numIterToColor(i))).toArray(Stop[]::new) :
                new Stop[]{new Stop(0.5, pl.numIterToColor(1))});
        LinearGradient linearGradient = new LinearGradient(leftIndent, 0, leftIndent + plotWidth, 0, false, CycleMethod.NO_CYCLE, stops);

        // draw rectangle on the canvas
        GraphicsContext gc = canvasPalette.getGraphicsContext2D();
        gc.setFill(linearGradient);
        gc.fillRect(0, 0, canvasPalette.getWidth(), canvasPalette.getHeight());
    }

    /**
     * Get palette.
     *
     * @return palette
     */
    public final IterativePaletteSin getPalette() {
        return palette;
    }

    /**
     * Set palette.
     *
     * @param palette palette
     */
    private final void setPalette(IterativePaletteSin palette) {
        this.palette = Objects.requireNonNull(palette);
    }

    /**
     * Get limit iterations of the ComplexFractalChecker.
     *
     * @return limit iterations
     */
    public int getMaxIter() {
        return maxIter;
    }

    /**
     * limit iterations of the ComplexFractalChecker.
     *
     * @param maxIter limit iterations
     */
    private void setMaxIter(int maxIter) {
        if (maxIter <= 1)
            throw new IllegalArgumentException("Uncorrected value of the maxIter");
        this.maxIter = maxIter;
    }


    /**
     * Reset palette.
     */
    @FXML
    private void defalutPalette() {
        currentPalette.setPaletteSettings(new IterativePaletteSin());
    }

    /**
     * Save current palette and close window.
     */
    @FXML
    private void savePaletteChanges() {
        // save palette
        setPalette(currentPalette.createPalette());
        // close window
        closeWindow();
    }

    /**
     * Close window without saving the palette.
     */
    @FXML
    private void cancelPaletteChanges() {
        // cancel changing
        currentPalette.setPaletteSettings(getPalette());
        // close window
        closeWindow();
    }

    @FXML
    private void setFractalColorAsEndPalette() {
        // get color at the end of the palette
        Color endColor = currentPalette.createPalette().numIterToColor(getMaxIter());
        fractalColor.setValue(endColor);
    }
}
