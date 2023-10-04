package gui;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Utilities;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import SubtitleWordFrq.SerializableWord;
import SubtitleWordFrq.Utils;
import SubtitleWordFrq.Word;
import javax.swing.JSeparator;

public class WFRQFrame extends JFrame implements WindowListener {
	private final File sessionFile = new File("data/session.json");
	private final File recentSubsFile = new File("data/recent_subtitles.json");
	
	private SubtitlesPanel subtitles_panel;

	private JMenuItem close_subtitles_menuitem;

	private JMenuItem open_subtitles_menuitem;

	private JMenu export_menu;
	
	private List<List<String>> recentSubtitles;
	private static final int MAX_RECENTS_SIZE = 5;

	private JMenu recentSubtitlesMenu;
	
	private File loadedForeignFile;
	private File loadedPrimaryFile;
	
	public WFRQFrame() {
		setTitle("Subtitle Word Frequency");
		addWindowListener(this);
		subtitles_panel = new SubtitlesPanel();
		getContentPane().add(subtitles_panel, BorderLayout.CENTER);
		
		JMenuBar menuBar = new JMenuBar();
		getContentPane().add(menuBar, BorderLayout.NORTH);
		
		JMenu file_menu = new JMenu("File");
		menuBar.add(file_menu);
		
		open_subtitles_menuitem = new JMenuItem("Open Subtitles");
		open_subtitles_menuitem.addActionListener(e -> openClicked());
		file_menu.add(open_subtitles_menuitem);
		
		close_subtitles_menuitem = new JMenuItem("Close Subtitles");
		close_subtitles_menuitem.setEnabled(false);
		close_subtitles_menuitem.addActionListener(e -> closeClicked());
		
		recentSubtitlesMenu = new JMenu("Recent Subtitles");
		recentSubtitlesMenu.setEnabled(false);
		file_menu.add(recentSubtitlesMenu);
		file_menu.add(close_subtitles_menuitem);
		
		JSeparator separator = new JSeparator();
		file_menu.add(separator);
		
		JMenu importMenu = new JMenu("Import");
		file_menu.add(importMenu);
		
		JMenuItem importWordDataMenuItem = new JMenuItem("Tabe Data");
		importWordDataMenuItem.setToolTipText("Import editable table data (definitions, tags, hidden)");
		importWordDataMenuItem.addActionListener(e -> importWordDataClicked());
		importMenu.add(importWordDataMenuItem);
		
		export_menu = new ExportMenu(subtitles_panel.getWordTable(), false);
		export_menu.setEnabled(false);
		file_menu.add(export_menu);
		
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(e -> WFRQFrame.this.dispose());
		
		JSeparator separator_1 = new JSeparator();
		file_menu.add(separator_1);
		file_menu.add(exitMenuItem);
		
		JMenu search_menu = new JMenu("Search");
		search_menu.setEnabled(false);
		menuBar.add(search_menu);
		
		JMenu settings_menu = new JMenu("Settings");
		settings_menu.setEnabled(false);
		menuBar.add(settings_menu);
		
		JMenu help_menu = new JMenu("Help");
		help_menu.setEnabled(false);
		menuBar.add(help_menu);
		
		// to separate GUI design from other ctor logic
		initialize();
	}
	
	private void initialize()
	{
		// load recent subtitles list
		try {
			recentSubtitles = Utils.deserialize("data/recent_subtitles.json", new TypeToken<List<List<String>>>() {}.getType());
			if(recentSubtitles != null) {
				for(List<String> pair : recentSubtitles) {
					File foreignSubsFile = new File(pair.get(0));
					File primarySubsFile;
					
					if(pair.size() > 1)  {
						primarySubsFile = new File(pair.get(1));
					} else {
						primarySubsFile = null;
					}
					
					addToRecents(foreignSubsFile, primarySubsFile);
				}
			}
		} catch (IOException e2) {
			Utils.logger.severe(e2.getMessage());
		}
		
		if(recentSubtitles == null) {
			recentSubtitles = new ArrayList<>(2);
		}
		
		// initialize application for debug mode
		if(Boolean.getBoolean("debug")) {
			File foreignLangFile = new File("sample_subtitles/barbarians-de.srt");
			File primaryLangFile = new File("sample_subtitles/barbarians-en.srt");
			
			loadSubtitles(foreignLangFile, primaryLangFile);
		}
	}
	
	private void importWordDataClicked()
	{
		JFileChooser chooser = Utils.fileChooser("JavaScript Object Notation (*.json)", ".json");
		
		if(chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
		{
			return;
		}
		
		subtitles_panel.importWordData(chooser.getSelectedFile());
	}
	
	private void openClicked()
	{
		JFileChooser chooser = Utils.fileChooser("Subtitles (*.srt)", ".srt", true);
		
		if(chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
		{
			return;
		}
		
		File[] files = chooser.getSelectedFiles();
		
		if(files.length > 2) {
			JOptionPane.showMessageDialog(this, "A maximus of 2 files can be chosen.\n"
					+ "1 foreign language subtitles and 1 primary language subtitles.");
		}
		
		File foreignLangFile = files[0];
		File primaryLangFile = null;
		
		if(files.length == 2) {
			IdentifyLanguageDialog identifyLanguageDialog = new IdentifyLanguageDialog(this, files[0], files[1]);
			identifyLanguageDialog.setVisible(true);

			if(identifyLanguageDialog.getResult() == null) {
				return;
			} else if(identifyLanguageDialog.getResult() == files[0]) {
				foreignLangFile = files[0];
				primaryLangFile = files[1];
			} else {
				foreignLangFile = files[1];
				primaryLangFile = files[0];
			}
		}
		
		loadSubtitles(foreignLangFile, primaryLangFile);
	}
	
	private void loadSubtitles(File foreignSubtitlesFile, File primarySubtitlesFile)
	{
		loadedForeignFile = foreignSubtitlesFile;
		loadedPrimaryFile = primarySubtitlesFile;
		
		if(subtitles_panel.isLoaded()) {
			subtitles_panel.exportWordData(sessionFile);
			subtitles_panel.unloadSubtitles();
		}
		
		try {
			subtitles_panel.loadSubtitles(foreignSubtitlesFile, primarySubtitlesFile);
			subtitles_panel.importWordData(sessionFile);
		} catch (IOException e) {
			Utils.logger.severe(e.getMessage());
			//e.printStackTrace();
		}
		
		open_subtitles_menuitem.setEnabled(false);
		close_subtitles_menuitem.setEnabled(true);
		export_menu.setEnabled(true);
	}
	
	private void closeClicked()
	{
		subtitles_panel.exportWordData(sessionFile);
		subtitles_panel.unloadSubtitles();
		open_subtitles_menuitem.setEnabled(true);
		close_subtitles_menuitem.setEnabled(false);
		export_menu.setEnabled(false);
		
		
		addToRecents(loadedForeignFile, loadedPrimaryFile);
		loadedForeignFile = null;
		loadedPrimaryFile = null;
	}
	
	public static void main(String args[])
	{
		try {
            // Set System L&F
	        UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (Exception e) {
	       Utils.logger.severe(e.getMessage());
	    }
		
		File dataFolder = new File("data");
		if(!dataFolder.exists()) {
			dataFolder.mkdir();
		}
		
		WFRQFrame frame = new WFRQFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(1183, 739);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void addToRecents(File foreignSubsFile, File primarySubsFile)
	{
		if(foreignSubsFile == null)
			return;
		
		String text;
		
		List<String> pair = new ArrayList<>(2);
		pair.add(foreignSubsFile.getAbsolutePath());
		if(primarySubsFile != null) {
			pair.add(primarySubsFile.getAbsolutePath());
			text = String.format("%s -> %s", 
					FilenameUtils.getName(pair.get(0)), 
					FilenameUtils.getName(pair.get(1)));
		} else {
			text = pair.get(0);
		}
		
		if(recentSubtitles.size() == MAX_RECENTS_SIZE) {
			recentSubtitles.remove(recentSubtitles.size() - 1);
		}
		int found = recentSubtitles.indexOf(pair);
		if(found != -1) {
			recentSubtitles.remove(found);
			recentSubtitlesMenu.remove(found);
		}
		recentSubtitles.add(0, pair);
		
		JMenuItem recentSubtitleMenuItem = new JMenuItem(text);
		recentSubtitleMenuItem.addActionListener(e -> loadSubtitles(foreignSubsFile, primarySubsFile));
		recentSubtitlesMenu.add(recentSubtitleMenuItem);
		
		recentSubtitlesMenu.setEnabled(true);
	}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		addToRecents(loadedForeignFile, loadedPrimaryFile);
		try {
			Utils.serialize(recentSubtitles, recentSubsFile, new TypeToken<List<List<String>>>(){}.getType());
		} catch (IOException e1) {
			Utils.logger.severe(e1.getMessage());
		}
		
		// check if subtitles are open right now
		if(close_subtitles_menuitem.isEnabled()) {
			int response = JOptionPane.showConfirmDialog(
				this, 
				"Would you like to save definitions and hidden word list?", 
				"Save session", 
				JOptionPane.YES_NO_CANCEL_OPTION
			);
			
			if(response != JOptionPane.CANCEL_OPTION) {
				if(response == JOptionPane.YES_OPTION) {
					subtitles_panel.exportWordData(sessionFile);
				}
				this.dispose();
			}
		} else {
			this.dispose();
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}
}
