/*
 * iSongs-RadioText - Radio-text part of iSongs.
 *
 * Copyright (C) 2023 - 2024  mhahnFr
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

package mhahnFr.iSongs;

import mhahnFr.iSongs.core.Settings;
import mhahnFr.iSongs.gui.MainWindow;

import java.awt.EventQueue;

/**
 * This class represents the main entry point of the application.
 *
 * @author mhahnFr
 * @since 14.03.23
 */
public class iSongs {
    public static void main(String[] args) {
        if (Settings.isMac) {
            System.setProperty("apple.awt.application.appearance", "system");
        }

        Settings.setupLaf(Settings.getInstance().getDarkMode());
        EventQueue.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
