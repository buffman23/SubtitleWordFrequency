package SubtitleWordFrq;

import java.util.List;
import java.util.stream.Collectors;

import javax.management.InstanceNotFoundException;

import com.google.gson.annotations.SerializedName;

public class SerializableWord implements Comparable<SerializableWord> {
	@SerializedName("word")
	public String value;
	public String definition;
	public List<String> tags;
	public Boolean hidden;
	public List<String> associatedWords;
	
	public SerializableWord()
	{
		
	}
	
	public SerializableWord(Word word)
	{
		this.value = word.toString();
		
		if(word.getDefiniton().length() > 0)
			this.definition = word.getDefiniton();
		if(word.getTags() != null && word.getTags().size() > 0)
			this.tags = word.getTags();
		if(word.getHidden() != null && word.getHidden() == Hidden.ON)
			this.hidden = true;
		if(word.getAssociatedWords() != null)
			this.associatedWords = word.getAssociatedWords().stream().map(Word::toString).collect(Collectors.toList());
	}
	
	public SerializableWord(SerializableWord other)
	{
		this.value = other.value;
		this.definition = other.definition;
		this.tags = other.tags;
		this.hidden = other.hidden;
		this.associatedWords = other.associatedWords;
	}
	
	public boolean isCapitalized()
	{
		return Character.isUpperCase(value.charAt(0));
	}
	
	public boolean isGroup()
	{
		return associatedWords != null && associatedWords.size() > 0;
	}
	
	@Override
	public String toString()
	{
		return value;
	}
	
	@Override
	public boolean equals(Object other) {
		return this.toString().equals(other.toString());
	}
	
	@Override
	public int compareTo(SerializableWord other)
	{
		return this.value.compareToIgnoreCase(other.value);
	}
}
