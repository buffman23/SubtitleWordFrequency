package gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.tuple.ImmutablePair;

import SubtitleWordFrq.Caption;
import SubtitleWordFrq.DocumentSubtitles;
import SubtitleWordFrq.Hidden;
import SubtitleWordFrq.SerializableWord;
import SubtitleWordFrq.Word;

public class WordTableModel extends AbstractTableModel {

	public static final int WORD_COLUMN = 0,  DEFINITION_COLUMN = 1, TAGS_COLUMN = 2, COUNT_COLUMN = 3,
			FOREIGN_EXAMPLE = 4, PRIMARY_EXAMPLE = 5, HIDDEN_COLUMN = 6;
	private static final String[] COLUMN_NAMES = new String[] { "Word", "Definition", "Tags", "Count", "Foreign Example", "Primary Example", "Hidden" };
	private static final Class<?>[] COLUMN_CLASSES = new Class<?>[] { 
		Word.class, String.class, String.class,Integer.class, String.class, String.class, Hidden.class
	};
	private static final Predicate<Word> IS_NOT_HIDDEN_PREDICATE = word -> word.getHidden() == Hidden.OFF;
	
	private List<Word> wordFrequencyList;
	private List<Word> notHiddenList;
	private DocumentSubtitles documentSubtitles;
	private boolean hiddenColumnEnabled;
	private Integer initialHashCode;

	public WordTableModel(List<Word> wordFrequencyList, DocumentSubtitles documentSubtitles)
	{
		this.hiddenColumnEnabled = true;
		this.setWordList(wordFrequencyList, documentSubtitles);
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		if(columnIndex == HIDDEN_COLUMN || columnIndex == DEFINITION_COLUMN || columnIndex == TAGS_COLUMN)
			return true;
		
		if(columnIndex == WORD_COLUMN)
			return getCurrentWordList().get(rowIndex).isGroup();
		
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
		return 6 + (hiddenColumnEnabled ? 1 : 0);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(wordFrequencyList == null)
			return null;
		
		Word word;
		if(hiddenColumnEnabled) {
			word = wordFrequencyList.get(rowIndex);
		} else {
			word = notHiddenList.get(rowIndex);
		}
		
		switch(columnIndex) 
		{
			case WORD_COLUMN:
				return word;
			case DEFINITION_COLUMN:
				return word.getDefiniton();
			case TAGS_COLUMN:
				if(word.getTags() == null)
					return "";
				return word.getTags().stream().collect(Collectors.joining(","));
			case COUNT_COLUMN:
				return word.getCount();
			case FOREIGN_EXAMPLE:
				return word.getSelectedReference().text;
			case PRIMARY_EXAMPLE:
				Caption selectedReference = word.getSelectedReference();
				List<ImmutablePair<Caption, Integer>> pairedCaptions = documentSubtitles.getPairedCaptions(selectedReference);
				if(pairedCaptions == null ||  pairedCaptions.stream()
						.anyMatch(pair -> pair.right == DocumentSubtitles.NO_MATCH))
					return "";
				return pairedCaptions.stream()
						.map(pair -> pair.left)
						.map(caption -> caption.text.toString().replace("\n", " "))
						.collect(Collectors.joining(" "));
						//TODO make joining string configurable
			case HIDDEN_COLUMN:
				return word.getHidden();
		}
		
		return null;
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex)
	{
		if(wordFrequencyList == null)
			return;
		
		if(columnIndex == HIDDEN_COLUMN) {
			if(value instanceof Boolean) {
				Boolean b = (Boolean)value;
				value = b ? Hidden.ON : Hidden.OFF;
			}
			getCurrentWordList().get(rowIndex).setHidden((Hidden)value);
		}
		
		if(columnIndex == DEFINITION_COLUMN)
			getCurrentWordList().get(rowIndex).setDefiniton(value.toString());
		
		if(columnIndex == TAGS_COLUMN) {
			
			getCurrentWordList().get(rowIndex).setTags(Arrays.asList(value.toString().replace(" ", "").split(",")));
		}
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
		setWordList(wordFrequencyList, null);
	}

	public void setWordList(List<Word> wordFrequencyList, DocumentSubtitles documentSubtitles) {
		// only record hashcode the first time the model's source list is set
		if(this.wordFrequencyList == null && wordFrequencyList != null)
			initialHashCode = wordFrequencyList.hashCode();
		
		this.wordFrequencyList = wordFrequencyList;
		this.notHiddenList = null;
		if(documentSubtitles != null)
			this.documentSubtitles = documentSubtitles;
		
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
	
	public List<SerializableWord> getSerializableWords()
	{
		if(wordFrequencyList == null)
			return List.of();
		
		return wordFrequencyList.stream()
			.filter(word -> word.getHidden() == Hidden.ON || word.getDefiniton().length() > 0 || 
					(word.getAssociatedWords() != null && word.getAssociatedWords().size() != 0) ||
					word.getTags() != null && word.getTags().size() > 0)
			.map(word -> new SerializableWord(word))
			.collect(Collectors.toList());
	}
	
	private void validateNotHiddenList()
	{
		if(wordFrequencyList == null)
		{
			notHiddenList = null;
			return;
		}
		
		notHiddenList = wordFrequencyList.stream().filter(IS_NOT_HIDDEN_PREDICATE).collect(Collectors.toList());
	}

	public boolean isModified() {
		if(wordFrequencyList == null || initialHashCode == null)
			return false;
		
		int currentHashCode = wordFrequencyList.hashCode();
		return initialHashCode != currentHashCode;
	}
}
