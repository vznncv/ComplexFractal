package local.fractal;

import javafx.application.Application;
import javafx.stage.Stage;
import local.fractal.frontend.MainWindow;

/**
 * This is main class of the program which this program starts.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainWindow.createWindow(primaryStage);
        primaryStage.show();
    }
}
