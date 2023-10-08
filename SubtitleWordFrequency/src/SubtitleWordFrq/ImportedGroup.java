package SubtitleWordFrq;

import java.util.ArrayList;
import java.util.List;

public class ImportedGroup {
	public SerializableWord originalGroup;
	public List<String> importedWords;
	
	public ImportedGroup(SerializableWord group, List<String> importedWords)
	{
		originalGroup = group;
		this.importedWords = importedWords;
	}
	
	public SerializableWord getMergedGroup(SerializableWord group)
	{
		SerializableWord mergedGroup = new SerializableWord(group);
		mergedGroup.associatedWords = new ArrayList<>();
		
		for(String originalWord : originalGroup.associatedWords) {
			// if it wasn't removed or wasn't imported
			if(group.associatedWords.contains(originalWord) || !importedWords.contains(originalWord)) {
				mergedGroup.associatedWords.add(originalWord);
			}
		}
		
		for(String word : group.associatedWords) {
			// if is a new word added to group
			if(!importedWords.contains(word))
				mergedGroup.associatedWords.add(word);
		}
		
		return mergedGroup;
	}
}
