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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import SubtitleWordFrq.Utils;
import SubtitleWordFrq.Word;

public class WordTable extends JTable {
	
	public WordTable()
	{
		super(new WordTableModel(null, null));
		
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
			toggleHiddenOnMenuItem.addActionListener(e -> toggleHidden(true));
			toggleHiddenMenu.add(toggleHiddenOnMenuItem);
			
			JMenuItem toggleHiddenOffMenuItem = new JMenuItem("Off");
			toggleHiddenOffMenuItem.addActionListener(e -> toggleHidden(false));
			toggleHiddenMenu.add(toggleHiddenOffMenuItem);
			
			this.addPopupMenuListener(this);
		}
		
		private void toggleHidden(boolean hidden)
		{
			for(int tableRow : getSelectedRows()) {
				int modelRow = convertRowIndexToModel(tableRow);
				Word word = getModel().getWordList().get(modelRow);
				word.setHidden(hidden);
			}
			getModel().fireTableDataChanged();
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
			
			List<Word> currentNoGroupWordList = getModel().getCurrentWordList().stream()
					.filter(word -> !word.isGroup())
					.collect(Collectors.toList());
					
			groupDialog.showCreateDialog(currentNoGroupWordList, selectedWords);
			Word group = groupDialog.getResult();
			if(group == null)
			{
				return;
			}
			
			// remove words that were added to group
			List<Word> newWordList = getModel().getWordList().stream()
					.filter(word -> !group.getAssociatedWords().contains(word))
					.collect(Collectors.toList());
			
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
			
			// update model to use new word list
			getModel().setWordList(newWordList);
			Utils.updateRowHeight(WordTable.this, 0);
		}
		
		private void ungroupClicked()
		{
			int selectedModelIndex = convertRowIndexToModel(getSelectedRow());
			Word selectedGroup = getModel().getCurrentWordList().get(selectedModelIndex);
			
			// create new word list
			List<Word> newWordList = new ArrayList<>(getModel().getWordList());
			newWordList.remove(selectedGroup);
			newWordList.addAll(selectedGroup.getAssociatedWords());
			
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
			Word selectedWord = getModel().getWordList().get(selectedRowModel);
			
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
						addEditGroupMenu = selectedWord.isGroup();
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
	
	private static class HiddenCellEditor extends DefaultCellEditor
	{

		public HiddenCellEditor() {
			super(getCheckbox());
		}
		
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
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
