package SubtitleWordFrq;

import java.util.HashMap;
import java.util.Map;

public class PreprocessConfig {
	public Map<String, String> replaceMap;
	
	private PreprocessConfig()
	{
		replaceMap = new HashMap<>();
	}
	
	public static PreprocessConfig load()
	{
		PreprocessConfig preprocessConfig = Utils.deserialize("preprocess.json", PreprocessConfig.class);
		
		if(preprocessConfig == null)
			preprocessConfig = new PreprocessConfig();
		
		return preprocessConfig;
	}
	
	public static void save(PreprocessConfig preprocessConfig)
	{
		Utils.serialize(preprocessConfig, "preprocess.json", PreprocessConfig.class);
	}
}
