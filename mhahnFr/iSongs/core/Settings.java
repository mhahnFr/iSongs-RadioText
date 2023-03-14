package iSongs.core;

import iSongs.iSongs;

import java.util.prefs.Preferences;

public class Settings {
    private static Settings instance;
    private final Preferences preferences = Preferences.userNodeForPackage(iSongs.class);

    private Settings() {}

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public static abstract class Key {
        private static final String BUNDLE_ID = Constants.NAME;

        public static final String WINDOW_HEIGHT = BUNDLE_ID + ".windowHeight";
        public static final String WINDOW_WIDTH  = BUNDLE_ID + ".windowWidth";
        public static final String WINDOW_X      = BUNDLE_ID + ".windowX";
        public static final String WINDOW_Y      = BUNDLE_ID + ".windowY";
    }
}
