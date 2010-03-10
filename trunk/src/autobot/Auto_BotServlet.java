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
import javax.jdo.Transaction;

import stats.UserStats;
import stats.WaveStats;
import waveutils.*;

import com.google.wave.api.*;
import com.google.wave.api.event.*;

public class Auto_BotServlet extends AbstractRobot {

	private static final long serialVersionUID = -8761201906629081656L;
	public static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName());
	private final long uniqueID = System.nanoTime();

	public static final int MAX_BLIPS = 200;
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
	public final static String NEW_WAVE_INDICATOR = "We're rolling out!";

	protected String getRobotName() {
		return "Auto-Bot";
	}

	public String getRobotAvatarUrl() {
		return "http://auto-bot.appspot.com/_wave/autobot.png";
	}

	public String getRobotProfilePageUrl() {
		return "auto-bot.appspot.com";
	}
	
	public Auto_BotServlet() {
		waveStatsMap = new HashMap<String, WaveStats>();
		openPM();
		log.log(Level.INFO, "Auto-bot is starting.");
	}
	
	
	public void destroy() {
		log.log(Level.INFO, "I wonder when this is called");
		closePM();
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

//		openPM();

		Transaction tx = pm.currentTransaction();
		tx.begin();
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
		Blip greeting = Utils.reply(wavelet, "\n" + WELCOME_SELF);
		greeting.append(optimusTransform);
		
		waveStats.setBlips( wavelet.getRootBlip().getChildBlipIds().size());
			
		log.log(Level.INFO, "AUTO-BOT: Wave had " + waveStats.getBlips() + " blips when I entered.");
		
		log.log(Level.INFO, "AUTO-BOT: Successfully greeted the wave.");
		
		tx.commit();
		//closePM();
	}
	
	@Override
	public void onDocumentChanged(DocumentChangedEvent event) {
		log.log(Level.INFO, "Should not be happening: " + event.getType());
	}
	
	@Override
	public void onWaveletBlipRemoved(WaveletBlipRemovedEvent event) {
		int numBlips = 0;
		Wavelet wavelet = event.getWavelet();
		String id = wavelet.serialize().getWaveId();
		WaveStats waveStats = null;
	
		openPM();
		//Transaction tx = pm.currentTransaction();
		//tx.begin();
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
		numBlips = waveStats.getBlips() - 1;
		waveStats.setBlips(numBlips);
		
		//Statistics
		String BLIP_AUTHOR = event.getBlip().getCreator();
		log.log(Level.INFO, "Attempting to decrement blip for "+ event.getBlip().getCreator());
		if (waveStats.getUser(BLIP_AUTHOR) == null) {
			UserStats user = new UserStats(BLIP_AUTHOR);
			waveStats.addUser(user);
		}
		waveStats.getUser(BLIP_AUTHOR).incrementDeleteCount();
		
		//tx.commit();
		closePM();
	}
	
	public void onBlipSubmitted(BlipSubmittedEvent event){
		int numBlips = 0;
		Wavelet wavelet = event.getWavelet();
		String id = wavelet.serialize().getWaveId();
		WaveStats waveStats= null;
		

		openPM();

		/*Transaction tx = pm.currentTransaction();
		if (!tx.isActive()) {
			tx.setOptimistic(true);
			tx.begin();
		}*/
		
		waveStats = waveStatsMap.get(id);
		if (waveStats == null) {
			try {
				waveStats = new WaveStats(id, 0);
				pm.makePersistent(waveStats);
			//	tx.commit();
			}
			catch (Exception ex) {
				log.log(Level.INFO, "Fuck couldn't persist : " + ex);
			}
			makeBlipsMap();
		}
		numBlips = waveStats.getBlips() + 1;
		waveStats.setBlips(numBlips);
		//tx.commit();
		//Statistics
		String BLIP_AUTHOR = event.getBlip().getCreator();
		log.log(Level.INFO,"Attempting to increment blip for "+ event.getBlip().getCreator());
		if (waveStats.getUser(BLIP_AUTHOR) == null) {
			UserStats user = new UserStats(BLIP_AUTHOR);
			waveStats.addUser(user);
		}
		waveStats.getUser(BLIP_AUTHOR).incrementBlipCount();
		//tx.commit();
		waveStats.fillWordBags(event.getBlip().getContent());
		//tx.commit();
		
		log.log(Level.INFO, "There are " + waveStats.getBlips() + " blips in this wave!");
		
		processBlip(event.getBlip(), wavelet, waveStats);
		
		
		if (numBlips == MAX_BLIPS + NUM_OF_VOTES) { /* Blip has reached its max, spawn a new one */
			log.log(Level.INFO, "AUTO-BOT: Blip count is " + numBlips + ", spawning a new wave.");
			Utils.reply(wavelet, NEW_WAVE_INDICATOR);
			Utils.createWave(this, wavelet, Tools.newTitle(waveStats), wavelet.getDomain(), wavelet.getParticipants());			
		} 
		else if (numBlips == MAX_BLIPS + NUM_OF_VOTES - 5) { /* Warning blip */
			String reply = "\n\n=============================";
			reply += "\nRolling out in 5 blips.";
			reply += "\n===========================";
			Utils.reply(wavelet, reply);
		}
		
		/* Stop bumping shit */
		if (numBlips > MAX_BLIPS + NUM_OF_VOTES) {
			wavelet.delete(event.getBlip());
		}
		
		/*
		
		XXX: This was getting annoying.  We'll wait for Rob to add some
		kind of round robin trolling mechanism.

		if (BLIP_AUTHOR.equals("twyphoon@googlewave.com")) {
			event.getBlip().append("\n\n");
			String likeStr = " Tim KO'd this blip in the first round!";
			Element like = new Image("https://wiki.endoftheinter.net/images/4/44/Like.png", 15, 15, "");
			event.getBlip().append(like);
			Utils.appendToBlip(event.getBlip(), likeStr);
		}
		
		if (BLIP_AUTHOR.equals("bmwracer0@googlewave.com")) {
			event.getBlip().append("\n\n");
			String likeStr = " Mike just caused an argument with this blip!";
			Element like = new Image("https://wiki.endoftheinter.net/images/4/44/Like.png", 15, 15, "");
			event.getBlip().append(like);
			Utils.appendToBlip(event.getBlip(), likeStr);
		}
		*/
		
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
	
	//TODO Add this to waveutils package
	public void createNewWave(Wavelet wavelet) {
		Utils.reply(wavelet, NEW_WAVE_INDICATOR + "\n\n!{fuck_this_thread_im_outta_here}!");
		Wavelet newWavelet = this.newWave(wavelet.getDomain(), wavelet.getParticipants());
		newWavelet.setTitle(Tools.newTitle(wavelet));
		newWavelet.submitWith(wavelet);
	}
}
