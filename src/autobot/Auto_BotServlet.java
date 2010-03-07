/* 
  Copyright (c) 2009 Michael Evans, David Forsythe, Nathan Lefler
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:
  1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE AUTHOR(S) ``AS IS'' AND ANY EXPRESS OR
  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE AUTHOR(S) BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package autobot;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import stats.*;
import java.util.Collections;


import blipProcessors.IBlipProcessor;
import blipProcessors.BlipProcessorMediator;
import blipProcessors.VoteToBanBlipProcessor;

import autobot.PMF;


import com.google.wave.api.*;

public class Auto_BotServlet extends AbstractRobotServlet {
	private static final long serialVersionUID = -8761201906629081656L;
	public static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName());
	private final long uniqueID = System.nanoTime();
	
	private static Map<String, Set<String>> wavesMap = new HashMap<String, Set<String>>();
	private static Map<String, Integer> blipsMap = new TreeMap<String, Integer>();
	Map<String, WaveStats> waveStatsMap;
	//private static ArrayList<Wave> waveList= new ArrayList<Wave>();
	public static Wave wave;
	
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
	
	public static final int MAX_BLIPS = 150;
	private int NUM_OF_VOTES = 0;
	private PersistenceManager pm;

	final static String WELCOME_SELF = "Autobots roll out.";
	
	
	public Auto_BotServlet() {
		log.log(Level.INFO, "I've started.");
		waveStatsMap = new TreeMap<String, WaveStats>();
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
			log.log(Level.INFO, "AUTO-BOT: Creating blips map.");
			
			tempList = (List<WaveStats>) pm.newQuery(query).execute();
			for (WaveStats ws : tempList) {
				waveStatsMap.put(ws.getWaveID(), ws);
			}
		}
	}
	
	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();
		String id = wavelet.getWaveId();
		
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
			
		
		/* Say hello */
		if (bundle.wasSelfAdded()) {
			WaveStats waveStats;
			
			wave= new Wave(wavelet.getTitle());
			
			for(String name: wavelet.getParticipants()){
				log.log(Level.INFO,"Adding "+name+" to the wave.Users");
				wave.addUser(new User(name));
			}
			
			log.log(Level.INFO, "AUTO-BOT: Attempting to greet the wave.");
			
			Image optimusTransform = new Image("http://imgur.com/m66zH.gif", 160, 120, "");
			Blip blip = wavelet.appendBlip();
			blip.getDocument().append(WELCOME_SELF);
			blip.getDocument().appendElement(optimusTransform);
			
			
			
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
			
			waveStats.setBlips( wavelet.getRootBlip().getChildBlipIds().size());
				
			log.log(Level.INFO, "AUTO-BOT: Wave had " + wavesMap.get(id) + " blips when I entered.");
			
			log.log(Level.INFO, "AUTO-BOT: Successfully greeted the wave.");
		}

		for (Event e : bundle.getEvents()) {
			/*if (e.getType() == EventType.WAVELET_PARTICIPANTS_CHANGED) {
				for (String usr : e.getRemovedParticipants()) {
					activeWavers.remove(usr);
				}
				for (String usr : e.getAddedParticipants()) {
					if (areBanned.contains(usr)) {
						wavelet.removeParticipant(usr);
					}
				}
			}*/

			if (e.getType() == EventType.BLIP_SUBMITTED) {
				int numBlips;
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
				
				numBlips = waveStats.getBlips() + 1;
				waveStats.setBlips(numBlips);
				
				//Statistics
				log.log(Level.INFO,"Attempting to increment blip for "+ e.getBlip().getCreator());
				wave.getUser(e.getBlip().getCreator()).incrementBlipCount();
				
				
				//log.log(Level.INFO, "Auto-Bot unique id: " + uniqueID);
				//log.log(Level.INFO, "Wave " + wavelet.getWaveId() + " (" + wavelet.getTitle() + ") has " + numBlips + " blips.");
				log.log(Level.INFO, "There are " + numBlips + " blips in this wave!");
				
				if (numBlips % 50 == 0) {
					log.log(Level.INFO, "AUTO-BOT: Wave '" + wavelet.getTitle() + "' has reached " + numBlips + " blips.");
				}
				
				processBlip(e.getBlip(), wavelet);
				
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
			if (e.getType() == EventType.BLIP_DELETED) {
				waveStatsMap.get(id).setBlips(waveStatsMap.get(id).getBlips() - 1);
				wave.getUser(e.getBlip().getCreator()).incrementDeleteCount();
			}
			if (e.getType() == EventType.BLIP_TIMESTAMP_CHANGED ){
				wave.getUser(e.getBlip().getCreator()).incrementEditCount();
			}
				
		}
		pm.close();
	}

	private void processBlip(Blip blip, Wavelet wavelet) {
		String id = wavelet.getWaveId();
		
		HashMap<String, Object> dataMap = new HashMap<String, Object>();
		
		dataMap.put("commandText", blip.getDocument().getText());
		dataMap.put("privelegedWavers", privelegedWavers);
		dataMap.put("numberOfActiveWavers", getNumberOfActiveWavers());
		dataMap.put("numberOfBlips", getNumberOfBlipsInWave(id));
		dataMap.put("waveletID", wavelet.getWaveletId());
		dataMap.put("waveID", wavelet.getWaveId());
		dataMap.put("autobotID",uniqueID);
		if (blip.getDocument().getText().contains(VoteToBanBlipProcessor.VOTE_TO_BAN)) {
			dataMap.put("banType", "ban");
		} else if (blip.getDocument().getText().contains(VoteToBanBlipProcessor.VOTE_TO_UNBAN)) {
			dataMap.put("banType", "unban");
		}
		
		blipProcessor.processBlip(blip, wavelet, dataMap);
		
		//consolidateBlips(blip);
	}

	private int getNumberOfActiveWavers() {
		return activeWavers.size();
	}
	
	private int getNumberOfBlipsInWave(String id) {
		return wavesMap.get(id).size();
	}
	
}
