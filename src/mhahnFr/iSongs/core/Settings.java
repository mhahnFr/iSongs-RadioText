package mhahnFr.iSongs.core;

import mhahnFr.iSongs.iSongs;
import mhahnFr.utils.gui.DarkModeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class Settings {
    private static Settings instance;
    private final Preferences preferences = Preferences.userNodeForPackage(iSongs.class);
    private final List<DarkModeListener> listeners = new ArrayList<>();

    private Settings() {}

    public int getWindowHeight() {
        return preferences.getInt(Key.WINDOW_HEIGHT, -1);
    }

    public int getWindowWidth() {
        return preferences.getInt(Key.WINDOW_WIDTH, -1);
    }

    public int getWindowX() {
        return preferences.getInt(Key.WINDOW_X, -1);
    }

    public int getWindowY() {
        return preferences.getInt(Key.WINDOW_Y, -1);
    }

    public boolean getDarkMode() {
        return preferences.getInt(Key.DARK_MODE, 0) == 1;
    }

    public long getDelay() {
        return preferences.getLong(Key.DELAY, 1000);
    }

    public String getURL() {
        return preferences.get(Key.URL, "");
    }

    public String getSavePath() {
        return preferences.get(Key.PATH, "");
    }

    public void setSavePath(final String path) {
        preferences.put(Key.PATH, path);
    }

    public Settings setURL(final String url) {
        preferences.put(Key.URL, url);
        return this;
    }

    public void setDelay(final long delay) {
        preferences.putLong(Key.DELAY, delay);
    }

    public void setDarkMode(final boolean dark) {
        preferences.putInt(Key.DARK_MODE, dark ? 1 : 0);
        for (final var listener : listeners) {
            listener.darkModeToggled(dark);
        }
    }

    public Settings setWindowHeight(final int height) {
        preferences.putInt(Key.WINDOW_HEIGHT, height);
        return this;
    }

    public Settings setWindowWidth(final int width) {
        preferences.putInt(Key.WINDOW_WIDTH, width);
        return this;
    }

    public Settings setWindowX(final int x) {
        preferences.putInt(Key.WINDOW_X, x);
        return this;
    }
    public Settings setWindowY(final int y) {
        preferences.putInt(Key.WINDOW_Y, y);
        return this;
    }

    public boolean flush() {
        try {
            preferences.flush();
        } catch (Exception __) {
            return false;
        }
        return true;
    }

    public boolean remove() {
        try {
            preferences.removeNode();
        } catch (Exception __) {
            return false;
        }
        return true;
    }

    public void addDarkModeListener(final DarkModeListener listener) {
        listeners.add(listener);
    }

    public void removeDarkModeListener(final DarkModeListener listener) {
        listeners.remove(listener);
    }

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
        public static final String DARK_MODE     = BUNDLE_ID + ".darkMode";
        public static final String DELAY         = BUNDLE_ID + ".delay";
        public static final String URL           = BUNDLE_ID + ".url";
        public static final String PATH          = BUNDLE_ID + ".path";
    }
}
