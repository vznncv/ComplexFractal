package local.fractal.frontend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * It's controller of the window of the save dialog.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class SaveDialog {
    // text field with file name for saving the fractal
    @FXML
    private TextField fileName;
    // width and height text fields
    @FXML
    private TextField imageWidth;
    @FXML
    private TextField imageHeight;

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

        return fxmlLoader.getController();
    }

    /**
     * Initialize function, it will be invoked after the scene graph is loaded.
     */
    public void initialize() {
        // set validation of the width and height
        TextFormatterUtil.setIntegerRange(imageWidth, 100, 8000, 1600);
        TextFormatterUtil.setIntegerRange(imageHeight, 100, 4500, 900);
    }


    /**
     * Choose file for saving the image of the fractal.
     *
     * @param e action event
     */
    @FXML
    private void chooseFile(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save image to file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));
        String fName = fileChooser.showSaveDialog(fileName.getScene().getWindow()).getAbsolutePath();
        if (!fName.endsWith(".png"))
            fName = fName + ".png";
        fileName.setText(fName);
    }

}
