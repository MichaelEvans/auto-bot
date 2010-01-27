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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import blipProcessors.BlipProcessor;
import blipProcessors.MasterBlipProcessor;
import blipProcessors.VoteToBanBlipProcessor;


import com.google.wave.api.*;

public class Auto_BotServlet extends AbstractRobotServlet {
	public static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName());
	private final long uniqueID = System.nanoTime();
	
	private static Map<String, Set<String>> wavesMap = new HashMap<String, Set<String>>();
	private Map<String, HashSet<String>> waversBlipsMap = new HashMap<String, HashSet<String>>();
	
	private final BlipProcessor blipProcessor = new MasterBlipProcessor();
	
	private ArrayList<String> activeWavers = new ArrayList<String>();
	private Set<String> privelegedWavers = new HashSet<String> () {{
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
	private Set<String> blipSet = new HashSet<String>();
	
	public static final int MAX_BLIPS = 150;
	private int NUM_OF_VOTES = 0;

	final String WELCOME_SELF = "Autobots roll out.";
	
	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();
		String id = wavelet.getWaveId() + wavelet.getWaveletId();
		String blipCreator;
		
		if (!wavesMap.containsKey(id)) {
			wavesMap.put(id, new HashSet<String>());
		}
		
		/* Say hello */
		if (bundle.wasSelfAdded()) {
			log.log(Level.INFO, "Attempting to greet the wave.");
			
			Image optimusTransform = new Image("http://imgur.com/m66zH.gif", 160, 120, "");
			Blip blip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.append(WELCOME_SELF);
			blip.getDocument().appendElement(optimusTransform);
			
			for (String s : wavelet.getRootBlip().getChildBlipIds()) {
				wavesMap.get(id).add(s);
			}
			log.log(Level.INFO, "Wave had " + blipSet.size() + " blips when I entered.");
			
			log.log(Level.INFO, "Successfully greeted the wave.");
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
				
				blipCreator = e.getBlip().getCreator();
				
				wavesMap.get(id).add(e.getBlip().getBlipId());
				if (!waversBlipsMap.containsKey(blipCreator)) {
					waversBlipsMap.put(blipCreator, new HashSet<String>());
				} else {
					waversBlipsMap.get(blipCreator).add(e.getBlip().getBlipId());
				}
				
				numBlips = getNumberOfBlipsByWavers();
				
				if (numBlips % 50 == 0) {
					log.log(Level.INFO, "Wave '" + wavelet.getTitle() + "' has reached " + numBlips + " blips.");
				}
				
				processBlip(e.getBlip(), wavelet);
				
				if (numBlips == MAX_BLIPS + NUM_OF_VOTES) {
					log.log(Level.INFO, "Blip count is " + numBlips + ", spawning a new wave.");
					
					wavelet.createWavelet(wavelet.getParticipants(), "ID").setTitle(WaveUtils.getNewTitle(wavelet));
				} else if (numBlips == MAX_BLIPS + NUM_OF_VOTES - 5) {
					Blip blip = wavelet.appendBlip();
					
					blip.getDocument().append("=============================\n");
					blip.getDocument().append("Rolling out in 5 blips.\n");
					blip.getDocument().append("===========================\n");
				}
			}
			if (e.getType() == EventType.BLIP_DELETED) {
				wavesMap.get(id).remove(e.getBlip().getBlipId());
			}
		}
	}

	private void processBlip(Blip blip, Wavelet wavelet) {
		String blipAuthor = blip.getCreator();
		String id = wavelet.getWaveId() + wavelet.getWaveletId();
		
		HashMap<String, Object> dataMap = new HashMap<String, Object>();
		
		if(!activeWavers.contains(blipAuthor) && !blipAuthor.contains("@appspot.com")) {
			activeWavers.add(blipAuthor);
		}
		
		dataMap.put("commandText", blip.getDocument().getText());
		dataMap.put("privelegedWavers", privelegedWavers);
		dataMap.put("numberOfActiveWavers", getNumberOfActiveWavers());
		dataMap.put("waversBlipsMap", waversBlipsMap);
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
	
	private int getNumberOfBlipsInWavelet(String id) {
		return wavesMap.get(id).size();
	}
	
	private int getNumberOfBlipsByWavers() {
		int sum = 0;
		
		for (Set<String> set : waversBlipsMap.values()) {
			sum += set.size();
		}
		
		return sum;
	}
}
