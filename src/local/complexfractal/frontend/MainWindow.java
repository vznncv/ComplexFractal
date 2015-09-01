package local.complexfractal.frontend;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import local.complexfractal.model.ComplexFractal;
import local.complexfractal.util.ComplexFractalCanvasDrawer;
import local.complexfractal.util.IterativePaletteSin;
import local.complexfractal.util.Point2DTransformer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * The {@code MainWindow} represents controller of the main window.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class MainWindow {
    // helper dialogs
    SinPaletteChoiceDialog sinPaletteChoiceDialog;
    SaveDialog saveDialog;
    ChooseComplexFractalDialog chooseComplexFractalDialog;
    // root node of the window
    @FXML
    private Parent root;
    // drawer of the fractal
    private ComplexFractalCanvasDrawer fd;
    // canvas for painting
    @FXML
    private Canvas mainCanvas;
    // indicator of the work
    @FXML
    private Circle workIndicator;

    // toggles of the navigation modes
    @FXML
    private ToggleGroup zoomToggleGroup;
    @FXML
    private ToggleGroup rotateShiftToggleGroup;

    // coordinates of the mouse over the canvas
    private double xMouseCanvas;
    private double yMouseCanvas;

    /**
     * Constructs window of the program.
     * This method construct and set {@code scene}, set window title and minimal size of the window, but doesn't show
     * window.
     *
     * @param stage stage of the window
     * @return controller of this window
     */
    public static MainWindow createWindow(Stage stage) {
        // load the graph scene
        FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("MainWindow.fxml"));
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // create and set scene
        stage.setScene(new Scene(root));
        // set title of the window
        stage.setTitle("Fractals");
        // set minimal size of window
        stage.setMinWidth(600);
        stage.setMinHeight(400);

        return fxmlLoader.getController();
    }

    /**
     * Initialization function, it will be invoked after the scene graph is loaded.
     */
    public void initialize() {
        fd = new ComplexFractalCanvasDrawer(mainCanvas, ChooseComplexFractalDialog.getDefaultComplexFractal(), SinPaletteChoiceDialog.getDefaultPalette());
        // set indicator of the working
        InvalidationListener updateWorkIndicator = (obs) -> {
            boolean status = ((ReadOnlyBooleanProperty) obs).get();
            List<String> styleClasses = workIndicator.getStyleClass();
            styleClasses.remove("working");
            styleClasses.remove("waiting");
            styleClasses.add(status ? "working" : "waiting");
        };
        // bind indicator
        fd.workProperty().addListener(updateWorkIndicator);
        // set initial value of the indicator
        updateWorkIndicator.invalidated(fd.workProperty());
    }

    /**
     * Determines the zoom option.
     *
     * @return true if "zoom in" is chosen, false if "zoom out" is chosen
     */
    private boolean isZoomInChosen() {
        final String zoomInId = "zoomInRadioButton";
        final String zoomOutId = "zoomOutRadioButton";
        String rbId = ((RadioButton) zoomToggleGroup.getSelectedToggle()).getId();
        switch (rbId) {
            case zoomInId:
                return true;
            case zoomOutId:
                return false;
            default:
                throw new IllegalStateException("Unknown radio button.");
        }
    }

    /**
     * Determines the shift/rotate option.
     *
     * @return true if "shift" is chosen, false if "rotate" is chosen
     */
    private boolean isTranslateChosen() {
        final String shiftId = "shiftRadioButton";
        final String rotateId = "rotateRadioButton";
        String rbId = ((RadioButton) rotateShiftToggleGroup.getSelectedToggle()).getId();
        switch (rbId) {
            case shiftId:
                return true;
            case rotateId:
                return false;
            default:
                throw new IllegalStateException("Unknown radio button.");
        }
    }

    /**
     * Drags mouse on canvas.
     *
     * @param event mouse event
     */
    @FXML
    private void canvasMouseDrag(MouseEvent event) {
        if (isTranslateChosen()) {
            // shift image
            Point2DTransformer prevTr = fd.getTransform();
            fd.translateImage(event.getX() - xMouseCanvas, event.getY() - yMouseCanvas);
            Point2DTransformer nextTr = fd.getTransform();
            // store coordinate of the mouse on the canvas if transform is changed
            if (!nextTr.equals(prevTr)) {
                xMouseCanvas = event.getX();
                yMouseCanvas = event.getY();
            }
        } else {
            // rotate image

            // coordinate of the center of the image
            double xC = mainCanvas.getWidth() / 2;
            double yC = mainCanvas.getHeight() / 2;
            // vector from the center of image to new mouse coordinate
            double vXN = xC - event.getX();
            double vYN = -(yC - event.getY());
            // vector from the center of image to old mouse coordinate
            double vXO = xC - xMouseCanvas;
            double vYO = -(yC - yMouseCanvas);
            // calculate the angle between {vXN, vYN} and {vXO, vYO}
            double angle = Math.atan2(vYO, vXO) - Math.atan2(vYN, vXN);
            if (!Double.isNaN(angle)) {
                fd.rotateImage(angle);
            }
            // store the coordinate of the mouse on the canvas
            xMouseCanvas = event.getX();
            yMouseCanvas = event.getY();
        }
    }

    /**
     * Starts dragging mouse on the canvas.
     *
     * @param event mouse event
     */
    @FXML
    private void canvasMousePressed(MouseEvent event) {
        // store the coordinate of the mouse on canvas
        xMouseCanvas = event.getX();
        yMouseCanvas = event.getY();
    }

    /**
     * Restores zoom and position setting to default.
     *
     * @param actionEvent button event
     */
    @FXML
    private void canvasDefaultScale(ActionEvent actionEvent) {
        fd.defaultScaleImage();
    }

    /**
     * Zooms fractal.
     *
     * @param event mouse event
     */
    @FXML
    private void canvasMouseClicked(MouseEvent event) {
        final double scaleCoefficient = 2;

        if (event.getClickCount() >= 2) {
            // scale image
            if (isZoomInChosen())
                fd.scaleImage(1 / scaleCoefficient, 1 / scaleCoefficient, event.getX(), event.getY());
            else
                fd.scaleImage(scaleCoefficient, scaleCoefficient, event.getX(), event.getY());
        }
    }

    /**
     * Opens save dialog.
     */
    @FXML
    private void openSaveDialog() {
        // create new dialog window if this is required
        if (saveDialog == null) {
            // create new stage
            Stage saveDialogWindow = new Stage();
            saveDialogWindow.initOwner(root.getScene().getWindow());
            saveDialogWindow.initModality(Modality.WINDOW_MODAL);
            // construct window
            saveDialog = SaveDialog.createWindow(saveDialogWindow);
        }
        // set current fractal parameters
        saveDialog.setComplexFractalChecker(fd.getFractal());
        saveDialog.setIterativePalette(fd.getPalette());
        saveDialog.setTransform(fd.getTransform());

        // show dialog
        saveDialog.showAndWait();
    }

    /**
     * Opens choosing palette dialog.
     */
    @FXML
    private void openSinPaletteChoiceDialog() {
        // get and check palette
        if (!(fd.getPalette() instanceof IterativePaletteSin)) {
            new Alert(Alert.AlertType.ERROR, "It cannot modify that palette type.").show();
            return;
        }
        IterativePaletteSin palette = (IterativePaletteSin) fd.getPalette();

        // get and check current fractal
        if (!(fd.getFractal() instanceof ComplexFractal)) {
            new Alert(Alert.AlertType.ERROR, "It cannot modify that palette type with that type of the fractal.").show();
            return;
        }
        ComplexFractal fractalChecker = (ComplexFractal) fd.getFractal();

        // create new dialog window if this is required
        if (sinPaletteChoiceDialog == null) {
            // create new stage
            Stage paletteDialogWindow = new Stage();
            paletteDialogWindow.initOwner(root.getScene().getWindow());
            paletteDialogWindow.initModality(Modality.WINDOW_MODAL);
            // construct window
            sinPaletteChoiceDialog = SinPaletteChoiceDialog.createWindow(paletteDialogWindow);
        }

        // set current palette
        sinPaletteChoiceDialog.setPalette(palette);
        // set maximum numbers of the iterations
        sinPaletteChoiceDialog.setMaxIter(fractalChecker.getMaxIter());


        // show dialog
        sinPaletteChoiceDialog.showAndWait();

        // save new palette
        fd.setPalette(sinPaletteChoiceDialog.getPalette());
    }

    /**
     * Opens choosing fractal dialog.
     */
    @FXML
    private void openComplexFractalDialog() {
        // get and check current fractal
        if (!(ChooseComplexFractalDialog.isSupportComplexFractal(fd.getFractal()))) {
            new Alert(Alert.AlertType.ERROR, "It cannot modify that type of the fractal.").show();
            return;
        }

        // create new dialog window if this is required
        if (chooseComplexFractalDialog == null) {
            // create new stage
            Stage paletteDialogWindow = new Stage();
            paletteDialogWindow.initOwner(root.getScene().getWindow());
            paletteDialogWindow.initModality(Modality.WINDOW_MODAL);
            // construct window
            chooseComplexFractalDialog = ChooseComplexFractalDialog.createWindow(paletteDialogWindow);
        }

        // set current complex fractal
        chooseComplexFractalDialog.setComplexFractal((ComplexFractal) fd.getFractal());

        // show dialog
        chooseComplexFractalDialog.showAndWait();

        // save new fractal
        fd.setFractal(chooseComplexFractalDialog.getComplexFractal());
    }
}
