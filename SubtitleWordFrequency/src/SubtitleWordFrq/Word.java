package SubtitleWordFrq;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Word {
	private String value;
	private int count; // frequency
	private String definiton;
	private boolean hidden; // if user would like to hide word (b/c they already know it)
	private List<Caption> references;
	private List<Word> associatedWords;
	
	public Word(String word)
	{
		this.value = word;
		this.definiton = "";
		// I decided to use LinkedList since many words will be low frequency and therefore not have many references.
		this.references = new LinkedList<>();
	}
	
	public Word(String word, Caption origin)
	{
		this(word);
		addReference(origin);
	}
	
	public int getTotalCount() {
		int total = count;
		
		if(associatedWords != null && !associatedWords.isEmpty())
		{
			total = associatedWords.stream()
					.mapToInt(word -> word.getTotalCount())
					.sum();
		}
		
		return total;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public List<Caption> getTotalReferences() {
		List<Caption> allReferences = new ArrayList<>(references);
		
		if(associatedWords != null && !associatedWords.isEmpty())
		{
			allReferences = associatedWords.stream()
					.map(word -> word.getTotalReferences())
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
		}
		
		return allReferences;
	}
	
	public void incrementCount(){
		++count;
	}
	
	public void addReference(Caption caption){
		// prevent duplicates
		if(!references.contains(caption))
			references.add(caption);
	}
	
	public List<Caption> getReferences() {
		return references;
	}

	public void setReferences(List<Caption> references) {
		this.references = references;
	}
	
	public List<Word> getAllAssociatedWords() {
		if(associatedWords == null || associatedWords.isEmpty())
			return associatedWords;
		
		List<Word> allAssociatedWords = associatedWords.stream()
				.map(word -> word.getAllAssociatedWords())
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		allAssociatedWords.addAll(associatedWords);
		
		return allAssociatedWords;
	}

	public List<Word> getAssociatedWords() {
		return associatedWords;
	}

	public void setAssociatedWords(List<Word> associatedWords) {
		this.associatedWords = associatedWords;
	}
	
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public int length()
	{
		return value.length();
	}
	
	public String getDefiniton() {
		return definiton;
	}

	public void setDefiniton(String definiton) {
		this.definiton = definiton;
	}

	@Override
	public String toString()
	{
		return value;
	}
}
