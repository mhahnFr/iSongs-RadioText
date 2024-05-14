/*
 * iSongs-RadioText - Radio-text part of iSongs.
 *
 * Copyright (C) 2023 - 2024  mhahnFr
 *
 * This file is part of the iSongs-RadioText.
 *
 * iSongs-RadioText is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * iSongs-RadioText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * iSongs-RadioText, see the file LICENSE.  If not, see <https://www.gnu.org/licenses/>.
 */

package mhahnFr.iSongs.core;

import mhahnFr.iSongs.core.appleScript.*;
import mhahnFr.iSongs.core.appleScript.ExecutionException;
import mhahnFr.iSongs.core.locale.StringID;
import mhahnFr.utils.Pair;
import mhahnFr.utils.StringStream;
import mhahnFr.utils.json.JSONParser;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * This class contains the song managing functions of this
 * application.
 *
 * @author mhahnFr
 * @since 14.03.23
 */
public class InfoLoader {
    /** The {@link ExecutorService} used for the multithreading.                        */
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    /** The data transfer object.                                                       */
    private final WebPlayerDTO dto = new WebPlayerDTO();
    /** The lock used for the {@link #currentSong}.                                     */
    private final Object currentSongLock = new Object();
    /** The callback called when a new song is recognized.                              */
    private final Runnable trackUpdater;
    /** The callback called when a song has been written.                               */
    private final Callback<Song> writeCallback;
    /** The callback called with the latest recognized radio text.                      */
    private final Callback<String> textUpdater;
    /** The callback to be called when an unrelated exception happens.                  */
    private final Callback<Exception> errorHandler;
    /** The AppleScript based info loader instance.                                     */
    private InfoLoaderAppleScript scriptLoader;
    /** The currently recognized song.                                                  */
    private Song currentSong;
    /** The {@link Future} used to control the song fetching task.                      */
    private ScheduledFuture<?> updateFuture;
    /** The current level of script support to be used.                                 */
    private ScriptSupport support;
    /** The previous song recognized.                                                   */
    private Song previous;
    /** The last song recognized using the JSON recognition.                            */
    private Song lastJson;
    /** The last song recognized by the AppleScript based loader.                       */
    private Song lastScript;
    private boolean allowNoSong;
    private boolean executionExceptionForwarded;
    private boolean jsonExceptionForwarded;
    private boolean uriExceptionForwarded;

    /**
     * Initializes this {@link InfoLoader}.
     *
     * @param trackUpdater  the callback called when a new song is recognized
     * @param writeCallback the callback called when a song has been written
     * @param textUpdater   the callback called when radio text is recognized
     * @param errorHandler  the callback called when an unrelated exception happened
     */
    public InfoLoader(final Runnable            trackUpdater,
                      final Callback<Song>      writeCallback,
                      final Callback<String>    textUpdater,
                      final Callback<Exception> errorHandler) {
        this.trackUpdater  = trackUpdater;
        this.writeCallback = writeCallback;
        this.textUpdater   = textUpdater;
        this.errorHandler  = errorHandler;
    }

    /**
     * Activates or deactivates the AppleScript based song recognition.
     *
     * @param enabled whether to enable the recognition
     */
    private void setAppleScriptEnabled(final boolean enabled) {
        if (enabled) {
            if (scriptLoader == null) {
                scriptLoader = loadScriptLoader();
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
        executionExceptionForwarded = false;
        jsonExceptionForwarded = false;
        uriExceptionForwarded = false;
        allowNoSong = Settings.getInstance().getNoSong();
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

    /**
     * Loads the AppleScript based song loader.
     *
     * @return the script based loader or {@code null} if the script was not found
     */
    private InfoLoaderAppleScript loadScriptLoader() {
        final var location = findScriptLocation("streamTitle.scpt");
        if (location == null || !location.exists()) {
            try (final var stream = Script.class.getClassLoader().getResourceAsStream("streamTitle.applescript")) {
                return new InfoLoaderAppleScript(Script.loadScript(stream));
            } catch (final IOException e) {
                errorHandler.update(e);
            }
        } else {
            return new InfoLoaderAppleScript(new CompiledScript(location));
        }
        return null;
    }

    /**
     * Constructs the location of the given file. The file is searched for in
     * the folder this class has been loaded from.
     *
     * @param fileName the name of the file to be searched for
     * @return the found file or {@code null} if not found
     * @see Class#getProtectionDomain()
     */
    private File findScriptLocation(final String fileName) {
        final var codeSource = InfoLoader.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            return null;
        }
        final var location = codeSource.getLocation();
        if (location == null) {
            return null;
        }
        final File folder;
        try {
            folder = new File(location.toURI());
        } catch (final URISyntaxException e) {
            return null;
        }
        return new File(folder.getParentFile(), fileName);
    }

    /**
     * Sets the level of script support to be used.
     *
     * @param support the script support level
     */
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
     * Updates the currently played song. Uses the JSON and script based
     * song recognition as set with {@link #setScriptSupport(ScriptSupport)}.
     */
    private void updateTrack() {
        final Optional<Song> json, script;
        switch (support) {
            case off -> {
                script = Optional.empty();
                json   = Optional.ofNullable(getTrackJSON());
            }
            case on -> {
                script = Optional.ofNullable(getTrackScript());
                json   = Optional.ofNullable(getTrackJSON());
            }
            case only -> {
                script = Optional.ofNullable(getTrackScript());
                json   = Optional.empty();
            }
            default -> throw new IllegalStateException("Script support switch was not exhaustive");
        }
        final var current = getCurrentSong();

        final Optional<Optional<Song>> newJson, newScript;
        if ((allowNoSong || json.isPresent()) && !Objects.equals(json.orElse(null), lastJson) && ((allowNoSong && previous == null) || !Objects.equals(json.orElse(null), previous)) && !Objects.equals(json.orElse(null), current)) {
            newJson = Optional.of(json);
        } else {
            newJson = Optional.empty();
        }
        lastJson = json.orElse(null);

        if (script.isPresent() && !Objects.equals(script.orElse(null), lastScript) && !Objects.equals(script.orElse(null), previous) && !Objects.equals(script.orElse(null), current)) {
            newScript = Optional.of(script);
        } else {
            newScript = Optional.empty();
        }
        lastScript = script.orElse(null);

        final Optional<Optional<Song>> newSong;
        if (newJson.isPresent()) {
            newSong = newJson;
        } else {
            newSong = newScript;
        }

        if (newSong.isPresent()) {
            previous = current;
            setCurrentSong(newSong.get().orElse(null));
            trackUpdater.run();
        }
    }

    /**
     * Loads and returns the song currently recognized by the script based loader.
     *
     * @return the currently recognized song
     */
    private Song getTrackScript() {
        final Pair<String, Song> result;
        try {
            result = scriptLoader.getScriptResult();
        } catch (final ExecutionException e) {
            if (!executionExceptionForwarded || support == ScriptSupport.only) {
                executionExceptionForwarded = true;
                errorHandler.update(e);
            }
            return null;
        }
        executionExceptionForwarded = false;
        textUpdater.update(result.getFirst());
        return result.getSecond();
    }

    /**
     * Loads and returns the song currently recognized by the JSON based recognition.
     *
     * @return the currently recognized song
     */
    private Song getTrackJSON() {
        final URL url;
        try {
            url = new URI(Settings.getInstance().getURL()).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            if (!uriExceptionForwarded || support == ScriptSupport.off) {
                uriExceptionForwarded = true;
                errorHandler.update(e);
            }
            return null;
        }
        uriExceptionForwarded = false;

        try (final var reader = new BufferedInputStream(url.openStream())) {
            new JSONParser(new StringStream(new String(reader.readAllBytes(), StandardCharsets.UTF_8))).readInto(dto);
        } catch (final Exception e) {
            if (!jsonExceptionForwarded || support == ScriptSupport.off) {
                jsonExceptionForwarded = true;
                errorHandler.update(e);
            }
            return null;
        }
        jsonExceptionForwarded = false;
        final var playedSong = getPlayedSong(dto);
        return playedSong == null ? null : new Song(playedSong.title, playedSong.artist);
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
        Song      written = null;
        Exception error   = null;
        try {
            written = saveTrack(song);
        } catch (final Exception e) {
            error = e;
        }
        writeCallback.update(written);
        if (error != null) {
            errorHandler.update(error);
        }
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
            throw new IllegalStateException(Settings.getInstance().getLocale().get(StringID.INTERNAL_NO_TRACK_RECOGNIZED));
        }
        final var path = Settings.getInstance().getSavePath();
        if (path == null || path.isBlank()) {
            throw new IllegalStateException(Settings.getInstance().getLocale().get(StringID.INTERNAL_SAVE_FOLDER_UNSET));
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
