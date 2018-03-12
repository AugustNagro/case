package us.nagro.august.caseapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import us.nagro.august.caseapp.elements.ViewManager;
import us.nagro.august.caseapp.prefs.UserPreferences;
import us.nagro.august.caseapp.utils.autotype.AutoType;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public class Main extends Application {

    public static void main(String[] args) {
        // configure logging. Ideally would use command line option
        // -Djava.util.logging.config.file, but javapackager
        // (used to build native images)
        // doesn't seem to support this.
        InputStream config = Main.class.getResourceAsStream("/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(this::errorDialog);

        Scene mainScene = new Scene(new ViewManager());
        mainScene.getStylesheets().addAll(UserPreferences.getStyles());

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Case");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // java doesn't exit properly without unregistering jnativehook
        AutoType.unregister();
    }

    private void errorDialog(Thread t, Throwable e) {
        new Alert(Alert.AlertType.ERROR, e.getLocalizedMessage()).showAndWait();
    }
}
