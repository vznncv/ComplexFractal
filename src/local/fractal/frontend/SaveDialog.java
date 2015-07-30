package local.fractal.frontend;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import local.fractal.model.ComplexFractalChecker;
import local.fractal.util.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * It's controller of the window of the save dialog.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class SaveDialog extends BaseDialog{
    // text field with file name for saving the fractal
    @FXML
    private TextField fileName;
    // width and height text fields
    @FXML
    private TextField imageWidth;
    @FXML
    private TextField imageHeight;
    // save button
    @FXML
    private Button saveButton;
    // progress bar
    @FXML
    private ProgressBar progressBar;

    // parameters for rendering fractal
    private ComplexFractalChecker complexFractalChecker;
    private IterativePalette iterativePalette;
    private Point2DTransformer transform;
    // fractal drawer
    private ComplexFractalDrawer fd = new ComplexFractalDrawer();

    /**
     * Construct window of the save dialog.
     *
     * @param stage window of the save dialog
     * @return controller of the window
     */
    public static SaveDialog createWindow(Stage stage) {
        // load the graph scene
        FXMLLoader fxmlLoader = new FXMLLoader(SaveDialog.class.getResource("SaveDialog.fxml"));
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // create and set scene
        stage.setScene(new Scene(root));
        // set title of the window
        stage.setTitle("Save current fractal");
        // set minimal size of the window
        stage.setMinWidth(400);
        stage.setMinHeight(200);
        // save stage
        SaveDialog controller = fxmlLoader.getController();
        controller.setStage(stage);

        // cancel rendering if the window is hide
        stage.setOnCloseRequest(event -> controller.fd.setPermitWork(false));

        return controller;
    }

    /**
     * Get current affine transform.
     *
     * @return affine transform
     */
    public Point2DTransformer getTransform() {
        return transform;
    }

    /**
     * Set current
     * Initial source of coordinate is in the center and image accommodate the square with coordinates of the corners:
     * (1, 1), (1, -1), (-1, -1), (-1, 1).
     *
     * @param transform new affine transform
     */
    public void setTransform(Point2DTransformer transform) {
        this.transform = Objects.requireNonNull(transform);
    }

    /**
     * Get initiative palette of the fractal
     *
     * @return initiative palette
     */
    public IterativePalette getIterativePalette() {
        return iterativePalette;
    }

    /**
     * Set initiative palette of the fractal.
     *
     * @param iterativePalette initiative palette
     */
    public void setIterativePalette(IterativePalette iterativePalette) {
        this.iterativePalette = iterativePalette;
    }

    /**
     * Get checker of the fractal.
     *
     * @return checker of the fractal
     */
    public ComplexFractalChecker getComplexFractalChecker() {
        return complexFractalChecker;
    }

    /**
     * Set checker of the fractal.
     *
     * @param complexFractalChecker checker of the fractal
     */
    public void setComplexFractalChecker(ComplexFractalChecker complexFractalChecker) {
        this.complexFractalChecker = complexFractalChecker;
    }

    /**
     * Initialize function, it will be invoked after the scene graph is loaded.
     */
    @FXML
    private void initialize() {
        // set validation of the width and height
        TextFormatterUtil.setIntegerRange(imageWidth, 100, 8000, 1600);
        TextFormatterUtil.setIntegerRange(imageHeight, 100, 4500, 900);
        // set auto resolver for absolute path for fileName
        Consumer<Object> pathResolver = obj -> {
            try {
                fileName.setText(Paths.get(fileName.getText()).toAbsolutePath().toString());
            } catch (InvalidPathException e) {
                // do nothing (error will show when user clicks "save" button
            }
        };
        fileName.onActionProperty().addListener((obj, o, n) -> pathResolver.accept(obj));
        fileName.focusedProperty().addListener((obj, o, n) -> {
            if (!n) {
                pathResolver.accept(obj);
            }
        });
        // bind progress bar
        final AtomicReference<Double> updateProgress = new AtomicReference<>();
        fd.progressProperty().addListener((obs, o, n) -> {
            if (updateProgress.getAndSet((Double) n) == null)
                Platform.runLater(() -> progressBar.setProgress(updateProgress.getAndSet(null)));
        });
    }


    /**
     * Choose file for saving the image of the fractal.
     */
    @FXML
    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save image to file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));
        File file = fileChooser.showSaveDialog(fileName.getScene().getWindow());
        if (file != null) {
            String fName = file.getAbsolutePath();
            if (!fName.endsWith(".png"))
                fName = fName + ".png";
            fileName.setText(fName);
        }
    }


    /**
     * Render and save the fractal.
     * Notice: error message may be show uncorrected (they cut too long text inside them)
     */
    @FXML
    private void save() {
        // check the needed parameters
        ComplexFractalChecker fractalChecker = getComplexFractalChecker();
        if (fractalChecker == null) {
            throw new IllegalStateException("ComplexFractalChecker isn't set");
        }
        Point2DTransformer transform = getTransform();
        if (transform == null) {
            throw new IllegalArgumentException("Point2DTransformer isn't set");
        }
        IterativePalette palette = getIterativePalette();
        if (palette == null) {
            throw new IllegalArgumentException("IterativePalette isn't set");
        }

        // get users input
        int width = Integer.valueOf(imageWidth.getText());
        int height = Integer.valueOf(imageHeight.getText());
        Path file;
        try {
            file = Paths.get(fileName.getText()).toAbsolutePath();
        } catch (InvalidPathException e) {
            new Alert(Alert.AlertType.ERROR, "Uncorrected file path: " + fileName.getText()).showAndWait();
            return;
        }
        // check that set file have ".png" extension
        if (!file.toString().endsWith(".png")) {
            new Alert(Alert.AlertType.ERROR, "The file must have \".png\" extension.").showAndWait();
            return;
        }
        // check that isn't directory
        if (Files.isDirectory(file)) {
            new Alert(Alert.AlertType.ERROR, "The directory with same name \"" + file + "\" exists.").showAndWait();
            return;
        }
        // check that file is available for writing if it exists
        if (Files.exists(file) && !Files.isWritable(file)) {
            new Alert(Alert.AlertType.ERROR, "The existing file \"" + file.toString() + "\" cannot be overwrote").showAndWait();
            return;
        }
        // check that it can create new file if it doesn't exist
        if (Files.notExists(file) && (file.getParent() == null || !Files.isWritable(file.getParent()))) {
            new Alert(Alert.AlertType.ERROR, "The file \"" + file + "\" cannot be created.").showAndWait();
            return;
        }

        // disable save button
        saveButton.setDisable(true);

        // prepare for drawing the fractal
        fd.setImage(new WritableImage(width, height));
        fd.setPermitWork(true);
        new Thread(() -> {
            // start drawing
            fd.drawFractal(ImageUtils.calculateInitialTransform(width, height).addAfter(getTransform()),
                    getComplexFractalChecker(), getIterativePalette());

            // if fractal has been drawn, than try to save it
            // (work isn't interrupted, i.e. permitWork set to true)
            if (fd.isPermitWork()) {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(fd.getImage(), null), "png", file.toFile());
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR, "The fractal cannot be save\n." + e.getMessage()).showAndWait();
                }
            }

            // enable save button
            saveButton.setDisable(false);
        }).start();
    }

    /**
     * Cancel current calculation or close window if calculation doesn't perform.
     */
    @FXML
    private void cancel() {
        if (saveButton.isDisabled()) {
            // cancel current calculation
            fd.setPermitWork(false);
        } else {
            // close window
            closeWindow();
        }

    }
}
