/*
 * iSongs-RadioText - Radio-text part of iSongs.
 *
 * Copyright (C) 2023 - 2025  mhahnFr
 *
 * This file is part of the iSongs-RadioText.
 *
 * iSongs-RadioText is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * iSongs-RadioText is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * iSongs-RadioText, see the file LICENSE.  If not, see <https://www.gnu.org/licenses/>.
 */

package mhahnFr.iSongs.core;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import mhahnFr.NDL.DarkModeCallback;
import mhahnFr.NDL.NDL;
import mhahnFr.iSongs.core.appleScript.ScriptSupport;
import mhahnFr.iSongs.core.locale.LanguageListener;
import mhahnFr.iSongs.core.locale.Locale;
import mhahnFr.iSongs.iSongs;
import mhahnFr.utils.gui.DarkModeListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * This class contains the settings singleton.
 *
 * @author mhahnFr
 * @since 14.03.23
 */
public class Settings implements DarkModeCallback {
    /** The one and only instance of this class.              */
    private static Settings instance;
    /** The underlying {@link Preferences}.                   */
    private final Preferences preferences = Preferences.userNodeForPackage(iSongs.class);
    /** A list with the registered {@link DarkModeListener}s. */
    private final List<DarkModeListener> listeners = new ArrayList<>();
    /** A list with the registered {@link LanguageListener}s. */
    private final List<LanguageListener> languageListeners = new ArrayList<>();

    /** The cached locale.                                    */
    private Locale locale = null;

    /**
     * The default constructor. Can only be used internally.
     */
    private Settings() {
        NDL.ifAvailable(() -> NDL.registerCallback(this));
    }

    /**
     * Returns the height of the window. If no data was set,
     * {@code -1} is returned.
     *
     * @return the height of the window
     */
    public int getWindowHeight() {
        return preferences.getInt(Key.WINDOW_HEIGHT, -1);
    }

    /**
     * Returns the width of the window. If no data was set,
     * {@code -1} is returned.
     *
     * @return the width of the window
     */
    public int getWindowWidth() {
        return preferences.getInt(Key.WINDOW_WIDTH, -1);
    }

    /**
     * Returns the X-coordinate of the window's location. If
     * no data was set, {@code -1} is returned.
     *
     * @return the X-coordinate of the window's location
     */
    public int getWindowX() {
        return preferences.getInt(Key.WINDOW_X, Integer.MIN_VALUE);
    }

    /**
     * Returns the Y-coordinate of the window's location. If
     * no data was set, {@code -1} is returned.
     *
     * @return the Y-coordinate of the window's location
     */
    public int getWindowY() {
        return preferences.getInt(Key.WINDOW_Y, Integer.MIN_VALUE);
    }

    /**
     * Returns whether the dark mode is activated.
     *
     * @return the state of the activation of the dark mode
     */
    public DarkMode getDarkMode() {
        return DarkMode.createDarkMode(preferences.getInt(Key.DARK_MODE, NDL.couldLoad() ? DarkMode.AUTO.ordinal()
                                                                                         : DarkMode.LIGHT.ordinal()));
    }

    @Override
    public void darkModeChanged() {
        if (getDarkMode() != DarkMode.AUTO) return;

        EventQueue.invokeLater(() -> {
            final var dark = getRenderDarkMode();
            setupLaf(dark);
            for (final var listener : listeners) {
                listener.darkModeToggled(dark);
            }
        });
    }

    /**
     * Returns whether the dark mode should be used for rendering.
     *
     * @return whether to render dark
     */
    public boolean getRenderDarkMode() {
        switch (getDarkMode()) {
            case LIGHT -> { return false; }
            case DARK  -> { return true;  }
            case AUTO  -> {
                return NDL.couldLoad() && NDL.queryDarkMode();
            }
        }
        return false;
    }

    /**
     * Returns the delay between fetching the song information.
     *
     * @return the delay
     */
    public long getDelay() {
        return preferences.getLong(Key.DELAY, 1000);
    }

    /**
     * Returns the URL to the song information. If no data
     * was set, an empty {@link String} is returned.
     *
     * @return the URL
     */
    public String getURL() {
        return preferences.get(Key.URL, "");
    }

    /**
     * Returns the path to the folder where the song information
     * should be stored. If no data was set, an empty {@link String}
     * is returned.
     *
     * @return the path to the save folder
     */
    public String getSavePath() {
        return preferences.get(Key.PATH, "");
    }

    /**
     * Returns the lastly stored locale.
     *
     * @return the locale to be used
     */
    public Locale getLocale() {
        if (locale == null) {
            locale = Locale.fromName(preferences.get(Key.LOCALE, ""));
        }
        return locale;
    }

    /**
     * Returns the setting for the script support.
     *
     * @return the currently set script support level
     */
    public ScriptSupport getScriptSupport() {
        return ScriptSupport.valueOf(preferences.get(Key.APPLE_SCRIPT, ScriptSupport.off.name()));
    }

    /**
     * Returns whether to display 'No song recognized'.
     *
     * @return whether to display no song recognized
     */
    public boolean getNoSong() {
        return preferences.getInt(Key.NO_SONG, 1) == 1;
    }

    /**
     * Sets whether to display 'No song recognized'.
     *
     * @param noSong whether to display no song recognized
     */
    public void setNoSong(final boolean noSong) {
        preferences.putInt(Key.NO_SONG, noSong ? 1 : 0);
    }

    /**
     * Sets the script support level.
     *
     * @param support the new script support level
     */
    public void setScriptSupport(final ScriptSupport support) {
        preferences.put(Key.APPLE_SCRIPT, support.name());
    }

    /**
     * Sets the given locale as the new locale for the app.
     * Calls the registered language listeners with the given locale.
     *
     * @param locale the new locale to be used
     */
    public void setLocale(final Locale locale) {
        preferences.put(Key.LOCALE, locale.getName());
        if (this.locale != locale) {
            languageListeners.forEach(listener -> listener.languageChanged(locale));
        }
        this.locale = locale;
    }

    /**
     * Sets the path to the folder where to save the song information.
     *
     * @param path the new path
     */
    public void setSavePath(final String path) {
        preferences.put(Key.PATH, path);
    }

    /**
     * Sets the URL to the song information.
     *
     * @param url the new URL
     * @return this instance
     */
    public Settings setURL(final String url) {
        preferences.put(Key.URL, url);
        return this;
    }

    /**
     * Sets the delay between the fetching of the song information.
     *
     * @param delay the new delay
     */
    public void setDelay(final long delay) {
        preferences.putLong(Key.DELAY, delay);
    }

    /**
     * Sets whether the dark mode is activated. All registered
     * {@link DarkModeListener}s are called with the new value.
     *
     * @param dark whether the dark mode is active
     * @see #addDarkModeListener(DarkModeListener)
     * @see #removeDarkModeListener(DarkModeListener)
     * @see DarkModeListener
     */
    public void setDarkMode(final DarkMode dark) {
        final var old = getDarkMode();

        preferences.putInt(Key.DARK_MODE, dark.ordinal());

        if (old != dark) {
            final var actuallyDark = getRenderDarkMode();
            setupLaf(actuallyDark);
            for (final var listener : listeners) {
                listener.darkModeToggled(actuallyDark);
            }
        }
    }

    /**
     * Sets the height of the window.
     *
     * @param height the new height
     * @return this instance
     */
    public Settings setWindowHeight(final int height) {
        preferences.putInt(Key.WINDOW_HEIGHT, height);
        return this;
    }

    /**
     * Sets the width of the window.
     *
     * @param width the new width
     * @return this instance
     */
    public Settings setWindowWidth(final int width) {
        preferences.putInt(Key.WINDOW_WIDTH, width);
        return this;
    }

    /**
     * Sets the X-coordinate of the window's location.
     *
     * @param x the new X-coordinate
     * @return this instance
     */
    public Settings setWindowX(final int x) {
        preferences.putInt(Key.WINDOW_X, x);
        return this;
    }

    /**
     * Sets the Y-coordinate of the window's location.
     *
     * @param y the new Y-coordinate
     * @return this instance
     */
    public Settings setWindowY(final int y) {
        preferences.putInt(Key.WINDOW_Y, y);
        return this;
    }

    /**
     * Flushes the settings. Returns whether that operation
     * was successful.
     *
     * @return whether the action was successful
     */
    public boolean flush() {
        try {
            preferences.flush();
        } catch (Exception _) {
            return false;
        }
        return true;
    }

    /**
     * Removes the settings. Returns whether the action was
     * successful. In order to take full effect, {@link #flush()}
     * should be called.
     *
     * @return whether the action was successful
     * @see #flush()
     */
    public boolean remove() {
        try {
            preferences.removeNode();
        } catch (Exception _) {
            return false;
        }
        return true;
    }

    /**
     * Adds the given {@link DarkModeListener}.
     *
     * @param listener the listener to be added
     */
    public void addDarkModeListener(final DarkModeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the given {@link DarkModeListener}.
     *
     * @param listener the listener to be removed
     */
    public void removeDarkModeListener(final DarkModeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Registers the given {@link LanguageListener}.
     *
     * @param listener the listener to be registered
     */
    public void addLanguageListener(final LanguageListener listener) {
        languageListeners.add(listener);
    }

    /**
     * Removes the given {@link LanguageListener}.
     *
     * @param listener the listener to be removed
     */
    public void removeLanguageListeners(final LanguageListener listener) {
        languageListeners.remove(listener);
    }

    /** Indicates whether the current OS is a version of macOS. */
    public static final boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

    /**
     * Activates the appropriate L&F. Uses a dark L&F if requested.
     *
     * @param dark whether to use a dark L&F
     */
    public static void setupLaf(final boolean dark) {
        if (dark) {
            if (isMac) FlatMacDarkLaf.setup();
            else       FlatDarkLaf.setup();
        } else {
            if (isMac) FlatMacLightLaf.setup();
            else       FlatLightLaf.setup();
        }
    }

    /**
     * Returns the one and only instance of this class.
     * When this method is called for the first time, the
     * singleton is created.
     *
     * @return the one and only {@link Settings} instance
     */
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    /**
     * This class contains the keys used to identify the settings.
     *
     * @author mhahnFr
     * @since 14.03.23
     */
    public static abstract class Key {
        /** The bundle identifier. */
        private static final String BUNDLE_ID = Constants.NAME;

        /** The key for the window height.         */
        public static final String WINDOW_HEIGHT = BUNDLE_ID + ".windowHeight";
        /** The key for the window width.          */
        public static final String WINDOW_WIDTH  = BUNDLE_ID + ".windowWidth";
        /** The key for the window's X-coordinate. */
        public static final String WINDOW_X      = BUNDLE_ID + ".windowX";
        /** The key for the window's Y-coordinate. */
        public static final String WINDOW_Y      = BUNDLE_ID + ".windowY";
        /** The key for the dark mode.             */
        public static final String DARK_MODE     = BUNDLE_ID + ".darkMode";
        /** The key for the delay.                 */
        public static final String DELAY         = BUNDLE_ID + ".delay";
        /** The key or the URL.                    */
        public static final String URL           = BUNDLE_ID + ".url";
        /** The key for the path.                  */
        public static final String PATH          = BUNDLE_ID + ".path";
        /** The key for the locale to be used.     */
        public static final String LOCALE        = BUNDLE_ID + ".locale";
        /** The key for the script support.        */
        public static final String APPLE_SCRIPT  = BUNDLE_ID + ".scriptSupport";
        /** The key for the no song display.       */
        public static final String NO_SONG       = BUNDLE_ID + ".noSong";
    }
}
