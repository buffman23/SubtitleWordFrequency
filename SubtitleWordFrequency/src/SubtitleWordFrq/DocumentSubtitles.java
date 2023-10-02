package SubtitleWordFrq;
import java.io.*;
import java.nio.CharBuffer;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class DocumentSubtitles implements Iterable<Caption> {
	public static final int MATCH = 0, NO_MATCH = 1;
	
	private List<Caption> captions;
	private List<ImmutablePair<Caption, Integer>>[] pairedCaptions;
	private String text;
	
	public DocumentSubtitles(String subtitles) throws IOException
	{
		captions = new ArrayList<Caption>();
		text = subtitles;
		
		try(BufferedReader br = new BufferedReader(new StringReader(subtitles))) {
			Caret caret = new Caret();
			Caption caption;
			while((caption = parseCaption(br, caret)) != null) {
				captions.add(caption);
				caption.text = CharBuffer.wrap(subtitles).subSequence(caption.textPosition, caption.textPosition + caption.textLength);
			}
		}
	}
	
	private Caption parseCaption(BufferedReader br, Caret caret) throws IOException
	{
		Caption caption = new Caption();
		String line;
		
		// skip blank lines
		while((line = br.readLine()) != null) {
			
			// every empty line also has a newline character (and maybe white space?)
			caret.moveLine(line);
			
			if(!line.isBlank())
				break;
		}
		
		if(line == null)
			return null;
		
		// record begin position of caption
		caption.startPosition = caret.pos;
		
		// parse sequence number
		caption.sequenceNumber = Integer.parseInt(line);
		
		// parse duration
		line = br.readLine();
		caret.moveLine(line);
		String[] split = line.split(" --> ");
		LocalTime begin = LocalTime.parse(split[0], Caption.DTF);
		LocalTime end = LocalTime.parse(split[1], Caption.DTF);
		caption.startTime = begin;
		caption.endTime = end;
		
		// parse text run
		caption.textPosition = caret.pos;
		int length = 0;
		while((line = br.readLine()) != null) {
			
			caret.moveLine(line);
			
			if(line.isBlank())
				break;
			
			length += line.length() + (length > 0 ? 1 : 0); // don't count newline if first time
		}
		caption.textLength = length;
		return caption;
	}
	
	@SuppressWarnings("unchecked")
	public void pairWith(DocumentSubtitles otherSubtitles)
	{
		pairedCaptions = new LinkedList[captions.size()];
		for(int i = 0; i < pairedCaptions.length; ++i)
			pairedCaptions[i] = new LinkedList<>(); 
		
		List<Caption> otherCaptions = otherSubtitles.captions;
		int i = 0, j = 0;
		Caption caption = null;
		Caption otherCaption = null;
		
		while(i < captions.size() && j < otherSubtitles.captions.size())
		{
			caption = captions.get(i);
			otherCaption = otherCaptions.get(j);
			
			if(caption.isOverlappingExclusive(otherCaption)) {
				pairedCaptions[i].add(new ImmutablePair<>(otherCaption, MATCH));
				if(j + 1 < otherSubtitles.captions.size()) {
					Caption nextOtherCaption = otherCaptions.get(j + 1);
					if(nextOtherCaption.startTime.isBefore(caption.endTime)) {
						++j;
					} else {
						++i;
					}
				} else {
					++i;
				}
			} else if(caption.isBefore(otherCaption)){
				Caption tempCaption = (j != 0 ? otherCaptions.get(j - 1) : otherCaption);
				pairedCaptions[i].add(new ImmutablePair<Caption, Integer>(tempCaption, NO_MATCH));
				++i;
			} else {
				++j;
			}
			
			
		}
		
		// assign remaining captions a pair.
		for(;i < captions.size(); ++i) {
			pairedCaptions[i].add(new ImmutablePair<>(otherCaption, NO_MATCH));
		}
	}
	
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
	
	private class Caret
	{
		public int pos;
		
		public void moveLine(String str)
		{
			pos += str.length() + 1;
		}
	}

	@Override
	public Iterator<Caption> iterator() {
		return captions.iterator();
	}

	public String getText() {
		return text;
	}
}
