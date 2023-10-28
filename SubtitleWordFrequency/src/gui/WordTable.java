package gui;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import SubtitleWordFrq.Hidden;
import SubtitleWordFrq.Utils;
import SubtitleWordFrq.Word;

public class WordTable extends JTable {
	private DefaultTableCellRenderer leftRenderer;
	
	public WordTable()
	{
		super(new WordTableModel(null, null));
		
		leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(JLabel.LEFT);
		setDefaultRenderer(Hidden.class, new HiddenRenderer());
		setComponentPopupMenu(new TablePopup());
		
	}
	
	public WordTableModel getModel()
	{
		return (WordTableModel)super.getModel();
	}
	
	@Override
	public void setModel(TableModel model)
	{
		super.setModel(model);
		reapplyAttributes();
	}
	
	@Override
	public void setRowSorter(RowSorter<? extends TableModel> rowSorter) 
	{
		super.setRowSorter(rowSorter);
		reapplyAttributes();
	}
	
	public void rebuildSorter()
	{
		WordTableModel wordTableModel = getModel();
		TableRowSorter<WordTableModel> sorter = new TableRowSorter<>(wordTableModel);
		ArrayList<RowSorter.SortKey> list = new ArrayList<>();
		list.add(new RowSorter.SortKey(WordTableModel.COUNT_COLUMN, SortOrder.DESCENDING));
		list.add(new RowSorter.SortKey(WordTableModel.WORD_COLUMN, SortOrder.ASCENDING));
		if(wordTableModel.getColumnCount() == 7)
			list.add(new RowSorter.SortKey(WordTableModel.HIDDEN_COLUMN, SortOrder.ASCENDING));
		sorter.setSortKeys(list);
		sorter.setSortable(WordTableModel.FOREIGN_EXAMPLE, false);
		sorter.setSortable(WordTableModel.PRIMARY_EXAMPLE, false);
		sorter.setSortable(WordTableModel.TAGS_COLUMN, false);
		//sorter.setComparator(WordTableModel.WORD_COLUMN, (w1, w2) -> w1.toString().compareToIgnoreCase(w2.toString()));
		setRowSorter(sorter);	
		getColumnModel().getColumn(WordTableModel.COUNT_COLUMN).setCellRenderer(leftRenderer);	
	}
	
	private void reapplyAttributes()
	{
		WordRenderer wordRenderer = new WordRenderer();
		getColumnModel().getColumn(WordTableModel.WORD_COLUMN).setCellRenderer(wordRenderer);
		//setDefaultRenderer(Word.class, wordRenderer);
		
		WordGroupEditor wordGroupEditor = new WordGroupEditor();
		getColumnModel().getColumn(WordTableModel.WORD_COLUMN).setCellEditor(wordGroupEditor);
		
		if(WordTableModel.HIDDEN_COLUMN < getColumnCount()) {
			HiddenCellEditor hiddenCellEditor = new HiddenCellEditor();
			getColumnModel().getColumn(WordTableModel.HIDDEN_COLUMN).setCellEditor(hiddenCellEditor);
			//setDefaultEditor(Boolean.class, cellEditor);
		}
		
		Utils.updateRowHeight(this, 0);
	}
	
	private class TablePopup extends JPopupMenu implements PopupMenuListener
	{
		JMenuItem capitalizeMenu;
		JMenuItem createGroupMenu;
		JMenuItem editGroupMenu;
		JMenuItem ungroupMenu;
		boolean capitalize;
		
		public TablePopup()
		{
			capitalizeMenu = new JMenuItem();
			capitalizeMenu.addActionListener(e -> capitalizeClicked());
			
			createGroupMenu = new JMenuItem("Create Group");
			createGroupMenu.addActionListener(e -> createGroupClicked());
			
			editGroupMenu = new JMenuItem("Edit Group");
			editGroupMenu.addActionListener(e -> editGroupClicked());
			
			ungroupMenu = new JMenuItem("Ungroup");
			ungroupMenu.addActionListener(e -> ungroupClicked());
			
			JMenu toggleHiddenMenu = new JMenu("Toggle Hidden");
			this.add(toggleHiddenMenu);
			
			JMenuItem toggleHiddenOnMenuItem = new JMenuItem("On");
			toggleHiddenOnMenuItem.addActionListener(e -> toggleHidden(Hidden.ON));
			toggleHiddenMenu.add(toggleHiddenOnMenuItem);
			
			JMenuItem toggleHiddenOffMenuItem = new JMenuItem("Off");
			toggleHiddenOffMenuItem.addActionListener(e -> toggleHidden(Hidden.OFF));
			toggleHiddenMenu.add(toggleHiddenOffMenuItem);
			
			JMenuItem toggleHiddenSessionMenuItem = new JMenuItem("Session");
			toggleHiddenSessionMenuItem.addActionListener(e -> toggleHidden(Hidden.SESSION));
			toggleHiddenSessionMenuItem.setToolTipText("Hides word(s) only for this session");
			toggleHiddenMenu.add(toggleHiddenSessionMenuItem);
			
			this.addPopupMenuListener(this);
		}
		
		private void toggleHidden(Hidden hidden)
		{
			for(int tableRow : getSelectedRows()) {
				int modelRow = convertRowIndexToModel(tableRow);
				Word word = getModel().getCurrentWordList().get(modelRow);
				word.setHidden(hidden);
			}
			getModel().setHiddenColumnEnabled(getModel().isHiddenColumnEnabled());
			rebuildSorter();
			Utils.updateRowHeight(WordTable.this, 0);
		}
		
		private void capitalizeClicked()
		{
			for(int row : getSelectedRows()) {
				int selectedModelRow = convertRowIndexToModel(row);
				Word selectedWord = (Word)getModel().getValueAt(selectedModelRow, WordTableModel.WORD_COLUMN);
				selectedWord.setCapitalized(capitalize);
				getModel().fireTableCellUpdated(selectedModelRow, WordTableModel.WORD_COLUMN);
			}
			
		}
		
		private void createGroupClicked()
		{
			GroupDialog groupDialog = new GroupDialog(SwingUtilities.getWindowAncestor(this));
			List<Word> selectedWords = IntStream.of(getSelectedRows())
					.map(i -> convertRowIndexToModel(i))
					.mapToObj(i -> getModel().getCurrentWordList().get(i))
					.collect(Collectors.toList());
			
			
			List<Word> currentWordList = getModel().getCurrentWordList();
			//List<Word> currentNoGroupWordList = getModel().getCurrentWordList().stream()
			//		.filter(word -> !word.isGroup())
			//		.collect(Collectors.toList());
					
			groupDialog.showCreateDialog(currentWordList, selectedWords);
			Word group = groupDialog.getResult();
			if(group == null)
			{
				return;
			}
			
			// remove words that were added to group AND SORT
			List<Word> newWordList = getModel().getWordList().stream()
					.filter(word -> !group.getAssociatedWords().contains(word))
					.sorted()
					.collect(Collectors.toList());
			
			group.ungroupAssociatedWords();
			
			// add new group
			Utils.insertSorted(newWordList, group);
			
			// update model to use new word list
			getModel().setWordList(newWordList);

			Utils.updateRowHeight(WordTable.this, 0);
		}
		
		private void editGroupClicked()
		{
			GroupDialog groupDialog = new GroupDialog(SwingUtilities.getWindowAncestor(this));
			int selectedModelIndex = convertRowIndexToModel(getSelectedRow());
			Word selectedGroup = getModel().getCurrentWordList().get(selectedModelIndex);
					
			List<Word> currentNoGroupWordList = getModel().getCurrentWordList().stream()
					.filter(word -> !word.isGroup())
					.collect(Collectors.toList());
			
			groupDialog.showEditDialog(currentNoGroupWordList, selectedGroup);
			Word newGroup = groupDialog.getResult();
			if(newGroup == null)
			{
				return;
			}
			
			List<Word> addedToGroup = newGroup.getAssociatedWords().stream()
					.filter(word -> !selectedGroup.getAssociatedWords().contains(word))
					.collect(Collectors.toList());
			
			List<Word> removedFromGroup = selectedGroup.getAssociatedWords().stream()
					.filter(word -> !newGroup.getAssociatedWords().contains(word))
					.collect(Collectors.toList());
			
			// create new word list
			List<Word> newWordList = new ArrayList<>(getModel().getWordList());
			newWordList.removeAll(addedToGroup);
			newWordList.remove(selectedGroup);
			newWordList.addAll(removedFromGroup);
			newWordList.add(newGroup);
			newWordList.sort(Word::compareTo);
			
			// update model to use new word list
			getModel().setWordList(newWordList);
			Utils.updateRowHeight(WordTable.this, 0);
		}
		
		private void ungroupClicked()
		{
			int selectedModelIndex = convertRowIndexToModel(getSelectedRow());
			Word selectedGroup = getModel().getCurrentWordList().get(selectedModelIndex);
			
			for(Word word : selectedGroup.getAssociatedWords())
			{
				word.setDefiniton(selectedGroup.getDefiniton());
				word.setTags(selectedGroup.getTags());
				word.setHidden(selectedGroup.getHidden());
			}
			
			// create new word list
			List<Word> newWordList = new ArrayList<>(getModel().getWordList());
			newWordList.remove(selectedGroup);
			newWordList.addAll(selectedGroup.getAssociatedWords());
			newWordList.sort(Word::compareTo);
			
			// update model to use new word list
			getModel().setWordList(newWordList);
			Utils.updateRowHeight(WordTable.this, 0);
			
			for(Word word : selectedGroup.getAssociatedWords()) {
				int selectedViewRow = convertRowIndexToView(newWordList.indexOf(word));
				WordTable.this.getSelectionModel().addSelectionInterval(selectedViewRow, selectedViewRow);
			}
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			Utils.updateRowHeight(WordTable.this, 0);
			if(getSelectedRowCount() == 0) {
				this.remove(capitalizeMenu);
				return;
			}
			
			int selectedViewColumn = getSelectedColumn();
			int selectedModelColumn = convertColumnIndexToModel(selectedViewColumn);
			
			int selectedRowModel = convertRowIndexToModel(getSelectedRow());
			Word selectedWord = getModel().getCurrentWordList().get(selectedRowModel);
			
			boolean addCapitalizeMenu = false;
			boolean addCreateGroupMenu = false;
			boolean addEditGroupMenu = false;
			
			if(selectedModelColumn ==  WordTableModel.WORD_COLUMN) {
				String targetWord = "...";
				int selectedRowCount = getSelectedRowCount();
				if(selectedRowCount >  0) {
					if(selectedRowCount == 1) {
						addCapitalizeMenu = true;
						targetWord = selectedWord.toString();
						if(Character.isUpperCase(selectedWord.toString().charAt(0))) {
							capitalizeMenu.setText(String.format("Uncapitalize (%s)", targetWord));
							capitalize = false;
						} else {
							capitalizeMenu.setText(String.format("Capitalize (%s)", targetWord));
							capitalize = true;
						}
						
						// check if word is a group
						if(selectedWord.isGroup()) {
							addEditGroupMenu = true;
						} else {
							addCreateGroupMenu = true;
						}
					} else {
						addCreateGroupMenu = true;
					}
				}
			}
			
			if(addCapitalizeMenu) {
				this.add(capitalizeMenu, 0);
			} else {
				this.remove(capitalizeMenu);
			}
			
			if(addCreateGroupMenu) {
				this.add(createGroupMenu, 1);
			} else {
				this.remove(createGroupMenu);
			} 
			
			if(addEditGroupMenu) {
				this.add(editGroupMenu, 1);
				this.add(ungroupMenu, 2);
			} else {
				this.remove(editGroupMenu);
				this.remove(ungroupMenu);
			} 
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {}
	}
	
	private class WordRenderer extends DefaultTableCellRenderer
	{
		private StringBuilder sb;
		private JTextArea textArea;
		
		public WordRenderer()
		{
			sb = new StringBuilder();
			textArea = new JTextArea();
			textArea.setFont(getFont());
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			
			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			Word word = (Word)value;
			
			if(word.isGroup())	{
				textArea.setBackground(label.getBackground());
				textArea.setForeground(label.getForeground());
				sb.setLength(0);
				sb.append(String.format("%s (G)", word.toString()));
				if(!word.isCollapsed())  {
					List<Word> assocWords = word.getAssociatedWords();
					for(Word assocWord : assocWords) {
						sb.append("\n  -");
						sb.append(assocWord.toString());
					}
				}
				textArea.setText(sb.toString());
				return textArea;
			}
			
			label.setText(word.toString());
			return label;
		}
	}
	
	private class HiddenRenderer extends DefaultTableCellRenderer
	{
		TableCellRenderer checkBoxRenderer;
		private HiddenRenderer()
		{
			checkBoxRenderer = WordTable.this.getDefaultRenderer(Boolean.class);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) 
		{
			String text = null;
			if(value instanceof Hidden) {
				Hidden hidden = (Hidden)value;
				value = hidden != hidden.OFF;
				if(hidden == Hidden.SESSION)
					text = "Session";
			}
			
			JCheckBox checkBox = (JCheckBox)checkBoxRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			checkBox.setText(text);
			return checkBox;
		}
	
	}
	
	private static class HiddenCellEditor extends DefaultCellEditor
	{

		public HiddenCellEditor() {
			super(getCheckbox());
		}
		
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			String text = null;
			Hidden hidden = (Hidden)value;
			if(hidden == Hidden.SESSION)
				text = "Session";
			JCheckBox checkBox = (JCheckBox)super.getTableCellEditorComponent(table, hidden != Hidden.OFF, isSelected, row, column);
			checkBox.setText(text);
			return checkBox;
		}
		
		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			return false;
		}
		
		private static JCheckBox getCheckbox() {
			JCheckBox checkbox = new JCheckBox();
			checkbox.setHorizontalAlignment(SwingConstants.CENTER);
			return checkbox;
		}
	}
	
	private class WordGroupEditor extends AbstractCellEditor implements TableCellEditor
	{
		private JTextArea textArea;
		private StringBuilder sb;
		private Word editingWord;
		
		public WordGroupEditor()
		{
		    textArea = new JTextArea();
		    textArea.setEditable(false);
		    textArea.setFont(new JLabel().getFont());
		    textArea.addMouseListener(new MouseAdapter() {
		    	@Override
		    	public void mouseClicked(MouseEvent e) {
		    		if(e.getClickCount() == 2) {
		    			updateTextArea();
		    		}
		    	}
			});
		    textArea.addFocusListener(new FocusAdapter() {
		    	@Override
		    	public void focusGained(FocusEvent e) {
		    		editingWord.setCollapsed(!editingWord.isCollapsed());
		    		Utils.updateRowHeight(WordTable.this, 0);
		    		fireEditingStopped();
		    	}
			});
		    sb = new StringBuilder();
		}

		@Override
		public Object getCellEditorValue() {
			return editingWord;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			editingWord = (Word)value;
			updateTextArea();
			return textArea;
		}
		
		@Override
		public boolean isCellEditable(EventObject e) {
			if(e instanceof MouseEvent) {
				MouseEvent me = (MouseEvent)e;
				return me.getClickCount() == 2;
			}
			
			return false;
		}
		
		private void updateTextArea()
		{
			sb.setLength(0);
			sb.append(String.format("%s (G)", editingWord.toString()));
			if(editingWord.isGroup() && !editingWord.isCollapsed())	{
				List<Word> assocWords = editingWord.getAssociatedWords();
				for(Word assocWord : assocWords) {
					sb.append("\n  -");
					sb.append(assocWord.toString());
				}
				
			}
			textArea.setText(sb.toString());
		}

	}
}
