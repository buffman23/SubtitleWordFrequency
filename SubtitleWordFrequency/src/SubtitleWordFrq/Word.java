package SubtitleWordFrq;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class Word implements Comparable<Word> {
	private String value;
	
	private int count; // frequency
	
	private String definiton;
	
	private List<String> tags; 
	
	private Hidden hidden; // if user would like to hide word (b/c they already know it)
	
	private boolean collapsed;
	
	private int selectedReferenceIndex;
	
	private List<Caption> references;
	
	private List<Word> associatedWords;
	
	public Word(String word)
	{
		this.value = word;
		this.definiton = "";
		// I decided to use LinkedList since many words will be low frequency and therefore not have many references.
		this.references = new LinkedList<>();
		this.collapsed = true;
		this.hidden = Hidden.OFF;
	}

	public Word(String value, Caption origin)
	{
		this(value);
		addReference(origin);
	}
	
	public Word(String value, List<Word> associatedWords)
	{
		this(value);
		setAssociatedWords(associatedWords);
		setReferences(associatedWords.stream()
			.flatMap(word -> word.getReferences().stream())
			.collect(Collectors.toList())
		);		
		setTags(associatedWords.stream()
			.flatMap(word -> word.getTags() != null ? word.getTags().stream() : Stream.empty())
			.sorted()
			.collect(Collectors.toList())
		);
		setDefiniton(associatedWords.stream()
			.map(Word::getDefiniton)
			.filter(Predicate.not(String::isBlank))
			.findFirst()
			.orElse("")
		);
	}
	
	public int getCount() {
		if(associatedWords != null) {
			return associatedWords.stream().mapToInt(Word::getCount).sum();
		}
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

	public List<Word> getAssociatedWords() {
		return associatedWords;
	}

	public void setAssociatedWords(List<Word> associatedWords) {
		this.associatedWords = associatedWords;
	}
	
	public boolean isGroup()
	{
		return associatedWords != null && !associatedWords.isEmpty();
	}
	
	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Hidden getHidden() {
		return hidden;
	}

	public void setHidden(Hidden hidden) {
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
		if(references.size() == 0) {
			System.out.println();
		}
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

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}
	
	public void ungroupAssociatedWords()
	{
		if(associatedWords == null)
			return;
		
		for(int i = associatedWords.size() - 1; i >= 0; --i) {
			Word assocWord = associatedWords.get(i);
			if(assocWord.isGroup()) {
				associatedWords.remove(i);
				associatedWords.addAll(assocWord.getAssociatedWords());
			}
		}
	}

	@Override
	public String toString()
	{
		return value;
	}

	@Override
	public int compareTo(Word o) {
		return value.compareToIgnoreCase(o.value);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if(other instanceof Word) { 
			Word otherWord = (Word)other;
			
			if(value.equalsIgnoreCase(otherWord.value)) {
				return true;
			}
			
			if(associatedWords != null && associatedWords.contains(otherWord)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		// hashcode is used to check if a session needs to be saved. We will treat Hidden.OFF and Hidden.SESSION as the same in this case.
		return Objects.hash(value, definiton, tags, hidden == Hidden.ON, associatedWords);
	}

}
