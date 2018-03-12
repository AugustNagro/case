package us.nagro.august.caseapp.elements;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

/**
 * A password field that shows the characters when focused.
 */
public class MaskedText extends StackPane {

    private StringProperty text = new SimpleStringProperty();

    private TextField     focusedTF   = new TextField();
    private PasswordField unfocusedPF = new PasswordField();

    public MaskedText() {
        getChildren().add(unfocusedPF);
        unfocusedPF.textProperty().bindBidirectional(text);
        focusedTF.textProperty().bindBidirectional(text);

        unfocusedPF.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                getChildren().setAll(focusedTF);
                focusedTF.requestFocus();
                focusedTF.selectAll();
            }
        });

        focusedTF.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) getChildren().setAll(unfocusedPF);
        });
    }

    public StringProperty textProperty() {
        return text;
    }
}
