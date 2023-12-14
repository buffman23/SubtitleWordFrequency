package SubtitleWordFrq;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gui.WordTableModel;

public class Utils {
	public static final Logger logger = Logger.getLogger(Utils.class.getName());
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public static Document createDocument(File file, String content) throws IOException
	{
		String extension = FilenameUtils.getExtension(file.getName());
		switch (extension)
		{
		case "srt":
			return new DocumentSubtitles(content);
		default:
			return new DocumentPlainText(content);
		}
	}
	
	
	public static<T> T deserialize(String filepath, Type type) throws IOException
	{
		return deserialize(new File(filepath), type);
	}
	
	public static<T> T deserialize(File file, Type type) throws IOException
	{
		if(file.exists()) {
			try(FileReader fr = new FileReader(file, Charset.forName("UTF-8"))){
				return gson.fromJson(fr, type);
			}
		}
		
		throw new IOException(String.format("Failed to deserialize file. File does not exist %s", file.getAbsolutePath()));
		
	}
	
	public static<T> void serialize(T object, String filepath, Type type) throws IOException
	{
		serialize(object, new File(filepath), type);
	}
	
	public static<T> void serialize(T object, File file, Type type) throws IOException
	{
		try(FileWriter fr = new FileWriter(file, Charset.forName("UTF-8"))){
			gson.toJson(object, type, fr);
		}
	}
	
	public static List<List<String>> loadCSV(File csvFile)
	{
		return null;
	}
	
	public static void saveCSV(File csvFile, List<List<String>> csvData) throws FileNotFoundException
	{
		try(PrintWriter pw = new PrintWriter(csvFile))
		{
			pw.print(csvData.stream()
				.map(csvEntry -> csvEntry.stream().collect(Collectors.joining(",")))
				.collect(Collectors.joining("\n"))
			);
		}
	}
	
	/**
	 * Loads a plain text file with values delimited by newline.
	 * 
	 * @param plain text file
	 * @return lines of the plain text file
	 * @throws IOException 
	 */
	public static List<String> loadPlainText(File file) throws IOException
	{
		return FileUtils.readLines(file, Charset.forName("UTF-8"));
	}
	
	public static void savePlainText(File file, List<String> data) throws FileNotFoundException
	{
		try(PrintWriter pw = new PrintWriter(file))
		{
			pw.print(data.stream().collect(Collectors.joining("\n")));
		}
	}
	
	public static void TableToCSV(JTable table, boolean exportAll, File outFile) throws IOException
	{
		TableModel tableModel = table.getModel();
		
		int columnCount = tableModel.getColumnCount();
		if(tableModel instanceof WordTableModel) {
			// never print the hidden column for csv export.
			columnCount -= ((WordTableModel)tableModel).isHiddenColumnEnabled() ? 1 : 0;
		}
		
		
		IntStream selectedRows;
		
		if(exportAll)
			selectedRows = IntStream.range(0, table.getRowCount());
		else
			selectedRows = IntStream.of(table.getSelectedRows());
		
		int[] columnIndexTable = new int[columnCount];
		for(int i = 0; i < columnCount; ++i) {
			columnIndexTable[i] = table.convertColumnIndexToModel(i); 
		}
		
		try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8")))
		{
			// print column headers
			//for(int i = 0; i < columnCount - 1; ++i) {
			//	pw.print(tableModel.getColumnName(columnIndexTable[i]));
			//	pw.print(',');
			//}
			
			// print last value separately avoid extra comma
			//pw.println(tableModel.getColumnName(columnCount - 1));
			
			int[] rows = selectedRows.toArray();
			for(int i = 0; i < rows.length; ++i) {
				int rowIndex = table.convertRowIndexToModel(rows[i]);
				for(int j = 0; j < columnCount - 1; ++j) {
					String csvValue = tableModel.getValueAt(rowIndex, columnIndexTable[j]).toString().replace("\n", " ");
					String escapedValue = StringEscapeUtils.escapeCsv(csvValue);
					pw.print(escapedValue);
					pw.print(',');
				}
				
				// print last value separately avoid extra comma
				String csvValue = tableModel.getValueAt(rowIndex, columnIndexTable[columnCount - 1]).toString().replace("\n", " ");
				String escapedValue = StringEscapeUtils.escapeCsv(csvValue);
				if(i == rows.length - 1) {
					// don't add newline for last line in csv file
					pw.print(escapedValue);
				} else {
					pw.println(escapedValue);
				}
			}
		}
	}
	
	public static JFileChooser fileChooser(String description, String fileExtension)
	{
		return fileChooser(description, fileExtension, false);
	}
	
	public static JFileChooser fileChooser(String description, String fileExtension, boolean multiselect)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(multiselect);
		chooser.setCurrentDirectory(new File("."));
		
		FileFilter csvFilter = new FileFilter() {
		   public String getDescription() {
		       return description;
		   }

		   public boolean accept(File f) {
		       if (f.isDirectory()) {
		           return true;
		       } else {
		           String filename = f.getName().toLowerCase();
		           return filename.endsWith(fileExtension);
		       }
		   }
		};
		
		chooser.setFileFilter(csvFilter);
		
		return chooser;
	}
	
	public static<E extends Comparable<E>> int insertSorted(List<E> list, E item)
	{
		int insertIdx = Collections.binarySearch(list, item);
		if(insertIdx < 0) {
			insertIdx = -(insertIdx + 1);
		}
		list.add(insertIdx, item);
		
		return insertIdx;
	}
	
	public static<E extends Comparable<E>> boolean removeSorted(List<E> list, E item)
	{
		int removeIdx = Collections.binarySearch(list, item);
		if(removeIdx < 0) {
			return false;
		}
		
		list.remove(removeIdx);
		return true;
	}
	
	public static void updateRowHeight(JTable table, int margin) {
	    final int rowCount = table.getRowCount();
	    final int colCount = table.getColumnCount();
	    for (int i = 0; i < rowCount; i++) {
	        int maxHeight = 0;
	        for (int j = 0; j < colCount; j++) {
	            final TableCellRenderer renderer = table.getCellRenderer(i, j);
	            maxHeight = Math.max(maxHeight, table.prepareRenderer(renderer, i, j).getPreferredSize().height);
	        }
	        table.setRowHeight(i, maxHeight + margin);
	    }
	}
}
