# Welcome to iSongs-RadioText!
This repository contains a project whose initial idea goes back to 2014. It is
an extraction from a project that I started 2013 for learning purposes. This
repository contains the reimplemented version from 2023, when it was completely
rewritten.  
Accordingly, Java 19 or higher is required for using this project.

### Screenshot
<p align="center">
    <picture>
        <source srcset="https://raw.githubusercontent.com/mhahnFr/iSongs-RadioText/main/screenshots/main_gui-light.png" media="(prefers-color-scheme: light), (prefers-color-scheme: no-preference)" />
        <source srcset="https://raw.githubusercontent.com/mhahnFr/iSongs-RadioText/main/screenshots/main_gui-dark.png" media="(prefers-color-scheme: dark)" />
        <img src="https://raw.githubusercontent.com/mhahnFr/iSongs-RadioText/main/screenshots/main_gui-light.png" alt="iSongs" />
    </picture>
</p>

#### Settings
<p align="center">
    <picture>
        <source srcset="https://raw.githubusercontent.com/mhahnFr/iSongs-RadioText/main/screenshots/settings-light.png" media="(prefers-color-scheme: light), (prefers-color-scheme: no-preference)" />
        <source srcset="https://raw.githubusercontent.com/mhahnFr/iSongs-RadioText/main/screenshots/settings-dark.png" media="(prefers-color-scheme: dark)" />
        <img src="https://raw.githubusercontent.com/mhahnFr/iSongs-RadioText/main/screenshots/settings-light.png" alt="iSongs settings" />
    </picture>
</p>

## Idea
The initial idea was to create an application which parsed the broadcast
radio text and displayed the latest recognized song. Back in 2014, the radio
text was broadcast via the web-radio feature of iTunes.

As time moved on, the radio text lost its relevance, so the necessary information
is gathered from the web player: It uses a JSON file, which is parsed by this
application.

The application should also be capable to save the title information to a
file, if the user wants to.

### Graphical user interface
The GUI of the application is kept quite simple: There is a single window
containing two text labels showing the name and the interpreter of the currently
broadcast song, underneath them one can find a button to write the
information of the song to a file. The title bar of the window serves as
status label. Also, there is a window which displays the settings of the
application and allows the user to edit them.

## Final notes
The application runs cross-platform.

This project is licensed under the terms of the GPL 3.0.

© Copyright 2014, 2019, 2023 - 2024 [mhahnFr](https://github.com/mhahnFr)