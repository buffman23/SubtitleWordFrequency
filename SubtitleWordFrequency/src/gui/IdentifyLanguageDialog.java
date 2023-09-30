package gui;

import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;

import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

public class IdentifyLanguageDialog extends JDialog {
	private static final int FIELD_WIDTH = 200;
	
	private JComboBox<Entry> foreign_lang_combobox;
	private JTextField primary_lang_field;
	private File result;
	
	public IdentifyLanguageDialog(Frame parent, File file1, File file2) {
		super(parent, true);
		
		setTitle("Identify Foreign Language");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		JPanel border_panel = new JPanel();
		border_panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(border_panel, BorderLayout.CENTER);
		border_panel.setLayout(new BorderLayout(0, 0));
		
		JPanel main_panel = new JPanel();
		main_panel.setBorder(new EmptyBorder(10, 10, 20, 10));
		border_panel.add(main_panel, BorderLayout.CENTER);
		main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));
		
		JPanel grid_panel = new JPanel();
		main_panel.add(grid_panel);
		GridBagLayout gbl_grid_panel = new GridBagLayout();
		gbl_grid_panel.columnWeights = new double[]{0.0, 0.0};
		gbl_grid_panel.rowWeights = new double[]{0.0, 0.0};
		grid_panel.setLayout(gbl_grid_panel);
		
		JLabel lblNewLabel = new JLabel("Foreign Language:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		grid_panel.add(lblNewLabel, gbc_lblNewLabel);
		
		foreign_lang_combobox = new JComboBox<>();
		foreign_lang_combobox.setPreferredSize(new Dimension(FIELD_WIDTH, foreign_lang_combobox.getPreferredSize().height));
		
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.anchor = GridBagConstraints.NORTH;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		grid_panel.add(foreign_lang_combobox, gbc_comboBox);
		
		JLabel lblNewLabel_1 = new JLabel("Primary Language:");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(5, 0, 0, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		grid_panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		primary_lang_field = new JTextField();
		primary_lang_field.setPreferredSize(new Dimension(FIELD_WIDTH, primary_lang_field.getPreferredSize().height));
		primary_lang_field.setEditable(false);
		GridBagConstraints gbc_primary_lang_field = new GridBagConstraints();
		gbc_primary_lang_field.insets = new Insets(5, 0, 0, 0);
		gbc_primary_lang_field.fill = GridBagConstraints.HORIZONTAL;
		gbc_primary_lang_field.gridx = 1;
		gbc_primary_lang_field.gridy = 1;
		grid_panel.add(primary_lang_field, gbc_primary_lang_field);
		primary_lang_field.setColumns(10);
		
		JLabel question_label = new JLabel("");
		GridBagConstraints gbc_question_label = new GridBagConstraints();
		gbc_question_label.gridx = 1;
		gbc_question_label.gridy = 2;
		grid_panel.add(question_label, gbc_question_label);
		
		JPanel bottom_panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) bottom_panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		border_panel.add(bottom_panel, BorderLayout.SOUTH);
		
		JButton ok_button = new JButton("Ok");
		ok_button.addActionListener(e -> close(false));
		
		JButton cancel_button = new JButton("Cancel");
		cancel_button.addActionListener(e -> close(true));
		ok_button.setPreferredSize(cancel_button.getPreferredSize());
		
		// Allow pressing enter to perform action on ok and cancel buttons.
		KeyAdapter ok_canel_Adapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e)
			{
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					close(e.getSource() == cancel_button);
				}
			}
		};
		ok_button.addKeyListener(ok_canel_Adapter);
		cancel_button.addKeyListener(ok_canel_Adapter);
		
		bottom_panel.add(ok_button);
		bottom_panel.add(cancel_button);
		
		setLocationRelativeTo(parent);
		pack();
		
		String file1Name = file1.getName();
		String file2Name = file2.getName();
		if(file1Name.equals(file2Name)) {
			file1Name = file1.getPath();
			file2Name = file2.getPath();
		}
		foreign_lang_combobox.addItem(new Entry(file1, file1Name));
		foreign_lang_combobox.addItem(new Entry(file2, file2Name));
		primary_lang_field.setText(file2Name);
		// register after items have been added
		foreign_lang_combobox.addActionListener(e -> itemSelected());
		
		ok_button.requestFocus();
	}
	
	public File getResult()
	{
		return result;
	}
	
	private void close(boolean cancel)
	{
		if(!cancel) {
			result = ((Entry)foreign_lang_combobox.getSelectedItem()).file;
		}
		
		setVisible(false);
	}
	
	private void itemSelected()
	{
		int foreignLangIndex = foreign_lang_combobox.getSelectedIndex();
		int primaryLangIndex = foreignLangIndex == 0 ? 1 : 0;
		primary_lang_field.setText(foreign_lang_combobox.getItemAt(primaryLangIndex).toString());
	}
	
	private class Entry
	{
		public Entry(File file, String displayName)
		{
			this.file = file;
			this.displayName = displayName;
		}
		
		public File file;
		public String displayName;
		
		public String toString()
		{
			return displayName;
		}
	}
}
