package gui;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

import SubtitleWordFrq.Word;

public class WordTableModel extends AbstractTableModel {

	public static final int WORD_COLUMN = 0,  COUNT_COLUMN = 1, DEFINITION_COLUMN = 2, HIDDEN_COLUMN = 3;
	private static final String[] COLUMN_NAMES = new String[] { "Word", "Count", "Definition", "Hidden" };
	private List<Word> wordFrequencyList;
	private List<Word> notHiddenList;
	private boolean hiddenColumnEnabled;
	private static final Predicate<Word> IS_NOT_HIDDEN_PREDICATE = word -> !word.isHidden();

	public WordTableModel(List<Word> wordFrequencyList)
	{
		this.hiddenColumnEnabled = true;
		setWordList(wordFrequencyList);
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		if(columnIndex == HIDDEN_COLUMN || columnIndex == DEFINITION_COLUMN)
			return true;
		
		return false;
	}
	
	@Override
	public String getColumnName(int columnIndex)
	{
		return COLUMN_NAMES[columnIndex];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		if(columnIndex == WORD_COLUMN)
			return Word.class;
		
		if(columnIndex == DEFINITION_COLUMN)
			return String.class;
		
		if(columnIndex == COUNT_COLUMN)
			return Integer.class;
		
		if(columnIndex == HIDDEN_COLUMN)
			return Boolean.class;
					
		return Object.class;	
	}
	
	@Override
	public int getRowCount() {
		if(wordFrequencyList == null)
			return 0;
		
		if(!hiddenColumnEnabled)
			return notHiddenList.size();
		
		return wordFrequencyList.size();
	}

	@Override
	public int getColumnCount() {
		return 3 + (hiddenColumnEnabled ? 1 : 0);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		Word word;
		if(hiddenColumnEnabled) {
			word = wordFrequencyList.get(rowIndex);
		} else {
			word = notHiddenList.get(rowIndex);
		}
		
		if(columnIndex == WORD_COLUMN)
			return word.toString();
		if(columnIndex == DEFINITION_COLUMN)
			return word.getDefiniton();
		if(columnIndex == COUNT_COLUMN)
			return word.getCount();
		if(columnIndex == HIDDEN_COLUMN)
			return word.isHidden();
		
		return "";
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex)
	{
		if(columnIndex == HIDDEN_COLUMN)
			wordFrequencyList.get(rowIndex).setHidden((Boolean)value);
		
		if(columnIndex == DEFINITION_COLUMN)
			wordFrequencyList.get(rowIndex).setDefiniton(value.toString());
	}
	
	public List<Word> getWordList() {
		if(wordFrequencyList == null)
			return null;
		
		return Collections.unmodifiableList(wordFrequencyList);
	}

	public List<Word> getCurrentWordList() {
		if(wordFrequencyList == null)
			return null;
		
		if(hiddenColumnEnabled)
			return Collections.unmodifiableList(wordFrequencyList);
		else 
			return Collections.unmodifiableList(notHiddenList);
	}

	public void setWordList(List<Word> wordFrequencyList) {
		this.fireTableDataChanged();
		this.wordFrequencyList = wordFrequencyList;
		this.notHiddenList = null;
		
		if(!hiddenColumnEnabled)
			validateNotHiddenList();
	}
	
	public boolean isHiddenColumnEnabled() {
		return hiddenColumnEnabled;
	}

	public void setHiddenColumnEnabled(boolean hiddenColumnEnabled) {
		this.hiddenColumnEnabled = hiddenColumnEnabled;
		validateNotHiddenList();
		fireTableStructureChanged();
	}
	
	private void validateNotHiddenList()
	{
		if(wordFrequencyList == null)
		{
			notHiddenList = null;
			return;
		}
		
		boolean allNotHidden = notHiddenList != null && notHiddenList.stream().allMatch(IS_NOT_HIDDEN_PREDICATE);
		if(!allNotHidden) {
			notHiddenList = wordFrequencyList.stream().filter(IS_NOT_HIDDEN_PREDICATE).collect(Collectors.toList());
		}
	}
}
