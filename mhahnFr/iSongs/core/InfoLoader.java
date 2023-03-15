package iSongs.core;

import mhahnFr.utils.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class InfoLoader {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private final Runnable trackUpdater;
    private ScheduledFuture<?> updateFuture;

    public InfoLoader(final Runnable trackUpdater) {
        this.trackUpdater = trackUpdater;
    }

    public void start() {
        updateFuture = executorService.scheduleAtFixedRate(this::updateTrack,
                0,
                Settings.getInstance().getDelay(),
                TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (updateFuture != null) {
            updateFuture.cancel(false);
        }
    }

    public Pair<String, String> getCurrentSong() {
        return new Pair<>("<title>", "<interpreter>");
    }

    private void updateTrack() {
        // TODO
    }

    public void saveTrack() throws IOException {
        if (!hasTrack()) {
            throw new IllegalStateException("No track recognized!");
        }
        final var path = Settings.getInstance().getSavePath();
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Save folder not set!");
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
        return Settings.getInstance().getSavePath() + File.separator + "Song_" +
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault())
                          .format(new Date());
    }
}
