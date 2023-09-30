package SubtitleWordFrq;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.*;

public class FrequencyEntry {
	public String word;
	public int count;
	public List<Integer> lineReferences;
	
	public FrequencyEntry(String word, int firstReference)
	{
		this.word = word;
		this.count = 1;
		this.lineReferences = new ArrayList<Integer>(1);
		lineReferences.add(firstReference);
	}
	
	@Override
	public String toString()
	{
		return String.format("%s,%d,%s", word, count, org.apache.commons.text.StringEscapeUtils.escapeCsv(StringUtils.join(lineReferences, ",")));
	}
}
