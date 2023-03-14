package iSongs.gui;

import mhahnFr.utils.gui.DarkComponent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
    private final List<DarkComponent<? extends JComponent>> components = new ArrayList<>();

    public MainWindow() {
//        super(Constants.NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        final var hasSettings = false;// Settings.getInstance().hasSettings();

        final var panel = new DarkComponent<>(new JPanel(new GridLayout(4, 1)), components).getComponent();
            final var label = new DarkComponent<>(new JLabel(" Aktueller Titel:"), components).getComponent();

            final var titleLabel = new DarkComponent<>(new JLabel("Laden...", SwingConstants.CENTER), components).getComponent();
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

            final var interpreter = new DarkComponent<>(new JLabel("Laden...", SwingConstants.CENTER), components).getComponent();

            final var saveButton = new JButton("Titel merken");
            final JComponent toAdd;
            if (hasSettings) {
                toAdd = saveButton;
            } else {
                toAdd = new DarkComponent<>(new JPanel(), components).getComponent();
                toAdd.setLayout(new BoxLayout(toAdd, BoxLayout.X_AXIS));
                    final var settingsButton = new JButton("Einstellungen");
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

    private void restoreBounds() {
        // TODO
        pack();
    }
}
