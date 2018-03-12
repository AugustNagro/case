package us.nagro.august.caseapp.elements;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.linguafranca.pwdb.Entry;
import us.nagro.august.caseapp.models.EntryFinishedEvent;
import us.nagro.august.caseapp.models.EntryModel;

import java.io.IOException;

public class EntryPane extends BorderPane {
    @FXML private GridPane fieldsPane;

    @FXML private FocusedTextField title;
    @FXML private FocusedTextField username;
    @FXML private MaskedText       password;
    @FXML private FocusedTextField website;
    @FXML private TextArea         notes;
    @FXML private DatePicker       expires;

    @FXML private Label group;
    @FXML private Label created;
    @FXML private Label updated;

    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private int numDefaultFields;
    private EntryModel entryModel;

    private MapChangeListener<String, StringProperty> addFieldListener = change -> {
        if (change.wasAdded()) {
            CustomField field = addField(change.getKey(), change.getValueAdded());
            field.requestFocus();
        }
    };

    public EntryPane() {
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/views/entry.fxml"));
        fxml.setRoot(this);
        fxml.setControllerFactory(cls -> this);
        try {
            fxml.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        numDefaultFields = fieldsPane.getRowCount();
    }

    public void initialize(EntryModel model) {
        // unset existing bindings, if contents were previously set
        if (entryModel != null) {
            title.textProperty().unbindBidirectional(entryModel.titleProperty());
            username.textProperty().unbindBidirectional(entryModel.usernameProperty());
            password.textProperty().unbindBidirectional(entryModel.passwordProperty());
            website.textProperty().unbindBidirectional(entryModel.websiteProperty());
            notes.textProperty().unbindBidirectional(entryModel.notesProperty());
            expires.valueProperty().unbindBidirectional(entryModel.expiresProperty());
            group.textProperty().unbindBidirectional(entryModel.groupProperty());
            entryModel.getCustomProperties().removeListener(addFieldListener);
        }

        this.entryModel = model;

        title.textProperty().bindBidirectional(entryModel.titleProperty());
        username.textProperty().bindBidirectional(entryModel.usernameProperty());
        password.textProperty().bindBidirectional(entryModel.passwordProperty());
        website.textProperty().bindBidirectional(entryModel.websiteProperty());
        notes.textProperty().bindBidirectional(entryModel.notesProperty());
        expires.valueProperty().bindBidirectional(entryModel.expiresProperty());

        // only Bidirectional binding allows us to set a StringConverter.
        // Either way, group is a Label so it should never get updated.
        group.textProperty().bindBidirectional(entryModel.groupProperty(), EntryModel.GROUP_STRING_CONVERTER);
        created.setText(entryModel.getCreated().toString());
        updated.setText(entryModel.getUpdated().toString());

        showCustomProps();
        entryModel.getCustomProperties().addListener(addFieldListener);

        cancelButton.disableProperty().bind(entryModel.hasDiverged.not());
        saveButton.disableProperty().bind(entryModel.hasDiverged.not());
    }

    /**
     * @return true if this controller's Entry has diverged from its original values.
     */
    public boolean hasDiverged() {
        return entryModel != null && entryModel.hasDiverged.get();
    }

    public void cancel(ActionEvent ae) {
        entryModel.resetChanges();
        showCustomProps();
    }

    public void save(ActionEvent actionEvent) {
        entryModel.save();
        showCustomProps();
    }

    public void delete(ActionEvent actionEvent) {
        Alert deleteWarning = new Alert(Alert.AlertType.WARNING);
        deleteWarning.setContentText("Delete this Entry?");

        deleteWarning.showAndWait().ifPresent(decision -> {
            if (decision.getButtonData().isDefaultButton()) {
                entryModel.delete();
                fireEvent(new EntryFinishedEvent(EntryFinishedEvent.DELETED));
            }
        });
    }

    public void newField(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setContentText("Enter field name");
        dialog.showAndWait().ifPresent(fieldName -> {
            if (Entry.STANDARD_PROPERTY_NAMES.contains(fieldName) || entryModel.getCustomProperties().containsKey(fieldName)) {
                new Alert(Alert.AlertType.ERROR, "Field name already exists").showAndWait();
                return;
            }
            SimpleStringProperty value = new SimpleStringProperty("");
            entryModel.getCustomProperties().put(fieldName, value);
        });
    }

    /**
     * clear any existing custom fields, and redisplay them
     */
    private void showCustomProps() {
        fieldsPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) >= numDefaultFields);
        entryModel.getCustomProperties().forEach(this::addField);
    }

    /**
     * Inserts a new field w/ label, returning the textfield
     */
    private CustomField addField(String fieldName, StringProperty fieldValueBinding) {
        int rowIndex = fieldsPane.getRowCount();

        Label key = new Label(fieldName);
        GridPane.setConstraints(key, 0, rowIndex);

        CustomField cf = new CustomField();
        cf.textProperty().bindBidirectional(fieldValueBinding);
        cf.setOnRemovePressed(click -> {
            entryModel.getCustomProperties().remove(fieldName);
            fieldsPane.getChildren().removeIf(node -> GridPane.getRowIndex(node) == rowIndex);
        });

        GridPane.setConstraints(cf, 1, rowIndex);

        fieldsPane.getChildren().addAll(key, cf);

        return cf;
    }
}
