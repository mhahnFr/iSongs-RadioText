package hahn.isongsrds.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;

import hahn.isongsrds.Interpreter;
import hahn.isongsrds.Interpreter.InternetState;

/**
 * Diese Klasse bildet das Hauptfenster von iSongs.
 * 
 * @author Manuel Hahn
 * @since 20.02.2019
 */
public class iSongsRDS extends JFrame implements ActionListener {
	private static final long serialVersionUID = 8900197028325227215L;
	/**
	 * Das Kommando, den aktuell gespielten Titel zu sichern.
	 */
	private static final String SAVE_TRACK 			= "merken";
	/**
	 * Das Kommando, anzuzeigen, das der Titel gespeichert wurde.
	 */
	private static final String SHOW_SAVED_TRACK	= "titel sichern";
	/**
	 * Ein ThreadPool, in dem sich zwei Threads um Aufgaben kümmern, die
	 * scheduled werden können.
	 */
	private final ScheduledExecutorService scheduledThreads = 
			Executors.newScheduledThreadPool(2);
	/**
	 * Der Interpreter, der Titel erkennt und Nicht-GUI-Aufgaben übernimmt.
	 */
	private Interpreter interpreter;
	/**
	 * Der Knopf, der den aktuell gespielten Titel sichert.
	 */
	private JButton saveTitle;
	/**
	 * Das {@link JLabel} mit dem erkannten Titelnamen.
	 */
	private JLabel titelText;
	/**
	 * Das {@link JLabel} mit dem erkannten Interpretennamen.
	 */
	private JLabel interpreterText;
	/**
	 * Das Fenster mit den Einstellungen.
	 */
	private JDialog settingsDialog;
	/**
	 * Ob der Titel des Hauptfensters aktualisiert werden soll oder nicht.
	 */
	private boolean blockTitleText;
	/**
	 * Zeigt an, ob der gerade angezeigte Titel schon gesichert wurde.
	 */
	private boolean titleSaved;
	/**
	 * Der Handler, mit dem der Thread, der sich um die Titelabfrage kümmert,
	 * gesteuert werden kann.
	 */
	private ScheduledFuture<?> trackCheckerHandler;
	/**
	 * Dieser {@link Timer} ist dazu da, den Status eines gesicherten Titels anzuzeigen.
	 */
	private Timer successTimer;
	/**
	 * Überprüft, ob sich der Titel sich geändert hat.
	 */
	private Runnable trackChecker = () -> checkTrack();
	/**
	 * Schreibt den aktuellen Titel. Gibt zurück, ob dabei ein Fehler aufgetreten ist.
	 */
	private Runnable trackWriter = () -> {
		boolean exceptionHappened = false;
		try {
			interpreter.writeTrack();
		} catch (IOException e) {
			System.err.println("Fehler afgetreten: " + e.getMessage());
			e.printStackTrace();
			System.err.println("------------------------------------");
			exceptionHappened = true;
		}
		final String title;
		if(exceptionHappened) {
			title = "Titel konnte nicht gesichert werden! Einstellungen überprüfen!";
			titleSaved = false;
		} else {
			title = "\"" + interpreter.getCurrentTrack() + "\" gesichert";
			titleSaved = true;
		}
		EventQueue.invokeLater(() -> {
			setTitle(title);
			saveTitle.setEnabled(!titleSaved);
		});
		successTimer.restart();
	};
	
	/**
	 * Erzeugt das Hauptanwendungsfenster.
	 */
	public iSongsRDS() {
		super("iSongsRDS");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			System.err.println("Konnte Look&Feel nicht auf Standard setzten.");
			System.err.println("Fehler aufgetreten: " + e1.getMessage());
			e1.printStackTrace();
			System.err.println("--------------------------------------");
		}
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		interpreter = new Interpreter();
		
		JLabel show = new JLabel(" Aktueller Titel:");
		titelText = new JLabel("Laden...", SwingConstants.CENTER);
		interpreterText = new JLabel("Laden...", SwingConstants.CENTER);
		titelText.setFont(titelText.getFont().deriveFont(Font.BOLD));
		saveTitle = new JButton("Titel merken");
		saveTitle.setActionCommand(SAVE_TRACK);
		saveTitle.addActionListener(this);
		getContentPane().setLayout(new GridLayout(4, 1));
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(saveTitle);
		add(show);
		add(titelText);
		add(interpreterText);
		add(buttonPanel);
		
		if(Desktop.getDesktop().isSupported(Desktop.Action.APP_PREFERENCES)) {
			Desktop.getDesktop().setPreferencesHandler(event -> {
				if(trackCheckerHandler != null && !trackCheckerHandler.isDone()) {
					trackCheckerHandler.cancel(false);
				}
				if(settingsDialog == null) {
					createSettings();
				}
				settingsDialog.setVisible(true);
			});
		}
		
		successTimer = new Timer(5000, this);
		successTimer.setActionCommand(SHOW_SAVED_TRACK);
		successTimer.setRepeats(false);
		
		int width = interpreter.getWindowWidth();
		int height = interpreter.getWindowHeight();
		if(!(width > 0) && !(height > 0)) {
			pack();
		} else {
			setBounds(interpreter.getWindowPositionX(), interpreter.getWindowPositionY(), width, height);
		}
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				interpreter.setWindowLocation(getX(), getY());
				interpreter.setWindowSize(getWidth(), getHeight());
				interpreter.flushSettings();
			}
		});
		setVisible(true);
		if(canWork()) {
			trackCheckerHandler = scheduledThreads.scheduleAtFixedRate(
					trackChecker,
					0,
					interpreter.getIntervalMillis(),
					TimeUnit.MILLISECONDS);
		} else {
			titelText.setText("Bitte Einstellungen überprüfen!");
			saveTitle.setEnabled(false);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case SAVE_TRACK:
			if(interpreter.hasTrack()) {
				scheduledThreads.schedule(trackWriter, 0, TimeUnit.SECONDS);
				setTitle("\"" + interpreter.getCurrentTrack() + "\" wird gesichert...");
				blockTitleText = true;
			}
			break;
			
		case SHOW_SAVED_TRACK:
			setTitle(interpreter.getRadioText());
			blockTitleText = false;
			break;
		}
	}
	
	/**
	 * Überprüft, ob sich der Titel geändert hat.
	 */
	private void checkTrack() {
		final String tt, it;
		final boolean changed = interpreter.hasTrackChanged();
		if(changed) {
			titleSaved = false;
			if(interpreter.hasTrack()) {
				tt = interpreter.getCurrentTrack();
				it = interpreter.getCurrentInterpreter();
			} else {
				tt = "Kein Titel";
				it = "Kein Interpret";
			}
		} else {
			tt = it = "";
		}
		Runnable guiChanger = () -> {
			if(changed) {
				titelText.setText(tt);
				interpreterText.setText(it);
			}
			if(interpreter.hasTextChanged() && !blockTitleText) {
				setTitle(interpreter.getRadioText());
			}
			if(interpreter.hasTrack() && !titleSaved) {
				if(!saveTitle.isEnabled()) {
					saveTitle.setEnabled(true);
				}
			} else {
				if(saveTitle.isEnabled()) {
					saveTitle.setEnabled(false);
				}
			}
		};
		EventQueue.invokeLater(guiChanger);
	}
	
	/**
	 * Erzeugt das Fenster mit den Einstellungen.
	 */
	private void createSettings() {
		settingsDialog = new JDialog(this, "Einstellungen", true);
		settingsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JRadioButton on = new JRadioButton("Immer");
		JRadioButton auto = new JRadioButton("Automatisch");
		JRadioButton off = new JRadioButton("Aus");
		ButtonGroup bg = new ButtonGroup();
		bg.add(off);
		bg.add(auto);
		bg.add(on);
		JLabel iUnt = new JLabel("Internet-Unterstützung:");
		JPanel inetUnt = new JPanel();
		inetUnt.setLayout(new BoxLayout(inetUnt, BoxLayout.Y_AXIS));
		inetUnt.add(iUnt);
		inetUnt.add(auto);
		inetUnt.add(on);
		inetUnt.add(off);
		inetUnt.setBorder(new EtchedBorder());
		
		JLabel webFile = new JLabel("Die URL zur Datei mit den aktuellen Titelinformatinen:");
		JTextField wf = new JTextField();
		JPanel inetFile = new JPanel();
		inetFile.setLayout(new GridLayout(2, 1));
		inetFile.add(webFile);
		inetFile.add(wf);
		inetFile.setBorder(new EtchedBorder());
		
		JLabel saveFolder = new JLabel("Der Ordner, in den die Titelinfos gespeichert werden sollen:");
		JLabel folder = new JLabel();
		folder.setFont(folder.getFont().deriveFont(Font.BOLD));
		JButton choose = new JButton("Ändern...");
		choose.addActionListener(event -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileHidingEnabled(true);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				folder.setText(file.getAbsolutePath());
				interpreter.setFolderForFiles(file);
			}
		});
		JPanel titleFolder = new JPanel();
		titleFolder.setLayout(new GridLayout(2, 1));
		JPanel editFolder = new JPanel();
		editFolder.setLayout(new BorderLayout());
		editFolder.add(folder, BorderLayout.CENTER);
		editFolder.add(choose, BorderLayout.EAST);
		titleFolder.add(saveFolder);
		titleFolder.add(editFolder);
		titleFolder.setBorder(new EtchedBorder());
		
		JLabel interval = new JLabel("Intervall zwischen den Titelabfragen (in Millisekunden):");
		JTextField iMillis = new JTextField(Integer.toString(interpreter.getIntervalMillis()));
		JPanel editInterval = new JPanel();
		editInterval.setLayout(new GridLayout(2, 1));
		editInterval.add(interval);
		editInterval.add(iMillis);
		editInterval.setBorder(new EtchedBorder());
		
		JButton deleteSettings = new JButton("Einstellungen löschen");
		deleteSettings.addActionListener(event -> {
			int option = JOptionPane.showConfirmDialog(
					settingsDialog,
					"Sollen die Einstellungen wirklich gelöscht werden?\n"
					+ "Diese Aktion ist nicht widerruflich!\n"
					+ "Das Programm wird anschließend beendet.",
					"Einstellungen löschen",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if(option == JOptionPane.OK_OPTION) {
				try {
					interpreter.deleteSettings();
					System.exit(0);
				} catch (BackingStoreException e1) {
					System.err.println("Fehler aufgetreten: " + e1.getMessage());
					e1.printStackTrace();
					System.err.println("-------------------------------------");
					JOptionPane.showMessageDialog(
							settingsDialog,
							"Fehler beim Löschen der Einstellungen aufgetreten: " + e1.getLocalizedMessage(),
							"Fehler: BackingStoreException",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		settingsDialog.setLayout(new BorderLayout());
		JPanel center = new JPanel();
		center.setLayout(new GridLayout(3, 1));
		center.add(inetFile);
		center.add(titleFolder);
		center.add(editInterval);
		settingsDialog.add(inetUnt, BorderLayout.NORTH);
		settingsDialog.add(center, BorderLayout.CENTER);
		settingsDialog.add(deleteSettings, BorderLayout.SOUTH);
		settingsDialog.pack();
		settingsDialog.setLocationRelativeTo(this);
		settingsDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				switch(interpreter.getInternetState()) {
				case ON:
					on.setSelected(true);
					break;
					
				case AUTO:
					auto.setSelected(true);
					break;
					
				case OFF:
					off.setSelected(true);
					break;
					
				default:
					System.err.println("Undefinierter Status der Internetunterstützung!");
					break;
				}
				iMillis.setText(Integer.toString(interpreter.getIntervalMillis()));
				URL url = interpreter.getURL();
				if(url != null) {
					wf.setText(url.toString());
				}
				folder.setText(interpreter.getFolderForFilesPath());
				if(trackCheckerHandler != null && !trackCheckerHandler.isDone()) {
					trackCheckerHandler.cancel(false);
				}
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				
				try {
					interpreter.setURL(wf.getText());
					interpreter.setIntervalMillis(Integer.parseInt(iMillis.getText()));
				} catch (MalformedURLException e1) {
					System.err.println("Fehler aufgetreten: " + e1.getMessage());
					e1.printStackTrace();
					System.err.println("-------------------------------------");
				} catch (NumberFormatException e2) {
					System.err.println("Fehler aufgetreten: " + e2.getMessage());
					e2.printStackTrace();
					System.err.println("-------------------------------------");
				}
				InternetState state;
				if(on.isSelected()) {
					state = InternetState.ON;
				} else if(off.isSelected()) {
					state = InternetState.OFF;
				} else {
					state = InternetState.AUTO;
				}
				interpreter.setInternetState(state);
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				interpreter.flushSettings();
				if(canWork()) {
					trackCheckerHandler = scheduledThreads.scheduleAtFixedRate(
							trackChecker,
							0,
							interpreter.getIntervalMillis(),
							TimeUnit.MILLISECONDS);
				}
			}
		});
	}
	
	/**
	 * Gibt zurück, ob das Program einsatzfähig ist.
	 * 
	 * @return ob die Erkennung starten kann
	 */
	private boolean canWork() {
		boolean hasUrl = interpreter.getURL() != null;
		boolean hasFolder = !interpreter.getFolderForFilesPath().equals("");
		switch(interpreter.getInternetState()) {
		case OFF:
			if(hasFolder) {
				return true;
			}
			break;
			
		case ON:
			if(hasUrl) {
				return true;
			}
			break;
			
		case AUTO:
			if(hasUrl && hasFolder) {
				return true;
			}
			break;
			
		default:
			return false;
		}
		return false;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> new iSongsRDS());
	}
}