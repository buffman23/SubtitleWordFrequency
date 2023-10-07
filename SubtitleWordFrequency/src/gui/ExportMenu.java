package gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FileUtils;

import com.google.gson.reflect.TypeToken;

import SubtitleWordFrq.SerializableWord;
import SubtitleWordFrq.Utils;
import SubtitleWordFrq.Word;

public class ExportMenu extends JMenu
{
	
	private JMenuItem exportHiddenWordListMenuItem;
	private JTable table;
	
	public ExportMenu(JTable table, boolean popupExportMenu)
	{
		super("Export");
		
		this.table = table;
		
		JMenu exportCSVMenu = new JMenu("CSV");
		JMenuItem exportCSVAllMenuItem = new JMenuItem("All");
		exportCSVAllMenuItem.addActionListener(e -> exportCSVClicked(true));
		exportCSVMenu.add(exportCSVAllMenuItem);
		
		JMenuItem exportCSVSelectedMenuItem = new JMenuItem("Selected");
		exportCSVSelectedMenuItem.addActionListener(e -> exportCSVClicked(false));
		exportCSVMenu.add(exportCSVSelectedMenuItem);
		this.add(exportCSVMenu);
		
		JMenuItem exportWordDataMenuItem = new JMenuItem("Table data");
		exportWordDataMenuItem.setToolTipText("Export editable table data (definitions, tags, hidden)");
		exportWordDataMenuItem.addActionListener(e -> exportWordDataClicked());
		add(exportWordDataMenuItem);
	}
	
	private void exportCSVClicked(boolean exportAll)
	{
		JFileChooser chooser = Utils.fileChooser("Comma Separated Values (*.csv)", ".csv");
		
		if(chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
		{
			return;
		}
		
		// add .csv extension if missing
		File selectedFile = chooser.getSelectedFile();
		if(!selectedFile.getName().toLowerCase().endsWith(".csv")) {
			selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
		}
		
		try {
			Utils.TableToCSV(table, exportAll, selectedFile);
			
			JOptionPane.showMessageDialog(null, "Exported to " + selectedFile.getName(), "Successful CSV export", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Failed CSV Export", JOptionPane.ERROR_MESSAGE);
			Utils.logger.severe(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	private void exportWordDataClicked()
	{
		JFileChooser chooser = Utils.fileChooser("Word Table (*.wrdtbl)", ".wrdtbl");
		
		if(chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
		{
			return;
		}
		
		File selectedFile = chooser.getSelectedFile();
		
		// add .json extension if missing
		if(!selectedFile.getName().toLowerCase().endsWith(".json")) {
			selectedFile = new File(selectedFile.getAbsolutePath() + ".json");
		}
		
		WordTableModel wordTableModel = (WordTableModel)table.getModel();
		List<SerializableWord> wordList = wordTableModel.getSerializableWords();
		
		try {
			Utils.serialize(wordList, selectedFile, new TypeToken<List<SerializableWord>>(){}.getType());
			JOptionPane.showMessageDialog(null, "Exported to " + selectedFile.getName(), "Successful words export", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			Utils.logger.severe(e.getMessage());
			JOptionPane.showMessageDialog(null, e.getMessage(), "Failed words export", JOptionPane.INFORMATION_MESSAGE);
			//e.printStackTrace();
		}
	}
}
