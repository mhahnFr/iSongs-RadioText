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

package mhahnFr.iSongs.gui;

import mhahnFr.iSongs.core.Constants;
import mhahnFr.iSongs.core.InfoLoader;
import mhahnFr.iSongs.core.Settings;
import mhahnFr.iSongs.core.Song;
import mhahnFr.iSongs.core.locale.Locale;
import mhahnFr.iSongs.core.locale.StringID;
import mhahnFr.utils.gui.DarkModeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;

/**
 * This class represents the main window of the iSongs project.
 *
 * @author mhahnFr
 * @since 14.03.23
 */
public class MainWindow extends JFrame implements DarkModeListener {
    /** The {@link InfoLoader}.                                                 */
    private final InfoLoader loader = new InfoLoader(this::updateUI,
                                                     this::writeCallback,
                                                     this::radioTextCallback,
                                                     this::errorCallback);
    /** The timer for resetting the title bar.                                  */
    private final Timer savedTimer = new Timer(5000, _ -> unblockTitle());
    /** The {@link JLabel} displaying the title of the song.                    */
    private final JLabel titleLabel;
    /** The {@link JLabel} displaying the interpreter of the song.              */
    private final JLabel interpreterLabel;
    /** The {@link JButton} used for saving the song.                           */
    private final JButton saveButton;
    /** The {@link Locale} to be used in this instance.                         */
    private final Locale locale = Settings.getInstance().getLocale();
    /** Indicates whether the window title should not be changed.               */
    private boolean blockedTitle = false;
    /** The title to be set once the window title is unblocked.                 */
    private String title;

    /**
     * Constructs this main window.
     */
    public MainWindow() {
        super(Constants.NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final var panel = new JPanel(new GridLayout(4, 1));
            final var label = new JLabel(" " + locale.get(StringID.MAIN_CURRENT_TITLE) + ":");

            titleLabel = new JLabel(locale.get(StringID.MAIN_NO_SONG), SwingConstants.CENTER);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

            interpreterLabel = new JLabel(locale.get(StringID.MAIN_NO_INTERPRETER), SwingConstants.CENTER);

            final var wrapper = new JPanel(new BorderLayout());
                final var toAdd = new JPanel();
                    saveButton = new JButton(locale.get(StringID.MAIN_SAVE_TITLE));
                    saveButton.addActionListener(_ -> saveTitle());
                    saveButton.setEnabled(false);

                    if (hasSettings()) {
                        addSettingsHook();
                    } else {
                        final var settingsButton = new JButton(locale.get(StringID.MAIN_SETTINGS));
                        settingsButton.addActionListener(_ -> showSettings());
                        toAdd.add(settingsButton);
                    }
                toAdd.add(saveButton);
            wrapper.add(toAdd, BorderLayout.CENTER);
        panel.add(label);
        panel.add(titleLabel);
        panel.add(interpreterLabel);
        panel.add(wrapper);
        getContentPane().add(panel);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                saveButton.requestFocusInWindow();
            }
        });
        maybeAddQuitHandler();
        restoreBounds();

        savedTimer.setRepeats(false);

        Settings.getInstance().addDarkModeListener(this);
        loader.start();
    }

    /**
     * Adds a {@link java.awt.desktop.QuitHandler} saving the UI
     * state if supported.
     */
    private void maybeAddQuitHandler() {
        if (Desktop.getDesktop().isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            Desktop.getDesktop().setQuitHandler((_, response) -> {
                saveSettings();
                response.performQuit();
            });
        }
    }

    @Override
    public void darkModeToggled(boolean dark) {
        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * Updates the UI in order to display the currently played song.
     * This method makes sure it runs in the {@link EventQueue}.
     */
    private void updateUI() {
        onUIThread(() -> {
            final var displayedSong = loader.getCurrentSong();
            if (displayedSong != null) {
                titleLabel.setText(displayedSong.title());
                interpreterLabel.setText(displayedSong.interpreter());
                saveButton.setEnabled(true);
            } else {
                titleLabel.setText(locale.get(StringID.MAIN_NO_SONG));
                interpreterLabel.setText(locale.get(StringID.MAIN_NO_INTERPRETER));
                saveButton.setEnabled(false);
            }
        });
    }

    /**
     * Runs the given {@link Runnable} in the {@link EventQueue}.
     *
     * @param cb the {@link Runnable} to be run on the UI thread
     */
    private static void onUIThread(final Runnable cb) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(() -> onUIThread(cb));
            return;
        }
        cb.run();
    }

    /**
     * Sets the window title to the given value. This method makes sure
     * it runs on the UI thread.
     *
     * @param value the new window title
     */
    private void radioTextCallback(final String value) {
        onUIThread(() -> setTitle(Objects.requireNonNullElse(value, Constants.NAME)));
    }

    /**
     * Shows the given {@link Exception} to the user. This method makes sure
     * it runs on the UI thread.
     *
     * @param e the {@link Exception} to handle
     */
    private void errorCallback(final Exception e) {
        onUIThread(() -> JOptionPane.showMessageDialog(this,
                locale.get(StringID.MAIN_ERROR_HAPPENED) + ": " + e.getLocalizedMessage(),
                Constants.NAME + ": " + locale.get(StringID.MAIN_ERROR),
                JOptionPane.ERROR_MESSAGE));
    }

    /**
     * Updates the UI after saving a song. This method makes
     * sure it runs in the {@link EventQueue}.
     *
     * @param song  the saved song
     */
    private void writeCallback(final Song song) {
        onUIThread(() -> {
            blockTitle();
            if (song != null) {
                super.setTitle("\"" + song.title() + "\" " + locale.get(StringID.MAIN_STORED));
                saveButton.setEnabled(false);
            } else {
                super.setTitle(locale.get(StringID.MAIN_SAVE_ERROR));
            }
            savedTimer.restart();
        });
    }

    /**
     * Blocks the window title. The current title is saved for the unblocking.
     *
     * @see #unblockTitle()
     * @see #blockedTitle
     * @see #title
     */
    private void blockTitle() {
        blockedTitle = true;
        title = getTitle();
    }

    @Override
    public void setTitle(final String title) {
        if (blockedTitle) {
            this.title = title;
        } else {
            super.setTitle(title);
        }
    }

    /**
     * Unblocks the window title. The previously stored title is reset.
     *
     * @see #blockTitle()
     * @see #blockedTitle
     * @see #title
     */
    private void unblockTitle() {
        blockedTitle = false;
        setTitle(title);
        title = null;
    }

    /**
     * Adds the settings hook.
     */
    private void addSettingsHook() {
        Desktop.getDesktop().setPreferencesHandler(_ -> showSettings());
    }

    /**
     * Returns whether the default settings action is supported.
     *
     * @return whether the default settings are supported
     */
    private boolean hasSettings() {
        return Desktop.getDesktop().isSupported(Desktop.Action.APP_PREFERENCES);
    }

    /**
     * Opens a {@link SettingsWindow}. Stops the song fetching and resets the window title.
     */
    private void showSettings() {
        loader.stop();
        setTitle(Constants.NAME);
        final var settingsWindow = new SettingsWindow(this);
        settingsWindow.setLocationRelativeTo(this);
        settingsWindow.setVisible(true);
        loader.start();
    }

    /**
     * Saves the currently played song.
     */
    private void saveTitle() {
        loader.saveSong();
    }

    /**
     * Restores the bounds of the window.
     */
    private void restoreBounds() {
        final var settings = Settings.getInstance();

        final int x      = settings.getWindowX(),
                  y      = settings.getWindowY(),
                  width  = settings.getWindowWidth(),
                  height = settings.getWindowHeight();

        if (width < 0 || height < 0) {
            pack();
        } else {
            setSize(width, height);
        }
        if (x < 0 || y < 0) {
            setLocationRelativeTo(null);
        } else {
            setLocation(x, y);
        }
    }

    /**
     * Stores the UI state.
     */
    private void saveSettings() {
        if (!Settings.getInstance().setWindowX(getX())
                                   .setWindowY(getY())
                                   .setWindowWidth(getWidth())
                                   .setWindowHeight(getHeight())
                                   .flush()) {
            JOptionPane.showMessageDialog(this,
                    locale.get(StringID.MAIN_UI_STATE_SAVE_ERROR) + "!",
                    Constants.NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        Settings.getInstance().removeDarkModeListener(this);
        saveSettings();
        super.dispose();
    }
}
