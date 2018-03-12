package us.nagro.august.caseapp.utils;

import us.nagro.august.caseapp.prefs.UserPreferences;

import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class PasswordGenerator {

    public enum PasswordOptions {
        ALPHABET_LOWER_CASE("abcdefghijklmnopqrstuvwxyz"),
        ALPHABET_UPPER_CASE(ALPHABET_LOWER_CASE.chars.toUpperCase()),
        DIGITS("0123456789"),
        PUNCTUATION("!@#$%^&*-_=+\\|~,./?"),
        BRACKETS("()[]{}<>"),
        ALL(ALPHABET_LOWER_CASE.chars + ALPHABET_UPPER_CASE.chars + DIGITS.chars + PUNCTUATION.chars + BRACKETS.chars);

        final String chars;

        PasswordOptions(String chars) {
            this.chars = chars;
        }
    }

    private static final Path NOUNS_PATH =
            Paths.get(PasswordGenerator.class.getResource("/password-gen/common-nouns.dat").toExternalForm());

    private static final int DEFAULT_PASSWORD_LENGTH = 16;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static String[] NOUNS;

    public static String generate() {
        return generate(null);
    }

    public static String generate(EnumSet<PasswordOptions> passOptions) {
        StringBuilder pass = new StringBuilder();

        if (passOptions == null && UserPreferences.getPasswordGenHumanReadable()) {
            if (NOUNS == null) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(NOUNS_PATH))) {
                    NOUNS = (String[]) ois.readObject();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            while (pass.length() < DEFAULT_PASSWORD_LENGTH) {
                pass.append(NOUNS[RANDOM.nextInt(NOUNS.length)]);
            }
            return pass.toString();
        }

        String passChars;
        if (passOptions == null || passOptions.size() == 0) {
            passChars = PasswordOptions.ALL.chars;
        } else {
            passChars = passOptions.stream().map(passOption -> passOption.chars).collect(Collectors.joining());
        }

        while (pass.length() < DEFAULT_PASSWORD_LENGTH) {
            char passChar = passChars.charAt(RANDOM.nextInt(passChars.length()));
            pass.append(passChar);
        }

        return pass.toString();
    }

}
