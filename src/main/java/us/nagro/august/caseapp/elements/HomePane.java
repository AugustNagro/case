package us.nagro.august.caseapp.elements;

import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;

public abstract class HomePane extends HBox {
    public abstract void lock(ActionEvent ae);
    public abstract void newEntry(ActionEvent ae);
    public abstract void newGroup(ActionEvent ae);
    public abstract void goPreferences(ActionEvent ae);
    public abstract void selectSearch(ActionEvent ae);
    public abstract void recycleBin(ActionEvent ae);
}
