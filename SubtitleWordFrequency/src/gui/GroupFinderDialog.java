package gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.text.similarity.LevenshteinDistance;

import SubtitleWordFrq.Utils;
import SubtitleWordFrq.Word;

import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class GroupFinderDialog extends JDialog {
	private GroupPanel groupPanel;
	private List<Word> inputWords;
	private List<Word> wordList; 
	private WorkerThread workerThread;
	private Object lock;
	private int distanceThreshold;
	private int initialWordListSize;
	private JLabel status_label;
	private boolean groupCreated;
	
	public GroupFinderDialog(Window parent)
	{
		super(parent, Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("Group Finder");
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (groupCreated && JOptionPane.showConfirmDialog(null, "Would you like to save groups before exit?", "Save before exit",
				        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					close(false);
				} else {
					close(true);
				}
				
			}
		});
		JPanel main_Panel = new JPanel(new BorderLayout());
		getContentPane().add(main_Panel, BorderLayout.CENTER);
		
		JPanel stacked_panel = new JPanel();
		main_Panel.add(stacked_panel, BorderLayout.CENTER);
		GridBagLayout gbl_stacked_panel = new GridBagLayout();
		gbl_stacked_panel.columnWeights = new double[]{1.0};
		gbl_stacked_panel.rowWeights = new double[]{1.0};
		stacked_panel.setLayout(gbl_stacked_panel);
		
		JPanel status_panel = new JPanel();
		status_panel.setOpaque(false);
		FlowLayout flowLayout_1 = (FlowLayout) status_panel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_status_panel = new GridBagConstraints();
		gbc_status_panel.insets = new Insets(5, 10, 0, 10);
		gbc_status_panel.fill = GridBagConstraints.BOTH;
		gbc_status_panel.gridx = 0;
		gbc_status_panel.gridy = 0;
		stacked_panel.add(status_panel, gbc_status_panel);
		
		status_label = new JLabel("Progress");
		status_panel.add(status_label);
		
		groupPanel = new GroupPanel();
		GridBagConstraints gbc_groupPanel = new GridBagConstraints();
		gbc_groupPanel.insets = new Insets(0, 10, 0, 10);
		gbc_groupPanel.fill = GridBagConstraints.BOTH;
		gbc_groupPanel.gridx = 0;
		gbc_groupPanel.gridy = 0;
		stacked_panel.add(groupPanel, gbc_groupPanel);
		
		JPanel group_action_panel = new JPanel();
		main_Panel.add(group_action_panel, BorderLayout.SOUTH);
		
		JButton create_group_button = new JButton("Create Group");
		create_group_button.addActionListener(e -> createGroupClicked());
		group_action_panel.add(create_group_button);
		
		JButton next_group_button = new JButton("Next Group");
		next_group_button.addActionListener(e -> nextGroupClicked());
		group_action_panel.add(next_group_button);
		
		JPanel bottom_panel = new JPanel(new BorderLayout());
		getContentPane().add(bottom_panel, BorderLayout.SOUTH);
		
		JPanel bottom_left_panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottom_left_panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		bottom_panel.add(bottom_left_panel, BorderLayout.CENTER);
		
		JPanel bottom_right_panel = new JPanel();
		bottom_panel.add(bottom_right_panel, BorderLayout.EAST);
		
		JButton ok_button = new JButton("Ok");
		ok_button.addActionListener(e -> close(false));
		bottom_right_panel.add(ok_button);
		
		JButton cancel_button = new JButton("Cancel");
		cancel_button.addActionListener(e -> close(true));
		bottom_right_panel.add(cancel_button);
		
		int buttonWidth = Math.max(create_group_button.getPreferredSize().width, next_group_button.getPreferredSize().width);
		create_group_button.setPreferredSize(new Dimension(buttonWidth, create_group_button.getPreferredSize().height));
		next_group_button.setPreferredSize(new Dimension(buttonWidth, next_group_button.getPreferredSize().height));
		
		buttonWidth = Math.max(ok_button.getPreferredSize().width, cancel_button.getPreferredSize().width);
		ok_button.setPreferredSize(new Dimension(buttonWidth, ok_button.getPreferredSize().height));
		cancel_button.setPreferredSize(new Dimension(buttonWidth, cancel_button.getPreferredSize().height));
		
		setSize(600, 375);
		
		initialize();
	}
	
	// non auto-generated UI ctor code
	public void initialize()
	{
		workerThread = new WorkerThread();
		lock = new Object();
	}
	
	public void showDialog(List<Word> wordList, int levenshteinDistanceThreshold)
	{
		this.wordList = new ArrayList<>(wordList);
		this.wordList.sort(Word::compareTo);
		
		inputWords = new ArrayList<>(wordList);
		inputWords.sort((w1, w2) -> Integer.compare(w1.toString().length(), w2.toString().length()));
		
		initialWordListSize = wordList.size();
		this.distanceThreshold = levenshteinDistanceThreshold;
		percentageUpdate(0, initialWordListSize);
		workerThread.start();
		setVisible(true);
	}
	
	private void groupFound(Word group)
	{
		groupPanel.load(wordList, group.getAssociatedWords(), group.toString());
	}
	
	private void processedAllWords()
	{
		percentageUpdate(initialWordListSize, initialWordListSize);
	}
	
	private void createGroupClicked()
	{
		Word group = groupPanel.getGroup();
		wordList.removeAll(group.getAssociatedWords());
		group.ungroupAssociatedWords();
		Utils.insertSorted(wordList, group);
		groupCreated = true;
		synchronized (lock) {
			lock.notify();
		}
	}
	
	private void nextGroupClicked()
	{
		synchronized (lock) {
			lock.notify();
		}
	}
	
	private void close(boolean canceled)
	{
		if(canceled)
			wordList = null;

		dispose();
		workerThread.interrupt();
		workerThread = null;
	}
	
	public List<Word> getWordList()
	{
		return wordList;
	}
	
	private void percentageUpdate(int numerator, int denominator)
	{
		status_label.setText(String.format("(%d/%d) Words Processed", numerator, denominator));
	}
	
	private class WorkerThread extends Thread
	{
		LevenshteinDistance levenshtein;
		List<Word> BFSQueue;
		List<Word> closeWords;
		
		public WorkerThread()
		{
			levenshtein = LevenshteinDistance.getDefaultInstance();
			BFSQueue = new ArrayList<>();
			closeWords = new ArrayList<>();
		}
		
		@Override
		public void run()
		{
			while(inputWords.size() > 1)
			{
				// grab from the back to prevent shifting
				Word word = inputWords.remove(inputWords.size() - 1);
				findClose(inputWords, word, distanceThreshold);
				if(closeWords.size() > 1) {
					closeWords.sort(Word::compareTo);
					Word group = new Word(word.toString(), new ArrayList<>(closeWords));
					
					// alert UI that a group has been found
					SwingUtilities.invokeLater(() -> groupFound(group)); 
					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							return;
						}
					}
					
				}
				
				// alert UI of percent complete
				SwingUtilities.invokeLater(() -> percentageUpdate(initialWordListSize - inputWords.size(),initialWordListSize));
			}
			
			// alert UI that all words have been processed
			SwingUtilities.invokeLater(() -> processedAllWords());
		}
		
		/**
		 * breadth first search for close words
		 * @param wordList (graph)
		 * @param word (starting node)
		 * @param levenshteinDistance (threshold)
		 */
		private void findClose(List<Word> wordList, Word word, int levenshteinDistance)
		{
			closeWords.clear();
			BFSQueue.clear();
			BFSQueue.add(word);
			
			while(BFSQueue.size() > 0) {
				Word currentWord = BFSQueue.remove(BFSQueue.size() - 1);
				closeWords.add(currentWord);
				
				for(int i = wordList.size() - 1; i >= 0; --i)
				{
					Word otherWord = wordList.get(i);
					int distance = levenshtein.apply(currentWord.toString(), otherWord.toString());
					if(distance <= distanceThreshold)
					{
						//BFSQueue.add(otherWord);
						wordList.remove(i);
						closeWords.add(otherWord);
					}
				}
				// alert UI of percent complete
				SwingUtilities.invokeLater(() -> percentageUpdate(initialWordListSize - wordList.size() + closeWords.size(),initialWordListSize));
			}
		}
	}
}
