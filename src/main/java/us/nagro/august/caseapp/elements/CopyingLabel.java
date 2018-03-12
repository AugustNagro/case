package us.nagro.august.caseapp.elements;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;

/**
 * A Label that copies the text of the Node referenced
 * by its labelFor property to the system clipboard,
 * if labelFor is a TextInputControl.
 */
public class CopyingLabel extends Label {
    public CopyingLabel() {
        getStyleClass().add("copyingLabel");
        setOnMouseClicked(this::copyToClipboard);
    }

    private void copyToClipboard(MouseEvent mouseEvent) {
        if (getLabelFor() != null && getLabelFor() instanceof TextInputControl) {
            TextInputControl labelFor = (TextInputControl) getLabelFor();

            ClipboardContent cbc = new ClipboardContent();
            cbc.putString(labelFor.getText());
            Clipboard.getSystemClipboard().setContent(cbc);
        }
    }
}
