package local.fractal;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.shape.Circle;
import local.fractal.model.MandelbrotSet;

import java.util.List;

/**
 * It's controller of the main window.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class Controller {
    ComplexFractalDrawer fd;
    // the root node
    //@FXML
    //private GridPane root;
    // canvas for painting
    @FXML
    private Canvas mainCanvas;
    // indicator of the work
    @FXML
    private Circle workIndicator;

    /**
     * Initialize function, it will be invoked after the scene graph is loaded.
     */
    public void initialize() {
        fd = new ComplexFractalDrawer(mainCanvas, new MandelbrotSet());
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
     * Repaint canvas.
     */
    public void repaintCanvas() {

    }
}
