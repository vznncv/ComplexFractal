package local.fractal;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * It's controller of the window of the save dialog.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class SaveDialogController {


    /**
     * This method showing save dialog for fractal image with blocking the {@code parentWindow}
     *
     * @param parentWindow parent windows
     * @param fd           fractal drawer
     */
    public static void showDialog(Stage parentWindow, ComplexFractalCanvasDrawer fd) {
        // load the graph scene
        FXMLLoader fxmlLoader = new FXMLLoader(SaveDialogController.class.getResource("/frontend/SaveDialog.fxml"));
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // create window
        Stage window = new Stage();
        window.setScene(new Scene(root));
        window.initOwner(parentWindow);
        window.initModality(Modality.WINDOW_MODAL);
        // set title of the window
        window.setTitle("Save current fractal image");
        // set minimal size of window
        window.minWidthProperty().set(400);
        window.minHeightProperty().set(200);
        // get controller
        SaveDialogController controller = fxmlLoader.getController();
        // set controller options

        // show windows
        window.showAndWait();
    }


}
