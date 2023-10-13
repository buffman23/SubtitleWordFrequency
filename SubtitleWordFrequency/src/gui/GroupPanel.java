package gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.BoxLayout;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import SubtitleWordFrq.Word;

public class GroupPanel extends JPanel {
	private JList<Word> groupJList;
	private JList<Word> wordJList;
	private SortedListModel<Word> groupListModel;
	private SortedListModel<Word> wordListModel;
	private JTextField textField;
	
	public GroupPanel()
	{
		GridBagLayout gbl_main_panel = new GridBagLayout();
		gbl_main_panel.rowWeights = new double[]{1.0};
		gbl_main_panel.columnWeights = new double[]{1.0, 0.0, 1.0};
		setLayout(gbl_main_panel);
		
		JPanel left_panel = new JPanel();
		GridBagConstraints gbc_left_panel = new GridBagConstraints();
		gbc_left_panel.fill = GridBagConstraints.BOTH;
		gbc_left_panel.insets = new Insets(0, 0, 0, 5);
		gbc_left_panel.gridx = 0;
		gbc_left_panel.gridy = 0;
		add(left_panel, gbc_left_panel);
		left_panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane word_scrollpane = new JScrollPane();
		word_scrollpane.setPreferredSize(new Dimension(0, 0));
		left_panel.add(word_scrollpane);
		
		wordJList = new JList<>();
		word_scrollpane.setViewportView(wordJList);
		
		Component left_panel_struct = Box.createVerticalStrut(20);
		left_panel.add(left_panel_struct, BorderLayout.NORTH);
		
		JPanel move_panel = new JPanel();
		GridBagConstraints gbc_move_panel = new GridBagConstraints();
		gbc_move_panel.insets = new Insets(0, 0, 0, 5);
		gbc_move_panel.gridx = 1;
		gbc_move_panel.gridy = 0;
		add(move_panel, gbc_move_panel);
		move_panel.setLayout(new BoxLayout(move_panel, BoxLayout.Y_AXIS));
		
		JButton btnMoveToGroup = new JButton("-->");
		btnMoveToGroup.addActionListener(e -> moveWords(wordListModel, groupListModel));
		move_panel.add(btnMoveToGroup);
		
		Component move_buttons_struct = Box.createVerticalStrut(5);
		move_panel.add(move_buttons_struct);
		
		JButton btnMoveOutGroup = new JButton("<--");
		btnMoveOutGroup.addActionListener(e -> moveWords(groupListModel, wordListModel));
		move_panel.add(btnMoveOutGroup);
		
		JPanel right_panel = new JPanel();
		GridBagConstraints gbc_right_panel = new GridBagConstraints();
		gbc_right_panel.fill = GridBagConstraints.BOTH;
		gbc_right_panel.gridx = 2;
		gbc_right_panel.gridy = 0;
		add(right_panel, gbc_right_panel);
		right_panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane group_scrollpane = new JScrollPane();
		group_scrollpane.setPreferredSize(new Dimension(0, 0));
		right_panel.add(group_scrollpane, BorderLayout.CENTER);
		
		groupJList = new JList<>();
		group_scrollpane.setViewportView(groupJList);
		
		JPanel group_name_panel = new JPanel();
		right_panel.add(group_name_panel, BorderLayout.NORTH);
		
		JLabel group_name_label = new JLabel("Group Name:");
		group_name_panel.add(group_name_label);
		
		textField = new JTextField();
		textField.setColumns(15);
		group_name_panel.add(textField);
		
		left_panel_struct.setPreferredSize(group_name_panel.getPreferredSize());
	}
	
	public void load(List<Word> wordList, List<Word> groupWords, String groupName)
	{
		textField.setText(groupName);
		
		// work with copies
		wordList = new ArrayList<>(wordList);
		wordList.sort(Word::compareTo);
		groupWords = new ArrayList<>(groupWords);
		
		// remove selected words in word list
		for(Word selectedWord : groupWords) {
			int index = Collections.binarySearch(wordList, selectedWord);
			if(index > 0) {
				wordList.remove(index);
			}
		}
		
		wordListModel = new SortedListModel<>(wordList);
		wordJList.setModel(wordListModel);
		
		groupListModel = new SortedListModel<>(groupWords);
		groupJList.setModel(groupListModel);
		SwingUtilities.invokeLater(() -> textField.requestFocus());
	}
	
	public Word getGroup()
	{
		String groupName = getGroupName();
		Word group = new Word(groupName, groupListModel.toList());
		// remove word that has same name as group, and add it to group
		int findIdx;
		if((findIdx = wordListModel.indexOf(group)) >= 0) {
			Word wordWithSameName = wordListModel.remove(findIdx);
			group.getAssociatedWords().add(wordWithSameName);
		}
		return group;
	}
	
	public String getGroupName()
	{
		return textField.getText();
	}
	
	private void moveWords(SortedListModel<Word> source, SortedListModel<Word> destination)
	{
		JList<Word> sourceList;
		
		if(source == groupListModel) {
			sourceList = groupJList;
		} else {
			sourceList = wordJList;
		}
		
		for(Word word : sourceList.getSelectedValuesList()) {
			destination.add(word);
			source.remove(word);
		}
	}
}
