package autobot;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import blipProcessors.*;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import stats.ImgTag;
import stats.ImgURL;
import stats.UserStats;
import stats.WaveStats;

import com.google.wave.api.*;
import com.google.wave.api.event.*;
import com.trollhouse.wave.utils.*;

public class Auto_BotServlet extends AbstractRobot {

	private static final long serialVersionUID = -8761201906629081656L;
	public static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName());
	private final long uniqueID = System.nanoTime();

	public static final int MAX_BLIPS = 250;
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

	public final static String WELCOME_SELF = "Autobots roll out.";
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
	
	private void makeBlipsMap(String id) {
		List<WaveStats> tempList;
		String query = "select from " + stats.WaveStats.class.getName() + " WHERE waveID == '" + id + "'";
		
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
		//makeBlipsMap();
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
		Blip greeting = Utils.reply(wavelet, "\n" + WELCOME_SELF + "\n");
		greeting.append(optimusTransform);
		
		waveStats.setBlips( wavelet.getRootBlip().getChildBlipIds().size());
			
		log.log(Level.INFO, "AUTO-BOT: Wave had " + waveStats.getBlips() + " blips when I entered.");
		
		log.log(Level.INFO, "AUTO-BOT: Successfully greeted the wave.");
		
		closePM();
	}
	
	
	
	@Override
	public void onWaveletBlipRemoved(WaveletBlipRemovedEvent event) {
		log.log(Level.INFO, "[DEL] Start");
		Wavelet wavelet = event.getWavelet();
		String id = wavelet.serialize().getWaveId();
		WaveStats waveStats = null;
		final String BLIP_ID = event.getRemovedBlipId();
		log.log(Level.INFO, "[DEL] " + BLIP_ID);
		if (BLIP_ID == null) {
			log.log(Level.INFO, "[DEL] Blip was null");
			return;
		}
		final String BLIP_DELETOR = event.getModifiedBy();
		log.log(Level.INFO, "[DEL] Removed by: " + BLIP_DELETOR);
	
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
		
		//Statistics
		boolean didRemove = waveStats.removeBlip(BLIP_ID);
		log.log(Level.INFO, "[DEL] Blip count after: " + waveStats.getBlips());
		
		/* Add deletor if they don't exist */
		if (waveStats.getUser(BLIP_DELETOR) == null) {
			UserStats user = new UserStats(BLIP_DELETOR);
			waveStats.addUser(user);
		}
		
		if (didRemove)
			waveStats.getUser(BLIP_DELETOR).incrementDeleteCount();
		
		pm.makePersistent(waveStats);
		
		closePM();
		log.log(Level.INFO, "[DEL] End");
	}
	
	public void onBlipSubmitted(BlipSubmittedEvent event){
		int numBlips = 0;
		Wavelet wavelet = event.getWavelet();
		String id = wavelet.serialize().getWaveId();
		WaveStats waveStats= null;
		/*Participants parts = wavelet.getParticipants();
		for (String part : parts) {
			log.log(Level.INFO, part + ": " + parts.getParticipantRole(part));
		}*/
		openPM();

		makeBlipsMap(id);
		waveStats = waveStatsMap.get(id);
		if (waveStats == null) {
			try {
				waveStats = new WaveStats(id, 0);
				pm.makePersistent(waveStats);
			}
			catch (Exception ex) {
				log.log(Level.INFO, "Fuck couldn't persist : " + ex);
			}
			makeBlipsMap(id);
		}
		numBlips = waveStats.getBlips() + 1;
		waveStats.setBlips(numBlips);
		
		//Statistics
		String BLIP_AUTHOR = event.getBlip().getCreator();
		String BLIP_EDITOR = event.getModifiedBy();
		log.log(Level.INFO, "[SUB] Blip ID: " + event.getBlip().getBlipId());
		log.log(Level.INFO, "[SUB] Created by : " + BLIP_AUTHOR);
		log.log(Level.INFO, "[SUB] Modified by: " + BLIP_EDITOR);
		boolean newPost = waveStats.addBlip(event.getBlip().getBlipId());
		
		/* Add author if they don't exist */
		if (waveStats.getUser(BLIP_AUTHOR) == null) {
			UserStats user = new UserStats(BLIP_AUTHOR);
			waveStats.addUser(user);
		}
		
		/* Add editor if they don't exist */
		if (waveStats.getUser(BLIP_EDITOR) == null) {
			UserStats user = new UserStats(BLIP_EDITOR);
			waveStats.addUser(user);
		}
		
		/* Update either author's blip count, or editor's edit count */
		if (newPost)
			waveStats.getUser(BLIP_AUTHOR).incrementBlipCount();
		else
			waveStats.getUser(BLIP_EDITOR).incrementEditCount();
		
		/* Link aggregator */
		String regexLinks = "(http://\\S*)";
		Pattern p = Pattern.compile(regexLinks);
		Matcher m = p.matcher(event.getBlip().getContent());
		while (m.find()) {
			log.log(Level.INFO, "[SUB] Found a link: " + m.group(0));
			boolean b = waveStats.addLink(m.group());
			if (b)
				waveStats.getUser(BLIP_AUTHOR).incrementLinkCount();
		}
		
		log.log(Level.INFO, "There are " + waveStats.getBlips() + " blips in this wave!");
		
		processBlip(event.getBlip(), wavelet, waveStats);
		
		
		
		if (numBlips == MAX_BLIPS + NUM_OF_VOTES) { /* Blip has reached its max, spawn a new one */
			waveStats.setEndTime();
			Utils.reply(wavelet, NEW_WAVE_INDICATOR + createWaveSummary(waveStats));
			
			Wavelet newWavelet = Utils.createWaveWithOther(this, wavelet, Tools.newTitle(wavelet), wavelet.getDomain(), wavelet.getParticipants());
			waveStats.setNextWaveID(newWavelet.serialize().getWaveId());
			waveStats.setNextWaveletID(newWavelet.serialize().getWaveletId());
			
		} 
		else if (numBlips == MAX_BLIPS + NUM_OF_VOTES - 5) { /* Warning blip */
			String reply = "\n\n=============================";
			reply += "\nRolling out in 5 blips.";
			reply += "\n===========================";
			Utils.reply(wavelet, reply);
		}
		
		/* Stop bumping shit */
		/*if (numBlips > MAX_BLIPS + NUM_OF_VOTES) {
			queue = QueueFactory.getDefaultQueue();
			queue.add(url("/markov").param("text", event.getBlip().getContent()).param("waveID", id).param("action", "clear").method(Method.POST));
			wavelet.delete(event.getBlip());
		}*/

		pm.makePersistent(waveStats);
		
		
		closePM();
	}
	
	private void processBlip(Blip blip, Wavelet wavelet, WaveStats waveStats) {
		String id = wavelet.serialize().getWaveId();
		
		HashMap<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("privelegedWavers", privelegedWavers);
		dataMap.put("autobotID", uniqueID);
		dataMap.put("WaveStats", waveStats);
		dataMap.put("robot", this);
		
		blipProcessor.processBlip(blip, wavelet, dataMap);
	}
	
	//TODO Add this to waveutils package
	public void createNewWave(Wavelet wavelet) {
		Utils.reply(wavelet, NEW_WAVE_INDICATOR + "\n\n!{fuck_this_thread_im_outta_here}!");
		Wavelet newWavelet = this.newWave(wavelet.getDomain(), wavelet.getParticipants());
		newWavelet.setTitle(Tools.newTitle(wavelet));
		newWavelet.submitWith(wavelet);
	}
	
	public void makeImgTag(String url, String ... tags) {
		String query = "select from " + stats.ImgURL.class.getName() + " WHERE url == '" + url + "'";
		ImgURL imgUrl;
		List<ImgURL> temp = (List<ImgURL>) pm.newQuery(query).execute();
		if (temp.isEmpty()) {
			imgUrl = new ImgURL(url);
			pm.makePersistent(imgUrl);
		}
		else {
			imgUrl = temp.get(0);
		}
		log.log(Level.INFO, "URL Key: " + imgUrl.getKey());
		for (String tag : tags) {
			String query2 = "select from " + stats.ImgTag.class.getName() + " WHERE tag == '" + tag + "'";
			ImgTag imgTag;
			List<ImgTag> temp2 = (List<ImgTag>) pm.newQuery(query2).execute();
			if (temp2.isEmpty()) {
				log.log(Level.INFO, "Creating new tag '" + tag + "'");
				imgTag = new ImgTag(tag, imgUrl.getKey());
				pm.makePersistent(imgTag);
			}
			else {
				log.log(Level.INFO, "Adding URL to tag '" + tag + "'");
				imgTag = temp2.get(0);
				imgTag.addUrl(imgUrl.getKey());
			}
		}
	}
	
	public String getTagList() {
		String query = "select from " + stats.ImgTag.class.getName() + " ORDER BY tag ASC";
		List<ImgTag> tags = (List<ImgTag>) pm.newQuery(query).execute();
		String ret = "";
		for (ImgTag tag : tags)
			ret += tag.getTag() + "|";
		
		return ret;
	}
	
	public String getRandomImg() {
		String query = "select from " + stats.ImgURL.class.getName();
		List<ImgURL> urls = (List<ImgURL>) pm.newQuery(query).execute();
		Random r = new Random(System.currentTimeMillis());
		int x = r.nextInt(urls.size());
		
		return urls.get(x).getURL();
	}
	
	public String getUserStats(String email) {
		String query = "select from " + stats.UserStats.class.getName() + " WHERE Name == '" + email + "'";
		List<UserStats> urls = (List<UserStats>) pm.newQuery(query).execute();
		int posts = 0;
		int edits = 0;
		int deletes = 0;
		for (UserStats us : urls) {
			posts += us.getBlipCount();
			edits += us.getEditCount();
			deletes += us.getDeleteCount();
		}
		
		String stats = "\nStats for: " + email;
		stats += "\nTotal blips: " + posts;
		stats += "\nTotal edits: " + edits + " (since 10/2010)";
		stats += "\nTotal deletes: " + deletes + " (since 10/2010)";
		stats += "\nTotal waves: " + urls.size();
		stats += "\n";
		stats += "\nBlips per wave: " + ((float)((float)posts / (float)urls.size()));
		stats += "\nEdits per wave: " + ((float)((float)edits / (float)urls.size()));
		stats += "\nDeletes per wave: " + ((float)((float)deletes / (float)urls.size()));
		
		return stats;
	}
	
	public void grabImg(String ... tags) {
		
	}
	
	private String createWaveSummary(WaveStats wavestats) {
		String ret = "\n\nAwards:";
		List<UserStats> users = wavestats.getUsers();
		UserStats highAct = null;
		UserStats highPost = null;
		UserStats highEdit = null;
		UserStats highDel = null;
		UserStats lowAct = null;
		UserStats lowPost = null;
		UserStats lowEdit = null;
		UserStats lowDel = null;
		for (UserStats user : users) {
			if (highAct == null || user.getActionsCount() > highAct.getActionsCount())
				highAct = user;
			if (highPost == null || user.getBlipCount() > highPost.getBlipCount())
				highPost = user;
			if (highEdit == null || user.getEditCount() > highEdit.getEditCount())
				highEdit = user;
			if (highDel == null || user.getDeleteCount() > highDel.getDeleteCount())
				highDel = user;
			if (lowAct == null || user.getActionsCount() < lowAct.getActionsCount())
				lowAct = user;
			if (user.getBlipCount() > 0 && (lowPost == null || user.getBlipCount() < highPost.getBlipCount()))
				lowPost = user;
			if (user.getEditCount() > 0 && (lowEdit == null || user.getEditCount() < highEdit.getEditCount()))
				lowEdit = user;
			if (user.getDeleteCount() > 0 && (lowDel == null || user.getDeleteCount() < highDel.getDeleteCount()))
				lowDel = user;
		}
		ret += "\nBlabbermouth: " + highPost.getName() + " (" + highPost.getBlipCount() + " blips)";
		if (lowPost != null)
			ret += "\nMostly Harmless: " + lowPost.getName() + " (" + lowPost.getBlipCount() + " blips)";
		ret += "\nADHD Fingers: " + highEdit.getName() + " (" + highEdit.getEditCount() + " edits)";
		if (lowEdit != null)
			ret += "\nCool, Calm, Collected: " + lowEdit.getName() + " (" + lowEdit.getEditCount() + " edits)";
		ret += "\nUnabomber: " + highDel.getName() + " (" + highDel.getDeleteCount() + " deletes)";
		if (lowDel != null)
			ret += "\nNo Regrets: " + lowDel.getName() + " (" + lowDel.getDeleteCount() + " deletes)";
		ret += "\nAction Jackson: " + highAct.getName() + " (" + highAct.getActionsCount() + " combined actions)";
		ret += "\nMost Cowardly: " + lowAct.getName() + " (" + lowAct.getActionsCount() + " combined actions)";
		ret += "\n\nLink summary (" + wavestats.getLinkCount() + " links)";
		ret += "\nWave lasted for: " + (wavestats.totalLength() / 1000) + " seconds";
//		ret += "\n" + wavestats.getLinks();
		
		return ret;
	}
}
