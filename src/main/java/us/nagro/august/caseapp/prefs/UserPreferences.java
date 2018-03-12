package us.nagro.august.caseapp.prefs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class UserPreferences {
    private static final Logger LOG = Logger.getLogger(UserPreferences.class.getName());

    private static Preferences PREFS = Preferences.userNodeForPackage(UserPreferences.class);

    private static final String SHOW_ENTRIES_FROM_ALL_SUBGROUPS = "showEntriesFromAllSubgroups";
    private static final String FIRST_TIME_OPENED               = "firstTimeOpened";
    private static final String AUTOTYPE_ENABLED                = "autotypeEnabled";
    private static final String PASSWORD_GEN_HUMAN_READABLE     = "passwordGenHumanReadable";
    private static final String AUTOLOCKOUT_ENABLED             = "autoLockoutEnabled";
    private static final String AUTOLOCKOUT_MINUTES             = "autoLockoutMinutes";
    private static final String CUSTOM_THEME_ENABLED            = "customThemeEnabled";
    private static final String CUSTOM_THEME                    = "customTheme";

    private static final String BASE_STYLE = UserPreferences.class
            .getResource("/css/base.css").toExternalForm();

    public static List<String> getStyles() {
        ArrayList<String> styles = new ArrayList<>();
        styles.add(BASE_STYLE);

        if (getIsCustomThemeEnabled()) {
            String customTheme = PREFS.get(CUSTOM_THEME, null);
            if (customTheme != null) styles.add(customTheme);
        }

        return styles;
    }

    public static void setCustomTheme(String customTheme) {
        PREFS.put(CUSTOM_THEME, customTheme);
    }

    public static String getCustomTheme() {
        return PREFS.get(CUSTOM_THEME, null);
    }

    public static boolean getIsCustomThemeEnabled() {
        return PREFS.getBoolean(CUSTOM_THEME_ENABLED, false);
    }

    public static void setIsCustomThemeEnabled(boolean customThemeEnabled) {
        PREFS.putBoolean(CUSTOM_THEME_ENABLED, customThemeEnabled);
    }

    public static boolean getIsAutoTypeEnabled() {
        return PREFS.getBoolean(AUTOLOCKOUT_ENABLED, true);
    }

    public static void setIsAutoTypeEnabled(boolean isAutoTypeEnabled) {
        PREFS.putBoolean(AUTOTYPE_ENABLED, isAutoTypeEnabled);
    }

    public static boolean getIsFirstTimeOpened() {
        return PREFS.getBoolean(FIRST_TIME_OPENED, true);
    }

    public static void setIsFirstTimeOpened(boolean isFirstTimeOpened) {
        PREFS.putBoolean(FIRST_TIME_OPENED, isFirstTimeOpened);
    }

    public static boolean getPasswordGenHumanReadable() {
        return PREFS.getBoolean(PASSWORD_GEN_HUMAN_READABLE, true);
    }

    public static void setPasswordGenHumanReadable(boolean humanReadable) {
        PREFS.putBoolean(PASSWORD_GEN_HUMAN_READABLE, humanReadable);
    }

    public static boolean getShowEntriesFromAllSubgroups() {
        return PREFS.getBoolean(SHOW_ENTRIES_FROM_ALL_SUBGROUPS, true);
    }

    public static void setShowEntriesFromAllSubgroups(boolean showEntriesFromAllSubgroups) {
        PREFS.putBoolean(SHOW_ENTRIES_FROM_ALL_SUBGROUPS, showEntriesFromAllSubgroups);
    }


    public static boolean getisAutoLockoutEnabled() {
        return PREFS.getBoolean(AUTOLOCKOUT_ENABLED, true);
    }

    public static int getAutoLockoutMinutes() {
        return PREFS.getInt(AUTOLOCKOUT_MINUTES, 10);
    }

    public static void setAutolockoutMinutes(int autolockoutMinutes) {
        PREFS.putInt(AUTOLOCKOUT_MINUTES, autolockoutMinutes);
    }

    public static void clearPrefs() {
        try {
            PREFS.clear();
        } catch (BackingStoreException e) {
            LOG.log(Level.WARNING, "could not clear preferences", e);
        }
    }
}
