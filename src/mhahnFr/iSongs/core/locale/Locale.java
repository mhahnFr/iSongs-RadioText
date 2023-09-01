/*
 * iSongs-RadioText - Radio-text part of iSongs.
 *
 * Copyright (C) 2023  mhahnFr
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

package mhahnFr.iSongs.core.locale;

/**
 * This interface defines the behaviour for locales.
 *
 * @author mhahnFr
 * @since 01.09.23
 */
public abstract class Locale {
    /**
     * Returns a {@link String} translating the requested sentence.
     *
     * @param id the id of the requested sentence
     * @return the translation
     */
    public abstract String get(final StringID id);

    public abstract String getName();

    public static Locale fromName(final String name) {
        return switch (name) {
            case German.name -> new German();

            default -> new English();
        };
    }

    @Override
    public String toString() {
        return getName();
    }
}
