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
        // mainCanvas.height =  root.height - hBorder;
        double wBorder = 40;
        double HBorder = 100;
        mainCanvas.widthProperty().bind(Bindings.subtract(root.widthProperty(), wBorder));
        mainCanvas.heightProperty().bind(Bindings.subtract(root.heightProperty(), HBorder));
    }

    /**
     * Repaint canvas.
     */
    public void repaintCanvas() {
        /*
        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        double width = mainCanvas.getWidth();
        double height = mainCanvas.getHeight();
        gc.clearRect(0, 0, width, height);
        gc.setFill(Color.GREEN);
        gc.setLineWidth(5);
        gc.fillOval(10, 10, width - 20, height - 20);

        Point2D a = new Point2D(1, 1);
        Point2DTransformer  tr = new Point2DTransformer();

        System.out.println("1) initial values:");
        System.out.println("   point : " + a);
        System.out.println("   matrix: " + tr);

        // shift
        tr.translation(1, 1);
        System.out.println("s) after shift:");
        System.out.println("   point : " + tr.apply(a));
        System.out.println("   matrix: " + tr);

        // rotate
        tr.rotate(-Math.PI/2);
        System.out.println("r) after rotate:");
        System.out.println("   point : " + tr.apply(a));
        System.out.println("   matrix: " + tr);

        // scaling
        tr.scale(2, 2);
        System.out.println("s) after scaling:");
        System.out.println("   point : " + tr.apply(a));
        System.out.println("   matrix: " + tr);
        */

        /*
        Point2D p = new Point2D(-1, 0);
        MandelbrotSet ms = new MandelbrotSet();
        System.out.println("Point: " + p + "; numIter: " + ms.numberIter(p.toComplex()));

        // draw palette
        IterativePaletteV1 pl = new IterativePaletteV1();
        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        double width = mainCanvas.getWidth();
        double height = mainCanvas.getHeight();
        PixelWriter pw = gc.getPixelWriter();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color c = pl.numIterToColor(i + j);
                pw.setColor(i, j, c);
            }
        }

        // test stream
        int w = 10;
        int y = 0;
        MandelbrotSet fr = new MandelbrotSet();
        IterativePaletteV1 plFr = new IterativePaletteV1();

        Object[] points = Stream.iterate(
                new Point2D(0, y),
                (prevPoint) -> new Point2D(prevPoint.getX() + 0.01, y))
                .limit(w).toArray();

        Point2D[] pn = (Point2D []) points;

        // calculate Color for point
        Stream<Color> colors = Arrays.stream(points)
                .map((point) -> fr.numberIter(point.toComplex()))
                .map((numIter) -> plFr.numIterToColor(numIter));

        System.out.println("Points:");
        Arrays.stream(points).forEach((point) -> System.out.print(point + "; "));
        System.out.println();

        System.out.println("Colors:");
        colors.forEach((color) -> System.out.print(color + "; "));
        System.out.println();*/

        ComplexFractalDrawer fd = new ComplexFractalDrawer(mainCanvas, new MandelbrotSet());
        fd.getTransform().scale(1, 1);
        fd.draw();


    }
}
