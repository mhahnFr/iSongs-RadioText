package iSongs;

import iSongs.gui.MainWindow;

import java.awt.EventQueue;

public class iSongs {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
