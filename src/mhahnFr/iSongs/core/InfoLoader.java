package mhahnFr.iSongs.core;

import mhahnFr.utils.Pair;
import mhahnFr.utils.StringStream;
import mhahnFr.utils.json.JSONParser;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class InfoLoader {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private final WebPlayerDTO dto = new WebPlayerDTO();
    private final Runnable trackUpdater;
    private final WriteCallback writeCallback;
    private volatile Pair<String, String> currentSong;
    private ScheduledFuture<?> updateFuture;

    public InfoLoader(final Runnable      trackUpdater,
                      final WriteCallback writeCallback) {
        this.trackUpdater  = trackUpdater;
        this.writeCallback = writeCallback;
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
        return currentSong;
    }

    private WebPlayerDTO.PlaylistDTO.SongDTO getPlayedSong(final WebPlayerDTO dto) {
        for (final var song : dto.playlist.data) {
            if (song.playingMode == 1) {
                return song;
            }
        }
        return null;
    }

    private URL createUrl() {
        try {
            return new URL(Settings.getInstance().getURL());
        } catch (Exception __) {
            return null;
        }
    }

    private void updateTrack() {
        final var url = createUrl();
        if (url == null) {
            currentSong = new Pair<>("Einstellungen überprüfen!", "");
            trackUpdater.run();
            return;
        }

        try (final var reader = new BufferedInputStream(url.openStream())) {
            new JSONParser(new StringStream(new String(reader.readAllBytes(), StandardCharsets.UTF_8))).readInto(dto);
        } catch (Exception e) {
            currentSong = new Pair<>(e.getLocalizedMessage(), "");
            e.printStackTrace();
            trackUpdater.run();
            return;
        }
        final var playedSong = getPlayedSong(dto);

        final boolean update;
        if (playedSong == null) {
            update = currentSong == null;
            currentSong = null;
        } else if (currentSong == null                              ||
                   !currentSong.getFirst().equals(playedSong.title) ||
                   !currentSong.getSecond().equals(playedSong.artist)) {
            currentSong = new Pair<>(playedSong.title, playedSong.artist);
            update = true;
        } else {
            update = false;
        }

        if (update) {
            trackUpdater.run();
        }
    }

    public void saveSong() {
        executorService.schedule(this::saveSongImpl, 0, TimeUnit.NANOSECONDS);
    }

    private void saveSongImpl() {
        Pair<String, String> song = null;
        Exception e               = null;
        try {
            song = saveTrack();
        } catch (Exception exception) {
            e = exception;
        }
        writeCallback.songWritten(song, e);
    }

    private Pair<String, String> saveTrack() throws IOException {
        if (!hasTrack()) {
            throw new IllegalStateException("No track recognized!");
        }
        final var path = Settings.getInstance().getSavePath();
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Save folder not set!");
        }
        final var song = getCurrentSong();
        final var buffer = "titel:" + song.getFirst() + System.lineSeparator() +
                           "interpreter:" + song.getSecond();
        try (final var writer = new BufferedWriter(new FileWriter(createFileName()))) {
            writer.write(buffer);
        }
        return song;
    }

    public boolean hasTrack() {
        return currentSong != null;
    }

    private String createFileName() {
        return Settings.getInstance().getSavePath() + File.separator + "Song_" +
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault())
                          .format(new Date());
    }
}
