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

package mhahnFr.iSongs.core;

import mhahnFr.iSongs.core.locale.StringID;

public enum DarkMode {
    DARK,
    LIGHT,
    AUTO;

    private static final DarkMode[] values = DarkMode.values();

    public String toString() {
        final var locale = Settings.getInstance().getLocale();
        return switch (this) {
            case DARK  -> locale.get(StringID.DARK_MODE_DARK);
            case LIGHT -> locale.get(StringID.DARK_MODE_LIGHT);
            case AUTO  -> locale.get(StringID.DARK_MODE_AUTO);
        };
    }

    public static DarkMode createDarkMode(final int ordinal) {
        return values[ordinal];
    }
}
