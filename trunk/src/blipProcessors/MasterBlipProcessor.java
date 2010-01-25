package blipProcessors;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

/**
 * 
 * @author n.lefler
 *
 */
public class MasterBlipProcessor implements BlipProcessor {
	private static final Logger log = Logger.getLogger(MasterBlipProcessor.class.getName());
	
	public final Map<String, BlipProcessor> processorsMap;
	
	public MasterBlipProcessor() {
		processorsMap = new HashMap<String, BlipProcessor>();
		processorsMap.put(CMD_OPEN_IDENT + ForceNewWaveBlipProcessor.FORCE_NEW_WAVE + CMD_CLOSE_IDENT, new ForceNewWaveBlipProcessor());
		processorsMap.put(CMD_OPEN_IDENT + VoteNewWaveBlipProcessor.VOTE_NEW_WAVE + CMD_CLOSE_IDENT, new VoteNewWaveBlipProcessor());
		processorsMap.put(CMD_OPEN_IDENT + WeatherRequestBlipProcessor.WEATHER, new WeatherRequestBlipProcessor());
		processorsMap.put(CMD_OPEN_IDENT + VoteToBanBlipProcessor.VOTE_TO_BAN, new VoteToBanBlipProcessor());
		processorsMap.put(CMD_OPEN_IDENT + VoteToBanBlipProcessor.VOTE_TO_UNBAN, new VoteToBanBlipProcessor());
		processorsMap.put(CMD_OPEN_IDENT + AutoInviteBlipProcessor.AUTO_INVITE + CMD_CLOSE_IDENT, new AutoInviteBlipProcessor());
		processorsMap.put(CMD_OPEN_IDENT + WaveStatsBlipProcessor.WAVE_STATS + CMD_CLOSE_IDENT, new WaveStatsBlipProcessor());
	}
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String commandText = (String)dataMap.get("commandText");
		BlipProcessor processor = processorsMap.get(commandText);
		
		return processor.processBlip(blip, wavelet, dataMap);
	}
}
