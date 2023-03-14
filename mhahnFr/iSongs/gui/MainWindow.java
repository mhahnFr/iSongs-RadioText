package iSongs.gui;

import iSongs.core.Constants;
import iSongs.core.Settings;
import mhahnFr.utils.gui.DarkComponent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();

    public MainWindow() {
        super(Constants.NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final var panel = new DarkComponent<>(new JPanel(new GridLayout(4, 1)), components).getComponent();
            final var label = new DarkComponent<>(new JLabel(" Aktueller Titel:"), components).getComponent();

            final var titleLabel = new DarkComponent<>(new JLabel("Laden...", SwingConstants.CENTER), components).getComponent();
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

            final var interpreter = new DarkComponent<>(new JLabel("Laden...", SwingConstants.CENTER), components).getComponent();

            final var saveButton = new JButton("Titel merken");
            saveButton.addActionListener(__ -> saveTitle());
            final JComponent toAdd;
            if (hasSettings()) {
                addSettingsHook();
                toAdd = saveButton;
            } else {
                toAdd = new DarkComponent<>(new JPanel(), components).getComponent();
                toAdd.setLayout(new BoxLayout(toAdd, BoxLayout.X_AXIS));
                    final var settingsButton = new JButton("Einstellungen");
                    settingsButton.addActionListener(__ -> showSettings());
                toAdd.add(settingsButton);
                toAdd.add(saveButton);
            }
        panel.add(label);
        panel.add(titleLabel);
        panel.add(interpreter);
        panel.add(toAdd);
        getContentPane().add(panel);

        restoreBounds();
    }

    private void addSettingsHook() {
        Desktop.getDesktop().setPreferencesHandler(__ -> showSettings());
    }

    private boolean hasSettings() {
        return Desktop.getDesktop().isSupported(Desktop.Action.APP_PREFERENCES);
    }

    private void showSettings() {
        final var settingsWindow = new SettingsWindow(this);
        settingsWindow.setVisible(true);
    }

    private void saveTitle() {
        // TODO
    }

    private void restoreBounds() {
        final var settings = Settings.getInstance();

        final int x      = settings.getWindowX(),
                  y      = settings.getWindowY(),
                  width  = settings.getWindowWidth(),
                  height = settings.getWindowHeight();

        if (width < 0 || height < 0) {
            pack();
        }
        if (x < 0 || y < 0) {
            setLocationRelativeTo(null);
        }
    }

    @Override
    public void dispose() {
        if (!Settings.getInstance().setWindowX(getX())
                                   .setWindowY(getY())
                                   .setWindowWidth(getWidth())
                                   .setWindowHeight(getHeight())
                                   .flush()) {
            JOptionPane.showMessageDialog(this,
                    "Konnte UI-State nicht speichern!",
                    Constants.NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
        super.dispose();
    }
}
