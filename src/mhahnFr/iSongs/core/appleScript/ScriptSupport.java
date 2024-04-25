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

/**
 * This enumeration indicates the level of which the AppleScript based
 * song recognition should be used.
 *
 * @author mhahnFr
 * @since 09.04.24
 */
public enum ScriptSupport {
    /** No script support.                  */
    off,
    /** Script support used additionally.   */
    on,
    /** Only script based song recognition. */
    only,
}
