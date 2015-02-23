package local.fractal;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import local.fractal.model.MandelbrotSet;

/**
 * It's controller of the main window.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class Controller {
    ComplexFractalDrawer fd;
    // the root node
    @FXML
    private GridPane root;
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
        fd.workProperty().addListener((obs) -> {
            Platform.runLater(() -> {
                boolean status = ((ReadOnlyBooleanProperty) obs).get();
                workIndicator.getStyleClass().clear();
                if (status) {
                    workIndicator.getStyleClass().add("working");
                } else {
                    workIndicator.getStyleClass().add("waiting");
                }
            });

        });

    }

    /**
     * Repaint canvas.
     */
    public void repaintCanvas() {

    }
}
