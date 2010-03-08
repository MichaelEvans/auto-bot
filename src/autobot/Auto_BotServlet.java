package autobot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import blipProcessors.*;

import javax.jdo.PersistenceManager;

import stats.UserStats;
import stats.WaveStats;

import com.google.wave.api.*;
import com.google.wave.api.event.BlipSubmittedEvent;
import com.google.wave.api.event.WaveletSelfAddedEvent;

public class Auto_BotServlet extends AbstractRobot {

	private static final long serialVersionUID = -8761201906629081656L;
	public static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName());
	private final long uniqueID = System.nanoTime();

	public static final int MAX_BLIPS = 150;
	private int NUM_OF_VOTES = 0;
	private PersistenceManager pm;
	Map<String, WaveStats> waveStatsMap;
	
	private final IBlipProcessor blipProcessor = new BlipProcessorMediator();
	
	private ArrayList<String> activeWavers = new ArrayList<String>();
	private Set<String> privelegedWavers = new HashSet<String> () {
		private static final long serialVersionUID = -2860494720744602410L;

	{
		add("n.lefler@googlewave.com");
		add("bmwracer0@googlewave.com");
		add("dforsythe@googlewave.com");
		add("themagnum@googlewave.com");
		add("twyphoon@googlewave.com");
		add("claudio.sayan@googlewave.com");
		add("rgalginaitis@googlewave.com");
		add("rob.kiefer@googlewave.com");
		add("patrick.dattilio@googlewave.com");
	}};

	final static String WELCOME_SELF = "Autobots roll out.";
	final String NEW_WAVE_INDICATOR = "We're rolling out!";

	protected String getRobotName() {
		return "Auto-Bot";
	}

	public String getRobotAvatarUrl() {
		return "http://i.imgur.com/CQoTo.png";
	}

	public String getRobotProfilePageUrl() {
		return "auto-bot.appspot.com";
	}
	
	public Auto_BotServlet() {
		waveStatsMap = new HashMap<String, WaveStats>();
	}

	
	private void makeBlipsMap() {
		List<WaveStats> tempList;
		String query = "select from " + stats.WaveStats.class.getName() + "";
		
		if (pm == null) {
			log.log(Level.INFO,"pm is null");
		}
		else if (query == null) {
			log.log(Level.INFO, "query is null");

		}
		else {
		//	log.log(Level.INFO, "AUTO-BOT: Creating blips map.");
			
			tempList = (List<WaveStats>) pm.newQuery(query).execute();
			for (WaveStats ws : tempList) {
				waveStatsMap.put(ws.getWaveID(), ws);
			}
		}
	}
	
	private void openPM() {
		try {
			pm = PMF.get().getPersistenceManager();
		}
		catch (Exception e) {
			log.log(Level.INFO, "Error opening PersistenceManager");
		}
		makeBlipsMap();
	}
	
	private void closePM() {
		try {
			pm.close();
		}
		catch (Exception e) {
			log.log(Level.INFO, "Error closing PersistenceManager");
		}
	}
	
	
	public void onWaveletSelfAdded(WaveletSelfAddedEvent event) {
		Wavelet wavelet = event.getWavelet();
		String id = wavelet.serialize().getWaveId();
		WaveStats waveStats = null;

		openPM();

		waveStats = waveStatsMap.get(id);
		if (waveStats == null) {
			try {
				waveStats = new WaveStats(id, 0);
				pm.makePersistent(waveStats);
			}
			catch (Exception ex) {
				log.log(Level.INFO, "Fuck couldn't persist");
			}
			makeBlipsMap();
		}

		for(String name: wavelet.getParticipants()){
			log.log(Level.INFO, "Adding " + name + " to the wave.Users");
			if (waveStats.getUser(name) == null) {
				UserStats user = new UserStats(name);
				waveStats.addUser(user);
			}
		}

		log.log(Level.INFO, "AUTO-BOT: Attempting to greet the wave.");
		
		Element optimusTransform = new Image("http://imgur.com/m66zH.gif", 160, 120, "");
		Blip greeting = WaveUtils.reply(wavelet, WELCOME_SELF);
		greeting.append(optimusTransform);
		
		waveStats.setBlips( wavelet.getRootBlip().getChildBlipIds().size());
			
		log.log(Level.INFO, "AUTO-BOT: Wave had " + waveStats.getBlips() + " blips when I entered.");
		
		log.log(Level.INFO, "AUTO-BOT: Successfully greeted the wave.");
		
		closePM();
	}
	
	public void onBlipSubmitted(BlipSubmittedEvent event){
		int numBlips = 0;
		Wavelet wavelet = event.getWavelet();
		String id = wavelet.serialize().getWaveId();
		WaveStats waveStats= null;
		
		log.log(Level.INFO, "Testing a new count " + wavelet.getBlips().size());

		openPM();

		waveStats = waveStatsMap.get(id);
		if (waveStats == null) {
			try {
				waveStats = new WaveStats(id, 0);
				pm.makePersistent(waveStats);
			}
			catch (Exception ex) {
				log.log(Level.INFO, "Fuck couldn't persist");
			}
			makeBlipsMap();
		}
		numBlips = waveStats.getBlips() + 1;
		waveStats.setBlips(numBlips);
		
		//Statistics
		String BLIP_AUTHOR = event.getBlip().getCreator();
		log.log(Level.INFO,"Attempting to increment blip for "+ event.getBlip().getCreator());
		if (waveStats.getUser(BLIP_AUTHOR) == null) {
			UserStats user = new UserStats(BLIP_AUTHOR);
			waveStats.addUser(user);
		}
		
		waveStats.getUser(BLIP_AUTHOR).incrementBlipCount();
		
		
		//log.log(Level.INFO, "Auto-Bot unique id: " + uniqueID);
		//log.log(Level.INFO, "Wave " + wavelet.getWaveId() + " (" + wavelet.getTitle() + ") has " + numBlips + " blips.");
		log.log(Level.INFO, "There are " + waveStats.getBlips() + " blips in this wave!");
		
		if (numBlips % 50 == 0) {
			log.log(Level.INFO, "AUTO-BOT: Wave '" + wavelet.getTitle() + "' has reached " + numBlips + " blips.");
		}
		
		processBlip(event.getBlip(), wavelet, waveStats);
		
		if (numBlips == MAX_BLIPS + NUM_OF_VOTES) {
			log.log(Level.INFO, "AUTO-BOT: Blip count is " + numBlips + ", spawning a new wave.");
			createNewWave(wavelet);			
		} 
		else if (numBlips == MAX_BLIPS + NUM_OF_VOTES - 5) {
			Blip blip = WaveUtils.reply(wavelet, "");
			WaveUtils.appendToBlip(blip, "=============================\n");
			WaveUtils.appendToBlip(blip, "Rolling out in 5 blips.\n");
			WaveUtils.appendToBlip(blip, "===========================\n");
		}
		
		closePM();
	}
	
	private void processBlip(Blip blip, Wavelet wavelet, WaveStats waveStats) {
		String id = wavelet.serialize().getWaveId();
		
		HashMap<String, Object> dataMap = new HashMap<String, Object>();
		
		dataMap.put("commandText", blip.getContent());
		dataMap.put("privelegedWavers", privelegedWavers);
		dataMap.put("numberOfActiveWavers", getNumberOfActiveWavers());
		dataMap.put("numberOfBlips", getNumberOfBlipsInWave(id));
		dataMap.put("waveletID", wavelet.serialize().getWaveletId());
		dataMap.put("waveID", wavelet.serialize().getWaveId());
		dataMap.put("autobotID",uniqueID);
		dataMap.put("WaveStats", waveStats);
		dataMap.put("robot", this);
		if (blip.getContent().contains(VoteToBanBlipProcessor.VOTE_TO_BAN)) {
			dataMap.put("banType", "ban");
		} else if (blip.getContent().contains(VoteToBanBlipProcessor.VOTE_TO_UNBAN)) {
			dataMap.put("banType", "unban");
		}
		
		blipProcessor.processBlip(blip, wavelet, dataMap);
		
		//consolidateBlips(blip);
	}
	
	private int getNumberOfActiveWavers() {
		return activeWavers.size();
	}
	
	private int getNumberOfBlipsInWave(String id) {
		WaveStats waveStats;
		
		waveStats = waveStatsMap.get(id);
		if (waveStats == null) {
			try {
				waveStats = new WaveStats(id, 0);
				pm.makePersistent(waveStats);
			}
			catch (Exception ex) {
				log.log(Level.INFO, "Fuck couldn't persist");
			}
			makeBlipsMap();
		}
		
		return waveStats.getBlips();
	}
	
	public void createNewWave(Wavelet wavelet) {
		WaveUtils.reply(wavelet, NEW_WAVE_INDICATOR);
		Wavelet newWavelet = this.newWave(wavelet.getDomain(), wavelet.getParticipants());
		newWavelet.setTitle(WaveUtils.getNewTitle(wavelet));
		newWavelet.submitWith(wavelet);
	}
}
