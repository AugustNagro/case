package us.nagro.august.caseapp.elements;

import javafx.scene.control.TextField;

/**
 * Highlights text when clicked.
 */
public class FocusedTextField extends TextField {
    public FocusedTextField() {
        setOnMouseClicked(me -> selectAll());
    }

    public FocusedTextField(String text) {
        this();
        setText(text);
    }
}
