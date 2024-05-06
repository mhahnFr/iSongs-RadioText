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

import java.io.*;

/**
 * This class represents an executable AppleScript.
 *
 * @author mhahnFr
 * @since 28.03.24
 */
public class Script {
    /** The content of the script. */
    private final String content;

    /**
     * Constructs a script using the given content.
     *
     * @param content the content of the script
     */
    public Script(final String content) {
        this.content = content;
    }

    /**
     * Executes this script. The script's content is passed to
     * {@code osascript}, the regular result is returned.
     *
     * @return the regular result of the script
     */
    public String execute() throws ExecutionException {
        return execute(new String[] { "osascript", "-e", content });
    }

    protected String execute(final String[] args) throws ExecutionException {
        try {
            final var p = Runtime.getRuntime().exec(args);
            try (final var in  = p.getInputStream();
                 final var err = p.getErrorStream()) {
                final var allIn  = in.readAllBytes();
                final var allErr = err.readAllBytes();

                if (allErr.length != 0) {
                    throw new ExecutionException(new String(allErr));
                }
                return new String(allIn);
            }
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * Loads an AppleScript from the given {@link InputStream}.
     *
     * @param stream the stream to load the script's content from
     * @return the {@link Script} or {@code null} if the script could not be loaded
     */
    private static Script load(final InputStream stream) {
        final String content;
        try (final var reader = new BufferedReader(new InputStreamReader(stream))) {
            final var buffer = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                buffer.append((char) c);
            }
            content = buffer.toString();
        } catch (final IOException e) {
            return null;
        }
        return new Script(content);
    }

    /**
     * Loads an AppleScript from disk.
     *
     * @param fileName the name of the file to be loaded as the source code of an AppleScript
     * @return the {@link Script} or {@code null} if the script could not be loaded
     */
    public static Script loadScript(final String fileName) {
        try (final var stream = new FileInputStream(fileName)) {
            return load(stream);
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * Loads an AppleScript from the given {@link InputStream}.
     *
     * @param stream the stream to load the script's content from
     * @return the {@link Script} or {@code null} if the script could not be loaded
     */
    public static Script loadScript(final InputStream stream) {
        return load(stream);
    }
}
