package us.nagro.august.caseapp.elements;

import javafx.scene.control.cell.TextFieldTreeCell;
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper;
import us.nagro.august.caseapp.models.EntryModel;

public class GroupTreeCell extends TextFieldTreeCell<DomGroupWrapper> {

    public GroupTreeCell() {
        setConverter(EntryModel.GROUP_STRING_CONVERTER);
    }

}
