package us.nagro.august.caseapp.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Visitor;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KdbxContext {
    private static final Logger LOG = Logger.getLogger(KdbxContext.class.getName());

    public final DomDatabaseWrapper db;
    public final KdbxCreds          credentials;
    public final Path               path;
    public final ObservableList<EntryModel> entries = FXCollections.observableArrayList();

    public KdbxContext(DomDatabaseWrapper db, KdbxCreds credentials, Path path) {
        this.db = db;
        this.credentials = credentials;
        this.path = path;

        KdbxContext thisContext = this;

        db.visit(new Visitor() {
            @Override
            public void startVisit(Group group) {

            }

            @Override
            public void endVisit(Group group) {

            }

            @Override
            public void visit(Entry entry) {
                entries.add(new EntryModel((DomEntryWrapper) entry, thisContext));
            }

            @Override
            public boolean isEntriesFirst() {
                return false;
            }
        });
    }

    /**
     * Saves db to original file. If saving an EntryModel, call EntryModel::save
     * instead.
     */
    public void save() {
        try {
            // todo wrap io operations in Services?
            db.save(credentials, Files.newOutputStream(path));
        } catch (IOException e) {
            String msg = "Could not save to: " + path;
            LOG.log(Level.SEVERE, msg, e);
            new Alert(Alert.AlertType.ERROR, msg).showAndWait();
        }
    }
}
