package SubtitleWordFrq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;

public class DocumentPlainText extends Document {

	public DocumentPlainText(String subtitles) throws IOException
	{
		captions = new ArrayList<Caption>();
		int sequenceNumber = 0;
		text = subtitles;
		
		try(BufferedReader br = new BufferedReader(new StringReader(subtitles))) {
			Caret caret = new Caret();
			Caption caption;
			while((caption = parseCaption(br, caret, sequenceNumber++)) != null) {
				captions.add(caption);
				caption.text = CharBuffer.wrap(subtitles).subSequence(caption.textPosition, caption.textPosition + caption.textLength);
			}
		}
	}
	
	private Caption parseCaption(BufferedReader br, Caret caret, int sequenceNumber) throws IOException
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
		
		// parse sequence number
		caption.sequenceNumber = sequenceNumber;
		
		// parse text run
		caption.textPosition = caret.pos - line.length() - 1;
		caption.textLength = line.length();
		caption.startPosition = caption.textPosition;
		
		return caption;
	}
	

	@Override
	public void pairWith(Document otherSubtitles) {}
}
