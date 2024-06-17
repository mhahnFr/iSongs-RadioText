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

/**
 * This enumeration contains the possible dark mode states.
 *
 * @author mhahnFr
 * @since 11.06.24
 */
public enum DarkMode {
    /** Indicates the dark state.      */
    DARK,
    /** Indicates the light state.     */
    LIGHT,
    /** Indicates the automatic state. */
    AUTO;

    /** The cached possible values.    */
    private static final DarkMode[] values = DarkMode.values();

    @Override
    public String toString() {
        final var locale = Settings.getInstance().getLocale();
        return switch (this) {
            case DARK  -> locale.get(StringID.DARK_MODE_DARK);
            case LIGHT -> locale.get(StringID.DARK_MODE_LIGHT);
            case AUTO  -> locale.get(StringID.DARK_MODE_AUTO);
        };
    }

    /**
     * Returns the {@link DarkMode} value corresponding to the given ordinal value.
     * The lookup list is cached.
     *
     * @param ordinal the ordinal value of the enumeration value
     * @return the appropriate enumeration value
     */
    public static DarkMode createDarkMode(final int ordinal) {
        return values[ordinal];
    }
}
