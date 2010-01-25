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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;
import java.util.Random;


import blipProcessors.AbstractBlipProcessor;
import blipProcessors.ForceNewWaveBlipProcessor;
import blipProcessors.VoteNewWaveBlipProcessor;
import blipProcessors.WaveStatsBlipProcessor;
import blipProcessors.WeatherRequestBlipProcessor;

import com.google.wave.api.*;

public class Auto_BotServlet extends AbstractRobotServlet {
	public static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName()); 
	
	
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
	}};
	Random generator = new Random();
	
	public static final int MAX_BLIPS = 150;
	private int BLIP_COUNT = 1;
	private int NUM_OF_VOTES = 0;
	
	/* Command Strings. */
	final String FORCE_NEW_WAVE = "force-new-wave";
	final String VOTE_NEW_WAVE = "roll-out";
	
	final String VOTE_TO_BAN = "vote-to-ban:";
	final String VOTE_TO_UNBAN = "vote-to-unban:";
	final String AUTO_INVITE = "auto-invite";
	final String AUTO_INVITE_ADD = "auto-invite-add:";
	final String AUTO_INVITE_REMOVE = "auto-invite-remove:";
	final String WAVE_STATS = "get-wave-stats";
	
	String LAST_BLIP_CREATOR;

	Map<String, Set<String>> banMap = new HashMap<String, Set<String>>();
	Map<String, Long> cantBan = new HashMap<String, Long>();
	Set<String> areBanned = new HashSet<String>();
		
	
	final Pattern voteToBanPattern = Pattern.compile(CMD_OPEN_IDENT + VOTE_TO_BAN + "(.+)" + CMD_CLOSE_IDENT);
	final Pattern voteToUnbanPattern = Pattern.compile(CMD_OPEN_IDENT + VOTE_TO_UNBAN + "(.+)" + CMD_CLOSE_IDENT);
	final Pattern autoInviteAddPattern = Pattern.compile(CMD_OPEN_IDENT + AUTO_INVITE_ADD + "(.+)" + CMD_CLOSE_IDENT);
	final Pattern autoInviteRemovePattern = Pattern.compile(CMD_OPEN_IDENT + AUTO_INVITE_REMOVE + "(.+)" + CMD_CLOSE_IDENT);
	final Pattern getWaveStatsPattern = Pattern.compile(CMD_OPEN_IDENT + WAVE_STATS + CMD_CLOSE_IDENT);
	
	
	
	final String WELCOME_SELF = "Autobots roll out.";

	final AbstractBlipProcessor waveStatsProcessor = new WaveStatsBlipProcessor();
	final AbstractBlipProcessor forceNewWaveProcessor = new ForceNewWaveBlipProcessor();
	final AbstractBlipProcessor voteNewWaveProcessor = new VoteNewWaveBlipProcessor();
	final AbstractBlipProcessor weatherRequestProcessor = new WeatherRequestBlipProcessor();
	
	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();

		/* Say hello */
		if (bundle.wasSelfAdded()) {
			log.log(Level.INFO, "Attempting to greet the wave.");
			
			Image optimusTransform = new Image("http://imgur.com/m66zH.gif", 160, 120, "");
			Blip blip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.append(WELCOME_SELF);
			blip.getDocument().appendElement(optimusTransform);
			
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
				++BLIP_COUNT;
				
				if (BLIP_COUNT % 50 == 0) {
					log.log(Level.INFO, "Wave '" + wavelet.getTitle() + "' has reached " + BLIP_COUNT + " blips.");
				}
				
				processBlip(e.getBlip(), wavelet);
				
				if (BLIP_COUNT == MAX_BLIPS + NUM_OF_VOTES) {
					log.log(Level.INFO, "Blip count is " + BLIP_COUNT + ", spawning a new wave.");
					
					wavelet.createWavelet(wavelet.getParticipants(), "ID").setTitle(WaveUtils.getNewTitle(wavelet));
				}
			}
			/*if (e.getType() == EventType.BLIP_DELETED) {
			}*/
		}
	}

	private void processBlip(Blip blip, Wavelet wavelet) {
		String text = blip.getDocument().getText();
		String waveAuthor = wavelet.getCreator();
		String blipAuthor = blip.getCreator();
		
		if(!activeWavers.contains(blipAuthor) && !blipAuthor.contains("@appspot.com")) {
			activeWavers.add(blipAuthor);
		}
		
		
		if (text.startsWith(AbstractBlipProcessor.CMD_OPEN_IDENT + FORCE_NEW_WAVE + AbstractBlipProcessor.CMD_CLOSE_IDENT) && privelegedWavers.contains(blipAuthor)) {
			/* Force a new Wave */
			forceNewWaveProcessor.processBlip(blip, wavelet, null);
		} else if(text.startsWith(AbstractBlipProcessor.CMD_OPEN_IDENT + VOTE_NEW_WAVE + AbstractBlipProcessor.CMD_CLOSE_IDENT)) {
			/* Vote for new Wave */
			voteNewWaveProcessor.processBlip(blip, wavelet, new HashMap<String, Object>() {{
				put("numberOfActiveWavers", getNumberOfActiveWavers());
			}});
		}
		/* else if(text.startsWith("!@russian-roulette@!")) {
			int drop = generator.nextInt(wavelet.getParticipants().size());
			//Blip newBlip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.append("\nThanks for transforming " + wavelet.getParticipants().get(drop) + ".");
			wavelet.removeParticipant(wavelet.getParticipants().get(drop));
		}*/
		else if (text.startsWith(AbstractBlipProcessor.CMD_OPEN_IDENT + WEATHER)) {
			/* Request weather */
			weatherRequestProcessor.processBlip(blip, wavelet, null);
		} else if (text.startsWith(CAbstractBlipProcessor.MD_OPEN_IDENT + VOTE_TO_BAN)) {
			/* Vote to ban user */
			
			if (cantBan.containsKey(blipAuthor)) {
				// Allow if user has been baned for longer than 10 minues
				if (cantBan.get(blipAuthor) < System.currentTimeMillis()) {
                    blip.getDocument().append("Message from Auto-Bot: Your ban vote-to-ban privileges have been temporarily revoked.");

                    return;
				} else {
					cantBan.remove(blipAuthor);
				}
			}
			
			Matcher mtchr = voteToBanPattern.matcher(text);
			TextView rootBlipDoc = wavelet.getRootBlip().getDocument();
			String usr;
			
			mtchr.lookingAt();
			
			try{
				usr = mtchr.group(1);
				
				if (usr.substring(0, usr.indexOf("@googlewave.com")).equalsIgnoreCase(blipAuthor)) {
					//cantBan.put(blipAuthor, System.currentTimeMillis() + 10*60*1000);
					
					return;
				}
				
				if (!rootBlipDoc.getText().contains("Motions to Ban")) {
					;
				}
				
				if (wavelet != null) {
					wavelet.getRootBlip().getDocument().append("\n" + blipAuthor + " motions to ban " + usr + ".");
				}
				
				if (!wavelet.getParticipants().contains(usr)) {
					throw new IllegalStateException();
				}
				
				if (banMap.containsKey(usr)) {
					banMap.get(usr).add(blipAuthor);
				} else {
					banMap.put(usr, new HashSet());
					banMap.get(usr).add(blipAuthor);
				}
				
				if (banMap.get(usr).size() >= ((2/3) * activeWavers.size())) {
					String message = "Motion to ban " + usr + " passed with " + banMap.get(usr).size() + " votes. Removing this user.";
					log.info(message);
					wavelet.getRootBlip().getDocument().append("\n" + message);
					wavelet.removeParticipant(usr);
				}
			}catch (IllegalStateException e) {
				wavelet.getRootBlip().getDocument().append("\n" + blipAuthor + " loses their ban vote privileges.");
				
				cantBan.put(blipAuthor, System.currentTimeMillis() + 10*60*1000);
			}catch (IndexOutOfBoundsException e) {
				//not this many matches
				String message = "Incorrect command form. Correct form is !@vote-to-ban:<user>@googlewave.com@!";
				wavelet.getRootBlip().getDocument().append("\n" + message);
			}	
		} else if (text.startsWith(AbstractBlipProcessor.CMD_OPEN_IDENT + VOTE_TO_UNBAN)) {
			/* Vote to unban user */
			
			/* The following if block can be removed once removeParticipant() works */
			if (cantBan.containsKey(blipAuthor)) {
				if (cantBan.get(blipAuthor) + 10*60*1000 < System.currentTimeMillis()) {
					return;
				} else {
					cantBan.remove(blipAuthor);
				}
			}
			
			Matcher mtchr = voteToUnbanPattern.matcher(text);
			TextView rootBlipDoc = wavelet.getRootBlip().getDocument();
			String usr;
			
			mtchr.lookingAt();
	
			try{
				usr = mtchr.group(1);
				
				if (!rootBlipDoc.getText().contains("Motions to Unban")) {
					;
				}
				
				if (wavelet != null) {
					wavelet.getRootBlip().getDocument().append("\n" + blipAuthor + " motions to unban " + usr + ".");
				}
				
				if (!wavelet.getParticipants().contains(usr)) {
					throw new IllegalStateException();
				}
				
				if (banMap.containsKey(usr)) {
					banMap.get(usr).add(blipAuthor);
				} else {
					banMap.put(usr, new HashSet());
					banMap.get(usr).add(blipAuthor);
				}
				
				if (banMap.get(usr).size() >= ((2/3) * activeWavers.size())) {
					String message = "Motion to unban " + usr + " passed with " + banMap.get(usr).size() + " votes. Unbanning this user.";
					log.info(message);
					wavelet.getRootBlip().getDocument().append("\n" + message);
					wavelet.addParticipant(usr);
					banMap.remove(usr);
					areBanned.remove(usr);
				}
			}catch (IllegalStateException e) {
				wavelet.getRootBlip().getDocument().append("\n" + blipAuthor + " loses their unban vote privileges.");
				
				cantBan.put(blipAuthor, System.currentTimeMillis());
			}catch (IndexOutOfBoundsException e) {
				//not this many matches
				String message = "Usage: !@vote-to-unban:user@googlewave.com@!";
				wavelet.getRootBlip().getDocument().append("\n" + message);
			}
		} else if (text.startsWith(AbstractBlipProcessor.CMD_OPEN_IDENT + AUTO_INVITE + AbstractBlipProcessor.CMD_CLOSE_IDENT)) {
			for (String usr : privelegedWavers) {
				wavelet.addParticipant(usr);
			}
		} else if (text.startsWith(AbstractBlipProcessor.CMD_OPEN_IDENT + WAVE_STATS + AbstractBlipProcessor.CMD_CLOSE_IDENT)) {
			waveStatsProcessor.processBlip(blip, wavelet, null);
		}
		
		//consolidateBlips(blip);
	}

	private void consolidateBlips(Blip blip) {
		if (blip.getCreator().equals(LAST_BLIP_CREATOR)) {
			int prevBlipIndex = blip.getParent().getChildren().indexOf(blip) - 1;
			Blip prevBlip = blip.getParent().getChild(prevBlipIndex);
			TextView prevBlipText = prevBlip.getDocument();
			
			log.info("Consilidating blips " + prevBlip.getBlipId() + " and " + blip.getBlipId());
			
			prevBlipText.append("\n");
			prevBlipText.append(Calendar.HOUR + ":" + Calendar.MINUTE + ":" + Calendar.SECOND);
			prevBlipText.append(blip.getDocument().toString());
			
			blip.getDocument().append("Consilidating blips " + prevBlip.getBlipId() + " and " + blip.getBlipId());
		} else {
			LAST_BLIP_CREATOR = blip.getCreator();
		}
		
		return;
	}

	private int getNumberOfActiveWavers() {
		return activeWavers.size();
	}
}
