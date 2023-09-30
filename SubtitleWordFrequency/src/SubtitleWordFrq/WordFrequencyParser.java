package SubtitleWordFrq;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import gui.SubtitlesPanel;

public class WordFrequencyParser {
	
	private static final String DEFAULT_STRIP_STRING = "[" + Pattern.quote("[]{}<>,./?;:'\"\\|-=_+!@#$%^&*()~` …") + "]";
	
	private String stripRegex = DEFAULT_STRIP_STRING;
	
	public WordFrequencyParser()
	{
		
	}
	
	public WordFrequencyParser(String stripRegex)
	{
		this.stripRegex = stripRegex;
	}

	public ArrayList<Word> createWordFrequencyList(String subtitles, DocumentSubtitles documentSubtitles)
	{

		TreeMap<String, Word> wordMap = new TreeMap<>();
		
		for(Caption caption : documentSubtitles)
		{
			parseCaption(caption, subtitles, wordMap);
		}
		
		return new ArrayList<>(wordMap.values());
	}
	
	private void parseCaption(Caption caption, String subtitles, TreeMap<String,Word> wordMap){
		String content = subtitles.substring(caption.textPosition, caption.textPosition + caption.textLength);
		content = content.replaceAll(stripRegex,  " ");
		
		String[] wordStrings = content.split("\\s+");
		
		for(String wordString : wordStrings)
		{
			if(wordString.isEmpty())
				continue;
			
			wordString = wordString.toLowerCase();
			
			Word word = wordMap.computeIfAbsent(wordString, (key) -> new Word(key));
			word.incrementCount();
			word.addReference(caption);
		}
	}
	
}
