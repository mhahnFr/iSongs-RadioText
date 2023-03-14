package iSongs;

import iSongs.gui.MainWindow;

import java.awt.EventQueue;

public class iSongs {
    public static void main(String[] args) {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.awt.application.appearance", "system");
        }

        EventQueue.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
