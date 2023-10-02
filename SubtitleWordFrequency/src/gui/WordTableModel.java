package gui;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;
import SubtitleWordFrq.Caption;
import SubtitleWordFrq.DocumentSubtitles;
import SubtitleWordFrq.Word;

public class WordTableModel extends AbstractTableModel {

	public static final int WORD_COLUMN = 0,  DEFINITION_COLUMN = 1, COUNT_COLUMN = 2,
			FOREIGN_EXAMPLE = 3, PRIMARY_EXAMPLE = 4, HIDDEN_COLUMN = 5;
	private static final String[] COLUMN_NAMES = new String[] { "Word", "Definition", "Count", "Foreign Example", "Primary Example", "Hidden" };
	private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] { 
		String.class, String.class, Integer.class, String.class, String.class, Boolean.class
	};
	private List<Word> wordFrequencyList;
	private List<Word> notHiddenList;
	private DocumentSubtitles documentSubtitles;
	private boolean hiddenColumnEnabled;
	private static final Predicate<Word> IS_NOT_HIDDEN_PREDICATE = word -> !word.isHidden();

	public WordTableModel(List<Word> wordFrequencyList, DocumentSubtitles documentSubtitles)
	{
		this.hiddenColumnEnabled = true;
		this.setData(wordFrequencyList, documentSubtitles);
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
		return COLUMN_CLASSES[columnIndex];	
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
		return 5 + (hiddenColumnEnabled ? 1 : 0);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		
		Word word;
		if(hiddenColumnEnabled) {
			word = wordFrequencyList.get(rowIndex);
		} else {
			word = notHiddenList.get(rowIndex);
		}
		
		switch(columnIndex) 
		{
			case WORD_COLUMN:
				return word.toString();
			case DEFINITION_COLUMN:
				return word.getDefiniton();
			case COUNT_COLUMN:
				return word.getCount();
			case FOREIGN_EXAMPLE:
				return word.getSelectedReference().text;
			case PRIMARY_EXAMPLE:
				Caption selectedReference = word.getSelectedReference();
				if(documentSubtitles.getPairedCaptions(selectedReference).stream()
						.anyMatch(pair -> pair.right == DocumentSubtitles.NO_MATCH))
					return "";
				return documentSubtitles.getPairedCaptions(selectedReference).stream()
						.map(pair -> pair.left)
						.map(caption -> caption.text)
						.collect(Collectors.joining(" "));
						//TODO make joining string configurable
			case HIDDEN_COLUMN:
				return word.isHidden();
		}
		
		return null;
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex)
	{
		if(columnIndex == HIDDEN_COLUMN)
			getCurrentWordList().get(rowIndex).setHidden((Boolean)value);
		
		if(columnIndex == DEFINITION_COLUMN)
			getCurrentWordList().get(rowIndex).setDefiniton(value.toString());
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

	public void setData(List<Word> wordFrequencyList, DocumentSubtitles documentSubtitles) {
		this.wordFrequencyList = wordFrequencyList;
		this.documentSubtitles = documentSubtitles;
		this.notHiddenList = null;
		
		if(!hiddenColumnEnabled)
			validateNotHiddenList();
		this.fireTableDataChanged();
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
