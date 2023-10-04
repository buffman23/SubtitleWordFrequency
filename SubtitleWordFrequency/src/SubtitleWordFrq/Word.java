package SubtitleWordFrq;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class Word implements Comparable<Word> {
	private String value;
	
	private int count; // frequency
	
	private String definiton;
	
	private List<String> tags; 
	
	private boolean hidden; // if user would like to hide word (b/c they already know it)
	
	private int selectedReferenceIndex;
	
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
	
	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
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
	
	public int getSelectedReferenceIndex() {
		return selectedReferenceIndex;
	}
	
	public Caption getSelectedReference() {
		return references.get(selectedReferenceIndex);
	}

	public void setSelectedReferenceIndex(int selectedReference) {
		this.selectedReferenceIndex = selectedReference;
	}
	
	public void setSelectedReference(Caption selectedReference) {
		this.selectedReferenceIndex = references.indexOf(selectedReference);
	}

	public void setDefiniton(String definiton) {
		this.definiton = definiton;
	}
	
	public boolean isCapitalized()
	{
		return Character.isUpperCase(value.charAt(0)); 
	}
	
	public void setCapitalized(boolean capitalized) {
		if(capitalized) 
			value = StringUtils.capitalize(value);
		 else 
			value = StringUtils.uncapitalize(value);
	}

	@Override
	public String toString()
	{
		return value;
	}

	@Override
	public int compareTo(Word o) {
		return value.compareTo(o.value);
	}
}
