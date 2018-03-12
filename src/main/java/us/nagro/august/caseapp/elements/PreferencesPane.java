package us.nagro.august.caseapp.elements;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;
import us.nagro.august.caseapp.models.KdbxContext;
import us.nagro.august.caseapp.models.RoutingEvent;
import us.nagro.august.caseapp.prefs.UserPreferences;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreferencesPane extends BorderPane {

    private static final Logger LOG = Logger.getLogger(PreferencesPane.class.getName());

    @FXML private CheckBox  autoTypeCB;
    @FXML private CheckBox  humanPassCB;
    @FXML private CheckBox  autoLockoutCB;
    @FXML private TextField autoLockoutMinTF;
    @FXML private CheckBox  customThemeCB;
    @FXML private Button    customThemeBtn;

    @FXML private Button goHomeBtn;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    private BooleanProperty autoTypeEnabled    = new SimpleBooleanProperty();
    private BooleanProperty humanPassEnabled   = new SimpleBooleanProperty();
    private BooleanProperty autoLockoutEnabled = new SimpleBooleanProperty();
    private IntegerProperty autoLockoutMin     = new SimpleIntegerProperty();
    private BooleanProperty customThemeEnabled = new SimpleBooleanProperty();
    private StringProperty  customTheme        = new SimpleStringProperty();

    private BooleanProperty hasDiverged = new SimpleBooleanProperty();

    private KdbxContext context;

    public PreferencesPane(KdbxContext context) {
        this.context = context;

        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/views/preferences.fxml"));
        fxml.setRoot(this);
        fxml.setControllerFactory(cls -> this);
        try {
            fxml.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initializeProps();

        autoTypeCB.selectedProperty().bindBidirectional(autoTypeEnabled);
        humanPassCB.selectedProperty().bindBidirectional(humanPassEnabled);
        autoLockoutCB.selectedProperty().bindBidirectional(autoLockoutEnabled);

        TextFormatter<Integer> integerFormatter = new TextFormatter<>(
                new IntegerStringConverter(),
                0,
                change -> {
                    try {
                        Integer.parseInt(change.getControlNewText());
                    } catch (NumberFormatException e) {
                        // ignore the invalid change
                        change.setText("");
                    }
                    return change;
                }
        );
        autoLockoutMinTF.setTextFormatter(integerFormatter);
        autoLockoutMinTF.textProperty().bindBidirectional(autoLockoutMin, new NumberStringConverter());
        autoLockoutMinTF.disableProperty().bind(autoLockoutEnabled.not());

        customThemeCB.selectedProperty().bindBidirectional(customThemeEnabled);
        customThemeBtn.disableProperty().bind(customThemeEnabled.not());

        goHomeBtn.disableProperty().bind(hasDiverged);
        cancelBtn.disableProperty().bind(hasDiverged.not());
        saveBtn.disableProperty().bind(hasDiverged.not());
    }

    public void goHome(ActionEvent ae) {
        fireEvent(new RoutingEvent(RoutingEvent.HOME, context));
    }

    public void resetToDefault(ActionEvent ae) {
        UserPreferences.clearPrefs();
        initializeProps();
    }

    public void cancel(ActionEvent ae) {
        initializeProps();
    }

    public void save(ActionEvent ae) {
        UserPreferences.setIsAutoTypeEnabled(autoTypeEnabled.get());
        UserPreferences.setPasswordGenHumanReadable(humanPassEnabled.get());
        UserPreferences.setIsAutoTypeEnabled(autoTypeEnabled.get());
        UserPreferences.setAutolockoutMinutes(autoLockoutMin.get());

        UserPreferences.setIsCustomThemeEnabled(customThemeEnabled.get());
        UserPreferences.setCustomTheme(customTheme.get());

        initializeProps();

        // Set the theme
        getScene().getStylesheets().setAll(UserPreferences.getStyles());
    }

    public void newTheme(ActionEvent ae) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a new Theme");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSS Stylesheet", "*.css"));
        File theme = fc.showOpenDialog(getScene().getWindow());
        if (theme == null) return;

        try {
            URL themeURL = theme.toURI().toURL();
            customTheme.set(themeURL.toExternalForm());
        } catch (MalformedURLException e) {
            LOG.log(Level.SEVERE, "malformed theme URL", e);
        }
    }

    private void initializeProps() {
        autoTypeEnabled.set(UserPreferences.getIsAutoTypeEnabled());
        humanPassEnabled.set(UserPreferences.getPasswordGenHumanReadable());
        autoLockoutEnabled.set(UserPreferences.getisAutoLockoutEnabled());
        autoLockoutMin.set(UserPreferences.getAutoLockoutMinutes());
        customThemeEnabled.set(UserPreferences.getIsCustomThemeEnabled());
        customTheme.set(UserPreferences.getCustomTheme());

        hasDiverged.bind(autoTypeEnabled.asObject().isNotEqualTo(autoTypeEnabled.get())
                .or(humanPassEnabled.asObject().isNotEqualTo(humanPassEnabled.get()))
                .or(autoLockoutEnabled.asObject().isNotEqualTo(autoLockoutEnabled.get()))
                .or(autoLockoutMin.isNotEqualTo(autoLockoutMin.get()))
                .or(customThemeEnabled.asObject().isNotEqualTo(customThemeEnabled.get()))
                .or(customTheme.isNotEqualTo(customTheme.get()))
        );
    }
}
