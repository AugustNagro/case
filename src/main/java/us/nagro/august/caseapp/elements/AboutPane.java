package us.nagro.august.caseapp.elements;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AboutPane extends BorderPane {

    public AboutPane() {
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/views/about.fxml"));
        fxml.setRoot(this);
        fxml.setControllerFactory(cls -> this);
        try {
            fxml.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void emailMe(ActionEvent ae) throws URISyntaxException, IOException {
        Desktop.getDesktop().mail(new URI("mailto:augustnagro@gmail.com?subject=[Case]"));
    }

    public void browseKeyPassJava2(ActionEvent ae) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://github.com/jorabin/KeePassJava2"));
    }

    public void browseJavaFX(ActionEvent ae) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://docs.oracle.com/javase/8/javase-clienttechnologies.htm"));
    }

    public void browseJfxMobile(ActionEvent ae) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://github.com/javafxports/javafxmobile-plugin"));
    }
}
