package blipProcessors;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import stats.WaveStats;

import waveutils.Utils;

import autobot.Auto_BotServlet;

import com.google.wave.api.Blip;
import com.google.wave.api.Element;
import com.google.wave.api.Gadget;
import com.google.wave.api.Image;
import com.google.wave.api.Wavelet;

/**
 * 
 * @author n.lefler
 *
 */

/* I'm migrating this shit to get rid of all the blip processors. They are essentially
 * function pointers, which seems like unnecessary overhead to make multiple function calls
 * just to do one thing.
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
		startsWithMap.put("like", CMD_OPEN_IDENT + "like" + CMD_CLOSE_IDENT);
		startsWithMap.put("dislike", CMD_OPEN_IDENT + "dislike" + CMD_CLOSE_IDENT);
		startsWithMap.put("spoiler", CMD_OPEN_IDENT + "spoiler" + CMD_CLOSE_IDENT);
		startsWithMap.put("roulette", CMD_OPEN_IDENT + "russian-roulette" + CMD_CLOSE_IDENT);
	}
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String commandText = ((String)dataMap.get("commandText")).trim();
		log.log(Level.INFO, "AUTO-BOT: Received '" + commandText + "'");
		
		/* this is where i rewrite the processing thing, no more processor objects */
		if (commandText.startsWith(startsWithMap.get("dislike"))) {
			return processDislikeCommand(blip, wavelet, dataMap);
		}
		else if (commandText.startsWith(startsWithMap.get("like"))) {
			return processLikeCommand(blip, wavelet, dataMap);
		}
		else if (commandText.startsWith(startsWithMap.get("roulette"))) {
			return processRussianRouletteCommand(blip, wavelet, dataMap);
		}
		else if (commandText.startsWith(startsWithMap.get("spoiler"))) {
			return processSpoilerCommand(blip, wavelet, dataMap);
		}
		
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
		} 
		else {
			return processorsMap.get("nonCommand");
		}
	}
	
	/* 'Dislike' this */
	private Wavelet processDislikeCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String likeStr = " " + blip.getCreator() + " dislikes this blip! >:|";
		blip.all().delete();
		blip.append("\n");
		Element like = new Image("http://imgur.com/VnwPf.png", 15, 15, "");
		blip.append(like);
		Utils.appendToBlip(blip, likeStr);
		
		return wavelet;
	}
	
	/* 'Like' this */
	private Wavelet processLikeCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String likeStr = " " + blip.getCreator() + " likes this blip!";
		blip.all().delete();
		blip.append("\n");
		Element like = new Image("https://wiki.endoftheinter.net/images/4/44/Like.png", 15, 15, "");
		blip.append(like);
		Utils.appendToBlip(blip, likeStr);
		
		return wavelet;
	}
	
	/* Russian roulette */
	private Wavelet processRussianRouletteCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		Random generator = new Random();
		int drop = generator.nextInt(2);
		Object participant;
		
		if (drop == 0)
			participant = blip.getCreator();
		else { 
			drop = generator.nextInt(wavelet.getParticipants().size());
			participant = wavelet.getParticipants().toArray()[drop];
		}

		// TODO FIX THIS
		Auto_BotServlet.log.log(Level.INFO, "Kicking " + participant + " out of the wave.");
		try {
			Utils.appendLineToBlip(blip, "\nThanks for transforming " + participant + ".");
			wavelet.getParticipants().remove(participant);
		}
		catch (UnsupportedOperationException e) {
			WaveStats ws = (WaveStats)dataMap.get("WaveStats");
			if (ws.getMuted() == null || ws.getMuted().equals(""))
				ws.setMuted(participant.toString());
		}
		
		return wavelet;
	}
	
	public Wavelet processSpoilerCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		Utils.replaceBlip(blip, "\n\nSpoiler: ");
		Auto_BotServlet.log.log(Level.INFO, "WaveDomain: " + wavelet.getWaveId().getDomain() + " | WaveID: " + wavelet.getWaveId().getId());
		Auto_BotServlet.log.log(Level.INFO, "WaveletDomain: " + wavelet.getWaveletId().getDomain() + " | WaveletID: " + wavelet.getWaveletId().getId());
		blip.append(new Gadget("http://spoil-bot.appspot.com/spoil.xml?wave=" + blip.serialize().getWaveId() + "&wavelet=" + blip.serialize().getWaveletId() + "&blip=" + blip.getBlipId()));
		
		
		return wavelet;
	}
}
