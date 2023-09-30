package SubtitleWordFrq;
import java.io.*;
import java.util.IllegalFormatCodePointException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.management.InstanceNotFoundException;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.internal.Streams;

import gui.WordTableModel;

public class Utils {
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
			
			for(int i : selectedRows.toArray()) {
				int rowIndex = table.convertRowIndexToModel(i);
				for(int j = 0; j < columnCount - 1; ++j) {
					String escapedValue = StringEscapeUtils.escapeCsv(tableModel.getValueAt(rowIndex, columnIndexTable[j]).toString());
					pw.print(escapedValue);
					pw.print(',');
				}
				// print last value separately avoid extra comma
				pw.println(tableModel.getValueAt(i, columnCount - 1));
			}
		}
	}
}
