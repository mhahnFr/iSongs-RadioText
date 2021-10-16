# Welcome to iSongs-RadioText!
This repository contains a project whose intial idea goes back to 2014. It is
an extraction from a project that I started 2013 for learning purposes. This
repository contains the reimplemented version from 2019, when I completely
rewrote it. The main changements were to add some Java best practices and some
multi-threading.

## Idea
The initial idea was to create an application which parsed the broadcasted
radio text and displayed the latest information about the name of the currently
broadcasted song. Back in 2014, the radio text was broadcasted via the webradio
of iTunes.

As the radio text of the station I was listening to became stuck sometimes, I
had to find an alternative way to get the newest information of the currently
broadcasted song. As I found out, the webplayer of that station still showed
the correct informations, so I read the source code of it and found a JSON file
which always contained the current informations.

As the radio station later stopped broadcasting their radio text on the
internet, the JSON file from the webplayer was the only way to get the
informations of the currently broadcasted song.

The application should also be capable to save the title informations to a
file, if the user wants to.

## Graphical user interface
The GUI of the application is kept quite simple: There is a single window
containing two text labels showing the name and the interpreter of the curently
broadcasted song, underneath them one can find a button to write the
infomormations of the title to a file. The title bar of the window serves as
status label. Also there is a window which displays the settings of the
application and allows the user to edit them.

## Approach
When invoked, the main function creates a new object of the ViewController
class (iSongsRDS) in the AWT-EventQueue thread. This class is a subclass of
JFrame and it implements the ActionListener interface. The constructor creates
a new application window and positions it at the same position and with the
same size as the last time. There is a window listener, which saves the current
window settings when the window is closed. Once
In order to get the currently broadcasted radio text from iTunes, I wrote a
small AppleScript which simply returned to contents of the appopriate variable
of iTunes.
