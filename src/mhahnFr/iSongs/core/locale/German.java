/*
 * iSongs-RadioText - Radio-text part of iSongs.
 *
 * Copyright (C) 2023 - 2024  mhahnFr
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

package mhahnFr.iSongs.core.locale;

/**
 * This class represents the german translation of this app.
 *
 * @author mhahnFr
 * @since 01.09.23
 */
public class German extends Locale {
    /** The localized name of the language. */
    public static final String name = "Deutsch";

    @Override
    public String get(final StringID id) {
        return switch (id) {
            case MAIN_CURRENT_TITLE       -> "Aktueller Titel";
            case MAIN_SAVE_TITLE          -> "Titel merken";
            case MAIN_SHOW_ERROR          -> "Fehler anzeigen";
            case MAIN_SETTINGS            -> "Einstellungen";
            case MAIN_NO_SONG             -> "Kein Titel";
            case MAIN_NO_INTERPRETER      -> "Kein Interpret";
            case MAIN_STORED              -> "gesichert";
            case MAIN_SAVE_ERROR          -> "Titel konnte nicht gesichert werden! Einstellungen überprüfen!";
            case MAIN_ERROR               -> "Fehler";
            case MAIN_NO_ERROR            -> "Kein Fehler aufgetreten";
            case MAIN_UI_STATE_SAVE_ERROR -> "Konnte UI-State nicht speichern";
            case MAIN_UI_CHECK_SETTINGS   -> "Bitte Einstellungen überprüfen!";
            case MAIN_ERROR_HAPPENED      -> "Fehler aufgetreten";

            case SETTINGS_CHOOSE_THEME          -> "Design wählen";
            case SETTINGS_CHOOSE_LANG           -> "Sprache wählen";
            case SETTINGS_JSON_URI_DESC         -> "Die URL zur Datei mit den aktuellen Titelinformationen";
            case SETTINGS_SONG_INFO_FOLDER_DESC -> "Der Ordner, in dem die Titelinfos gespeichert werden sollen";
            case SETTINGS_CHANGE                -> "Ändern";
            case SETTINGS_SONG_REFRESH_RATE     -> "Intervall zwischen den Titelabfragen (in Millisekunden)";
            case SETTINGS_REMOVE                -> "Einstellungen löschen";
            case SETTINGS_REMOVE_ERROR          -> "Fehler beim Löschen der Einstellungen aufgetreten";
            case SETTINGS_SAVE_ERROR            -> "Konnte Einstellungen nicht sichern";
            case SETTINGS_LANGUAGE_RESTART      -> "Anwendung neu starten zur Anwendung der Sprachänderung";
            case SETTINGS_APPLESCRIPT_DESC      -> "Apple Event basierte Titelerkennung";
            case SETTINGS_APPLESCRIPT_OFF       -> "Deaktiviert";
            case SETTINGS_APPLESCRIPT_ON        -> "Aktiviert";
            case SETTINGS_APPLESCRIPT_ONLY      -> "Alleinig (Internet basierte Erkennung deaktiviert)";
            case SETTINGS_ALLOW_NO_SONG         -> "Kein Titel erkannt anzeigen";
            case SETTINGS_REMOVE_REALLY         -> """
                                                   Sollen die Einstellungen wirklich gelöscht werden?
                                                   Diese Aktion ist nicht widerruflich!
                                                   Das Programm wird anschließend beendet.
                                                   """;

            case DARK_MODE_LIGHT -> "Hell";
            case DARK_MODE_DARK  -> "Dunkel";
            case DARK_MODE_AUTO  -> "Automatisch";

            case INTERNAL_NO_TRACK_RECOGNIZED -> "Kein Titel erkannt!";
            case INTERNAL_SAVE_FOLDER_UNSET   -> "Titelinfo-Ordner nicht gesetzt!";
        };
    }

    @Override
    public String getName() {
        return name;
    }
}
