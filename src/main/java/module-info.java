module us.nagro.august.caseapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.prefs;
    requires java.logging;

    // todo remove this
    requires java.desktop;

    requires org.linguafranca.pwdb.database;
    requires org.linguafranca.pwdb.kdbx.dom;
    requires org.linguafranca.pwdb.kdbx;
    // todo pull request adding automatic module name
    requires jnativehook;

    opens us.nagro.august.caseapp.elements to javafx.fxml;
    opens us.nagro.august.caseapp to javafx.graphics;
}
