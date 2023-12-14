package SubtitleWordFrq;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class WordFrequencyParser {
		
	private PreprocessConfig preprocessConfig;
	
	public WordFrequencyParser()
	{
		preprocessConfig = PreprocessConfig.load();
	}

	public ArrayList<Word> createWordFrequencyList(String subtitles, Document documentSubtitles)
	{
		TreeMap<String, Word> wordMap = new TreeMap<>();
		
		for(Caption caption : documentSubtitles)
		{
			parseCaption(caption, subtitles, wordMap);
		}
		
		return new ArrayList<>(wordMap.values());
	}
	
	public String preprocessWord(String word)
	{
		for(Entry<String, String> entry : preprocessConfig.replaceMap.entrySet()) {
			String key = "[" + Pattern.quote(entry.getKey()) + "]";
			word = word.replaceAll(key,  entry.getValue());
		}
		
		return word.replaceAll("\\s+", "");
	}
	
	private void parseCaption(Caption caption, String subtitles, TreeMap<String,Word> wordMap){
		String content = subtitles.substring(caption.textPosition, caption.textPosition + caption.textLength);
		//private static final String DEFAULT_STRIP_STRING = "[" + Pattern.quote("[]{}<>,./?;:'\"\\|-=_+!@#$%^&*()~` …") + "]";
		for(Entry<String, String> entry : preprocessConfig.replaceMap.entrySet()) {
			String key = "[" + Pattern.quote(entry.getKey()) + "]";
			content = content.replaceAll(key, entry.getValue());
		}
		
		
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
