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
 * This interface defines the possibility to listen to
 * language changes.
 *
 * @author mhahnFr
 * @since 02.09.23
 */
public interface LanguageListener {
    /**
     * Called when the language has changed.
     *
     * @param newLocale the new locale to be used
     */
    void languageChanged(final Locale newLocale);
}
