package local.fractal.frontend;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import local.fractal.model.MandelbrotSet;
import local.fractal.util.ComplexFractalCanvasDrawer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * It's controller of the main window.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class MainWindow {
    // Drawer of the fractal
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

    // coordinate of the mouse over the canvas
    private double xMouseCanvas;
    private double yMouseCanvas;

    /**
     * Construct window of the program.
     * This method construct and set {@code scene}, set window title and minimal size of the window,
     * but doesn't show window.     *
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
        stage.minWidthProperty().set(600);
        stage.minHeightProperty().set(400);

        return fxmlLoader.getController();
    }

    /**
     * Initialize function, it will be invoked after the scene graph is loaded.
     */
    public void initialize() {
        fd = new ComplexFractalCanvasDrawer(mainCanvas, new MandelbrotSet());
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
     * Determine the zoom option.
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
     * Determine the shift/rotate option.
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
     * Drag mouse on canvas.
     *
     * @param event mouse event
     */
    public void canvasMouseDrag(MouseEvent event) {
        if (isTranslateChosen()) {
            // shift image
            fd.translateImage(event.getX() - xMouseCanvas, event.getY() - yMouseCanvas);
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
            double angle = Math.atan2(vYN, vXN) - Math.atan2(vYO, vXO);
            if (!Double.isNaN(angle)) {
                fd.rotateImage(angle);
            }
        }

        // store the coordinate of the mouse on the canvas
        xMouseCanvas = event.getX();
        yMouseCanvas = event.getY();
    }

    /**
     * Start dragging mouse on the canvas.
     *
     * @param event mouse event
     */
    public void canvasMousePressed(MouseEvent event) {
        // store the coordinate of the mouse on canvas
        xMouseCanvas = event.getX();
        yMouseCanvas = event.getY();
    }

    /**
     * Restore zoom and position setting to default.
     *
     * @param actionEvent button event
     */
    public void canvasDefaultScale(ActionEvent actionEvent) {
        fd.defaultScaleImage();
    }

    /**
     * Zoom fractal.
     *
     * @param event mouse event
     */
    public void canvasMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            // scale image
            if (isZoomInChosen())
                fd.scaleImage(0.5, 0.5, event.getX(), event.getY());
            else
                fd.scaleImage(2, 2, event.getX(), event.getY());
        }
    }

    /**
     * Open save dialog
     *
     * @param actionEvent button event
     */
    public void openSaveDialog(ActionEvent actionEvent) throws IOException {
        // show open save dialog
        //SaveDialog.showDialog(mainWindows, fd);
        //((Node) actionEvent.getSource()).getScene().;
    }
}
