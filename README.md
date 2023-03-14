# Welcome to iSongs-RadioText!
This repository contains a project whose initial idea goes back to 2014. It is
an extraction from a project that I started 2013 for learning purposes. This
repository contains the reimplemented version from 2019, when I completely
rewrote it. The main changes were to add some Java best practices and some
multi-threading. Also, I updated it to use the newest JRE of that time, so in
order to compile the project or run the release, Java in version 9 or higher is
needed.

## Idea
The initial idea was to create an application which parsed the broadcast
radio text and displayed the latest information about the name of the currently
broadcast song. Back in 2014, the radio text was broadcast via the web-radio
of iTunes.

As the radio text of the station I was listening to became stuck sometimes, I
had to find an alternative way to get the newest information of the currently
broadcast song. As I found out, the web-player of that station still showed
the correct information, so I read the source code of it and found a JSON file
which always contained the current information.

As the radio station later stopped broadcasting their radio text on the
internet, the JSON file from the web-player was the only way to get the
information of the currently broadcast song.

The application should also be capable to save the title information to a
file, if the user wants to.

## Graphical user interface
<p><img src="https://raw.githubusercontent.com/mhahnFr/iSongs-RadioText/master/screenshots/main_gui.png" alt=""/></p>

The GUI of the application is kept quite simple: There is a single window
containing two text labels showing the name and the interpreter of the currently
broadcast song, underneath them one can find a button to write the
information of the song to a file. The title bar of the window serves as
status label. Also, there is a window which displays the settings of the
application and allows the user to edit them.

## Approach
When invoked, the main function creates a new object of the ViewController
class (iSongsRDS) in the AWT-EventQueue thread. This class is a subclass of
JFrame, and it implements the ActionListener interface. The constructor creates
a new application window and positions it at the same position and with the
same size as the last time. There is a window listener, which saves the current
window settings when the window is closed. Once everything is settled up, a
timer is started, which triggers the system to check whether a (new) song is
broadcast. In order to get the currently broadcast radio text from iTunes,
I wrote a small AppleScript which simply returned to contents of the appropriate
variable of iTunes. If the script returns the currently broadcast radio text,
a parser object scans for the specific pattern in that string to see
whether it contains song information. If there is no content in that variable
or if it is indicating that iTunes has no such variable (at the moment), the
system to check the JSON file from the web-player is activated. It parses the
JSON file, which simply includes some HTML pieces which contain the song
information. If the detected song differs from the one that is currently
displayed, it will be shown in the application window. In order to not block
the GUI thread, those tasks are running in a separate thread.

### The save title button
If the user clicks on the button to save the currently shown title, the current
song information are saved temporary and a file containing this information
is written into the indicated directory. This task also runs in its own thread.
The writing status is displayed in the title bar of the application window.
This task is however run on the AWT-EventQueue.

### The script
The script to get the song information can be saved anywhere, or it can be
missing at all.

### The multi-threading
To make the execution of this application run more smoothly, I used the
ScheduledExecutorService. It is initialized with two threads. All task are
added to the queue of that service, so they can be run concurrently. As
there are at most two tasks running parallel, I decided to use only two threads
of that service. All GUI tasks are passed to the AWT-EventQueue to respect the
Java threading guidelines.

The benefits of the multithreading in this application are to be able to write
song information to a file, meanwhile refresh the title information
without losing the optionally written out information and to keep the GUI
reactive while using it to display the progress of the writing.

### The settings
<p><img src="https://raw.githubusercontent.com/mhahnFr/iSongs-RadioText/master/screenshots/settings.png" alt=""/></p>

All relevant variables can be changed in the settings window.
 - The URL to the JSON file can be adjusted,
 - whether it should be checked,
 - where to write the files containing the song information,
 - the delay to wait between checking for the currently broadcast song.
 - Finally, there is a button to reset all settings.

Those variables are not the only ones to be saved, the position and the width
of the application window are also saved.

### The parsing
The song information in the radio text is expected to be in the following
format: ``<title name> / <interpreter name>``. More explicitly, the parser
checks whether a slash preceded and followed by a space is present. If that is
the case, the last occurrence of such is used to determine the name of the song
and its interpreter.

I did not include a JSON parsing library nor wrote it myself, as the JSON file
is only containing a few HTML pieces, which I also do not parse in a
traditional way. All I did is to hardcode the pattern, where to find the song
information.

## Final notes
The application is optimized for macOS, hence the usage of an AppleScript, but
it also runs on any other system when only the JSON file is used as input.

© 2014, 2019 [mhahnFr](https://www.github.com/mhahnFr)
