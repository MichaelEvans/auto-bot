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
public class BlipProcessorMediator implements IBlipProcessor {
	private static final Logger log = Logger.getLogger(BlipProcessorMediator.class.getName());
	
	private Map<String, String> startsWithMap;
	public final Map<String, IBlipProcessor> processorsMap;
	
	public BlipProcessorMediator() {
		processorsMap = new HashMap<String, IBlipProcessor>();
		processorsMap.put("voteNewWave", new VoteNewWaveBlipProcessor());
		processorsMap.put("voteToBan", new VoteToBanBlipProcessor());
		processorsMap.put("voteToUnBan", new VoteToBanBlipProcessor());
		processorsMap.put("nonCommand", new NonCommandBlipProcessor());
		
		startsWithMap = new HashMap<String, String>();
		startsWithMap.put("forceNewWave", CMD_OPEN_IDENT + ForceNewWaveBlipProcessor.FORCE_NEW_WAVE + CMD_CLOSE_IDENT);
		startsWithMap.put("voteNewWave", CMD_OPEN_IDENT + VoteNewWaveBlipProcessor.VOTE_NEW_WAVE + CMD_CLOSE_IDENT);
		startsWithMap.put("weather", CMD_OPEN_IDENT + WeatherRequestBlipProcessor.WEATHER);
		startsWithMap.put("voteToBan", CMD_OPEN_IDENT + VoteToBanBlipProcessor.VOTE_TO_BAN);
		startsWithMap.put("voteToUnBan", CMD_OPEN_IDENT + VoteToBanBlipProcessor.VOTE_TO_UNBAN);
		startsWithMap.put("autoInvite", CMD_OPEN_IDENT + AutoInviteBlipProcessor.AUTO_INVITE + CMD_CLOSE_IDENT);
		startsWithMap.put("waveStats", CMD_OPEN_IDENT + WaveStatsBlipProcessor.WAVE_STATS + CMD_CLOSE_IDENT);
		startsWithMap.put("runTests", CMD_OPEN_IDENT + RunTestsBlipProcessor.RUN_TESTS + CMD_CLOSE_IDENT);
	}
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String commandText = (String)dataMap.get("commandText");
		
		try {
			return getProcessor(commandText).processBlip(blip, wavelet, dataMap);
		} catch (InvalidBlipProcessorException e) {
			log.log(Level.SEVERE, "Caught InvalidBlipProcessorException for command string '" + commandText + "'!");
		}

		return wavelet;
	}
	
	private IBlipProcessor getProcessor(String commandText) throws InvalidBlipProcessorException {
		if (commandText.startsWith(startsWithMap.get("forceNewWave"))) {
			return new ForceNewWaveBlipProcessor(); 
		} else if (commandText.startsWith(startsWithMap.get("voteNewWave"))) {
			return processorsMap.get("voteNewWave");
		} else if (commandText.startsWith(startsWithMap.get("weather"))) {
			return new WeatherRequestBlipProcessor();
		} else if (commandText.startsWith(startsWithMap.get("voteToBan"))) {
			return processorsMap.get("voteToBan");
		} else if (commandText.startsWith(startsWithMap.get("voteToUnBan"))) {
			return processorsMap.get("voteToUnBan");
		} else if (commandText.startsWith(startsWithMap.get("autoInvite"))) {
			return new AutoInviteBlipProcessor();
		} else if (commandText.startsWith(startsWithMap.get("waveStats"))) {
			return new WaveStatsBlipProcessor();
		} else if (commandText.startsWith(startsWithMap.get("runTests"))) {
			return new RunTestsBlipProcessor();
		} else {
			return processorsMap.get("nonCommand");
		}
	}
}
