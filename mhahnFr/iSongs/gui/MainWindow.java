package iSongs.gui;

import iSongs.core.Constants;
import iSongs.core.InfoLoader;
import iSongs.core.Settings;
import mhahnFr.utils.Pair;
import mhahnFr.utils.gui.DarkComponent;
import mhahnFr.utils.gui.DarkModeListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame implements DarkModeListener {
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();
    private final InfoLoader loader = new InfoLoader(this::updateUI, this::writeCallback);
    private final Timer savedTimer = new Timer(5000, __ -> setTitle(Constants.NAME));
    private final JLabel titleLabel;
    private final JLabel interpreterLabel;
    private final JButton saveButton;

    public MainWindow() {
        super(Constants.NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final var panel = new DarkComponent<>(new JPanel(new GridLayout(4, 1)), components).getComponent();
            final var label = new DarkComponent<>(new JLabel(" Aktueller Titel:"), components).getComponent();

            titleLabel = new DarkComponent<>(new JLabel("Laden...", SwingConstants.CENTER), components).getComponent();
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

            interpreterLabel = new DarkComponent<>(new JLabel("Laden...", SwingConstants.CENTER), components).getComponent();

            final var toAdd = new DarkComponent<>(new JPanel(), components).getComponent();
                saveButton = new JButton("Titel merken");
                saveButton.addActionListener(__ -> saveTitle());

                if (hasSettings()) {
                    toAdd.setLayout(new FlowLayout());
                    addSettingsHook();
                } else {
                    toAdd.setLayout(new BoxLayout(toAdd, BoxLayout.X_AXIS));
                    final var settingsButton = new JButton("Einstellungen");
                    settingsButton.addActionListener(__ -> showSettings());
                    toAdd.add(settingsButton);
                }
                toAdd.add(saveButton);
        panel.add(label);
        panel.add(titleLabel);
        panel.add(interpreterLabel);
        panel.add(toAdd);
        getContentPane().add(panel);

        maybeAddQuitHandler();
        restoreBounds();

        savedTimer.setRepeats(false);

        Settings.getInstance().addDarkModeListener(this);
        darkModeToggled(Settings.getInstance().getDarkMode());
        loader.start();
    }

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

    private void updateUI() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(this::updateUI);
            return;
        }
        if (loader.hasTrack()) {
            final var song = loader.getCurrentSong();
            titleLabel.setText(song.getFirst());
            interpreterLabel.setText(song.getSecond());
            saveButton.setEnabled(true);
        } else {
            titleLabel.setText("Kein Titel");
            interpreterLabel.setText("Kein Interpret");
            saveButton.setEnabled(false);
        }
    }

    private void writeCallback(final Pair<String, String> song,
                               final Exception            error) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(() -> writeCallback(song, error));
            return;
        }
        if (error == null) {
            setTitle("\"" + song.getFirst() + "\" gesichert");
            saveButton.setEnabled(false);
        } else {
            setTitle("Titel konnte nicht gesichert werden! Einstellungen überprüfen!");
        }
        savedTimer.restart();
    }

    private void addSettingsHook() {
        Desktop.getDesktop().setPreferencesHandler(__ -> showSettings());
    }

    private boolean hasSettings() {
        return Desktop.getDesktop().isSupported(Desktop.Action.APP_PREFERENCES);
    }

    private void showSettings() {
        loader.stop();
        final var settingsWindow = new SettingsWindow(this);
        settingsWindow.setLocationRelativeTo(this);
        settingsWindow.setVisible(true);
        loader.start();
    }

    private void saveTitle() {
        loader.saveSong();
    }

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

    private void saveSettings() {
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
    }

    @Override
    public void dispose() {
        Settings.getInstance().removeDarkModeListener(this);
        saveSettings();
        super.dispose();
    }
}
