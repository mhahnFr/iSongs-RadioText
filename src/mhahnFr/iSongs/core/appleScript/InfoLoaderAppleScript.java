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

import mhahnFr.utils.Pair;

public class InfoLoaderAppleScript {
    private final Script script;

    public InfoLoaderAppleScript(final Script script) {
        this.script = script;
    }

    public Pair<String, Pair<String, String>> getScriptResult() {
        final String text;
        try {
            text = script.execute();
        } catch (final Exception e) {
            return null;
        }
        final Pair<String, String> song;
        final var index = text.indexOf(" / ");
        if (index != -1) {
            song = new Pair<>(text.substring(0, index), text.substring(index + 3));
        } else {
            song = null;
        }
        return new Pair<>(text, song);
    }
}
