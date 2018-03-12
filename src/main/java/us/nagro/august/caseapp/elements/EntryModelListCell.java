package us.nagro.august.caseapp.elements;

import javafx.scene.control.cell.TextFieldListCell;
import us.nagro.august.caseapp.models.EntryModel;

public class EntryModelListCell extends TextFieldListCell<EntryModel> {
    @Override
    public void updateItem(EntryModel item, boolean empty) {
        textProperty().unbind();
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            textProperty().bind(item.titleProperty());
        }
    }
}
