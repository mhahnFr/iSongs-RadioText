/*
 * iSongs-RadioText - Radio-text part of iSongs.
 *
 * Copyright (C) 2023 - 2024  mhahnFr
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

package mhahnFr.iSongs.core;

/**
 * This interface defines the callback function after saving a song.
 *
 * @author mhahnFr
 * @since 15.03.23
 */
@FunctionalInterface
public interface WriteCallback {
    /**
     * Called when a song has been written. If the song could not be
     * written, the song parameter is set to {@code null} and the
     * {@link Exception} that happened is passed.
     * <br>
     * Otherwise, the written song is passed and the exception parameter
     * is set to {@code null}.
     *
     * @param song      the song that has been written
     * @param exception the {@link Exception} that happened while writing
     */
    void songWritten(final Song song, final Exception exception);
}
