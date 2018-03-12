package us.nagro.august.caseapp.utils;

import javafx.collections.transformation.FilteredList;
import us.nagro.august.caseapp.models.EntryModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchEngine {

    private static final Matcher WHITESPACE_REGEX = Pattern.compile("\\s+").matcher("");

    /**
     * Applies a predicate to filteredEntries that attempts to find the EntryModels
     * applicable to searchString
     */
    public static void filter(String searchString, FilteredList<EntryModel> filteredEntries) {
        searchString = replaceWhitespace(searchString);

        Pattern searchRegex = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = searchRegex.matcher("");

        filteredEntries.setPredicate(entry -> {
            String title = replaceWhitespace(entry.titleProperty().get());
            String userName = replaceWhitespace(entry.usernameProperty().get());
            String website = replaceWhitespace(entry.websiteProperty().get());
            return matcher.reset(title).find()
                    || matcher.reset(userName).find()
                    || matcher.reset(website).find();
        });

    }

    private static String replaceWhitespace(String s) {
        return WHITESPACE_REGEX.reset(s).replaceAll("");
    }
}
