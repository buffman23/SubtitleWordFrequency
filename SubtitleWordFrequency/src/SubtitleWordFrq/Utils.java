package SubtitleWordFrq;
import java.io.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gui.WordTableModel;

public class Utils {
	public static final Logger logger = Logger.getLogger(Utils.class.getName());
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public static<T> T deserialize(String filepath, Class<T> clazz)
	{
		File file = new File(filepath);
		if(file.exists()) {
			try(FileReader fr = new FileReader(file)){
				return gson.fromJson(fr, clazz);
			} catch (IOException e) {
				logger.severe(e.getMessage());
			}
		} else {
			logger.severe(String.format("Failed to deserialize file. File does not exist %s", filepath));
		}
		
		return null;
	}
	
	public static<T> void serialize(T object, String filepath, Class<T> clazz)
	{
		File file = new File(filepath);

		try(FileWriter fr = new FileWriter(file)){
			gson.toJson(object, clazz, fr);
		} catch (IOException e) {
			logger.severe(e.getMessage());
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
		for(int i = 0; i < columnCount - 1; ++i) {
			columnIndexTable[i] = table.convertColumnIndexToModel(i); 
		}
		
		try(PrintWriter pw = new PrintWriter(outFile))
		{
			// print column headers
			for(int i = 0; i < columnCount - 1; ++i) {
				pw.print(tableModel.getColumnName(columnIndexTable[i]));
				pw.print(',');
			}
			// print last value separately avoid extra comma
			pw.println(tableModel.getColumnName(columnCount - 1));
			
			int[] rows = selectedRows.toArray();
			for(int i = 0; i < rows.length; ++i) {
				int rowIndex = table.convertRowIndexToModel(rows[i]);
				for(int j = 0; j < columnCount - 1; ++j) {
					String escapedValue = StringEscapeUtils.escapeCsv(tableModel.getValueAt(rowIndex, columnIndexTable[j]).toString());
					pw.print(escapedValue);
					pw.print(',');
				}
				// print last value separately avoid extra comma
				if(i == rows.length - 1) {
					// don't add newline for last line in csv file
					pw.print(tableModel.getValueAt(i, columnCount - 1));
				} else {
					pw.println(tableModel.getValueAt(i, columnCount - 1));

				}
			}
		}
	}
}
