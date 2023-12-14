package SubtitleWordFrq;
import java.io.*;
import java.nio.CharBuffer;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class DocumentSubtitles extends Document {
	
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
	public void pairWith(Document otherSubtitles)
	{
		pairedCaptions = new LinkedList[captions.size()];
		for(int i = 0; i < pairedCaptions.length; ++i)
			pairedCaptions[i] = new LinkedList<>(); 
		
		List<Caption> otherCaptions = otherSubtitles.getCaptions();
		int i = 0, j = 0;
		Caption caption = null;
		Caption otherCaption = null;
		
		while(i < captions.size() && j < otherSubtitles.getCaptions().size())
		{
			caption = captions.get(i);
			otherCaption = otherCaptions.get(j);
			
			if(caption.isOverlappingExclusive(otherCaption)) {
				pairedCaptions[i].add(new ImmutablePair<>(otherCaption, MATCH));
				if(j + 1 < otherSubtitles.getCaptions().size()) {
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
}
