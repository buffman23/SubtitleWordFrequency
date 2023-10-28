package gui;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.gson.reflect.TypeToken;

import SubtitleWordFrq.Caption;
import SubtitleWordFrq.DocumentSubtitles;
import SubtitleWordFrq.ImportedGroup;
import SubtitleWordFrq.SerializableWord;
import SubtitleWordFrq.Utils;
import SubtitleWordFrq.Word;
import SubtitleWordFrq.WordFrequencyParser;

import javax.swing.SwingConstants;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.border.EmptyBorder;
import java.awt.Font;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Component;
import javax.swing.Box;

public class SubtitlesPanel extends JPanel {
	private static final SimpleAttributeSet YELLOW_HIGHLIGHT;
	private static final SimpleAttributeSet ORANGE_HIGHLIGHT;
	private static final SimpleAttributeSet NO_HIGHLIGHT;
	static {
		YELLOW_HIGHLIGHT = new SimpleAttributeSet();
	    StyleConstants.setBackground(YELLOW_HIGHLIGHT, Color.YELLOW);
	    
	    ORANGE_HIGHLIGHT = new SimpleAttributeSet();
	    StyleConstants.setBackground(ORANGE_HIGHLIGHT, Color.ORANGE);
	    
	    NO_HIGHLIGHT = new SimpleAttributeSet();
	    StyleConstants.setBackground(NO_HIGHLIGHT, new Color(0, 0, 0, 0));
	}
	
	private boolean loaded;
	private String foreignSubtitleString;
	private String primarySubtitleString;
	
	// Words that were imported but are not present in the subtitles, so are not part of the table.
	// They must be saved to be later joined back in for exports.
	private List<SerializableWord> nonPresentWords;
	private HashMap<String,ImportedGroup> importedGroups;
	private SubtitleTextPane foreign_textpane;
	private SubtitleTextPane primary_textpane;
	private WordTable wordTable;
	private WordTableModel wordTableModel;
	private Word selectedWord;
	private int currentGoToReference;
	private int initialGoToReference = -1;
	private JLabel nav_word_label;
	private JScrollPane scrollPane_foreign;
	private JScrollPane scrollPane_primary;
	private JButton toggleHidenButton;
	private JButton next_button;
	private JButton prev_button;

	private DocumentSubtitles foreignDocumentSubtitles;
	private JLabel table_info_label;
	private WordFrequencyParser wordFrequencyParser;
	private JScrollPane scrollPane_table;
	
	public SubtitlesPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel horz_flow_panel = new JPanel();
		add(horz_flow_panel);
		horz_flow_panel.setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.3);
		horz_flow_panel.add(splitPane_1);
		
		JPanel table_panel = new JPanel();
		splitPane_1.setLeftComponent(table_panel);
		GridBagLayout gbl_table_panel = new GridBagLayout();
		gbl_table_panel.columnWeights = new double[]{1.0};
		gbl_table_panel.rowWeights = new double[]{1.0, 0.0};
		table_panel.setLayout(gbl_table_panel);
		
		wordTableModel = new WordTableModel(null, null);
				
		scrollPane_table = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		table_panel.add(scrollPane_table, gbc_scrollPane);
		wordTable = new WordTable();
		wordTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		wordTable.setFillsViewportHeight(true);
		wordTable.setCellSelectionEnabled(true);
		wordTable.getSelectionModel().addListSelectionListener(e -> wordTableRowSelected(e));
		scrollPane_table.setViewportView(wordTable);
		wordTable.setModel(wordTableModel);
		JPanel sub_view_panel = new JPanel();
		splitPane_1.setRightComponent(sub_view_panel);
		sub_view_panel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel foreign_panel = new JPanel();
		foreign_panel.setBorder(new EmptyBorder(2, 2, 2, 2));
		sub_view_panel.add(foreign_panel);
		foreign_panel.setLayout(new BorderLayout(0, 0));
		
		scrollPane_foreign = new JScrollPane();
		foreign_panel.add(scrollPane_foreign);
		
		foreign_textpane = new SubtitleTextPane(null);
		foreign_textpane.setEditable(false);
		foreign_textpane.setComponentPopupMenu(new ForeignSubsPopup());
		foreign_textpane.addMouseListener(new WordClickAdapter());
		scrollPane_foreign.setViewportView(foreign_textpane);
		
		JLabel foreign_label = new JLabel("Foreign Language Subtitles");
		foreign_label.setHorizontalAlignment(SwingConstants.CENTER);
		foreign_panel.add(foreign_label, BorderLayout.NORTH);
		
		JPanel primary_panel = new JPanel();
		primary_panel.setBorder(new EmptyBorder(2, 2, 2, 2));
		sub_view_panel.add(primary_panel);
		primary_panel.setLayout(new BorderLayout(0, 0));
		
		scrollPane_primary = new JScrollPane();
		primary_panel.add(scrollPane_primary);
		
		primary_textpane = new SubtitleTextPane(null);
		primary_textpane.setEditable(false);
		primary_textpane.setComponentPopupMenu(new PrimarySubsPopup());
		scrollPane_primary.setViewportView(primary_textpane);
		
		JLabel native_label = new JLabel("Primary Language Subtitles");
		native_label.setHorizontalAlignment(SwingConstants.CENTER);
		primary_panel.add(native_label, BorderLayout.NORTH);
		
		JPanel bottom_panel = new JPanel();
		GridBagConstraints gbc_bottom_panel = new GridBagConstraints();
		gbc_bottom_panel.anchor = GridBagConstraints.WEST;
		gbc_bottom_panel.fill = GridBagConstraints.VERTICAL;
		gbc_bottom_panel.gridx = 0;
		gbc_bottom_panel.gridy = 1;
		table_panel.add(bottom_panel, gbc_bottom_panel);
		GridBagLayout gbl_bottom_panel = new GridBagLayout();
		gbl_bottom_panel.columnWeights = new double[]{0.0, 0.0};
		gbl_bottom_panel.rowWeights = new double[]{0.0, 0.0};
		bottom_panel.setLayout(gbl_bottom_panel);
		
		table_info_label = new JLabel("0 Words, 0 Selected");
		GridBagConstraints gbc_table_info_label = new GridBagConstraints();
		gbc_table_info_label.fill = GridBagConstraints.HORIZONTAL;
		gbc_table_info_label.insets = new Insets(0, 5, 5, 0);
		gbc_table_info_label.gridx = 0;
		gbc_table_info_label.gridy = 0;
		bottom_panel.add(table_info_label, gbc_table_info_label);
		
		JPanel nav_panel = new JPanel();
		nav_panel.setBorder(new TitledBorder(null, "Go to Reference", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_nav_panel = new GridBagConstraints();
		gbc_nav_panel.insets = new Insets(0, 0, 0, 5);
		gbc_nav_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_nav_panel.gridx = 0;
		gbc_nav_panel.gridy = 1;
		bottom_panel.add(nav_panel, gbc_nav_panel);
		GridBagLayout gbl_nav_panel = new GridBagLayout();
		gbl_nav_panel.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_nav_panel.rowWeights = new double[]{0.0};
		nav_panel.setLayout(gbl_nav_panel);
		
		prev_button = new JButton("Previous");
		prev_button.setEnabled(false);
		prev_button.addActionListener(e -> prevRefBtnClicked());
		
		Component horizontalStrut = Box.createHorizontalStrut(100);
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.gridx = 0;
		gbc_horizontalStrut.gridy = 0;
		nav_panel.add(horizontalStrut, gbc_horizontalStrut);
		
		nav_word_label = new JLabel("(0/0)");
		nav_word_label.setFont(new Font("Tahoma", Font.PLAIN, 15));

		GridBagConstraints gbc_nav_word_label = new GridBagConstraints();
		gbc_nav_word_label.insets = new Insets(0, 0, 0, 5);
		gbc_nav_word_label.anchor = GridBagConstraints.WEST;
		gbc_nav_word_label.gridx = 0;
		gbc_nav_word_label.gridy = 0;
		nav_panel.add(nav_word_label, gbc_nav_word_label);
		GridBagConstraints gbc_prev_button = new GridBagConstraints();
		gbc_prev_button.fill = GridBagConstraints.HORIZONTAL;
		gbc_prev_button.insets = new Insets(0, 0, 0, 5);
		gbc_prev_button.anchor = GridBagConstraints.NORTHWEST;
		gbc_prev_button.gridx = 1;
		gbc_prev_button.gridy = 0;
		nav_panel.add(prev_button, gbc_prev_button);
		
		next_button = new JButton("Next");
		next_button.setEnabled(false);
		next_button.addActionListener(e ->nextRefBtnClicked());
		GridBagConstraints gbc_next_button = new GridBagConstraints();
		gbc_next_button.fill = GridBagConstraints.HORIZONTAL;
		gbc_next_button.anchor = GridBagConstraints.NORTHWEST;
		gbc_next_button.gridx = 2;
		gbc_next_button.gridy = 0;
		nav_panel.add(next_button, gbc_next_button);
		
		int maxWidthButton = Integer.max(prev_button.getPreferredSize().width, next_button.getPreferredSize().width);
		prev_button.setPreferredSize(new Dimension(maxWidthButton, prev_button.getPreferredSize().height));
		next_button.setPreferredSize(new Dimension(maxWidthButton, next_button.getPreferredSize().height));
		
		toggleHidenButton = new JButton("Hide Hidden");
		toggleHidenButton.setEnabled(false);
		toggleHidenButton.addActionListener(e -> toggleHidden());
		GridBagConstraints gbc_toggleHidenButton = new GridBagConstraints();
		gbc_toggleHidenButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_toggleHidenButton.gridx = 1;
		gbc_toggleHidenButton.gridy = 1;
		bottom_panel.add(toggleHidenButton, gbc_toggleHidenButton);
		
		prev_button.setMinimumSize(prev_button.getPreferredSize());
		next_button.setMinimumSize(prev_button.getPreferredSize());
		
		updateTableInfoText();
		nonPresentWords = new ArrayList<>();
		importedGroups = new HashMap<>();
	}
	
	private void wordTableRowSelected(ListSelectionEvent e)
	{
		updateTableInfoText();
		
		if(e.getValueIsAdjusting())
			return;
		
		int selectedRow = wordTable.getSelectedRow();
		if(selectedRow == -1) { // happens when hiding rows.
			selectedWord = null;
			setReferenceNavEnabled(false);
			highlightCaption(foreign_textpane, null);
			highlightCaption(primary_textpane, null);
			return;
		}
		setReferenceNavEnabled(true);
		
		if(wordTable.getSelectedColumn() == WordTableModel.HIDDEN_COLUMN)
			return;
		
		int selectedIndex = wordTable.convertRowIndexToModel(selectedRow);
		
		selectedWord = wordTableModel.getCurrentWordList().get(selectedIndex);
		currentGoToReference = initialGoToReference != -1 ? initialGoToReference : selectedWord.getSelectedReferenceIndex();
		updateReferenceNavText();
		highlightCaption(foreign_textpane, selectedWord.getReferences().get(currentGoToReference));
		
		
		List<ImmutablePair<Caption, Integer>> pairedCaption = foreignDocumentSubtitles.getPairedCaptions(selectedWord.getReferences().get(currentGoToReference));
		if(pairedCaption != null)
			highlightCaptions(primary_textpane, pairedCaption);
	}
	
	private void prevRefBtnClicked()
	{
		currentGoToReference = currentGoToReference - 1;
		if(currentGoToReference == -1)
			currentGoToReference = selectedWord.getReferences().size() - 1;
		highlightCaption(foreign_textpane, selectedWord.getReferences().get(currentGoToReference));
		
		List<ImmutablePair<Caption, Integer>> pairedCaption = foreignDocumentSubtitles.getPairedCaptions(selectedWord.getReferences().get(currentGoToReference));
		if(pairedCaption != null)
			highlightCaptions(primary_textpane, pairedCaption);
		
		updateReferenceNavText();
	}
	
	private void nextRefBtnClicked()
	{
		currentGoToReference = (currentGoToReference + 1) % selectedWord.getReferences().size();
		highlightCaption(foreign_textpane, selectedWord.getReferences().get(currentGoToReference));
		
		List<ImmutablePair<Caption, Integer>> pairedCaption = foreignDocumentSubtitles.getPairedCaptions(selectedWord.getReferences().get(currentGoToReference));
		if(pairedCaption != null)
			highlightCaptions(primary_textpane, pairedCaption);
		
		updateReferenceNavText();
	}
	
	private void highlightCaption(JTextPane textPane, Caption caption)
	{
		highlightCaptions(textPane, caption == null ? null : List.of(new ImmutablePair<Caption,Integer>(caption, DocumentSubtitles.MATCH)));
	}
	
	private void highlightCaptions(JTextPane textPane, List<ImmutablePair<Caption, Integer>> captions)
	{
		// clear existing highlight
		Style defaultStyle = StyleContext.
				   getDefaultStyleContext().
				   getStyle(StyleContext.DEFAULT_STYLE);
		textPane.getStyledDocument().setCharacterAttributes(0, textPane.getDocument().getLength(), NO_HIGHLIGHT, true);
				
		if(selectedWord == null)
			return;
		
		jumpToReference(textPane, captions.get(0).left);
		
		for(ImmutablePair<Caption, Integer> pair : captions) {
			Caption caption = pair.left;
			SimpleAttributeSet highlight = pair.right == DocumentSubtitles.MATCH ? YELLOW_HIGHLIGHT : ORANGE_HIGHLIGHT;
			textPane.getStyledDocument().setCharacterAttributes(caption.textPosition, caption.textLength, highlight, false);
		}
	}
	
	private void toggleHidden()
	{
		if(wordTableModel.isHiddenColumnEnabled()) {
			wordTableModel.setHiddenColumnEnabled(false);
			toggleHidenButton.setText("Show Hidden");
		} else {
			wordTableModel.setHiddenColumnEnabled(true);
			toggleHidenButton.setText("Hide Hidden");
		}
		wordTable.rebuildSorter();
		updateTableInfoText();
	}
	
	private void jumpToReference(JTextPane textPane, Caption caption)
	{
		JScrollPane scrollPane = textPane == foreign_textpane ? scrollPane_foreign : scrollPane_primary;
		Rectangle viewRect;
		int viewportHeight = scrollPane.getSize().height;
		int halfViewportHeight = viewportHeight/2;
		int scrollYPos = scrollPane.getVerticalScrollBar().getValue();
		try {
			viewRect = (Rectangle)textPane.modelToView2D(caption.startPosition);
			CharBuffer captionText = CharBuffer.wrap(textPane.getText()).subSequence(caption.textPosition, caption.textPosition + caption.textLength);
			int captionHeight = viewRect.height * 3 + viewRect.height * StringUtils.countMatches(captionText, "\n");
			if(scrollYPos + viewportHeight < viewRect.y + captionHeight) {
				viewRect.y += halfViewportHeight;
			} else if(scrollYPos > viewRect.y) {
				viewRect.y -= halfViewportHeight;
			}
			
	        textPane.scrollRectToVisible(viewRect);
	        
	        //viewRect = (Rectangle)foreign_textpane.modelToView2D(caption.textPosition + caption.textLength);
	        //foreign_textpane.scrollRectToVisible(viewRect);
		} catch (BadLocationException e) {
			Utils.logger.severe(e.getMessage());
			//e.printStackTrace();
		}
        
	}
	
	public void loadSubtitles(File foreignLangFile, File primaryLangFile) throws IOException
	{
		foreignSubtitleString = FileUtils.readFileToString(foreignLangFile, Charset.forName("UTF-8"));
		foreignDocumentSubtitles = new DocumentSubtitles(foreignSubtitleString);
		foreign_textpane.setDocumentSubtitles(foreignDocumentSubtitles);
		foreign_textpane.setText(foreignSubtitleString);
		
		wordFrequencyParser = new WordFrequencyParser();
		List<Word> wordList = wordFrequencyParser.createWordFrequencyList(foreignSubtitleString, foreignDocumentSubtitles);
		wordList.sort(Word::compareTo);
		wordTableModel.setWordList(wordList, foreignDocumentSubtitles);
		wordTable.rebuildSorter();
		 
		if(primaryLangFile != null) {
			String primarySubtitles = FileUtils.readFileToString(primaryLangFile, Charset.forName("UTF-8"));
			DocumentSubtitles primaryDocumentSubtitles = new DocumentSubtitles(primarySubtitles);
			primary_textpane.setDocumentSubtitles(primaryDocumentSubtitles);
			primary_textpane.setText(primarySubtitles);
			
			foreignDocumentSubtitles.pairWith(primaryDocumentSubtitles);
		}
		
		// scroll to top of scroll panes
		foreign_textpane.setCaretPosition(0);
		primary_textpane.setCaretPosition(0);
		
		toggleHidenButton.setEnabled(true);
		updateTableInfoText();
		Utils.updateRowHeight(wordTable, 0);
		loaded = true;
	}
	
	public void unloadSubtitles()
	{
		wordTable.setRowSorter(null);
		wordTableModel.setWordList(null, null);
		foreign_textpane.setText("");
		foreign_textpane.setDocumentSubtitles(null);
		primary_textpane.setText("");
		primary_textpane.setDocumentSubtitles(null);
		
		setReferenceNavEnabled(false);
		toggleHidenButton.setEnabled(false);
		updateTableInfoText();
		loaded = false;
		nonPresentWords.clear();
		importedGroups.clear();
	}
	
	public void exportWordData(File file) throws IOException
	{
		Type listType = new TypeToken<List<SerializableWord>>() {}.getType();
		List<SerializableWord> tableWordData = wordTableModel.getSerializableWords();
		List<SerializableWord> combinedWordData = new ArrayList<>(tableWordData.size() + nonPresentWords.size());
		
		for(SerializableWord word : tableWordData) {
			if(word.isGroup()) {
				// compare the current state of a group to it's imported state to merge changes.
				ImportedGroup importedGroup = importedGroups.get(word.value);
				if(importedGroup != null) {
					word = importedGroup.getMergedGroup(word);
				} 
			}
			combinedWordData.add(word);
		}
		combinedWordData.addAll(nonPresentWords);
		combinedWordData.sort(SerializableWord::compareTo);
		Utils.serialize(combinedWordData, file, listType);
	}
	
	public void importWordData(File file)
	{
		nonPresentWords.clear();
		importedGroups.clear();
		try {
			List<SerializableWord> importedWordData = Utils.deserialize(file, new TypeToken<List<SerializableWord>>() {}.getType());
			if(importedWordData == null) {
				return;
			}
			// create a copy of word list
			List<Word> newWordList = new ArrayList<>(wordTableModel.getWordList());
			newWordList.sort(Word::compareTo);
			
			for(SerializableWord importedWord : importedWordData) {
				if(importedWord.isGroup()) {
					List<Word> assocWords = new ArrayList<>(importedWord.associatedWords.size());
					// add imported Word to its own associated words list if not already. This is to prevent
					// a group and a word existing at the same time with the same value.
					if(!importedWord.associatedWords.contains(importedWord.toString())) {
						importedWord.associatedWords.add(importedWord.toString());
					}
					for(String wordString : importedWord.associatedWords) {
						int idx = Collections.binarySearch(newWordList, new Word(wordString));
						if(idx >= 0)
							assocWords.add(newWordList.get(idx));
					}
					// only add group to table if not empty
					if(assocWords.size() > 0) {
						Word group = new Word(importedWord.toString(), assocWords);
						if(importedWord.hidden != null)
							group.setHidden(importedWord.hidden);
						if(importedWord.definition != null)
							group.setDefiniton(importedWord.definition);
						if(importedWord.tags != null) 
							group.setTags(importedWord.tags);
						//if(importedWord.isCapitalized())
							//group.setCapitalized(true);
						newWordList.removeAll(assocWords);
						Utils.insertSorted(newWordList, group);
						importedGroups.put(importedWord.toString(),
							new ImportedGroup(importedWord, assocWords.stream().map(Word::toString).collect(Collectors.toList())));
					} else {
						nonPresentWords.add(importedWord);
					}
				} else {
					int index = Collections.binarySearch(newWordList, new Word(importedWord.value));
					
					if(index >= 0) {
						Word word = newWordList.get(index);
						if(importedWord.hidden != null)
							word.setHidden(importedWord.hidden);
						if(importedWord.definition != null)
							word.setDefiniton(importedWord.definition);
						if(importedWord.tags != null) 
							word.setTags(importedWord.tags);
						if(importedWord.isCapitalized())
							word.setCapitalized(true);
						//word.setAssociatedWords(importedWord.associatedWords);
					} else {
						nonPresentWords.add(importedWord);
					}
				}
			}
			
			wordTableModel.setWordList(newWordList);
			Utils.updateRowHeight(wordTable, 0);
		} catch (IOException e) {
			Utils.logger.severe(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	private void updateReferenceNavText()
	{
		int totalReferences = selectedWord != null ? selectedWord.getReferences().size() : 0;
			
		String text = String.format("%s(%d/%d)", selectedWord, currentGoToReference + 1, totalReferences);
		nav_word_label.setText(text);
	}
	
	private void updateTableInfoText()
	{
		int wordCount = wordTableModel.getWordList() != null ? wordTableModel.getWordList().size() : 0;
		int showingCount = wordTableModel.getRowCount();
		int selectedCount = wordTable.getSelectedRowCount();
		String text;
		if(selectedCount == 1) {
			int selectedRow = wordTable.getSelectedRow();
			text = String.format("%d Words, %d Showing, Row %d selected", 
					wordCount, showingCount, selectedRow);
		} else {
			text = String.format("%d Words, %d Showing, %d Rows selected", 
				wordCount, showingCount, selectedCount);
		}
		
		
		table_info_label.setText(text);
	}
	
	public WordTable getWordTable()
	{
		return wordTable;
	}
	
	public List<Word> getWordList()
	{
		return wordTableModel.getWordList();
	}
	
	private void setReferenceNavEnabled(boolean enabled) {
		prev_button.setEnabled(enabled);
		next_button.setEnabled(enabled); 
		if(!enabled){
			nav_word_label.setText("(0/0)");
		}
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	private class ForeignSubsPopup extends JPopupMenu implements PopupMenuListener
	{
		private JMenuItem setExampleMenuItem;
		
		public ForeignSubsPopup()
		{
			setExampleMenuItem = new JMenuItem();
			setExampleMenuItem.addActionListener(e -> setExampleClicked());
			add(setExampleMenuItem);
			
			addPopupMenuListener(this);
		}
		
		private void setExampleClicked()
		{
			if(selectedWord != null) {
				selectedWord.setSelectedReferenceIndex(currentGoToReference);
				int selectedModelRow = wordTable.convertRowIndexToModel(wordTable.getSelectedRow());
				wordTableModel.fireTableRowsUpdated(selectedModelRow, selectedModelRow);
			}
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			String wordString = "";
			if(selectedWord == null) {
				setExampleMenuItem.setEnabled(false);
			} else {
				wordString = selectedWord.toString();
				setExampleMenuItem.setEnabled(true);
			}
			setExampleMenuItem.setText(String.format("Set as example for \"%s\"", wordString));
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {}
	}
	
	private class PrimarySubsPopup extends JPopupMenu implements PopupMenuListener
	{
		private JMenuItem defineMenuItem;
		private String definitionText;
		
		public PrimarySubsPopup()
		{
			defineMenuItem = new JMenuItem();
			defineMenuItem.addActionListener(e -> defineClicked());
			add(defineMenuItem);
			addPopupMenuListener(this);
		}
		
		private void defineClicked()
		{
			if(selectedWord != null) {
				selectedWord.setDefiniton(definitionText);
				int selectedModelRow = wordTable.convertRowIndexToModel(wordTable.getSelectedRow());
				int selectedModelColumn = wordTable.convertColumnIndexToModel(WordTableModel.DEFINITION_COLUMN);
				wordTableModel.fireTableCellUpdated(selectedModelRow, selectedModelColumn);
			}
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			String wordString = "";
			definitionText = primary_textpane.getHighlightedText();
			if(definitionText == null)
				definitionText = primary_textpane.getHoveredText();
			
			if(selectedWord == null) {
				defineMenuItem.setEnabled(false);
			} else {
				wordString = selectedWord.toString();
				defineMenuItem.setEnabled(definitionText != null);
			}
			defineMenuItem.setText(String.format("Set as definition for \"%s\"", wordString));
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {}
	}
	
	private class WordClickAdapter extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e) {
			Point mousePos = foreign_textpane.getMousePosition();
			if(mousePos == null)
				return;
			
			int start = foreign_textpane.viewToModel2D(mousePos);
			
			if(start == -1) {
				return;
			}
			
			for(;start > 0 && !Character.isWhitespace(foreignSubtitleString.charAt(start)); --start);
			// remove the white space;
			++start;
			int end = start;
			for(;end < foreignSubtitleString.length() - 1 && !Character.isWhitespace(foreignSubtitleString.charAt(end)); ++end);
			
			String selectedString = foreignSubtitleString.substring(start, end + 1).toLowerCase();
			selectedString = wordFrequencyParser.preprocessWord(selectedString);
			Word selectedWord = new Word(selectedString);
			// search for word in Model
			int foundIndex = -1;
			for(int i = 0; i < wordTableModel.getRowCount(); ++i) {
				if(wordTableModel.getValueAt(i, WordTableModel.WORD_COLUMN).equals(selectedWord)) {
					foundIndex = i;
					break;
				}
			}
			
			if(foundIndex == -1)
				return;
			
			int selectedRow = wordTable.convertRowIndexToView(foundIndex);
			Caption selectedCaption = foreignDocumentSubtitles.getCaptionAtTextPostion(start);
			int referenceNumber;
			
			if(selectedCaption != null)
				referenceNumber = wordTableModel.getCurrentWordList().get(foundIndex).getReferences().indexOf(selectedCaption);
			else {
				referenceNumber = 0;
			}
			
			// scroll to word in wordTable
			initialGoToReference = referenceNumber;
			wordTable.clearSelection();
			wordTable.setRowSelectionInterval(selectedRow, selectedRow);
			wordTable.addColumnSelectionInterval(0, wordTableModel.getColumnCount() - 1);
			initialGoToReference = -1;
			
			Rectangle cellRect = wordTable.getCellRect(selectedRow, 0, true);
			
			// scrolling to the bottom first will ensure that 
			// the highlighted selection is at the top of the table
			JScrollBar vertical = scrollPane_table.getVerticalScrollBar();
			vertical.setValue( vertical.getMaximum() );
			
			wordTable.scrollRectToVisible(cellRect);
		}
	}
}
