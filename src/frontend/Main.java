package frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This is main class of the program, which this program starts.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // create the graph scene
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        // create the scene
        Scene scene = new Scene(root);
        // add the scene to the stage
        primaryStage.setScene(scene);

        // set title of the window
        primaryStage.setTitle("Fractals");
        // set minimal size of window
        primaryStage.minWidthProperty().set(600);
        primaryStage.minHeightProperty().set(400);

        // show windows
        primaryStage.show();
    }
}
