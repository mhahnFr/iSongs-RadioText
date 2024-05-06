/*
 * iSongs-RadioText - Radio-text part of iSongs.
 *
 * Copyright (C) 2024  mhahnFr
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

package mhahnFr.iSongs.core.appleScript;

import mhahnFr.iSongs.core.Song;
import mhahnFr.utils.Pair;

/**
 * This class acts as a song recognizer based on its {@link Script}.
 *
 * @author mhahnFr
 * @since 28.03.24
 */
public class InfoLoaderAppleScript {
    /** The script to load the radio text with. */
    private final Script script;

    /**
     * Constructs an AppleScript based loader.
     *
     * @param script the {@link Script} to be used for the loading
     */
    public InfoLoaderAppleScript(final Script script) {
        this.script = script;
    }

    /**
     * Executes the {@link Script} and returns the recognized song and radio text.
     *
     * @return the song and radio text
     */
    public Pair<String, Song> getScriptResult() throws ExecutionException {
        final String text;
        text = script.execute();
        final Song song;
        final var index = text.indexOf(" / ");
        if (index != -1) {
            song = new Song(text.substring(0, index).strip(), text.substring(index + 3).strip());
        } else {
            song = null;
        }
        return new Pair<>(text.strip(), song);
    }
}
