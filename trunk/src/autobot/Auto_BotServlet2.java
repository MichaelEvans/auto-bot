package autobot;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import stats.UserStats;
import stats.WaveStats;

import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Blip;
import com.google.wave.api.Image;
import com.google.wave.api.Wavelet;
import com.google.wave.api.event.BlipSubmittedEvent;
import com.google.wave.api.event.WaveletSelfAddedEvent;

public class Auto_BotServlet2 extends AbstractRobot {

	private static final long serialVersionUID = -8761201906629081656L;
	public static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName());
	private final long uniqueID = System.nanoTime();

	public static final int MAX_BLIPS = 150;
	private int NUM_OF_VOTES = 0;
	private PersistenceManager pm;
	Map<String, WaveStats> waveStatsMap;

	final static String WELCOME_SELF = "Autobots roll out.";

	@Override
	protected String getRobotName() {
		return "Auto-Bot";
	}

	@Override
	public String getRobotAvatarUrl() {
		return "http://i.imgur.com/CQoTo.png";
	}

	@Override
	public String getRobotProfilePageUrl() {
		return "auto-bot.appspot.com";
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
	
	
	public void onWaveletSelfAdded(WaveletSelfAddedEvent event) {
		Wavelet wavelet = event.getWavelet();
		String id = wavelet.serialize().getWaveId();
		WaveStats waveStats= null;

		try {
			pm = PMF.get().getPersistenceManager();
		}
		catch (NoClassDefFoundError e) {
			log.log(Level.INFO, "Help: " + e);
		}
		catch (Exception e) {
			log.log(Level.INFO, "Fuck me: " + e);
			e.printStackTrace();
		}
		makeBlipsMap();

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
			log.log(Level.INFO,"Adding "+name+" to the wave.Users");
			if (waveStats.getUser(name) == null) {
				UserStats user = new UserStats(name);
				pm.makePersistent(user);
				waveStats.addUser(user);
			}
		}

		log.log(Level.INFO, "AUTO-BOT: Attempting to greet the wave.");
		
		Image optimusTransform = new Image("http://imgur.com/m66zH.gif", 160, 120, "");
		wavelet.reply(WELCOME_SELF + optimusTransform);
		
		waveStats.setBlips( wavelet.getRootBlip().getChildBlipIds().size());
			
		log.log(Level.INFO, "AUTO-BOT: Wave had " + waveStats.getBlips() + " blips when I entered.");
		
		log.log(Level.INFO, "AUTO-BOT: Successfully greeted the wave.");		
	}
	
	public void onBlipSubmitted(BlipSubmittedEvent event){
		int numBlips = 0;
		Wavelet wavelet = event.getWavelet();
		String id = wavelet.serialize().getWaveId();
		WaveStats waveStats= null;

		try {
			pm = PMF.get().getPersistenceManager();
		}
		catch (NoClassDefFoundError e) {
			log.log(Level.INFO, "Help: " + e);
		}
		catch (Exception e) {
			log.log(Level.INFO, "Fuck me: " + e);
			e.printStackTrace();
		}
		makeBlipsMap();

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
			pm.makePersistent(user);
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
			
			wavelet.createWavelet(wavelet.getParticipants(), "ID").setTitle(WaveUtils.getNewTitle(wavelet));
		} else if (numBlips == MAX_BLIPS + NUM_OF_VOTES - 5) {
			Blip blip = wavelet.appendBlip();
			
			blip.getDocument().append("=============================\n");
			blip.getDocument().append("Rolling out in 5 blips.\n");
			blip.getDocument().append("===========================\n");
		}
	}
	
	private void processBlip(Blip blip, Wavelet wavelet, WaveStats waveStats) {
		String id = wavelet.serialize().getWaveId();
		
		HashMap<String, Object> dataMap = new HashMap<String, Object>();
		
		dataMap.put("commandText", blip.getDocument().getText());
		dataMap.put("privelegedWavers", privelegedWavers);
		dataMap.put("numberOfActiveWavers", getNumberOfActiveWavers());
		dataMap.put("numberOfBlips", getNumberOfBlipsInWave(id));
		dataMap.put("waveletID", wavelet.getWaveletId());
		dataMap.put("waveID", wavelet.getWaveId());
		dataMap.put("autobotID",uniqueID);
		dataMap.put("WaveStats", waveStats);
		if (blip.getDocument().getText().contains(VoteToBanBlipProcessor.VOTE_TO_BAN)) {
			dataMap.put("banType", "ban");
		} else if (blip.getDocument().getText().contains(VoteToBanBlipProcessor.VOTE_TO_UNBAN)) {
			dataMap.put("banType", "unban");
		}
		
		blipProcessor.processBlip(blip, wavelet, dataMap);
		
		//consolidateBlips(blip);
	}
}
