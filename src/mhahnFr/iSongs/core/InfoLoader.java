/*
 * iSongs-RadioText - Radio-text part of iSongs.
 *
 * Copyright (C) 2023 - 2024  mhahnFr
 *
 * This file is part of the iSongs-RadioText. This program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program, see the file LICENSE.  If not, see <https://www.gnu.org/licenses/>.
 */

package mhahnFr.iSongs.core;

import mhahnFr.iSongs.core.appleScript.InfoLoaderAppleScript;
import mhahnFr.iSongs.core.appleScript.Script;
import mhahnFr.iSongs.core.appleScript.ScriptSupport;
import mhahnFr.iSongs.core.locale.StringID;
import mhahnFr.utils.StringStream;
import mhahnFr.utils.json.JSONParser;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * This class contains the song managing functions of this
 * application.
 *
 * @author mhahnFr
 * @since 14.03.23
 */
public class InfoLoader {
    /** The {@link ExecutorService} used for the multithreading.   */
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    /** The data transfer object.                                                       */
    private final WebPlayerDTO dto = new WebPlayerDTO();
    /** The lock used for the {@link #currentSong}.                                     */
    private final Object currentSongLock = new Object();
    /** The callback called when a new song is recognized.                              */
    private final Runnable trackUpdater;
    /** The callback called when a song has been written.                               */
    private final WriteCallback writeCallback;
    private InfoLoaderAppleScript scriptLoader;
    /** The currently recognized song.                                                  */
    private Song currentSong;
    /** The {@link Future} used to control the song fetching task. */
    private ScheduledFuture<?> updateFuture;
    private ScriptSupport support;

    /**
     * Initializes this {@link InfoLoader}.
     *
     * @param trackUpdater  the callback called when a new song is recognized
     * @param writeCallback the callback called when a song has been written
     */
    public InfoLoader(final Runnable      trackUpdater,
                      final WriteCallback writeCallback) {
        this.trackUpdater  = trackUpdater;
        this.writeCallback = writeCallback;
    }

    public void setAppleScriptEnabled(final boolean enabled) {
        if (enabled) {
            if (scriptLoader == null) {
                try (final var stream = Script.class.getClassLoader().getResourceAsStream("streamTitle.applescript")) {
                    scriptLoader = new InfoLoaderAppleScript(Script.loadScript(stream));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            scriptLoader = null;
        }
    }

    /**
     * Starts the song fetching task.
     *
     * @see #stop()
     */
    public void start() {
        setScriptSupport(Settings.getInstance().getScriptSupport());
        updateFuture = executorService.scheduleAtFixedRate(this::updateTrack,
                0,
                Settings.getInstance().getDelay(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the song fetching task.
     *
     * @see #start()
     */
    public void stop() {
        if (updateFuture != null) {
            updateFuture.cancel(false);
        }
    }

    /**
     * Returns the currently recognized song. If no song has
     * been recognized, {@code null} is returned.
     *
     * @return the currently recognized song
     * @see #hasTrack()
     * @see #setCurrentSong(Song)
     */
    public Song getCurrentSong() {
        synchronized (currentSongLock) { return currentSong; }
    }

    private void setScriptSupport(final ScriptSupport support) {
        setAppleScriptEnabled(support != ScriptSupport.off);
        this.support = support;
    }

    /**
     * Sets the currently played song.
     *
     * @param newSong the new song to be stored
     * @see #getCurrentSong()
     */
    private void setCurrentSong(final Song newSong) {
        synchronized (currentSongLock) {
            currentSong = newSong;
        }
    }

    /**
     * Extracts and returns the currently played song from the
     * given data transfer object. If no song is being played
     * currently, {@code null} is returned.
     *
     * @param dto the data transfer object
     * @return the played song
     */
    private WebPlayerDTO.PlaylistDTO.SongDTO getPlayedSong(final WebPlayerDTO dto) {
        for (final var song : dto.playlist.data) {
            if (song.playingMode == 1) {
                return song;
            }
        }
        return null;
    }

    /**
     * Constructs the URL used to fetch the song information.
     * If the URL could not be constructed, {@code null} is
     * returned.
     *
     * @return the URL
     */
    private URL createUrl() {
        try {
            return new URI(Settings.getInstance().getURL()).toURL();
        } catch (Exception __) {
            return null;
        }
    }

    private void updateTrack() {
        final var url = createUrl();
        if (url == null) {
            setCurrentSong(new Pair<>(Settings.getInstance().getLocale().get(StringID.MAIN_UI_CHECK_SETTINGS), ""));
            trackUpdater.run();
            return;
        }

        try (final var reader = new BufferedInputStream(url.openStream())) {
            new JSONParser(new StringStream(new String(reader.readAllBytes(), StandardCharsets.UTF_8))).readInto(dto);
        } catch (Exception e) {
            setCurrentSong(new Pair<>(e.getLocalizedMessage(), ""));
            e.printStackTrace();
            trackUpdater.run();
            return;
        }
        final var playedSong = getPlayedSong(dto);

        final var currentSong = getCurrentSong();
        final boolean update;
        if (playedSong == null) {
            update = currentSong != null;
            setCurrentSong(null);
        } else if (currentSong == null                              ||
                   !currentSong.getFirst().equals(playedSong.title) ||
                   !currentSong.getSecond().equals(playedSong.artist)) {
            setCurrentSong(new Pair<>(playedSong.title, playedSong.artist));
            update = true;
        } else {
            update = false;
        }

        if (update) {
            trackUpdater.run();
        }
    }

    /**
     * Starts the task to save the song information of the
     * currently recognized song.
     *
     * @see #saveSongImpl(Song)
     */
    public void saveSong() {
        final var currentSong = getCurrentSong();
        executorService.schedule(() -> saveSongImpl(currentSong), 0, TimeUnit.NANOSECONDS);
    }

    /**
     * Saves the currently recognized song. After finishing,
     * the callback is invoked.
     *
     * @param song the song to be saved
     * @see #writeCallback
     */
    private void saveSongImpl(final Song song) {
        Song      savedSong = null;
        Exception e         = null;
        try {
            savedSong = saveTrack(song);
        } catch (Exception exception) {
            e = exception;
        }
        writeCallback.songWritten(savedSong, e);
    }

    /**
     * Writes the currently recognized song to a file. The
     * file is placed into the folder returned by {@link Settings#getSavePath()}.
     *
     * @param song the song to be saved
     * @return the saved song
     * @throws IOException if the file could not be written
     * @throws IllegalStateException if the file cannot be written
     */
    private Song saveTrack(final Song song) throws IOException {
        if (!hasTrack()) {
            throw new IllegalStateException("No track recognized!");
        }
        final var path = Settings.getInstance().getSavePath();
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Save folder not set!");
        }
        final var buffer = "titel:" + song.title() + System.lineSeparator() +
                           "interpreter:" + song.interpreter();
        try (final var writer = new BufferedWriter(new FileWriter(createFileName()))) {
            writer.write(buffer);
        }
        return song;
    }

    /**
     * Returns whether currently a song has been recognized.
     *
     * @return whether a song is recognized
     * @see #getCurrentSong()
     */
    public boolean hasTrack() {
        return getCurrentSong() != null;
    }

    /**
     * Creates a file name for storing a song.
     *
     * @return the file path
     */
    private String createFileName() {
        return Settings.getInstance().getSavePath() + File.separator + "Song_" +
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault())
                          .format(new Date());
    }
}
