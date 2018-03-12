package us.nagro.august.caseapp.elements;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import us.nagro.august.caseapp.prefs.UserPreferences;
import us.nagro.august.caseapp.utils.ViewBuilderFactory;

public class CaseMenuBar extends MenuBar {

    private final Menu fileMenu   = new Menu("File");
    private final Menu windowMenu = new Menu("Window");

    private final ObservableList<MenuItem> file   = fileMenu.getItems();
    private final ObservableList<MenuItem> window = windowMenu.getItems();

    public CaseMenuBar() {
        if (ViewBuilderFactory.IS_MOBILE) {
            setManaged(false);
            setVisible(false);
            return;
        }

        setUseSystemMenuBar(true);
        addDefaultMenuItems();
    }

    private void addDefaultMenuItems() {
        getMenus().addAll(fileMenu, windowMenu);

        MenuItem minimize = new MenuItem("Minimize");
        minimize.setOnAction(this::minimize);
        minimize.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN));
        window.add(minimize);

        MenuItem about = new MenuItem("About");
        about.setOnAction(this::aboutDialog);
        fileMenu.getItems().add(about);
    }

    private void aboutDialog(ActionEvent actionEvent) {
        Stage aboutStage = new Stage();
        Scene scene = new Scene(new AboutPane());
        scene.getStylesheets().addAll(UserPreferences.getStyles());
        aboutStage.setScene(scene);
        aboutStage.setAlwaysOnTop(false);
        aboutStage.show();
    }

    private void minimize(ActionEvent actionEvent) {
        ((Stage) getScene().getWindow()).setIconified(true);
    }

    public ObservableList<MenuItem> getFile() {
        return file;
    }

    public ObservableList<MenuItem> getWindow() {
        return window;
    }
}
