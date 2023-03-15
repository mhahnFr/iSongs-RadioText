package iSongs.gui;

import iSongs.core.Constants;
import iSongs.core.Settings;
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkModeListener;
import mhahnFr.utils.gui.DarkTextComponent;
import mhahnFr.utils.gui.HintTextField;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

public class SettingsWindow extends JDialog implements DarkModeListener {
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    private final JLabel folderChangeLabel;
    private final JSpinner delaySpinner;
    private final JTextField urlField;

    public SettingsWindow(final JFrame owner) {
        super(owner, Constants.NAME + ": Einstellungen", true);

        final var panel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
            final var darkBox = new DarkComponent<>(new JCheckBox("Dark"), components).getComponent();

            final var centerPanel = new DarkComponent<>(new JPanel(new GridLayout(3, 1)), components).getComponent();
                final var urlPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var urlLabel = new DarkComponent<>(new JLabel("URL:"), components).getComponent();

                    urlField = new DarkTextComponent<>(new HintTextField("https://www.example.org/infos.json"), components).getComponent();
                urlPanel.add(urlLabel);
                urlPanel.add(urlField);
                urlPanel.setBorder(new EtchedBorder());

                final var folderPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var folderDescription = new DarkComponent<>(new JLabel("Folder:"), components).getComponent();

                    final var folderChangePanel = new DarkComponent<>(new JPanel(new BorderLayout()), components).getComponent();
                        folderChangeLabel = new DarkComponent<>(new JLabel(), components).getComponent();
                        folderChangeLabel.setFont(folderChangeLabel.getFont().deriveFont(Font.BOLD));

                        final var folderChangeButton = new JButton("Change");
                    folderChangePanel.add(folderChangeLabel, BorderLayout.CENTER);
                    folderChangePanel.add(folderChangeButton, BorderLayout.EAST);
                folderPanel.add(folderDescription);
                folderPanel.add(folderChangePanel);
                folderPanel.setBorder(new EtchedBorder());

                final var delayPanel = new DarkComponent<>(new JPanel(new GridLayout(2, 1)), components).getComponent();
                    final var delayLabel = new DarkComponent<>(new JLabel("Delay:"), components).getComponent();

                    delaySpinner = new DarkComponent<>(new JSpinner(), components).getComponent();
                delayPanel.add(delayLabel);
                delayPanel.add(delaySpinner);
                delayPanel.setBorder(new EtchedBorder());
            centerPanel.add(urlPanel);
            centerPanel.add(folderPanel);
            centerPanel.add(delayPanel);

            final var deleteButton = new JButton("Delete");
        panel.add(darkBox,      BorderLayout.NORTH);
        panel.add(centerPanel,  BorderLayout.CENTER);
        panel.add(deleteButton, BorderLayout.SOUTH);

        getContentPane().add(panel);

        final var settings = Settings.getInstance();
        darkBox.addItemListener(__ -> settings.setDarkMode(darkBox.isSelected()));
        darkBox.setSelected(settings.getDarkMode());

        urlField.setText(settings.getURL());

        delaySpinner.setValue(settings.getDelay());
        delaySpinner.addChangeListener(__ -> settings.setDelay((Long) delaySpinner.getValue()));

        folderChangeLabel.setText(settings.getSavePath());
        folderChangeButton.addActionListener(__ -> chooseSaveFolder());

        deleteButton.addActionListener(__ -> removeSettings());

        settings.addDarkModeListener(this);
        darkModeToggled(settings.getDarkMode());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

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

    private void removeSettings() {
        if (JOptionPane.showConfirmDialog(this,
                "Einstellungen löschen?!",
                Constants.NAME,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            if (!Settings.getInstance().remove() || !Settings.getInstance().flush()) {
                JOptionPane.showMessageDialog(this,
                        "Fehler!",
                        Constants.NAME,
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
        if (!settings.setDelay((Long) delaySpinner.getValue())
                     .setURL(urlField.getText())
                     .flush()) {
            JOptionPane.showMessageDialog(this,
                    "Konnte Einstellungen nicht sichern!",
                    Constants.NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
        super.dispose();
    }
}
