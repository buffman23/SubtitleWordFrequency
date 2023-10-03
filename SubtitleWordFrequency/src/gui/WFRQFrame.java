package gui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import java.io.File;
import java.io.IOException;
import SubtitleWordFrq.Utils;

public class WFRQFrame extends JFrame {
	private SubtitlesPanel subtitles_panel;

	private JMenuItem close_subtitles_menuitem;

	private JMenuItem open_subtitles_menuitem;

	private JMenu export_menu;
	
	public WFRQFrame() {
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
		file_menu.add(close_subtitles_menuitem);
		
		JMenu importMenu = new JMenu("Import");
		file_menu.add(importMenu);
		
		JMenuItem importDefinitionsMenuItem = new JMenuItem("Definitions");
		importMenu.add(importDefinitionsMenuItem);
		
		JMenuItem importHiddenWordListMenuItem = new JMenuItem("Hidden Word List");
		importMenu.add(importHiddenWordListMenuItem);
		
		export_menu = new ExportMenu(subtitles_panel.getWordTable(), false);
		export_menu.setEnabled(false);
		file_menu.add(export_menu);
		
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(e -> WFRQFrame.this.dispose());
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
		
		if(Boolean.getBoolean("debug")) {
			File foreignLangFile = new File("sample_subtitles/barbarians-de.srt");
			File primaryLangFile = new File("sample_subtitles/barbarians-en.srt");
			
			try {
				subtitles_panel.loadSubtitles(foreignLangFile, primaryLangFile);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			open_subtitles_menuitem.setEnabled(false);
			close_subtitles_menuitem.setEnabled(true);
			export_menu.setEnabled(true);
		}
	}
	
	private void openClicked()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		chooser.setMultiSelectionEnabled(true);
		
		FileFilter srtFilter = new FileFilter() {
		   public String getDescription() {
		       return "Subtitles (*.srt)";
		   }

		   public boolean accept(File f) {
		       if (f.isDirectory()) {
		           return true;
		       } else {
		           String filename = f.getName().toLowerCase();
		           return filename.endsWith(".srt");
		       }
		   }
		};
		
		chooser.setFileFilter(srtFilter);
		
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
		
		try {
			subtitles_panel.loadSubtitles(foreignLangFile, primaryLangFile);
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
		subtitles_panel.unloadSubtitles();
		open_subtitles_menuitem.setEnabled(true);
		close_subtitles_menuitem.setEnabled(false);
		export_menu.setEnabled(false);
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
		
		WFRQFrame frame = new WFRQFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(1183, 739);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
