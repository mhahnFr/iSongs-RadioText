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
 * This class represents the english translation of this app.
 *
 * @author mhahnFr
 * @since 01.09.23
 */
public class English extends Locale {
    /** The localized name of the language. */
    public static final String name = "English";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String get(final StringID id) {
        switch (id) {
            case MAIN_CURRENT_TITLE       -> { return "Current song";                            }
            case MAIN_SAVE_TITLE          -> { return "Save song";                               }
            case MAIN_SHOW_ERROR          -> { return "Show error";                              }
            case MAIN_SETTINGS            -> { return "Settings";                                }
            case MAIN_NO_SONG             -> { return "No title";                                }
            case MAIN_NO_INTERPRETER      -> { return "No interpreter";                          }
            case MAIN_STORED              -> { return "saved";                                   }
            case MAIN_SAVE_ERROR          -> { return "Could not save song! Validate settings!"; }
            case MAIN_ERROR               -> { return "Error";                                   }
            case MAIN_NO_ERROR            -> { return "No error occurred";                       }
            case MAIN_UI_STATE_SAVE_ERROR -> { return "Could not save UI state";                 }
            case MAIN_UI_CHECK_SETTINGS   -> { return "Please check the settings!";              }
            case MAIN_ERROR_HAPPENED      -> { return "Error happened";                          }

            case SETTINGS_ACTIVATE_DARK_MODE    -> { return "Activate dark-mode";                                     }
            case SETTINGS_CHOOSE_LANG           -> { return "Choose language";                                        }
            case SETTINGS_JSON_URI_DESC         -> { return "URL to the file containing the song data";               }
            case SETTINGS_SONG_INFO_FOLDER_DESC -> { return "The path where to store the song info";                  }
            case SETTINGS_CHANGE                -> { return "Change";                                                 }
            case SETTINGS_SONG_REFRESH_RATE     -> { return "Refresh rate for fetching song data (in milliseconds)";  }
            case SETTINGS_REMOVE                -> { return "Delete settings";                                        }
            case SETTINGS_REMOVE_ERROR          -> { return "Error occurred while deleting settings";                 }
            case SETTINGS_SAVE_ERROR            -> { return "Could not save settings";                                }
            case SETTINGS_LANGUAGE_RESTART      -> { return "Restart the app for the language change to take effect"; }
            case SETTINGS_APPLESCRIPT_DESC      -> { return "Apple Event based recognition";                          }
            case SETTINGS_APPLESCRIPT_OFF       -> { return "Disabled";                                               }
            case SETTINGS_APPLESCRIPT_ON        -> { return "Activated";                                              }
            case SETTINGS_APPLESCRIPT_ONLY      -> { return "Only (Internet based recognition disabled)";             }
            case SETTINGS_ALLOW_NO_SONG         -> { return "Show no song recognized";                                }
            case SETTINGS_REMOVE_REALLY         -> { return """
                                                            Really delete settings?
                                                            This action cannot be undone.
                                                            The application will quit afterwards.
                                                            """; }

            case INTERNAL_NO_TRACK_RECOGNIZED -> { return "No track recognized!"; }
            case INTERNAL_SAVE_FOLDER_UNSET   -> { return "Save folder not set!"; }
        }
        throw new IllegalStateException("Missing an english word for: " + id + "!");
    }
}
