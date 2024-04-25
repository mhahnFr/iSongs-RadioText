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

/**
 * This interface defines a callback to be called with a single argument.
 *
 * @param <T> the type of the argument passed
 * @author mhahnFr
 * @since 09.04.24
 */
@FunctionalInterface
public interface Callback<T> {
    /**
     * Called to notify about the given value.
     *
     * @param value the value to be handled
     */
    void update(final T value);
}
