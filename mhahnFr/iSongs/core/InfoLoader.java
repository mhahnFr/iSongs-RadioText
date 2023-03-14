package iSongs.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class InfoLoader {
    public void saveTrack() throws IOException {
        if (!hasTrack()) {
            throw new IllegalStateException("No track recognized!");
        }
        final var buffer = "titel:" + "<title>" + System.lineSeparator() +
                           "interpreter:" + "<interpreter>";
        try (final var writer = new BufferedWriter(new FileWriter(createFileName()))) {
            writer.write(buffer);
        }
    }

    public boolean hasTrack() {
        return false;
    }

    private String createFileName() {
        return "TODO" + File.separator + "Song_" +
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault())
                          .format(new Date());
    }
}
