/*
 * iSongs-RadioText - Radio-text part of iSongs.
 *
 * Copyright (C) 2024  mhahnFr
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

package mhahnFr.iSongs.core.appleScript;

import java.io.*;

public class Script {
    private final String content;

    public Script(final String content) {
        this.content = content;
    }

    public String execute() {
        final String toReturn;
        try (final var reader = Runtime.getRuntime().exec(new String[] { "osascript", "-e", content }).inputReader()) {
            final var builder = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                builder.append((char) c);
            }
            toReturn = builder.toString();
        } catch (final IOException e) {
            return null;
        }
        return toReturn;
    }

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

    public static Script loadScript(final String fileName) {
        try (final var stream = new FileInputStream(fileName)) {
            return load(stream);
        } catch (final IOException e) {
            return null;
        }
    }

    public static Script loadScript(final InputStream stream) {
        return load(stream);
    }
}
