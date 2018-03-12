package us.nagro.august.caseapp.elements;

import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class NewKeyStoreDialog extends Dialog<NewKeyStoreDialog.NewKeyStoreDialogResult> {

    public static class NewKeyStoreDialogResult {
        public final String name;
        public final String password;
        public final String description;

        NewKeyStoreDialogResult(String name, String password, String description) {
            this.name = name;
            this.password = password;
            this.description = description;
        }
    }

    private static final int MASTER_PASS_MIN_LENGTH = 8;

    @FXML private TextField     name;
    @FXML private PasswordField pass;
    @FXML private PasswordField confirmPass;
    @FXML private TextField     description;

    public NewKeyStoreDialog(String defaultName) {
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/views/new-keystore-dialog.fxml"));
        VBox dialogContent = new VBox();
        fxml.setRoot(dialogContent);
        fxml.setControllerFactory(cls -> this);
        try {
            fxml.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        name.setText(defaultName);
        getDialogPane().setContent(dialogContent);

        ButtonType confirmButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().setAll(confirmButton, cancelButton);

        BooleanBinding nameIsEmpty = name.textProperty().isEmpty();
        BooleanBinding passIsBad = pass.textProperty().length().lessThan(MASTER_PASS_MIN_LENGTH)
                .or(pass.textProperty().isNotEqualTo(confirmPass.textProperty()));

        getDialogPane().lookupButton(confirmButton).disableProperty().bind(nameIsEmpty.or(passIsBad));

        setResultConverter(button -> {
            if (button.getButtonData().isCancelButton()) return null;
            else return new NewKeyStoreDialogResult(name.getText(), pass.getText(), description.getText());
        });
    }


}
