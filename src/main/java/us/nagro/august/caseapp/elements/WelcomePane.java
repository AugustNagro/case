package us.nagro.august.caseapp.elements;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import us.nagro.august.caseapp.prefs.UserPreferences;
import us.nagro.august.caseapp.utils.autotype.AutoType;

import java.io.IOException;

public class WelcomePane extends BorderPane {

    @FXML private Button continueToAppButton;
    @FXML private VBox   centerContent;
    @FXML private VBox   autoTypeNotice;

    public WelcomePane() {
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/views/welcome.fxml"));
        fxml.setRoot(this);
        fxml.setControllerFactory(cls -> this);
        try {
            fxml.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            centerContent.getChildren().add(autoTypeNotice);
            continueToAppButton.setDisable(true);
        }
    }

    /**
     * Mac requires Accessibility features for jnativehook to work.
     */
    public void registerAutoTypeMac(ActionEvent ae) {
        UserPreferences.setIsAutoTypeEnabled(true);
        AutoType.register(null);
        getScene().getWindow().hide();
    }

    public void close(ActionEvent ae) {
        getScene().getWindow().hide();
    }

    public void disableAutoType(ActionEvent ae) {
        UserPreferences.setIsAutoTypeEnabled(false);
        continueToAppButton.setDisable(false);
        autoTypeNotice.setDisable(true);
    }
}
