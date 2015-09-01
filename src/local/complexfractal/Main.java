package local.complexfractal;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import local.complexfractal.frontend.MainWindow;

/**
 * This is main class of the program which this program starts.
 *
 * @author Kochin Konstantin Alexandrovich
 */
public class Main extends Application {
    private static HostServices hostServices;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Shows page at the default browser.
     *
     * @param uri uri of the page
     * @throws IllegalStateException if application isn't initialized
     */
    public static void showDocument(String uri) {
        if (hostServices == null)
            throw new IllegalStateException("Application isn't initialized");
        hostServices.showDocument(uri);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // save host service
        hostServices = getHostServices();
        // create and show window
        MainWindow.createWindow(primaryStage);
        // set icon
        primaryStage.getIcons().add(new Image(Main.class.getResource("icon.png").toString()));
        // show window
        primaryStage.show();
    }
}
