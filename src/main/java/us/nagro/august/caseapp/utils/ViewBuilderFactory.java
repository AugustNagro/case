package us.nagro.august.caseapp.utils;

import javafx.stage.Screen;
import us.nagro.august.caseapp.elements.*;
import us.nagro.august.caseapp.models.KdbxContext;

/**
 * Returns ViewBuilders that may return different views
 * depending on the devices's screen size, os, etc.
 */
public class ViewBuilderFactory {

    public static final boolean IS_MOBILE = Screen.getPrimary().getBounds().getWidth() < 400;

    public interface ViewBuilder {
        HomePane home(KdbxContext ctx);
        LoginPane login();
        EntryPane entry();
        AutoTypePasswordPicker autoTypePicker(KdbxContext ctx);
        PreferencesPane preferences(KdbxContext context);
    }

    private static abstract class AbstractViewBuilder implements ViewBuilder {
        public abstract HomePane home(KdbxContext ctx);

        @Override
        public LoginPane login() {
            return new LoginPane();
        }

        @Override
        public EntryPane entry() {
            return new EntryPane();
        }

        @Override
        public AutoTypePasswordPicker autoTypePicker(KdbxContext ctx) {
            return new AutoTypePasswordPicker(ctx);
        }

        @Override
        public PreferencesPane preferences(KdbxContext context) {
            return new PreferencesPane(context);
        }
    }

    public static class DesktopViewBuilder extends AbstractViewBuilder {
        @Override
        public HomePane home(KdbxContext ctx) {
            return new HomePaneDesktop(ctx);
        }
    }

    public static class MobileViewBuilder extends AbstractViewBuilder {
        // todo
        @Override
        public HomePane home(KdbxContext ctx) {
            return null;
        }
    }

    public static ViewBuilder getViewBuilder() {
        if (IS_MOBILE) return new MobileViewBuilder();
        else return new DesktopViewBuilder();
    }
}
