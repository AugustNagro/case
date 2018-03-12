package us.nagro.august.caseapp.prefs;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class RecentKeyFiles {

    private static final Logger LOG = Logger.getLogger(RecentKeyFiles.class.getName());

    private static final Preferences PREFS = Preferences.userNodeForPackage(RecentKeyFiles.class);
    private static final String OPENED_KDBX_FILES = "openedKDBXFiles";

    /**
     * list of previously opened keyFiles, sorted by lastModification date
     */
    public static List<Path> getRecentKeyFiles() {
        ArrayList<Path> keyFiles = new ArrayList<>();

        byte[] serialized = PREFS.getByteArray(OPENED_KDBX_FILES, null);
        if (serialized == null) return keyFiles;

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized))) {
            String[] pathStrings = (String[]) ois.readObject();
            for (String ps : pathStrings) {
                Path p = Paths.get(ps);
                if (Files.exists(p)) keyFiles.add(p);
            }
        } catch (IOException | ClassNotFoundException e) {
            LOG.log(Level.WARNING, "malformed preferences for " + OPENED_KDBX_FILES, e);
            PREFS.remove(OPENED_KDBX_FILES);
        }

        keyFiles.sort((a, b) -> {
            try {
                return Files.getLastModifiedTime(a).compareTo(Files.getLastModifiedTime(b));
            } catch (IOException e) {
                LOG.log(Level.WARNING, "could not get lastModifiedTime", e);
                return 0;
            }
        });

        return keyFiles;
    }

    public static void setRecentKeyFiles(List<Path> keyFiles) {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            String[] serializable = keyFiles.stream().map(Path::toString).toArray(String[]::new);
            oos.writeObject(serializable);
            PREFS.putByteArray(OPENED_KDBX_FILES, bos.toByteArray());
        } catch (IOException e) {
            LOG.log(Level.WARNING, "could not serialize keyfiles array");
        }
    }
}
