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
public abstract class AbstractBlipProcessor {
	private static final Logger log = Logger.getLogger(AbstractBlipProcessor.class.getName());
	
	public static final String CMD_OPEN_IDENT = "!@";
	public static final String CMD_CLOSE_IDENT = "@!";
	
	public static final Map<String, Class> processorsMap =
		new HashMap<String, Class>() {{
			put(CMD_OPEN_IDENT + ForceNewWaveBlipProcessor.FORCE_NEW_WAVE + CMD_CLOSE_IDENT, ForceNewWaveBlipProcessor.class);
			put(CMD_OPEN_IDENT + VoteNewWaveBlipProcessor.VOTE_NEW_WAVE + CMD_CLOSE_IDENT, VoteNewWaveBlipProcessor.class);
			put(CMD_OPEN_IDENT + WeatherRequestBlipProcessor.WEATHER, WeatherRequestBlipProcessor.class);
			put(CMD_OPEN_IDENT + VoteToBanBlipProcessor.VOTE_TO_BAN, VoteToBanBlipProcessor.class);
			put(CMD_OPEN_IDENT + VoteToBanBlipProcessor.VOTE_TO_UNBAN, VoteToBanBlipProcessor.class);
			put(CMD_OPEN_IDENT + AutoInviteBlipProcessor.AUTO_INVITE + CMD_CLOSE_IDENT, AutoInviteBlipProcessor.class);
			put(CMD_OPEN_IDENT + WaveStatsBlipProcessor.WAVE_STATS + CMD_CLOSE_IDENT, WaveStatsBlipProcessor.class);
		}};
		
	public static Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String commandText = (String)dataMap.get("commandText");
		Class<AbstractBlipProcessor> processorClass = processorsMap.get(commandText);
		
		try {
			return processorClass.newInstance().processBlip(blip, wavelet, dataMap);
		} catch (InstantiationException e) {
			log.log(Level.SEVERE, "Could not launch blip processing");
			
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			log.log(Level.SEVERE, "Could not launch blip processing");
			
			e.printStackTrace();
		}
		
		return null;
	}
}
