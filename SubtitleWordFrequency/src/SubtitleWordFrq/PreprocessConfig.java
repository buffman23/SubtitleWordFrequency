package SubtitleWordFrq;

import java.io.IOException;
import java.lang.System.Logger;
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
		PreprocessConfig preprocessConfig;
		try {
			preprocessConfig = Utils.deserialize("data/preprocess.json", PreprocessConfig.class);
		} catch (IOException e) {
			Utils.logger.severe(e.getMessage());
			//e.printStackTrace();
			preprocessConfig = new PreprocessConfig();
		}
		
		return preprocessConfig;
	}
	
	public static void save(PreprocessConfig preprocessConfig)
	{
		try {
			Utils.serialize(preprocessConfig, "data/preprocess.json", PreprocessConfig.class);
		} catch (IOException e) {
			Utils.logger.severe(e.getMessage());
			//e.printStackTrace();
		}
	}
}
