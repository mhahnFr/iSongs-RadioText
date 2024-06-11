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
import mhahnFr.iSongs.core.DarkMode;
import mhahnFr.iSongs.core.Settings;
import mhahnFr.iSongs.core.appleScript.ScriptSupport;
import mhahnFr.iSongs.core.locale.English;
import mhahnFr.iSongs.core.locale.German;
import mhahnFr.iSongs.core.locale.Locale;
import mhahnFr.iSongs.core.locale.StringID;
import mhahnFr.utils.gui.DarkModeListener;
import mhahnFr.utils.gui.components.HintTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Objects;
import java.util.Optional;

/**
 * This class represents the settings window of the iSongs project.
 *
 * @author mhahnFr
 * @since 14.03.23
 */
public class SettingsWindow extends JDialog implements DarkModeListener {
    /** The label displaying the folder for saving songs.     */
    private final JLabel folderChangeLabel;
    /** The text field for the URL to the song information.   */
    private final JTextField urlField;
    /** The {@link Locale} to be used by this instance.       */
    private final Locale locale = Settings.getInstance().getLocale();

    /**
     * Constructs this settings window using the given owner.
     * This window is modal.
     *
     * @param owner the owner
     */
    public SettingsWindow(final JFrame owner) {
        super(owner, Constants.NAME + ": " + Settings.getInstance().getLocale().get(StringID.MAIN_SETTINGS), true);

        final var panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
            final var northPanel = new JPanel(new BorderLayout());
                final var noSongBox = new JCheckBox(locale.get(StringID.SETTINGS_ALLOW_NO_SONG));

                final var scriptSupportPanel = getScriptSupportPanel(Settings.getInstance().getScriptSupport());
            northPanel.add(noSongBox, BorderLayout.CENTER);
            scriptSupportPanel.ifPresent(jPanel -> northPanel.add(jPanel, BorderLayout.SOUTH));

            final var centerPanel = new JPanel(new GridLayout(5, 1));
                final var themePanel = new JPanel(new GridLayout(1, 1));
                themePanel.setBorder(new TitledBorder(locale.get(StringID.SETTINGS_CHOOSE_THEME) + ":"));
                final var darkChooser = new JComboBox<DarkMode>();
                themePanel.add(darkChooser);

                final var localePanel = new JPanel(new GridLayout(1, 1));
                localePanel.setBorder(new TitledBorder(locale.get(StringID.SETTINGS_CHOOSE_LANG) + ":"));
                    final var localeBox = new JComboBox<Locale>();
                localePanel.add(localeBox);

                final var urlPanel = new JPanel(new GridLayout(1, 1));
                urlPanel.setBorder(new TitledBorder(locale.get(StringID.SETTINGS_JSON_URI_DESC) + ":"));
                    urlField = new HintTextField("https://www.example.org/infos.json");
                urlPanel.add(urlField);

                final var folderPanel = new JPanel(new GridLayout(1, 1));
                folderPanel.setBorder(new TitledBorder(locale.get(StringID.SETTINGS_SONG_INFO_FOLDER_DESC) + ":"));
                    final var folderChangePanel = new JPanel(new BorderLayout());
                        folderChangeLabel = new JLabel();
                        folderChangeLabel.setFont(folderChangeLabel.getFont().deriveFont(Font.BOLD));

                        final var folderChangeButton = new JButton(locale.get(StringID.SETTINGS_CHANGE) + "...");
                    folderChangePanel.add(folderChangeLabel, BorderLayout.CENTER);
                    folderChangePanel.add(folderChangeButton, BorderLayout.EAST);
                folderPanel.add(folderChangePanel);

                final var delayPanel = new JPanel(new GridLayout(1, 1));
                delayPanel.setBorder(new TitledBorder(locale.get(StringID.SETTINGS_SONG_REFRESH_RATE) + ":"));
                    final var delaySpinner = new JSpinner();
                delayPanel.add(delaySpinner);
            centerPanel.add(themePanel);
            centerPanel.add(localePanel);
            centerPanel.add(urlPanel);
            centerPanel.add(folderPanel);
            centerPanel.add(delayPanel);

            final var deleteButton = new JButton(locale.get(StringID.SETTINGS_REMOVE));
        panel.add(northPanel,   BorderLayout.NORTH);
        panel.add(centerPanel,  BorderLayout.CENTER);
        panel.add(deleteButton, BorderLayout.SOUTH);

        getContentPane().add(panel);

        final var settings = Settings.getInstance();
        darkChooser.addItem(DarkMode.DARK);
        darkChooser.addItem(DarkMode.AUTO); // TODO: Only if NDL is available
        darkChooser.addItem(DarkMode.LIGHT);
        darkChooser.setEditable(false);
        darkChooser.setSelectedItem(settings.getDarkMode());
        darkChooser.addActionListener(_ -> settings.setDarkMode((DarkMode) Objects.requireNonNull(darkChooser.getSelectedItem())));

        noSongBox.setSelected(settings.getNoSong());
        noSongBox.addItemListener(_ -> settings.setNoSong(noSongBox.isSelected()));

        localeBox.addItem(locale instanceof English ? locale : new English());
        localeBox.addItem(locale instanceof German ? locale : new German());
        localeBox.setSelectedItem(locale);
        localeBox.addItemListener(this::onLocaleChanged);

        urlField.setText(settings.getURL());

        delaySpinner.setValue(settings.getDelay());
        delaySpinner.addChangeListener(_ -> settings.setDelay((Integer) delaySpinner.getValue()));

        folderChangeLabel.setText(settings.getSavePath());
        folderChangeButton.addActionListener(_ -> chooseSaveFolder());

        deleteButton.addActionListener(_ -> removeSettings());

        settings.addDarkModeListener(this);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    /**
     * Creates the AppleScript support settings panel. The panel is only created on macOS.
     *
     * @param selection which radio button to select
     * @return the optionally available AppleScript support settings panel
     */
    private Optional<JPanel> getScriptSupportPanel(final ScriptSupport selection) {
        if (!Settings.isMac) return Optional.empty();

        final var scriptSupportPanel = new JPanel(new GridLayout(1, 1));
        scriptSupportPanel.setBorder(new TitledBorder(locale.get(StringID.SETTINGS_APPLESCRIPT_DESC) + ":"));
            final var scriptSupportButtonPanel = new JPanel(new GridLayout(3, 1));
                final var scriptSupportOff = new JRadioButton(locale.get(StringID.SETTINGS_APPLESCRIPT_OFF));

                final var scriptSupportMixed = new JRadioButton(locale.get(StringID.SETTINGS_APPLESCRIPT_ON));

                final var scriptSupportOnly = new JRadioButton(locale.get(StringID.SETTINGS_APPLESCRIPT_ONLY));
            scriptSupportButtonPanel.add(scriptSupportOff);
            scriptSupportButtonPanel.add(scriptSupportMixed);
            scriptSupportButtonPanel.add(scriptSupportOnly);
        scriptSupportPanel.add(scriptSupportButtonPanel);

        final var group = new ButtonGroup();
        group.add(scriptSupportOff);
        group.add(scriptSupportMixed);
        group.add(scriptSupportOnly);

        switch (selection) {
            case off  -> scriptSupportOff.setSelected(true);
            case on   -> scriptSupportMixed.setSelected(true);
            case only -> scriptSupportOnly.setSelected(true);
        }

        scriptSupportOff  .addItemListener(_ -> Settings.getInstance().setScriptSupport(ScriptSupport.off));
        scriptSupportMixed.addItemListener(_ -> Settings.getInstance().setScriptSupport(ScriptSupport.on));
        scriptSupportOnly .addItemListener(_ -> Settings.getInstance().setScriptSupport(ScriptSupport.only));

        return Optional.of(scriptSupportPanel);
    }

    /**
     * Handles the event of changing the language.
     *
     * @param event the item event
     */
    private void onLocaleChanged(final ItemEvent event) {
        if (event.getStateChange() != ItemEvent.SELECTED) return;

        final var newLocale = (Locale) event.getItem();
        Settings.getInstance().setLocale(newLocale);
        if (newLocale != locale) {
            JOptionPane.showMessageDialog(
                    this,
                    newLocale.get(StringID.SETTINGS_LANGUAGE_RESTART) + ".",
                    Constants.NAME + ": " + newLocale.get(StringID.MAIN_SETTINGS),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Opens a {@link JFileChooser} for choosing the folder where
     * to save the songs.
     */
    private void chooseSaveFolder() {
        final var chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileHidingEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final var path = chooser.getSelectedFile().getAbsolutePath();
            folderChangeLabel.setText(path);
            Settings.getInstance().setSavePath(path);
        }
    }

    /**
     * Prompts the user if he wishes to remove the settings.
     * Does so, if the user wants to.
     */
    private void removeSettings() {
        if (JOptionPane.showConfirmDialog(this,
                locale.get(StringID.SETTINGS_REMOVE_REALLY),
                Constants.NAME + ": " + locale.get(StringID.MAIN_SETTINGS),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            if (!Settings.getInstance().remove() || !Settings.getInstance().flush()) {
                JOptionPane.showMessageDialog(this,
                        locale.get(StringID.SETTINGS_REMOVE_ERROR) + "!",
                        Constants.NAME + ": " + locale.get(StringID.MAIN_SETTINGS),
                        JOptionPane.ERROR_MESSAGE);
            }
            System.exit(0);
        }
    }

    @Override
    public void darkModeToggled(boolean dark) {
        SwingUtilities.updateComponentTreeUI(this);
    }

    @Override
    public void dispose() {
        final var settings = Settings.getInstance();

        settings.removeDarkModeListener(this);
        if (!settings.setURL(urlField.getText())
                     .flush()) {
            JOptionPane.showMessageDialog(this,
                    locale.get(StringID.SETTINGS_SAVE_ERROR) + "!",
                    Constants.NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
        super.dispose();
    }
}
