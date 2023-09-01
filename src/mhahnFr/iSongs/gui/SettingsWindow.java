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

package mhahnFr.iSongs.gui;

import mhahnFr.iSongs.core.Constants;
import mhahnFr.iSongs.core.Settings;
import mhahnFr.iSongs.core.locale.English;
import mhahnFr.iSongs.core.locale.German;
import mhahnFr.iSongs.core.locale.Locale;
import mhahnFr.utils.gui.components.DarkComponent;
import mhahnFr.utils.gui.DarkModeListener;
import mhahnFr.utils.gui.components.DarkTextComponent;
import mhahnFr.utils.gui.components.HintTextField;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Constructs this settings window using the given owner.
     * This window is modal.
     *
     * @param owner the owner
     */
    public SettingsWindow(final JFrame owner) {
        super(owner, Constants.NAME + ": Einstellungen", true);

        final var panel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
            final var darkBox = new DarkComponent<>(new JCheckBox("Dunkelmodus aktivieren"), components).getComponent();

            final var centerPanel = new DarkComponent<>(new JPanel(new GridLayout(4, 1)), components).getComponent();
                final var localePanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var localeLabel = new DarkComponent<>(new JLabel("Sprache wählen:"), components).getComponent();

                    final var localeBox = new DarkComponent<>(new JComboBox<Locale>(), components).getComponent();
                localePanel.add(localeLabel);
                localePanel.add(localeBox);
                localePanel.setBorder(new EtchedBorder());

                final var urlPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var urlLabel = new DarkComponent<>(new JLabel("Die URL zur Datei mit den aktuellen Titelinformationen:"), components).getComponent();

                    urlField = new DarkTextComponent<>(new HintTextField("https://www.example.org/infos.json"), components).getComponent();
                urlPanel.add(urlLabel);
                urlPanel.add(urlField);
                urlPanel.setBorder(new EtchedBorder());

                final var folderPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var folderDescription = new DarkComponent<>(new JLabel("Der Ordner, in den die Titelinfos gespeichert werden sollen:"), components).getComponent();

                    final var folderChangePanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                        folderChangeLabel = new DarkComponent<>(new JLabel(), components).getComponent();
                        folderChangeLabel.setFont(folderChangeLabel.getFont().deriveFont(Font.BOLD));

                        final var folderChangeButton = new JButton("Ändern...");
                    folderChangePanel.add(folderChangeLabel, BorderLayout.CENTER);
                    folderChangePanel.add(folderChangeButton, BorderLayout.EAST);
                folderPanel.add(folderDescription);
                folderPanel.add(folderChangePanel);
                folderPanel.setBorder(new EtchedBorder());

                final var delayPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var delayLabel = new DarkComponent<>(new JLabel("Intervall zwischen den Titelabfragen (in Millisekunden):"), components).getComponent();

                    final var delaySpinner = new DarkComponent<>(new JSpinner(), components).getComponent();
                delayPanel.add(delayLabel);
                delayPanel.add(delaySpinner);
                delayPanel.setBorder(new EtchedBorder());
            centerPanel.add(localePanel);
            centerPanel.add(urlPanel);
            centerPanel.add(folderPanel);
            centerPanel.add(delayPanel);

            final var deleteButton = new JButton("Einstellungen löschen");
        panel.add(darkBox,      BorderLayout.NORTH);
        panel.add(centerPanel,  BorderLayout.CENTER);
        panel.add(deleteButton, BorderLayout.SOUTH);

        getContentPane().add(panel);

        final var settings = Settings.getInstance();
        darkBox.addItemListener(__ -> settings.setDarkMode(darkBox.isSelected()));
        darkBox.setSelected(settings.getDarkMode());

        localeBox.addItem(new English());
        localeBox.addItem(new German());
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

    private void onLocaleChanged(final ItemEvent event) {
        if (event.getStateChange() != ItemEvent.SELECTED) return;

        Settings.getInstance().setLocale((Locale) event.getItem());
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
                """
                         Sollen die Einstellungen wirklich gelöscht werden?
                         Diese Aktion ist nicht widerruflich!
                         Das Programm wird anschließend beendet.
                         """,
                Constants.NAME + ": Einstellungen",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            if (!Settings.getInstance().remove() || !Settings.getInstance().flush()) {
                JOptionPane.showMessageDialog(this,
                        "Fehler beim Löschen der Einstellungen aufgetreten!",
                        Constants.NAME + ": Einstellungen",
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
                    "Konnte Einstellungen nicht sichern!",
                    Constants.NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
        super.dispose();
    }
}
