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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.InputStream;

public class Script {
    private final String content;
    private final ScriptEngine engine;

    public Script(final String content, final ScriptEngine engine) {
        this.content = content;
        this.engine  = engine;
    }

    public Script(final String content) {
        this(content, new ScriptEngineManager().getEngineByName("AppleScript"));
    }

    public static Script loadScript(final String fileName) {
        // TODO: Implement
        return null;

    }

    public static Script loadScript(final InputStream stream) {
        // TODO: Implement
        return null;
    }
}
