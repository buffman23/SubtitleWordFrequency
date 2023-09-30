package gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.Font;

public class AnkiPanel extends JPanel {
	public AnkiPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel("<html>Export to Anki not yet implemented :(.<br/>For now you can export to CSV then import CSV to Anki.</html>");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblNewLabel);
	}

}
