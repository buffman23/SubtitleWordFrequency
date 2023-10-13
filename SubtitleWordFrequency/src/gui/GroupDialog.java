package gui;

import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JButton;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.Box;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import SubtitleWordFrq.Word;
import javax.swing.border.EmptyBorder;

public class GroupDialog extends JDialog {
	private Word resultGroup;
	private Word groupIn;
	private GroupPanel groupPanel;
	
	public GroupDialog(Window parent)
	{
		super(parent, Dialog.ModalityType.APPLICATION_MODAL);
		
		groupPanel = new GroupPanel();
		groupPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		getContentPane().add(groupPanel, BorderLayout.CENTER);

		JPanel bottom_panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottom_panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(bottom_panel, BorderLayout.SOUTH);
		
		JButton ok_button = new JButton("Ok");
		ok_button.addActionListener(e -> close(false));
		bottom_panel.add(ok_button);
		
		JButton cancel_button = new JButton("Cancel");
		cancel_button.addActionListener(e -> close(true));
		bottom_panel.add(cancel_button);
		
		int buttonWidth = Math.max(ok_button.getPreferredSize().width, cancel_button.getPreferredSize().width);
		ok_button.setPreferredSize(new Dimension(buttonWidth, ok_button.getPreferredSize().height));
		cancel_button.setPreferredSize(new Dimension(buttonWidth, cancel_button.getPreferredSize().height));
		
		setSize(600, 375);
	}
	
	public void showCreateDialog(List<Word> wordList, List<Word> selectedWords)
	{
		setTitle("Group Create");
		
		Optional<Word> optionalWord = selectedWords.stream().filter(Word::isGroup).findFirst();
		String groupName = optionalWord.isPresent() ? optionalWord.get().toString() : "";
		groupPanel.load(wordList, selectedWords, groupName);
		setVisible(true);
	}
	
	public void showEditDialog(List<Word> wordList, Word group)
	{
		setTitle("Group Edit");
		groupIn = group;
		groupPanel.load(wordList, group.getAssociatedWords(), group.toString());
		setVisible(true);
	}
	
	public Word getResult()
	{
		return resultGroup;
	}
	
	private void close(boolean canceled)
	{
		if(!canceled) {
			if(groupPanel.getGroupName().isBlank()) {
				JOptionPane.showMessageDialog(this, "Group name cannot be blank");
				return;
			}
			
			resultGroup = groupPanel.getGroup();
			if(groupIn != null) {
				resultGroup.setDefiniton(groupIn.getDefiniton());
				resultGroup.setTags(groupIn.getTags());
				resultGroup.setHidden(groupIn.isHidden());
			}
		}
		this.dispose();
	}
}
