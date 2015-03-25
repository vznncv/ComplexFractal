package local.fractal;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.RadioButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import local.fractal.model.MandelbrotSet;

import java.util.List;

/**
 * It's controller of the main window.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class Controller {
    // Drawer of the fractal
    ComplexFractalCanvasDrawer fd;
    // canvas for painting
    @FXML
    private Canvas mainCanvas;
    // indicator of the work
    @FXML
    private Circle workIndicator;

    @FXML
    private RadioButton zoomInRadioButton;

    // coordinate of the mouse over the canvas
    private double xMouseCanvas;
    private double yMouseCanvas;

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

    public void translateFractal(MouseEvent event) {
        double dx = event.getX() - xMouseCanvas;
        double dy = event.getY() - yMouseCanvas;
        fd.translateImage(dx, dy);
        xMouseCanvas = event.getX();
        yMouseCanvas = event.getY();
    }

    public void translateFractalStart(MouseEvent event) {
        xMouseCanvas = event.getX();
        yMouseCanvas = event.getY();
    }

    public void defaultScale(ActionEvent actionEvent) {
        fd.defaultScaleImage();
    }

    public void scaleFractal(MouseEvent event) {
        if (event.getClickCount() == 2) {
            // scale image
            if (zoomInRadioButton.isSelected()) {
                fd.scaleImage(0.5, 0.5, event.getX(), event.getY());
            } else {
                fd.scaleImage(2, 2, event.getX(), event.getY());
            }
        }
    }
}
