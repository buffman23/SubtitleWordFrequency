package SubtitleWordFrq;
import java.io.*;
import java.nio.CharBuffer;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;

public abstract class Document implements Iterable<Caption> {
	public static final int MATCH = 0, NO_MATCH = 1;
	
	
	protected List<Caption> captions;
	protected List<ImmutablePair<Caption, Integer>>[] pairedCaptions;
	protected String text;
	
	public abstract void pairWith(Document otherSubtitles);
	
	public Caption getCaptionAtTextPostion(int textPosition)
	{
		// TODO optimize by using binary search
		for(Caption caption : captions) {
			if(textPosition >= caption.textPosition && textPosition < (caption.textPosition + caption.textLength))
				return caption;
		}
		
		return null;
	}
	
	public List<Caption> getCaptions() {
		return captions;
	}

	public List<ImmutablePair<Caption, Integer>>[] getPairedCaptions() {
		return pairedCaptions;
	}
	
	public List<ImmutablePair<Caption, Integer>> getPairedCaptions(Caption caption) {
		if(pairedCaptions == null)
			return null;
		
		return pairedCaptions[caption.sequenceNumber - 1];
	}

	@Override
	public Iterator<Caption> iterator() {
		return captions.iterator();
	}

	public String getText() {
		return text;
	}
	
	protected class Caret
	{
		public int pos;
		
		public void moveLine(String str)
		{
			pos += str.length() + 1;
		}
	}
}
