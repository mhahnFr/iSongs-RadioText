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
import mhahnFr.utils.gui.components.DarkComponent;
import mhahnFr.utils.gui.DarkModeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents the main window of the iSongs project.
 *
 * @author mhahnFr
 * @since 14.03.23
 */
public class MainWindow extends JFrame implements DarkModeListener {
    /** The list with the components, enabling the dark mode.      */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    /** The {@link InfoLoader}.                                    */
    private final InfoLoader loader = new InfoLoader(this::updateUI,
                                                     this::writeCallback,
                                                     this::radioTextCallback,
                                                     this::errorCallback);
    /** The timer for resetting the title bar.                     */
    private final Timer savedTimer = new Timer(5000, __ -> unblockTitle());
    /** The {@link JLabel} displaying the title of the song.       */
    private final JLabel titleLabel;
    /** The {@link JLabel} displaying the interpreter of the song. */
    private final JLabel interpreterLabel;
    /** The {@link JButton} used for saving the song.              */
    private final JButton saveButton;
    /** The {@link JButton} displaying the {@link #lastException}. */
    private final JButton errorButton;
    /** The {@link Locale} to be used in this instance.            */
    private final Locale locale = Settings.getInstance().getLocale();
    /** The last {@link Exception} that happened.                  */
    private Exception lastException;
    private boolean blockedTitle = false;
    private String title;

    /**
     * Constructs this main window.
     */
    public MainWindow() {
        super(Constants.NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final var panel = new DarkComponent<>(new JPanel(new GridLayout(4, 1)), components).getComponent();
            final var label = new DarkComponent<>(new JLabel(" " + locale.get(StringID.MAIN_CURRENT_TITLE) + ":"), components).getComponent();

            titleLabel = new DarkComponent<>(new JLabel(locale.get(StringID.MAIN_LOADING) + "...", SwingConstants.CENTER), components).getComponent();
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

            interpreterLabel = new DarkComponent<>(new JLabel(locale.get(StringID.MAIN_LOADING) + "...", SwingConstants.CENTER), components).getComponent();

            final var wrapper = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                final var toAdd = new DarkComponent<>(new JPanel(), components).getComponent();
                    saveButton = new JButton(locale.get(StringID.MAIN_SAVE_TITLE));
                    saveButton.addActionListener(__ -> saveTitle());

                    errorButton = new JButton(locale.get(StringID.MAIN_SHOW_ERROR));
                    errorButton.addActionListener(__ -> showLastError());

                    if (hasSettings()) {
                        addSettingsHook();
                    } else {
                        final var settingsButton = new JButton(locale.get(StringID.MAIN_SETTINGS));
                        settingsButton.addActionListener(__ -> showSettings());
                        toAdd.add(settingsButton);
                    }
                toAdd.add(saveButton);
                toAdd.add(errorButton);
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
        darkModeToggled(Settings.getInstance().getDarkMode());
        loader.start();
    }

    /**
     * Adds a {@link java.awt.desktop.QuitHandler} saving the UI
     * state if supported.
     */
    private void maybeAddQuitHandler() {
        if (Desktop.getDesktop().isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            Desktop.getDesktop().setQuitHandler((__, response) -> {
                saveSettings();
                response.performQuit();
            });
        }
    }

    @Override
    public void darkModeToggled(boolean dark) {
        for (final var component : components) {
            component.setDark(dark);
        }
    }

    /**
     * Updates the UI in order to display the currently played song.
     * This method makes sure it runs in the {@link EventQueue}.
     */
    private void updateUI() {
        onUIThread(() -> {
            errorButton.setVisible(false);
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

    private static void onUIThread(final Runnable cb) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(() -> onUIThread(cb));
            return;
        }
        cb.run();
    }

    private void radioTextCallback(final String value) {
        onUIThread(() -> setTitle(Objects.requireNonNullElse(value, Constants.NAME)));
    }

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
     * @param error the {@link Exception} that happened
     */
    private void writeCallback(final Song      song,
                               final Exception error) {
        onUIThread(() -> {
            blockTitle();
            if (error == null) {
                super.setTitle("\"" + song.title() + "\" " + locale.get(StringID.MAIN_STORED));
                saveButton.setEnabled(false);
            } else {
                super.setTitle(locale.get(StringID.MAIN_SAVE_ERROR));
                errorButton.setVisible(true);
                lastException = error;
            }
            savedTimer.restart();
        });
    }

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

    private void unblockTitle() {
        blockedTitle = false;
        setTitle(title);
        title = null;
    }

    /**
     * Shows the last error that occurred during writing a song to
     * disk.
     *
     * @see #lastException
     */
    private void showLastError() {
        if (lastException == null) {
            JOptionPane.showMessageDialog(this,
                                          locale.get(StringID.MAIN_NO_ERROR) + ".",
                                          Constants.NAME,
                                          JOptionPane.INFORMATION_MESSAGE);
        } else {
            final var sw = new StringWriter();
            lastException.printStackTrace(new PrintWriter(sw));
            JOptionPane.showMessageDialog(this,
                                          sw,
                                          Constants.NAME + ": " + locale.get(StringID.MAIN_ERROR),
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Adds the settings hook.
     */
    private void addSettingsHook() {
        Desktop.getDesktop().setPreferencesHandler(__ -> showSettings());
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
     * Opens a {@link SettingsWindow}. Stops the song fetching.
     */
    private void showSettings() {
        loader.stop();
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
