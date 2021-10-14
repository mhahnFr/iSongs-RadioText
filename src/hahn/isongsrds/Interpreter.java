package hahn.isongsrds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Diese Klasse beinhaltet sämtliche Logik des Programms. Es kümmert sich um die 
 * Sicherung von Einstellungen, es erkennt im Radio laufende Titel, ...
 * 
 * @author Manuel Hahn
 * @since 20.02.2019
 */
public class Interpreter {
	/**
	 * Der Schlüssel für die Internetbenutzung.
	 */
	private static final String USE_WEB_KEY 			= "internetstate";
	/**
	 * Der Schlüssel für den X-Wert der Fensterposition.
	 */
	private static final String WINDOW_LOCATION_X_KEY 	= "windowlocationx";
	/**
	 * Der Schlüssel für den Y-Wert der Fensterposition.
	 */
	private static final String WINDOW_LOCATION_Y_KEY 	= "windowlocationy";
	/**
	 * Der Schlüssel für die Fensterhöhe.
	 */
	private static final String WINDOW_HEIGHT_KEY 		= "windowheight";
	/**
	 * Der Schlüssel für die Dauer des Intervalls zwischen den Titelabfragen (in Millisekunden).
	 */
	private static final String INTERVAL_MILLIS_KEY		= "intervalmillis";
	/**
	 * Der Schlüssel für die Fensterbreite.
	 */
	private static final String WINDOW_WIDTH_KEY 		= "windowwidth";
	/**
	 * Der Schlüssel für den Ordner, in denen die Dateien mit den Titelinfos
	 * gespeichert werden sollen.
	 */
	private static final String FILE_FOLDER_KEY			= "filefolder";
	/**
	 * Der Schlüssel für die URL zur Datei im Internet mit den aktuellen Titelinfos.
	 */
	private static final String INTERNET_FILE			= "internetfile";
	/**
	 * Die Einstellungen des Nutzer und für dieses Programm.
	 */
	private Preferences preferences;
	/**
	 * Der zuletzt erkannte Titelname.
	 */
	private String currentTrack;
	/**
	 * Der zuletzt erkannte Interpretenname.
	 */
	private String currentInterpreter;
	/**
	 * Der aktuelle Radiotext, der eventuell den aktuell gespielten Titel enthalten kann.
	 */
	private String currentRadioText;
	/**
	 * Die URL zur Datei im Internet mit den aktuellen Titelinformationen.
	 */
	private URL url;
	/**
	 * Gibt an, ob sich der {@link #currentRadioText RadioText} geändert hat oder nicht.
	 */
	private boolean hasTextChanged;
	/**
	 * Eine Zwischenspeicherung des Status der Internetunterstützung.
	 */
	private InternetState state;
	
	/**
	 * Erzeugt diesen Interpreter.
	 */
	public Interpreter() {
		preferences = Preferences.userNodeForPackage(getClass());
		currentTrack = " ";
		currentInterpreter = " ";
		currentRadioText = " ";
		flushSettings();
	}

	/**
	 * Gibt zurück, ob sich der aktuell gespielte Titel geändert hat. Das wird von
	 * dieser Methode überprüft.
	 * 
	 * @return ob der aktuelle Titel sich geändert hat
	 */
	public boolean hasTrackChanged() {
		if(getInternetState() != InternetState.ON) {
			String returned = null;
			try {
				returned = getScriptResult();
			} catch (IOException | InterruptedException e) {
				System.err.println("Fehler aufgetreten: " + e.getMessage());
				e.printStackTrace();
				System.err.println("-------------------------------------");
			}
			if(returned == null || returned.equals("") || (returned = returned.trim()).equalsIgnoreCase("missing value")) {
				if(state != InternetState.OFF) {
					return getCurrentTrackInternet();
				}
			}
			hasTextChanged = !returned.equals(currentRadioText);
			currentRadioText = returned;
			if(returned.contains(" / ")) {
				int index = returned.lastIndexOf(" / ");
				String newTrack = returned.substring(0, index);
				String newInterpreter = returned.substring(index + 3);
				if(!newTrack.equals(currentTrack) && !newInterpreter.equals(currentInterpreter)) {
					currentTrack = newTrack;
					currentInterpreter = newInterpreter;
					return true;
				}
			}
		} else if(state == InternetState.ON) {
			return getCurrentTrackInternet();
		}
		return false;
	}
	
	/**
	 * Gibt den aktuellen RadioText zurück.
	 * 
	 * @return den RadioText
	 */
	public String getRadioText() {
		return currentRadioText;
	}
	
	/**
	 * Gibt zurück, ob sich der Radiotext geändert hat oder nicht.
	 * 
	 * @return ob sich der Radiotext geändert hat
	 */
	public boolean hasTextChanged() {
		return hasTextChanged;
	}
		
	/**
	 * Gibt zurück, ob sich der aktuell erkannte Titel sich geändert hat. Dazu wird die 
	 * Datei im Internet abgerfufen, die verlinkt ist.
	 * 
	 * @return ob sich der Titel geändert hat
	 * @see #getURL()
	 * @see #setURL(URI)
	 * @see #currentTrack
	 * @see #currentInterpreter
	 * @see #getCurrentTrack()
	 * @see #getCurrentInterpreter()
	 * @see #hasTrack()
	 */
	private boolean getCurrentTrackInternet() {
		String web = "Titel von swr1.de";
		hasTextChanged = !currentRadioText.equals(web);
		currentRadioText = web;
		try (InputStreamReader reader = new InputStreamReader(getURL().openStream(), "UTF-8")) {
			String text = "";
			int last = 0;
			while(last != -1) {
				last = reader.read();
				if(last != -1) {
					text += (char) last;
				}
			}
			text = text.substring(text.indexOf("onairmusictitle"));
			String id = "\\u000a                        ";
			int index = text.indexOf(id);
			String newTrack, newInterpreter;
			if(index == -1) {
				newTrack = "";
				newInterpreter = "";
			} else {
				index += id.length();
				newTrack = text.substring(index, text.indexOf("\\u000a", index));
				index = text.lastIndexOf(id) + id.length();
				newInterpreter = text.substring(index, text.indexOf("\\u000a", index));
			}
			if(!currentTrack.equals(newTrack) && !currentInterpreter.equals(newInterpreter)) {
				currentTrack = newTrack;
				currentInterpreter = newInterpreter;
				return true;
			}
		} catch (IOException | StringIndexOutOfBoundsException e) {
			System.err.println("Fehler aufgetreten: " + e.getMessage());
			e.printStackTrace();
			System.err.println("-------------------------------------");
		}
		return false;
	}
	
	/**
	 * Gibt das Ergebnis des Applescripts zurück. Sollte ein Fehler dabei auftreten,
	 * wird eine entsprechende Exception geworfen.
	 * 
	 * @return das, was das AppleScript zurück gibt
	 * @throws InterruptedException sollte nicht auf die Ausführung gewartet werden können
	 * @throws IOException sollte ein Lesefehler auftreten
	 */
	private String getScriptResult() throws InterruptedException, IOException {
		Process process = Runtime.getRuntime().exec("osascript "
				+ getFolderForFilesPath() + File.separator + "SongRetrieval.scpt");
		process.waitFor();
		byte[] bytes;
		try (InputStream is = process.getInputStream()) {
			bytes = is.readAllBytes();
		} catch (IOException e) {
			throw e;
		}
		process.destroy();
		bytes[bytes.length - 1] = '\0';
		return new String(bytes);
	}
	
	/**
	 * Speichert den aktuell gespielten Titel. Sollte kein Titel gespielt werden,
	 * wird eine {@link IllegalStateException} geworfen.
	 * 
	 * @throws IOException sollte beim Schreiben ein Fehler auftreten
	 * @throws IllegalStateException sollte kein Titel gespielt werden
	 */
	public void writeTrack() throws IOException {
		if(!hasTrack()) {
			throw new IllegalStateException("No track played!");
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(getFolderForFilesPath() + File.separator +
				"Song " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.GERMAN)
				.format(new Date()))))) {
			writer.write("titel:" + currentTrack + System.lineSeparator());
			writer.write("interpreter:" + currentInterpreter);
		} catch(IOException e) {
			throw e;
		}
	}
	
	/**
	 * Gibt zurück, ob mutmaßlich gerade ein Titel gespielt wird.
	 * 
	 * @return ob derzeit ein Titel gespielt wird
	 */
	public boolean hasTrack() {
		return currentTrack != null 
				&& currentInterpreter != null 
				&& !currentTrack.equals("") 
				&& !currentInterpreter.equals("");
	}
	
	/**
	 * Gibt den zuletzt erkannten Titelnamen zurück. Sollte mutmaßlich kein
	 * Titel gespielt werden, kann dieser Wert {@code null} sein.
	 * 
	 * @return den zuletzt erkannten Titelnamen
	 */
	public String getCurrentTrack() {
		return currentTrack;
	}
	
	/**
	 * Gibt den zuletzt erkannten Interpretennamen zurück. Sollte mutmaßlich
	 * kein Titel gespielt werden, wird {@code null} zurückgegegeben.
	 * 
	 * @return den zuletzt erkannten Interpretennamen
	 */
	public String getCurrentInterpreter() {
		return currentInterpreter;
	}
	
	/**
	 * Versucht, die Einstellungen fest zu schreiben.
	 */
	public void flushSettings() {
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			System.err.println("Fehler aufgetreten: " + e.getMessage());
			e.printStackTrace();
			System.err.println("-------------------------------------");
		}
	}
	
	/**
	 * Speichert den Ordner, in welchen die Dateien mit den Titelinfos 
	 * gesepeichert werden sollen.
	 * 
	 * @param folder der Ordner
	 */
	public void setFolderForFiles(File folder) {
		preferences.put(FILE_FOLDER_KEY, folder.getAbsolutePath());
	}
	
	/**
	 * Speichert die URL zur Datei im Internet mit den aktuellen Titelinfos.
	 * 
	 * @param url die URL zur Internetdatei
	 * @throws MalformedURLException sollte die URL nicht korrekt sein
	 */
	public void setURL(String url) throws MalformedURLException {
		this.url = new URL(url);
		preferences.put(INTERNET_FILE, url);
	}
	
	/**
	 * Gibt den Pfad zum Ordner, in den die Dateien mit den Titelinfos gespeichert
	 * werden sollen, zurück.
	 * 
	 * @return den Pfad zum Ordner
	 */
	public String getFolderForFilesPath() {
		return preferences.get(FILE_FOLDER_KEY, "");
	}
	
	/**
	 * Gibt die URL zur Datei im Internet mit den aktuellen Titelinfos zurück.
	 * 
	 * @return die URL
	 */
	public URL getURL() {
		if(url == null) {
			try {
				url = new URL(preferences.get(INTERNET_FILE, ""));
			} catch (MalformedURLException e) {
				System.err.println("URL-Abfrage: Die gespeicherte URL ist keine!");
				url = null;
			}
		}
		return url;
	}
	
	/**
	 * Gibt die URL zur Datei im Internet mit den aktuellen Titelinfos als String
	 * zurück.
	 * 
	 * @return die URL als String
	 */
	public String getURLAsString() {
		return getURL().toString();
	}
	
	/**
	 * Gibt die Dauer des Intervalls zwischen den Titelabfragen in Millisekunden zurück.
	 * 
	 * @return die Dauer des Intervalls in Millisekunden
	 */
	public int getIntervalMillis() {
		return preferences.getInt(INTERVAL_MILLIS_KEY, 1000);
	}
	
	/**
	 * Speichert die Dauer des Intervalls zwischen den Titelabfragen in Millisekunden.
	 * 
	 * @param millis die Dauer des Intervalls in Millisekunden
	 */
	public void setIntervalMillis(int millis) {
		preferences.putInt(INTERVAL_MILLIS_KEY, millis);
	}
	
	/**
	 * Schreibt die Fensterposition fest.
	 * 
	 * @param x der X-Wert
	 * @param y der Y-Wert
	 */
	public void setWindowLocation(int x, int y) {
		preferences.putInt(WINDOW_LOCATION_X_KEY, x);
		preferences.putInt(WINDOW_LOCATION_Y_KEY, y);
	}
	
	/**
	 * Speichert den Status der Internetunterstützung.
	 * 
	 * @param state der Status
	 */
	public void setInternetState(InternetState state) {
		this.state = state;
		preferences.put(USE_WEB_KEY, state.name());
	}
	
	/**
	 * Gibt zurück, wie die Internetunterstützung verwendet werden soll.
	 * 
	 * @return niemals {@link InternetState#UNKNOWN}
	 */
	public InternetState getInternetState() {
		if(state == null || state == InternetState.UNKNOWN) {
			state = InternetState.valueOf(preferences.get(USE_WEB_KEY, InternetState.AUTO.name()));
		}
		return state;
	}
	
	/**
	 * Gibt den Y-Wert der letzten Fensterposition zurück. Er ist mindestenz 0.
	 * 
	 * @return den Y-Wert der Fensterposition
	 */
	public int getWindowPositionY() {
		return preferences.getInt(WINDOW_LOCATION_Y_KEY, 0);
	}
	
	/**
	 * Gibt den X-Wert der letzten Position zurück. Er ist mindestenz 0.
	 * 
	 * @return den X-Wert der Position
	 */
	public int getWindowPositionX() {
		return preferences.getInt(WINDOW_LOCATION_X_KEY, 0);
	}
	
	/**
	 * Gibt die Breite des Hauptfensters zurück. Sollte die Breite nicht existieren,
	 * wird -1 zurückgegeben.
	 * 
	 * @return die Breite des Hauptfenster, oder -1
	 */
	public int getWindowWidth() {
		return preferences.getInt(WINDOW_WIDTH_KEY, -1);
	}
	
	/**
	 * Gibt die zuletzt gespeicherte Höhe des Hauptfensters zurück. Sollte sie nicht 
	 * existieren, wird -1 zurückgegeben.
	 * 
	 * @return die letzte Höhe des Hauptfensters, oder -1
	 */
	public int getWindowHeight() {
		return preferences.getInt(WINDOW_HEIGHT_KEY, -1);
	}
	
	/**
	 * Schreibt die Fenstergröße in die Einstellungen.
	 * 
	 * @param width die Fensterbreite
	 * @param height die Fensterhöhe
	 */
	public void setWindowSize(int width, int height) {
		preferences.putInt(WINDOW_HEIGHT_KEY, height);
		preferences.putInt(WINDOW_WIDTH_KEY, width);
	}
	
	/**
	 * Löscht die Einstellungen. Wird erneut eine Instanz dieser Klasse erzeugt, werden 
	 * auch die Einstellungen erneut geschrieben.
	 * 
	 * @throws BackingStoreException sollten die Einstellungen nicht gelöscht werden können
	 */
	public void deleteSettings() throws BackingStoreException {
		preferences.removeNode();
		preferences.flush();
	}

	/**
	 * Zählt die möglichen Zustände der Internetunterstützung auf.
	 * 
	 * @author Manuel Hahn
	 * @since 20.02.2019
	 */
	public enum InternetState {
		/**
		 * Internetunterstützung soll ausschließlich genutzt werden.
		 */
		ON,
		/**
		 * Internetunterstützung soll nicht genutzt werden.
		 */
		OFF,
		/**
		 * Internetunterstützung soll bei Bedarf benutzt werden.
		 */
		AUTO,
		/**
		 * Unbekannte Nutzung der Internetunterstützung.
		 */
		UNKNOWN
	}
}