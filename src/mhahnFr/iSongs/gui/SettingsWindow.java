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

package mhahnFr.iSongs.gui;

import mhahnFr.iSongs.core.Constants;
import mhahnFr.iSongs.core.Settings;
import mhahnFr.iSongs.core.locale.English;
import mhahnFr.iSongs.core.locale.German;
import mhahnFr.iSongs.core.locale.Locale;
import mhahnFr.iSongs.core.locale.StringID;
import mhahnFr.utils.gui.components.DarkComponent;
import mhahnFr.utils.gui.DarkModeListener;
import mhahnFr.utils.gui.components.DarkTextComponent;
import mhahnFr.utils.gui.components.HintTextField;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class represents the settings window of the iSongs project.
 *
 * @author mhahnFr
 * @since 14.03.23
 */
public class SettingsWindow extends JDialog implements DarkModeListener {
    /** The list with the components, enabling the dark mode. */
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
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

        final var panel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
            final var northPanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                final var darkBox = new DarkComponent<>(new JCheckBox(locale.get(StringID.SETTINGS_ACTIVATE_DARK_MODE)), components).getComponent();

                final var scriptSupportPanel = getScriptSupportPanel();
            northPanel.add(darkBox, BorderLayout.NORTH);
            scriptSupportPanel.ifPresent(jPanel -> northPanel.add(jPanel, BorderLayout.CENTER));

            final var centerPanel = new DarkComponent<>(new JPanel(new GridLayout(4, 1)), components).getComponent();
                final var localePanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var localeLabel = new DarkComponent<>(new JLabel(locale.get(StringID.SETTINGS_CHOOSE_LANG) + ":"), components).getComponent();

                    final var localeBox = new DarkComponent<>(new JComboBox<Locale>(), components).getComponent();
                localePanel.add(localeLabel);
                localePanel.add(localeBox);
                localePanel.setBorder(new EtchedBorder());

                final var urlPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var urlLabel = new DarkComponent<>(new JLabel(locale.get(StringID.SETTINGS_JSON_URI_DESC) + ":"), components).getComponent();

                    urlField = new DarkTextComponent<>(new HintTextField("https://www.example.org/infos.json"), components).getComponent();
                urlPanel.add(urlLabel);
                urlPanel.add(urlField);
                urlPanel.setBorder(new EtchedBorder());

                final var folderPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var folderDescription = new DarkComponent<>(new JLabel(locale.get(StringID.SETTINGS_SONG_INFO_FOLDER_DESC) + ":"), components).getComponent();

                    final var folderChangePanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                        folderChangeLabel = new DarkComponent<>(new JLabel(), components).getComponent();
                        folderChangeLabel.setFont(folderChangeLabel.getFont().deriveFont(Font.BOLD));

                        final var folderChangeButton = new JButton(locale.get(StringID.SETTINGS_CHANGE) + "...");
                    folderChangePanel.add(folderChangeLabel, BorderLayout.CENTER);
                    folderChangePanel.add(folderChangeButton, BorderLayout.EAST);
                folderPanel.add(folderDescription);
                folderPanel.add(folderChangePanel);
                folderPanel.setBorder(new EtchedBorder());

                final var delayPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var delayLabel = new DarkComponent<>(new JLabel(locale.get(StringID.SETTINGS_SONG_REFRESH_RATE) + ":"), components).getComponent();

                    final var delaySpinner = new DarkComponent<>(new JSpinner(), components).getComponent();
                delayPanel.add(delayLabel);
                delayPanel.add(delaySpinner);
                delayPanel.setBorder(new EtchedBorder());
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
        darkBox.addItemListener(__ -> settings.setDarkMode(darkBox.isSelected()));
        darkBox.setSelected(settings.getDarkMode());

        localeBox.addItem(locale instanceof English ? locale : new English());
        localeBox.addItem(locale instanceof German ? locale : new German());
        localeBox.setSelectedItem(locale);
        localeBox.addItemListener(this::onLocaleChanged);

        urlField.setText(settings.getURL());

        delaySpinner.setValue(settings.getDelay());
        delaySpinner.addChangeListener(__ -> settings.setDelay((Integer) delaySpinner.getValue()));

        folderChangeLabel.setText(settings.getSavePath());
        folderChangeButton.addActionListener(__ -> chooseSaveFolder());

        deleteButton.addActionListener(__ -> removeSettings());

        settings.addDarkModeListener(this);
        darkModeToggled(settings.getDarkMode());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    private Optional<JPanel> getScriptSupportPanel() {
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) return Optional.empty();

        final var scriptSupportPanel = new DarkComponent<>(new JPanel(), components).getComponent();
        // FIXME: Translations
        scriptSupportPanel.setBorder(new TitledBorder("AppleScript support"));
            final var scriptSupportButtonPanel = new DarkComponent<>(new JPanel(new GridLayout(3, 1)), components).getComponent();
                final var scriptSupportOff = new DarkComponent<>(new JRadioButton("Off"), components).getComponent();

                final var scriptSupportMixed = new DarkComponent<>(new JRadioButton("On"), components).getComponent();

                final var scriptSupportOnly = new DarkComponent<>(new JRadioButton("Only"), components).getComponent();
            scriptSupportButtonPanel.add(scriptSupportOff);
            scriptSupportButtonPanel.add(scriptSupportMixed);
            scriptSupportButtonPanel.add(scriptSupportOnly);
        scriptSupportPanel.add(scriptSupportButtonPanel);
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
        for (final var component : components) {
            component.setDark(dark);
        }
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
