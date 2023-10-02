package SubtitleWordFrq;
import java.nio.CharBuffer;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Caption {
	public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HH:mm:ss,SSS");
	public int startPosition;
	public int sequenceNumber;
	public LocalTime startTime;
	public LocalTime endTime;
	public int textPosition;
	public int textLength;
	
	public Caption()
	{
		
	}
	
	public Caption(int captionPosition, int sequenceNumber, LocalTime startTime, LocalTime endTime, int textIndex, int textLength) {
		this.startPosition = captionPosition;
		this.sequenceNumber = sequenceNumber;
		this.startTime = startTime;
		this.endTime = endTime;
		this.textPosition = textIndex;
		this.textLength = textLength;
	}
	
	public boolean isBefore(Caption other) {
		return endTime.isBefore(other.startTime);
	}
	
	public boolean isAfter(Caption other) {
		return other.endTime.isBefore(startTime);
	}
	
	public boolean isOverlappingInclusive(Caption other) {
		return (startTime.isBefore(other.endTime) || startTime.equals(other.endTime)) && 
				(other.startTime.isBefore(endTime) || other.startTime.equals(endTime));
		
	}
	
	public boolean isOverlappingExclusive(Caption other) {
		return startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
		
	}
	
	@Override
	public String toString()
	{

		return String.format("%d\n%s --> %s\nFrom:%d To:%d", sequenceNumber, DTF.format(startTime), DTF.format(endTime), textPosition, textPosition + textLength);
	}
	
	public String toString(String subtitlesString)
	{
		CharBuffer text = CharBuffer.wrap(subtitlesString).subSequence(textPosition, textPosition + textLength);
		return String.format("%d\n%s --> %s\n%s", sequenceNumber, DTF.format(startTime), DTF.format(endTime), text);
	}
}
