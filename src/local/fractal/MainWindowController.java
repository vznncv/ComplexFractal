package local.fractal;

import javafx.application.Platform;
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * It's controller of the main window.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class MainWindowController {
    // Main window
    private Stage mainWindows;

    // Drawer of the fractal
    private ComplexFractalCanvasDrawer fd;
    // canvas for painting
    @FXML
    private Canvas mainCanvas;
    // indicator of the work
    @FXML
    private Circle workIndicator;

    // indicators of the choice
    @FXML
    private ToggleGroup zoomToggleGroup;
    @FXML
    private ToggleGroup rotateShiftToggleGroup;

    // coordinate of the mouse over the canvas
    private double xMouseCanvas;
    private double yMouseCanvas;

    /**
     * Load scene for the window and show it.
     *
     * @param stage stage for scene
     * @return controller of this window
     */
    public static MainWindowController createWindow(Stage stage) {
        // load the graph scene
        FXMLLoader fxmlLoader = new FXMLLoader(MainWindowController.class.getResource("/frontend/MainWindow.fxml"));
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
        // get controller and initialize his field mainWindow
        MainWindowController controller = fxmlLoader.getController();
        controller.mainWindows = stage;
        // show windows
        stage.show();

        return controller;
    }

    /**
     * Initialize function, it will be invoked after the scene graph is loaded.
     */
    public void initialize() {
        fd = new ComplexFractalCanvasDrawer(mainCanvas, new MandelbrotSet());
        // set indicator of the working
        InvalidationListener updateWorkIndicator = (obs) -> Platform.runLater(() -> {
            boolean status = ((ReadOnlyBooleanProperty) obs).get();
            List<String> styleClasses = workIndicator.getStyleClass();
            styleClasses.clear();
            styleClasses.add(status ? "working" : "waiting");
        });
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
    private boolean isZoomInChoosed() {
        final String zoomInStr = "Zoom in";
        final String zoomOutStr = "Zoom out";
        String rbText = ((RadioButton) zoomToggleGroup.getSelectedToggle()).getText();
        if (rbText.equals(zoomInStr))
            return true;
        else if (rbText.equals(zoomOutStr))
            return false;
        else
            throw new IllegalStateException("Unknown radio button.");
    }

    /**
     * Determine the shift/rotate option.
     *
     * @return true if shift" is chosen, false if "rotate" is chosen
     */
    private boolean isTranslateChoosed() {
        final String shiftStr = "Shift image";
        final String rotateStr = "Rotate image";
        String rbText = ((RadioButton) rotateShiftToggleGroup.getSelectedToggle()).getText();
        if (rbText.equals(shiftStr))
            return true;
        else if (rbText.equals(rotateStr))
            return false;
        else
            throw new IllegalStateException("Unknown radio button.");
    }

    /**
     * Drag mouse on canvas.
     *
     * @param event mouse event
     */
    public void canvasMouseDrag(MouseEvent event) {
        if (isTranslateChoosed()) {
            // shift the image
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

        // store the coordinate of the mouse on canvas
        xMouseCanvas = event.getX();
        yMouseCanvas = event.getY();
    }

    /**
     * Start dragging mouse on canvas.
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
            if (isZoomInChoosed())
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
        SaveDialogController.showDialog(mainWindows, fd);
    }
}
