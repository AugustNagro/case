package us.nagro.august.caseapp.elements;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import us.nagro.august.caseapp.elements.NewKeyStoreDialog.NewKeyStoreDialogResult;
import us.nagro.august.caseapp.models.KdbxContext;
import us.nagro.august.caseapp.models.RoutingEvent;
import us.nagro.august.caseapp.prefs.RecentKeyFiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class LoginPane extends BorderPane {

    @FXML private Button        submitPass;
    @FXML private VBox          openedFilesVbox;
    @FXML private PasswordField passField;

    private Path selectedKeyFile;
    private List<Path> keyFiles = RecentKeyFiles.getRecentKeyFiles();

    public LoginPane() {
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        fxml.setRoot(this);
        fxml.setControllerFactory(cls -> this);
        try {
            fxml.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        submitPass.disableProperty().bind(passField.textProperty().isEmpty());

        // build list of recently opened keystores
        if (keyFiles.size() == 0) return;
        selectKeyFile(keyFiles.get(0));

        Text recentlyOpened = new Text("Recently Opened:");
        openedFilesVbox.getChildren().add(recentlyOpened);

        for (Path p : keyFiles) {
            HBox btnContainer = new HBox();
            btnContainer.setAlignment(Pos.CENTER);
            btnContainer.getStyleClass().add("padding-top");

            Button kbxFile = new Button(p.toString());
            kbxFile.getStyleClass().add("combined-button");
            kbxFile.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
            HBox.setHgrow(kbxFile, Priority.ALWAYS);
            kbxFile.setOnAction(ae -> selectKeyFile(p));

            Button remove = new Button("x");
            remove.getStyleClass().add("combined-button");
            remove.setOnAction(ae -> {
                if (selectedKeyFile == p) {
                    selectedKeyFile = null;
                    passField.setDisable(true);
                    passField.setPromptText("Please select a keystore");
                }
                keyFiles.remove(p);
                RecentKeyFiles.setRecentKeyFiles(keyFiles);
                openedFilesVbox.getChildren().remove(btnContainer);
            });

            btnContainer.getChildren().addAll(kbxFile, remove);
            openedFilesVbox.getChildren().add(btnContainer);
        }
    }

    public void unlock(ActionEvent ae) throws IOException {
        KdbxCreds creds = new KdbxCreds(passField.getText().getBytes());
        DomDatabaseWrapper db;
        try {
            db = DomDatabaseWrapper.load(creds, Files.newInputStream(selectedKeyFile));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Wrong Password").showAndWait();
            return;
        }

        fireEvent(new RoutingEvent(RoutingEvent.HOME, new KdbxContext(db, creds, selectedKeyFile)));
    }

    public void openKeyFile(ActionEvent ae) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select a KeyPass Archive");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("KeePass Archive (*.kdbx)", "*.kdbx"));
        File keyFile = fc.showOpenDialog(openedFilesVbox.getScene().getWindow());
        if (keyFile == null) return;
        Path keyPath = keyFile.toPath();
        keyFiles.add(keyPath);
        selectKeyFile(keyPath);
        RecentKeyFiles.setRecentKeyFiles(keyFiles);
    }

    public void createKeyFile(ActionEvent actionEvent) throws IOException {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select location of new KeyStore");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("KeePass Archive (*.kdbx)", "*.kdbx"));
        File keyFileLocation = fc.showSaveDialog(openedFilesVbox.getScene().getWindow());
        if (keyFileLocation == null) return;
        Path keyFilePath = keyFileLocation.toPath();

        String defaultName = keyFileLocation.getName().substring(0, keyFileLocation.getName().indexOf('.'));
        Optional<NewKeyStoreDialogResult> resOption = new NewKeyStoreDialog(defaultName).showAndWait();
        if (resOption.isPresent() && resOption.get() != null) {
            DomDatabaseWrapper newDb = new DomDatabaseWrapper();
            NewKeyStoreDialogResult res = resOption.get();
            newDb.setName(res.name);
            newDb.setDescription(res.description);

            KdbxCreds creds = new KdbxCreds(res.password.getBytes());
            newDb.save(creds, Files.newOutputStream(keyFilePath));

            keyFiles.add(keyFilePath);
            RecentKeyFiles.setRecentKeyFiles(keyFiles);

            fireEvent(new RoutingEvent(RoutingEvent.HOME, new KdbxContext(newDb, creds, keyFilePath)));
        }
    }

    private void selectKeyFile(Path p) {
        selectedKeyFile = p;
        passField.setPromptText(p.getFileName().toString());
        passField.setDisable(false);
        passField.requestFocus();
    }
}
