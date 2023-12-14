package gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JScrollBar;
import javax.swing.JTextPane;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import SubtitleWordFrq.Caption;
import SubtitleWordFrq.Document;
import SubtitleWordFrq.DocumentSubtitles;
import SubtitleWordFrq.WordFrequencyParser;

public class SubtitleTextPane extends JTextPane implements MouseListener, MouseMotionListener {
	public static final SimpleAttributeSet UNDERLINE = new SimpleAttributeSet();
	public static final SimpleAttributeSet NO_UNDERLINE = new SimpleAttributeSet();
	private Style defaultStyle = StyleContext.
			   getDefaultStyleContext().
			   getStyle(StyleContext.DEFAULT_STYLE);
    static {
    	StyleConstants.setUnderline(UNDERLINE, true);
    	StyleConstants.setUnderline(NO_UNDERLINE, false);
    }
    
    private Document documentSubtitles;
    private int hoveredTextStart;
    private int hoveredTextEnd;
    
	public SubtitleTextPane(Document documentSubtitles)
	{
		this.setDocumentSubtitles(documentSubtitles);
		this.hoveredTextStart = -1;
		addMouseMotionListener(this);
	}
	
	public Document getDocumentSubtitles() {
		return documentSubtitles;
	}

	public void setDocumentSubtitles(Document documentSubtitles) {
		this.documentSubtitles = documentSubtitles;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(documentSubtitles == null)
			return;

		Point mousePosition;
		if((mousePosition = this.getMousePosition()) == null)
			return;
		
		int start = this.viewToModel2D(mousePosition);
		
		
		if(start == -1) {
			hoveredTextStart = hoveredTextEnd = -1;
			return;
		}
		
		for(;start > 0 && !Character.isWhitespace(getText().charAt(start)); --start);
		// remove the white space;
		++start;
		int end = start;
		for(;end < getText().length() - 1 && !Character.isWhitespace(getText().charAt(end)); ++end);
		
		Caption hoveredCaption = documentSubtitles.getCaptionAtTextPostion(start);
		
		// don't underline anything if no text is hovered
		if(hoveredCaption == null) {
			getStyledDocument().setCharacterAttributes(0, getDocument().getLength(), NO_UNDERLINE, false);
			hoveredTextStart = hoveredTextEnd = -1;
			return;
		}
		
		// don't update if hovering the same text
		if(start != hoveredTextStart) {
			// clear existing style
			getStyledDocument().setCharacterAttributes(0, getDocument().getLength(), NO_UNDERLINE, false);
			getStyledDocument().setCharacterAttributes(start, end - start, UNDERLINE, false);
			hoveredTextStart = start;
			hoveredTextEnd = end;
		}
	}
	
	public String getHoveredText()
	{
		if(hoveredTextStart == -1)
			return null;
		return documentSubtitles.getText().substring(hoveredTextStart, hoveredTextEnd);
	}
	
	public String getHighlightedText()
	{

		Highlight[] highlights = getHighlighter().getHighlights();
		if(highlights.length == 0) {
			return null;
		}
		
		Highlight highlight = highlights[0];
		int start = highlight.getStartOffset();
		int end = highlight.getEndOffset();
		
		return documentSubtitles.getText().substring(start, end);
	}

}
