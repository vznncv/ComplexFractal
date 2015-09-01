package local.complexfractal.frontend;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import local.complexfractal.util.BaseDialog;
import local.complexfractal.util.IterativePalette;
import local.complexfractal.util.IterativePaletteSin;
import local.complexfractal.util.IterativePaletteSinPropertyVersion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * The {@code SinPaletteChoiceDialog} represents controller of the window for to change settings of the palette.
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
    // helper object for corrected canvasPalette updates
    // (size of GraphicsContext lfchanges later than Canvas size)
    private ExecutorService singlePool = Executors.newSingleThreadExecutor((task) -> {
        Thread t = new Thread(task);
        t.setDaemon(true);
        return t;
    });
    private AtomicBoolean dataIsUpdated = new AtomicBoolean(true);
    private int msDelay = 100;
    // chart
    @FXML
    private LineChart<Number, Number> lineChart;
    // current palette for storing the changes
    private IterativePaletteSinPropertyVersion currentPalette = new IterativePaletteSinPropertyVersion(getDefaultPalette());
    // resulting palette
    private IterativePaletteSin palette;
    /**
     * Defines maximum number of the iterations of the FractalChecker.
     *
     * @defaultValue 1024
     */
    private IntegerProperty maxIter = new SimpleIntegerProperty(1024);
    /**
     * Defines maximal value of the period of the colors.
     * It binds to {@code maxIter} by default.
     */
    private IntegerProperty maxColorPeriod = new SimpleIntegerProperty();
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

    {
        maxColorPeriod.bind(maxIter);
    }

    /**
     * Constructor.
     */
    private SinPaletteChoiceDialog() {
    }

    /**
     * Constructs window for choosing palette.
     *
     * @param stage window of the choosing palette dialog
     * @return controller of the window
     */
    public static SinPaletteChoiceDialog createWindow(Stage stage) {
        // load the graph scene
        FXMLLoader fxmlLoader = new FXMLLoader(SinPaletteChoiceDialog.class.getResource("SinPaletteChoiceDialog.fxml"));
        fxmlLoader.setControllerFactory(obj -> new SinPaletteChoiceDialog());
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
        stage.setMinHeight(450);
        // save current stage
        controller.setStage(stage);
        // add stylesheets of the root to scene
        // it's necessary for correct style of the ColorPicker
        stage.getScene().getStylesheets().addAll(root.getStylesheets());

        return controller;
    }

    /**
     * Gets default palette.
     *
     * @return palette
     */
    static public IterativePaletteSin getDefaultPalette() {
        return new IterativePaletteSin();
    }

    private int getMaxColorPeriod() {
        return maxColorPeriod.get();
    }

    private void setMaxColorPeriod(int maxColorPeriod) {
        this.maxColorPeriod.set(maxColorPeriod);
    }

    private IntegerProperty maxColorPeriodProperty() {
        return maxColorPeriod;
    }

    public int getMaxIter() {
        return maxIter.get();
    }

    public void setMaxIter(int maxIter) {
        this.maxIter.set(maxIter);
    }

    public IntegerProperty maxIterProperty() {
        return maxIter;
    }

    /**
     * Gets palette.
     *
     * @return palette
     */
    public final IterativePaletteSin getPalette() {
        return palette;
    }

    /**
     * Sets palette.
     *
     * @param palette palette
     * @throws NullPointerException if palette is null
     */
    public final void setPalette(IterativePaletteSin palette) {
        this.palette = Objects.requireNonNull(palette);
        currentPalette.setPaletteSettings(palette);
    }

    /**
     * Resizes length of the lines at the chart or create it, if they doesn't exist.
     * Note: {@code currentPalette} must be initialized.
     *
     * @param newSize new length of the lines
     * @throws IllegalArgumentException is newSize < 0
     */
    private void resizeChart(int newSize) {
        if (newSize < 0)
            throw new IllegalArgumentException("newSize is less than zero");

        // check that lines are created
        if (lineChart.getData().isEmpty()) {
            // create empty series
            lineChart.getData().addAll(
                    new XYChart.Series<>("red", FXCollections.observableArrayList()),
                    new XYChart.Series<>("green", FXCollections.observableArrayList()),
                    new XYChart.Series<>("blue", FXCollections.observableArrayList()));
        }
        // get series
        ObservableList<XYChart.Data<Number, Number>> rLine = lineChart.getData().get(0).getData();
        ObservableList<XYChart.Data<Number, Number>> gLine = lineChart.getData().get(1).getData();
        ObservableList<XYChart.Data<Number, Number>> bLine = lineChart.getData().get(2).getData();
        // check that size is changed (all line size have same size)
        int oldSize = rLine.size();
        if (oldSize != newSize) {
            if (newSize < oldSize) {
                // reduce lines
                rLine.remove(newSize, oldSize);
                gLine.remove(newSize, oldSize);
                bLine.remove(newSize, oldSize);
            } else {
                // increase lines
                Supplier<XYChart.Data<Number, Number>[]> appendedPoints = () ->
                        IntStream.range(oldSize + 1, newSize + 1).
                                mapToObj(x -> new XYChart.Data<>(x, 0)).
                                toArray(XYChart.Data[]::new);
                rLine.addAll(appendedPoints.get());
                gLine.addAll(appendedPoints.get());
                bLine.addAll(appendedPoints.get());
            }
            // update limit for x axis
            NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
            xAxis.setLowerBound(1);
            xAxis.setTickUnit(Math.pow(10, Math.round(Math.log10(newSize)) - 1));
            xAxis.setUpperBound(newSize);
            // update chart and canvas
            updateChart();
            updateCanvas();
        }
    }

    /**
     * Updates lines on the chart using {@code currentPalette}.
     * Note: lines must be exists.
     */
    private void updateChart() {
        // get series
        ObservableList<XYChart.Data<Number, Number>> rLine = lineChart.getData().get(0).getData();
        ObservableList<XYChart.Data<Number, Number>> gLine = lineChart.getData().get(1).getData();
        ObservableList<XYChart.Data<Number, Number>> bLine = lineChart.getData().get(2).getData();
        // calculate colors
        int maxIter = getMaxIter();
        IterativePalette pl = currentPalette.createPalette();
        Color colors[] = IntStream.range(1, maxIter + 1).mapToObj(pl::numIterToColor).toArray(Color[]::new);
        // update lines
        for (int i = 0; i < maxIter; i++) {
            Color c = colors[i];
            rLine.get(i).setYValue(c.getRed());
            gLine.get(i).setYValue(c.getGreen());
            bLine.get(i).setYValue(c.getBlue());
        }
    }

    /**
     * Updates {@code canvasPalette} using {@code currentPalette} and {@code lineChart}.
     */
    private void updateCanvas() {
        // update canvas with some delay
        if (dataIsUpdated.getAndSet(false)) {
            singlePool.execute(() -> {
                // delay
                try {
                    Thread.sleep(msDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    dataIsUpdated.set(true);

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
                    int maxIter = getMaxIter();
                    Stop[] stops;
                    if (maxIter > 1) {
                        stops = IntStream.range(1, maxIter + 1).mapToObj(i -> new Stop((i - 1.0) / (maxIter - 1.0), pl.numIterToColor(i))).toArray(Stop[]::new);
                    } else if (maxIter == 1) {
                        stops = new Stop[]{new Stop(0.5, pl.numIterToColor(1))};
                    } else {
                        stops = new Stop[]{new Stop(0.5, Color.BLACK)};
                    }
                    LinearGradient linearGradient = new LinearGradient(leftIndent, 0, leftIndent + plotWidth, 0, false, CycleMethod.NO_CYCLE, stops);


                    // draw rectangle on the canvas
                    GraphicsContext gc = canvasPalette.getGraphicsContext2D();
                    gc.setFill(linearGradient);
                    gc.fillRect(0, 0, canvasPalette.getWidth(), canvasPalette.getHeight());
                });
            });
        }
    }

    /**
     * Initialization function, it will be invoked after the scene graph is loaded.
     * Warning: LineChart is being initialized slowly.
     */
    @FXML
    private void initialize() {
        // bindings of the currentPalette
        // sliders and labels
        Slider colorsPerSlider[] = {redPeriodSlider, greenPeriodSlider, bluePeriodSlider};
        Label colorsPerLabel[] = {redPeriodLabel, greenPeriodLabel, bluePeriodLabel};
        Slider colorsPhi0Slider[] = {redPhi0Slider, greenPhi0Slider, bluePhi0Slider};
        Label colorsPhi0Label[] = {redPhi0Label, greenPhi0Label, bluePhi0Label};
        // property for bidirectional binding
        DoubleProperty colorsPerProp[] = {currentPalette.perRProperty(), currentPalette.perGProperty(), currentPalette.perBProperty()};
        DoubleProperty colorsPhi0Prop[] = {currentPalette.phi0RProperty(), currentPalette.phi0GProperty(), currentPalette.phi0BProperty()};
        // helper string converter for the phase
        NumberStringConverter phaseConverter = new NumberStringConverter(new DecimalFormat("0.00"));
        // helper string converter for the period
        NumberStringConverter periodConverter = new NumberStringConverter(new DecimalFormat("0.00"));
        // configure sliders and listeners for labels
        for (int i = 0; i < 3; i++) {
            // configure phase slider
            Slider phiSlider = colorsPhi0Slider[i];
            phiSlider.setMin(-Math.PI);
            phiSlider.setMax(Math.PI);
            phiSlider.valueProperty().bindBidirectional(colorsPhi0Prop[i]);
            // bind label to the slider
            // note: bidirectional binding uses because it can covert property with StringConverter
            colorsPhi0Label[i].textProperty().bindBidirectional(phiSlider.valueProperty(), phaseConverter);

            // configure period slider
            Slider periodSlider = colorsPerSlider[i];
            periodSlider.setMin(1.0);
            periodSlider.maxProperty().bind(Bindings.createDoubleBinding(() -> (double) getMaxColorPeriod(), maxColorPeriodProperty()));
            periodSlider.valueProperty().bindBidirectional(colorsPerProp[i]);
            // bind label to slider value
            // note: bidirectional binding uses because it can covert property with StringConverter
            colorsPerLabel[i].textProperty().bindBidirectional(periodSlider.valueProperty(), periodConverter);
        }
        // configure color picker for choosing color of the fractal
        fractalColor.valueProperty().bindBidirectional(currentPalette.fractalColorProperty());


        // update plot and canvas
        resizeChart(getMaxIter());

        // update plot when maxIter is changed
        maxIterProperty().addListener((obj, oldVal, newVal) -> {
            if (newVal.intValue() < 0)
                throw new IllegalArgumentException("Value of the maxIter property is less that zero: " + newVal);
            // update plot and canvas
            resizeChart(newVal.intValue());
        });

        // update plot and canvas if currentPalette is changed
        currentPalette.addListener(observable -> {
            updateChart();
            updateCanvas();
        });

        // update canvas if width of the chart is resized
        lineChart.widthProperty().addListener((obj, oldVal, newVal) -> updateCanvas());
    }

    /**
     * Resets palette.
     */
    @FXML
    private void defaultPalette() {
        currentPalette.setPaletteSettings(getDefaultPalette());
    }

    /**
     * Saves current palette and closes window.
     */
    @FXML
    private void savePaletteChanges() {
        // save palette
        setPalette(currentPalette.createPalette());
        // close window
        closeWindow();
    }

    /**
     * Closes window without saving the palette.
     */
    @FXML
    private void cancelPaletteChanges() {
        // cancel changing
        currentPalette.setPaletteSettings(getPalette());
        // close window
        closeWindow();
    }

    /**
     * Sets color of the fractal as the color in the end of palette.
     */
    @FXML
    private void setFractalColorAsEndPalette() {
        // get color at the end of the palette
        Color endColor = currentPalette.createPalette().numIterToColor(getMaxIter());
        fractalColor.setValue(endColor);
    }
}
