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

import java.io.File;
import java.io.IOException;

/**
 * Represents a compiled AppleScript.
 *
 * @author mhahnFr
 * @since 15.04.24
 */
public class CompiledScript extends Script {
    /** The file representation of the compiled script. */
    final File location;

    /**
     * Constructs a compiled AppleScript.
     *
     * @param location the file of the compiled AppleScript
     */
    public CompiledScript(final File location) {
        super(null);

        this.location = location;
    }

    @Override
    public String execute() {
        try (final var stream = Runtime.getRuntime().exec(new String[] { "osascript", location.getAbsolutePath() }).getInputStream()) {
            return new String(stream.readAllBytes());
        } catch (final IOException e) {
            return null;
        }
    }
}
