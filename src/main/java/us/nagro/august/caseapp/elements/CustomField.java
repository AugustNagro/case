package us.nagro.august.caseapp.elements;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class CustomField extends HBox {

    // todo look at how DatePicker sizes its icon
    private static Image x = new Image(CustomField.class.getResourceAsStream("/images/x.png"), 10, 10, true, true);

    private FocusedTextField field;
    private Button remove;

    public CustomField() {
        field = new FocusedTextField();
        HBox.setHgrow(field, Priority.ALWAYS);

        remove = new Button();
        remove.getStyleClass().add("combined-button");
        remove.setGraphic(new ImageView(x));

        getChildren().addAll(field, remove);
    }

    public StringProperty textProperty() {
        return field.textProperty();
    }

    public void setOnRemovePressed(EventHandler<ActionEvent> ae) {
        remove.setOnAction(ae);
    }

    public void requestFocus() {
        field.requestFocus();
    }
}
