package blipProcessors;

import java.awt.Toolkit;
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
	
	private static final String CMD_DISLIKE = "//dislike";
	private static final String CMD_FORCE = "//force";
	private static final String CMD_HELP = "//help";
	private static final String CMD_IMG_ADD = "//iadd:";
	private static final String CMD_IMG_CMD = "//img:";
	private static final String CMD_LIKE = "//like";
	private static final String CMD_LINKS = "//links";
	private static final String CMD_SPOILER = "//spoiler";
	private static final String CMD_STATS = "//stats";
	private static final String CMD_USER = "//user";
	private static final String CMD_WEATHER = "//weather";
	
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String commandText = blip.getContent().trim();
		log.log(Level.INFO, "[BPM] Received '" + commandText + "'");
		
		/* this is where i rewrite the processing thing, no more processor objects */
		if (commandText.startsWith(CMD_DISLIKE))
			return processDislikeCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(CMD_FORCE))
			return processForceCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(CMD_HELP))
			return processHelpCommand(blip, wavelet);
		else if (commandText.startsWith(CMD_IMG_ADD))
			return processImgAddCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(CMD_IMG_CMD))
			return processImgCmdCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(CMD_LIKE))
			return processLikeCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(CMD_LINKS))
			return processLinksCommand(blip, wavelet, dataMap);
		//else if (commandText.startsWith(startsWithMap.get("nuke")))
		//	return processNukeCommand(blip, wavelet, dataMap);
		//else if (commandText.startsWith(startsWithMap.get("roulette")))
		//	return processRussianRouletteCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(CMD_SPOILER))
			return processSpoilerCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(CMD_USER))
			return processUserCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(CMD_WEATHER))
			return processWeatherCommand(blip, wavelet, dataMap);
		else if (commandText.startsWith(CMD_STATS))
			return processWaveStatsCommand(blip, wavelet, dataMap);
		else
			return wavelet;
	}
	
	/* 'Dislike' this */
	private Wavelet processDislikeCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String likeStr = " " + blip.getCreator() + " dislikes this blip! >:|";
		Element like = new Image("http://imgur.com/VnwPf.png", 15, 15, "");
		log.log(Level.INFO, "Processing dislike");
		Utils.replaceBlipContent(blip, CMD_DISLIKE, "\n!@thumbdown@!!@dislike@!");
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
	
	/* Show 'Help' blip */
	public Wavelet processHelpCommand(Blip blip, Wavelet wavelet) {
		String newBlip = "\n\nAuto-bot Help Guide:\n" +
						"//dislike - Dislike the previous blip\n" +
						"//force - Create a new sequel wave before hitting blip cap\n" +
						"//help - Show this help guide\n" +
						"//like - Like the previous blip\n" +
						"//links - Display a list of links posted in this wave\n" +
						"//stats - Post wave-specific stats\n" +
						"//spoiler - Post spoilers easily\n" +
						"//weather:XXXXX - Get the weather for zip code XXXXX\n";
		
		Utils.replaceBlipContent(blip, CMD_HELP, newBlip);
		
		return wavelet;
	}
	
	/* Add image to db */
	public Wavelet processImgAddCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String content = blip.getContent();
		content = content.replace(CMD_IMG_ADD, "");
		Matcher m = Pattern.compile("([ \\S]+)").matcher(content);
		try {
			while (m.find()) {
				content = m.group();
				log.log(Level.INFO, "[BPM] " + content);
				String args[] = content.split(",");
				for (int i=0; i < args.length; i++) {
					args[i] = args[i].trim();
					if (i > 0)
						args[i] = args[i].toLowerCase();
				}
				log.log(Level.INFO, "[BPM] Image URL: " + args[0]);
			
				String tags[] = new String[args.length - 1];
				for (int i=1; i < args.length; i++) {
					log.log(Level.INFO, "[BPM] Tag '" + args[i] + "'");
					tags[i-1] = args[i];
				}
		
				((Auto_BotServlet)dataMap.get("robot")).makeImgTag(args[0], tags);
				break;
			}
			Utils.replaceBlipContent(blip, "//iadd:" + content, "Image added!");
		}
		catch (Exception e) {
			Utils.appendLineToBlip(blip, "\nImage add failed!");
		}
		
		return wavelet;
	}
	
	public Wavelet processImgCmdCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String command = blip.getContent().replace(CMD_IMG_CMD, "").trim().toLowerCase();
		Auto_BotServlet robot = ((Auto_BotServlet)dataMap.get("robot"));
		if (command.equals("tags")) {
			log.log(Level.INFO, "Printing tag list");
			String taglist = robot.getTagList();
			Utils.replaceBlipContent(blip, CMD_IMG_CMD + "tags", taglist + "\n");
		}
		else if (command.equals("random")) {
			log.log(Level.INFO, "Getting random image");
			String url = robot.getRandomImg();
			Image img = new Image();
			img.setUrl(url);
			//log.log(Level.INFO, "Img dimensions: " + img.getWidth() + "x" + img.getHeight());
			Utils.replaceBlipContent(blip, CMD_IMG_CMD + "random", img);
		}
		
		return wavelet;
	}
	
	/* 'Like' this */
	private Wavelet processLikeCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String likeStr = " " + blip.getCreator() + " likes this blip!";
		Element like = new Image("https://wiki.endoftheinter.net/images/4/44/Like.png", 15, 15, "");
		log.log(Level.INFO, "Processing like");
		Utils.replaceBlipContent(blip, CMD_LIKE, "\n!@thumbup@!!@like@!");
		Utils.replaceBlipContent(blip, "!@thumbup@!", like);
		Utils.replaceBlipContent(blip, "!@like@!", likeStr);
		
		return wavelet;
	}
	
	/* Show all links posted in wave */
	private Wavelet processLinksCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		WaveStats waveStats = (WaveStats) dataMap.get("WaveStats");
		String replaceBlip = "\n\nLINKS:\n\n" + waveStats.getLinks();
		Utils.replaceBlipContent(blip, "//links", replaceBlip);
		return wavelet;
	}
	
	private Wavelet processNukeCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		wavelet.reply("\nWe're all fucked! Enemy nuke incoming!");
		
		
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
		Utils.replaceBlipContent(blip, CMD_SPOILER, "\n\nSpoiler:\n");
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
		
		responseBuffer.append("\n\n\n");
		
		responseBuffer.append("Wave created: ");
		responseBuffer.append(sdf.format(new Date(wavelet.getCreationTime())));
		responseBuffer.append("\n");
		
		responseBuffer.append("Number of Blips: ");
		responseBuffer.append(waveStats.getBlipCount());
		responseBuffer.append("\n");
		
		responseBuffer.append("Number of Links: ");
		responseBuffer.append(waveStats.getLinkCount());
		responseBuffer.append("\n");
		
		Utils.replaceBlip(blip, responseBuffer.toString());
		
		return wavelet;
	}
	
	public Wavelet processUserCommand(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		Matcher m = Pattern.compile(CMD_USER + ":?([\\S]+)").matcher(blip.getContent());
		String user = blip.getCreator();
		while (m.find()) {
			log.log(Level.INFO, "Username: " + m.group(1));
			if (m.group(1).isEmpty() || m.group(1).equals(":"))
				break;
			user = m.group(1) + "@googlewave.com";
		}
		
		String s = ((Auto_BotServlet)dataMap.get("robot")).getUserStats(user);
		
		Utils.appendLineToBlip(blip, s);
		
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
