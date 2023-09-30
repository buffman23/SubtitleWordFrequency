package gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import SubtitleWordFrq.Utils;

public class ExportMenu extends JMenu
{
	
	private JMenuItem exportHiddenWordListMenuItem;
	private JTable table;
	
	public ExportMenu(JTable table, boolean popupExportMenu)
	{
		super("Export");
		
		this.table = table;
		
		if(popupExportMenu) {
			JMenu exportCSVMenu = exportCSVMenu = new JMenu("CSV");
			JMenuItem exportCSVAllMenuItem = new JMenuItem("All");
			exportCSVAllMenuItem.addActionListener(e -> exportCSVClicked(true));
			exportCSVMenu.add(exportCSVAllMenuItem);
			
			JMenuItem exportCSVSelectedMenuItem = new JMenuItem("Selected");
			exportCSVSelectedMenuItem.addActionListener(e -> exportCSVClicked(false));
			exportCSVMenu.add(exportCSVSelectedMenuItem);
			this.add(exportCSVMenu);
		} else {
			JMenuItem exportCSVMenuItem = new JMenuItem("CSV");
			exportCSVMenuItem.addActionListener(e -> exportCSVClicked(true));
			this.add(exportCSVMenuItem);
		}
		
		exportHiddenWordListMenuItem = new JMenuItem("Hidden Word List");
		exportHiddenWordListMenuItem.addActionListener(e -> exportHiddenWordListClicked());
		exportHiddenWordListMenuItem.setEnabled(false);
		
		
		this.add(exportHiddenWordListMenuItem);
	}
	
	private void exportCSVClicked(boolean exportAll)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		
		FileFilter csvFilter = new FileFilter() {
		   public String getDescription() {
		       return "Comma Separated Values (*.csv)";
		   }

		   public boolean accept(File f) {
		       if (f.isDirectory()) {
		           return true;
		       } else {
		           String filename = f.getName().toLowerCase();
		           return filename.endsWith(".csv");
		       }
		   }
		};
		
		chooser.setFileFilter(csvFilter);
		
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
			
			JOptionPane.showMessageDialog(null, "Exported to " + selectedFile.getName(), "Successful CSV Export", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Failed CSV Export", JOptionPane.ERROR_MESSAGE);
			Utils.logger.severe(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	private void exportHiddenWordListClicked()
	{
		
	}
}
