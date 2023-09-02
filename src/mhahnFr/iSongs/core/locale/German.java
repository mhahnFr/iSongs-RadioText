/*
 * iSongs-RadioText - Radio-text part of iSongs.
 *
 * Copyright (C) 2023  mhahnFr
 *
 * This file is part of the iSongs-RadioText. This program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program, see the file LICENSE.  If not, see <https://www.gnu.org/licenses/>.
 */

package mhahnFr.iSongs.core.locale;

public class German extends Locale {
    public static final String name = "German";

    @Override
    public String get(final StringID id) {
        switch (id) {
            case MAIN_CURRENT_TITLE       -> { return "Aktueller Titel";                                                }
            case MAIN_LOADING             -> { return "Laden";                                                          }
            case MAIN_SAVE_TITLE          -> { return "Titel merken";                                                   }
            case MAIN_SHOW_ERROR          -> { return "Fehler anzeigen";                                                }
            case MAIN_SETTINGS            -> { return "Einstellungen";                                                  }
            case MAIN_NO_SONG             -> { return "Kein Titel";                                                     }
            case MAIN_NO_INTERPRETER      -> { return "Kein Interpret";                                                 }
            case MAIN_STORED              -> { return "gesichert";                                                      }
            case MAIN_SAVE_ERROR          -> { return "Titel konnte nicht gesichert werden! Einstellungen überprüfen!"; }
            case MAIN_ERROR               -> { return "Fehler";                                                         }
            case MAIN_NO_ERROR            -> { return "Kein Fehler aufgetreten";                                        }
            case MAIN_UI_STATE_SAVE_ERROR -> { return "Konnte UI-State nicht speichern";                                }

            case SETTINGS_ACTIVATE_DARK_MODE    -> { return "Dunkelmodus aktivieren";                                      }
            case SETTINGS_CHOOSE_LANG           -> { return "Sprache wählen";                                              }
            case SETTINGS_JSON_URI_DESC         -> { return "Die URL zur Datei mit den aktuellen Titelinformationen";      }
            case SETTINGS_SONG_INFO_FOLDER_DESC -> { return "Der Ordner, in dem die Titelinfos gespeichert werden sollen"; }
            case SETTINGS_CHANGE                -> { return "Ändern";                                                      }
            case SETTINGS_SONG_REFRESH_RATE     -> { return "Intervall zwischen den Titelabfragen (in Millisekunden)";     }
            case SETTINGS_REMOVE                -> { return "Einstellungen löschen";                                       }
            case SETTINGS_REMOVE_ERROR          -> { return "Fehler beim Löschen der Einstellungen aufgetreten";           }
            case SETTINGS_SAVE_ERROR            -> { return "Konnte Einstellungen nicht sichern";                          }
            case SETTINGS_REMOVE_REALLY         -> { return """
                                                            Sollen die Einstellungen wirklich gelöscht werden?
                                                            Diese Aktion ist nicht widerruflich!
                                                            Das Programm wird anschließend beendet.
                                                            """; }
        }
        throw new IllegalStateException("Missing a german word for: " + id + "!");
    }

    @Override
    public String getName() {
        return name;
    }
}
