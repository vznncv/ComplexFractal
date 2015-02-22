package local.fractal;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.GridPane;
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

    /**
     * Initialize function, it will be invoked after the scene graph is loaded.
     */
    public void initialize() {
        // set canvas size as:
        // mainCanvas.width = root.width - wBorder
        // mainCanvas.height = root.height - hBorder;
        double wBorder = 40;
        double HBorder = 100;
        mainCanvas.widthProperty().bind(Bindings.subtract(root.widthProperty(), wBorder));
        mainCanvas.heightProperty().bind(Bindings.subtract(root.heightProperty(), HBorder));

        fd = new ComplexFractalDrawer(mainCanvas, new MandelbrotSet());
    }

    /**
     * Repaint canvas.
     */
    public void repaintCanvas() {

        //fd.getTransform().clear();
        //fd.draw();
    }
}
