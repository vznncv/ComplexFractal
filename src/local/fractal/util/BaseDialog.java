package local.fractal.util;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * It's helper superclass for controller of additional windows.
 */
public class BaseDialog {
    /**
     * State of the window.
     *
     * @defaultValue null
     */
    private ReadOnlyObjectWrapper<Stage> stage = new ReadOnlyObjectWrapper<>();

    public final Stage getStage() {
        return stage.get();
    }

    protected final void setStage(Stage stage) {
        this.stage.set(Objects.requireNonNull(stage));
    }

    public ReadOnlyObjectProperty<Stage> stageProperty() {
        return stage.getReadOnlyProperty();
    }

    /**
     * Shows the window, but does not wait for a user response.
     */
    public void show() {
        getStage().show();
    }

    /**
     * Shows the dialog and waits for the user response.
     */
    public void showAndWait() {
        getStage().showAndWait();
    }

    /**
     * Hide the window.
     */
    protected void closeWindow() {
        getStage().close();
    }
}
