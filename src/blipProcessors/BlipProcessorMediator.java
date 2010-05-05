package blipProcessors;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import stats.WaveStats;


import autobot.Auto_BotServlet;
import autobot.Tools;
import autobot.WeatherParser;

import com.google.wave.api.Blip;
import com.google.wave.api.BlipContent;
import com.google.wave.api.Element;
import com.google.wave.api.Gadget;
import com.google.wave.api.Image;
import com.google.wave.api.Participants;
import com.google.wave.api.Plaintext;
import com.google.wave.api.Wavelet;
import com.trollhouse.wave.utils.Utils;

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
		startsWithMap.put("forceNewWave", CMD_OPEN_IDENT + "force-new-wave" + CMD_CLOSE_IDENT);
		startsWithMap.put("voteNewWave", CMD_OPEN_IDENT + VoteNewWaveBlipProcessor.VOTE_NEW_WAVE + CMD_CLOSE_IDENT);
		startsWithMap.put("weather", CMD_OPEN_IDENT + "weather");
		startsWithMap.put("voteToBan", CMD_OPEN_IDENT + VoteToBanBlipProcessor.VOTE_TO_BAN);
		startsWithMap.put("voteToUnBan", CMD_OPEN_IDENT + VoteToBanBlipProcessor.VOTE_TO_UNBAN);
		startsWithMap.put("autoInvite", CMD_OPEN_IDENT + AutoInviteBlipProcessor.AUTO_INVITE + CMD_CLOSE_IDENT);
		startsWithMap.put("waveStats", CMD_OPEN_IDENT + "get-wave-stats" + CMD_CLOSE_IDENT);
		startsWithMap.put("like", CMD_OPEN_IDENT + "like" + CMD_CLOSE_IDENT);
		startsWithMap.put("dislike", CMD_OPEN_IDENT + "dislike" + CMD_CLOSE_IDENT);
		startsWithMap.put("spoiler", CMD_OPEN_IDENT + "spoiler" + CMD_CLOSE_IDENT);
		startsWithMap.put("roulette", CMD_OPEN_IDENT + "russian-roulette" + CMD_CLOSE_IDENT);
	}
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String commandText = ((String)dataMap.get("commandText")).trim();
		log.log(Level.INFO, "AUTO-BOT: Received '" + commandText + "'");
		
		/* this is where i rewrite the processing thing, no more processor objects */
		if (commandText.startsWith(startsWithMap.get("dislike")))
			return processDislikeCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(startsWithMap.get("forceNewWave")))
			return processForceCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(startsWithMap.get("like")))
			return processLikeCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(startsWithMap.get("roulette")))
			return processRussianRouletteCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(startsWithMap.get("spoiler")))
			return processSpoilerCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(startsWithMap.get("weather")))
			return processWeatherCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(startsWithMap.get("waveStats"))) {
			return processWaveStatsCommand(blip, wavelet, dataMap);
		}
		else
			return wavelet;
	}
	
	/* 'Dislike' this */
	private Wavelet processDislikeCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String likeStr = " " + blip.getCreator() + " dislikes this blip! >:|";
		Element like = new Image("http://imgur.com/VnwPf.png", 15, 15, "");
		log.log(Level.INFO, "Processing dislike");
		Utils.replaceBlipContent(blip, "!@dislike@!", "\n!@thumbdown@!!@dislike@!");
		Utils.replaceBlipContent(blip, "!@thumbdown@!", like);
		Utils.replaceBlipContent(blip, "!@dislike@!", likeStr);
		
		return wavelet;
	}
	
	/* Force new wave */
	public Wavelet processForceCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		if (((HashSet<String>)dataMap.get("privelegedWavers")).contains(blip.getCreator())) {
			
			Auto_BotServlet.log.info(blip.getCreator() + " is forcing a new wave.");
			
			//TODO: Deal with NEW_WAVE_INDICATOR better
			Utils.reply(wavelet, Auto_BotServlet.NEW_WAVE_INDICATOR + "\n\n!{fuck_this_thread_im_outta_here}!");
			Utils.createWaveWithOther((Auto_BotServlet)dataMap.get("robot"), wavelet, Tools.newTitle(wavelet), wavelet.getDomain(), wavelet.getParticipants());
		}
		
		return wavelet;
	}
	
	/* 'Like' this */
	private Wavelet processLikeCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String likeStr = " " + blip.getCreator() + " likes this blip!";
		Element like = new Image("https://wiki.endoftheinter.net/images/4/44/Like.png", 15, 15, "");
		log.log(Level.INFO, "Processing like");
		Utils.replaceBlipContent(blip, "!@like@!", "\n!@thumbup@!!@like@!");
		Utils.replaceBlipContent(blip, "!@thumbup@!", like);
		Utils.replaceBlipContent(blip, "!@like@!", likeStr);
		
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
			wavelet.getParticipants().setParticipantRole(participant.toString(), Participants.Role.READ_ONLY);
		}
		catch (UnsupportedOperationException e) {
			WaveStats ws = (WaveStats)dataMap.get("WaveStats");
			if (ws.getMuted() == null || ws.getMuted().equals(""))
				ws.setMuted(participant.toString());
		}
		
		return wavelet;
	}
	
	/* Spoiler command */
	public Wavelet processSpoilerCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		Utils.replaceBlipContent(blip, "!@spoiler@!", "\n\nSpoiler:\n");
		//Utils.replaceBlip(blip, "\n\nSpoiler:\n");
		Auto_BotServlet.log.log(Level.INFO, "WaveDomain: " + wavelet.getWaveId().getDomain() + " | WaveID: " + wavelet.getWaveId().getId());
		Auto_BotServlet.log.log(Level.INFO, "WaveletDomain: " + wavelet.getWaveletId().getDomain() + " | WaveletID: " + wavelet.getWaveletId().getId());
		blip.append(new Gadget("http://spoil-bot.appspot.com/spoil.xml?wave=" + blip.serialize().getWaveId() + "&wavelet=" + blip.serialize().getWaveletId() + "&blip=" + blip.getBlipId()));
		
		
		return wavelet;
	}
	
	/* Wave stats command */
	public Wavelet processWaveStatsCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		log.log(Level.INFO, "AUTO-BOT: Processing 'get-wave-stats'");
		StringBuffer responseBuffer = new StringBuffer();
		
		WaveStats waveStats = (WaveStats) dataMap.get("WaveStats");
		
		responseBuffer.append("\n\n");
		
		responseBuffer.append("Auto-Bot's Unique ID: ");
		responseBuffer.append(dataMap.get("autobotID"));
		responseBuffer.append("\n\n");
		
		responseBuffer.append("Wave created: ");
		responseBuffer.append(sdf.format(new Date(wavelet.getCreationTime())));
		responseBuffer.append("\n\n");
		
		responseBuffer.append("This Wave's ID: ");
		responseBuffer.append(dataMap.get("waveID"));
		responseBuffer.append("\n\n");
		
		responseBuffer.append("This wavelet's ID: ");
		responseBuffer.append(dataMap.get("waveletID"));
		responseBuffer.append("\n\n");
		
		
		responseBuffer.append("Number of Blips (According to Auto-Bot): ");
		responseBuffer.append(dataMap.get("numberOfBlips"));
		responseBuffer.append("\n\n");
		
		responseBuffer.append("This blip's ID: ");
		responseBuffer.append(blip.getBlipId());
		responseBuffer.append("\n\n");
		
		responseBuffer.append(blip.getCreator()+":\n");
		responseBuffer.append("BlipCount:" + waveStats.getUser(blip.getCreator()).getBlipCount() + "\n");
		responseBuffer.append("DeleteCount:" + waveStats.getUser(blip.getCreator()).getDeleteCount() + "\n");
		responseBuffer.append("EditCount:" + waveStats.getUser(blip.getCreator()).getEditCount() + "\n");
		
		Utils.replaceBlip(blip, responseBuffer.toString());
		
		return wavelet;
	}
	
	/* Weather command */
	public Wavelet processWeatherCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		final String REGEX = "weather:([0-9]{5})";
		Pattern weatherPattern = Pattern.compile(REGEX);
		Matcher mtchr = weatherPattern.matcher(blip.getContent());
		String current = "";
		String imageURL = "";
		while(mtchr.find()) {
			log.log(Level.INFO, "Found something: " + mtchr.group());
			String zip = mtchr.group(1);
		
			try {
				try {
					log.log(Level.INFO, "Getting weather for " + zip);
					
					current = WeatherParser.getLocation(zip);
					current += "\nNow - " + WeatherParser.getTemp(zip);
					current += "\n" + WeatherParser.getForecast(zip);
					imageURL = WeatherParser.getImage(zip);
					
					log.log(Level.INFO, "This is something? " + current);
				} catch (NumberFormatException e) {
					Utils.appendLineToBlip(blip, "Encountered an error when requesting weather");
				
					Auto_BotServlet.log.log(Level.WARNING, "Caught NumberFormatException when requesting weather, message was: " + e.getLocalizedMessage());
					e.printStackTrace();
				} catch (IOException e) {
					Utils.appendLineToBlip(blip, "Encountered an error when requesting weather");
				
					log.warning("Caught IOException when requesting weather, message was: " + e.getLocalizedMessage());
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					log.warning("Could not construct xml parser");
				} catch (SAXException e) {
					log.warning("Unable to parse weather xml");
				}
			
				log.log(Level.INFO, "Replacing blip with: " + current);

			} catch (IllegalStateException e) {
				log.warning("Caught IllegalStateException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				Utils.appendLineToBlip(blip, "Incorrect command form. Correct form is " + CMD_OPEN_IDENT + "weather" + ":<zip code, 5 digits>" + CMD_CLOSE_IDENT);
			
				log.warning("Caught IndexOutOfBoundsException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		Utils.replaceBlip(blip, "\n\n\n" + current);
		blip.append(new Image(imageURL,52,52,""));

		return wavelet;
	}
}
