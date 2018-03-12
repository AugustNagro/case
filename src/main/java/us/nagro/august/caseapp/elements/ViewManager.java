package us.nagro.august.caseapp.elements;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import us.nagro.august.caseapp.models.KdbxContext;
import us.nagro.august.caseapp.models.RoutingEvent;
import us.nagro.august.caseapp.prefs.UserPreferences;
import us.nagro.august.caseapp.utils.ViewBuilderFactory;
import us.nagro.august.caseapp.utils.ViewBuilderFactory.ViewBuilder;
import us.nagro.august.caseapp.utils.autotype.AutoType;

/**
 * Primary purpose is to handle events, like login and logout
 */
public class ViewManager extends StackPane {

    private boolean isLoggedIn = false;
    private KdbxContext context;
    private Stage autoTypePassPickStage;

    // the global key listener, activated on Ctrl+G.
    private NativeKeyListener nkl = new NativeKeyListener() {
        @Override
        public void nativeKeyTyped(NativeKeyEvent key) {

        }

        @Override
        public void nativeKeyPressed(NativeKeyEvent key) {
            if (key.getKeyCode() == NativeKeyEvent.VC_G && (key.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) {
                if (autoTypePassPickStage == null) autoTypePassPickStage = new Stage(StageStyle.UTILITY);
                ViewBuilder vb = ViewBuilderFactory.getViewBuilder();

                Parent view = isLoggedIn ? vb.autoTypePicker(context) : vb.login();
                view.prefWidth(300);
                view.prefHeight(200);

                Scene scene = new Scene(view);
                scene.getStylesheets().addAll(UserPreferences.getStyles());

                scene.addEventHandler(RoutingEvent.HOME, event -> {
                    context = event.context;
                    scene.setRoot(vb.autoTypePicker(context));
                    // when a user logs into on this scene's stage, also login on the main screen.
                    fireEvent(new RoutingEvent(RoutingEvent.HOME, event.context));
                });

                autoTypePassPickStage.setScene(scene);
                autoTypePassPickStage.setTitle("Select a password");
                autoTypePassPickStage.centerOnScreen();
                autoTypePassPickStage.showAndWait();
            }
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent key) {

        }
    };

    public ViewManager() {
        ViewBuilder vb = ViewBuilderFactory.getViewBuilder();

        // login pane is initial stage.
        getChildren().setAll(vb.login());

        addEventHandler(RoutingEvent.LOG_OUT, event -> {
            isLoggedIn = false;
            getChildren().setAll(vb.login());
        });

        addEventHandler(RoutingEvent.HOME, event -> {
            isLoggedIn = true;
            context = event.context;
            getChildren().setAll(vb.home(context));
        });

        addEventHandler(RoutingEvent.PREFERENCES, event -> {
            getChildren().setAll(vb.preferences(context));
        });

        if (UserPreferences.getIsFirstTimeOpened()) {
            UserPreferences.setIsFirstTimeOpened(false);
            // todo is multiple stages supported on mobile?
            Scene welcomeScene = new Scene(new WelcomePane());
            welcomeScene.getStylesheets().addAll(UserPreferences.getStyles());

            Stage stage = new Stage();
            stage.setTitle("Welcome to Case");
            stage.setScene(welcomeScene);
            stage.centerOnScreen();
            stage.showAndWait();
        }

        if (UserPreferences.getIsAutoTypeEnabled()) {
            AutoType.register(nkl);
        }
    }
}
