/* 
  Copyright (c) 2009 Michael Evans, David Forsythe
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;
import java.util.Random;

import sun.util.calendar.BaseCalendar.Date;

import com.google.wave.api.*;

public class Auto_BotServlet extends AbstractRobotServlet {
	static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName()); 
	
	public HashMap<String, Integer> votes = new HashMap<String, Integer>();
	public ArrayList<String> activeWavers = new ArrayList<String>();
	public Set<String> privelegedWavers = new HashSet<String> () {{
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
	
	private int MAX_BLIPS = 150;
	private int BLIP_COUNT = 1;
	private int NUM_OF_VOTES = 0;
	
	/* Command Strings. */
	final String CMD_OPEN_IDENT = "!@";
	final String CMD_CLOSE_IDENT = "@!";
	final String FORCE_NEW_WAVE = "force-new-wave";
	final String VOTE_NEW_WAVE = "roll-out";
	final String WEATHER = "weather";
	final String VOTE_TO_BAN = "vote-to-ban:";
	final String VOTE_TO_UNBAN = "vote-to-unban:";
	final String AUTO_INVITE = "auto-invite";
	final String AUTO_INVITE_ADD = "auto-invite-add:";
	final String AUTO_INVITE_REMOVE = "auto-invite-remove:";
	
	final String CONT_IDENT = "// Part ";
	String WAVE_BASE_TITLE;
	String LAST_BLIP_CREATOR;

	final String NEW_WAVE_INDICATOR = "We're rolling out!";

	Map<String, Set<String>> banMap = new HashMap<String, Set<String>>();
	Map<String, Long> cantBan = new HashMap<String, Long>();
	Set<String> areBanned = new HashSet<String>();
		
	final Pattern weatherPattern = Pattern.compile(CMD_OPEN_IDENT + WEATHER + ":(\\d{5})" + CMD_CLOSE_IDENT);
	final Pattern voteToBanPattern = Pattern.compile(CMD_OPEN_IDENT + VOTE_TO_BAN + "(.+)" + CMD_CLOSE_IDENT);
	final Pattern voteToUnbanPattern = Pattern.compile(CMD_OPEN_IDENT + VOTE_TO_UNBAN + "(.+)" + CMD_CLOSE_IDENT);
	final Pattern autoInviteAdd = Pattern.compile(CMD_OPEN_IDENT + AUTO_INVITE_ADD + "(.+)" + CMD_CLOSE_IDENT);
	final Pattern autoInviteRemove = Pattern.compile(CMD_OPEN_IDENT + AUTO_INVITE_REMOVE + "(.+)" + CMD_CLOSE_IDENT);
	
	
	final String NW_VOTE_QUOTE = "Before your president decides, please ask him this: What if we leave, and you're wrong?";
	final String WELCOME_SELF = "Autobots roll out.";

	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();

		/* Say hello */
		if (bundle.wasSelfAdded()) {
			log.log(Level.INFO, "Attempting to greet the wave.");
			WAVE_BASE_TITLE = wavelet.getTitle();
			
			Image optimusTransform = new Image("http://imgur.com/m66zH.gif", 160, 120, "");
			Blip blip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.append(WELCOME_SELF);
			blip.getDocument().appendElement(optimusTransform);
			
			log.log(Level.INFO, "Successfully greeted the wave.");
		}

		for (Event e : bundle.getEvents()) {
			if (e.getType() == EventType.WAVELET_PARTICIPANTS_CHANGED) {
				for (String usr : e.getRemovedParticipants()) {
					activeWavers.remove(usr);
				}
				for (String usr : e.getAddedParticipants()) {
					if (areBanned.contains(usr)) {
						wavelet.removeParticipant(usr);
					}
				}
			}

			if (e.getType() == EventType.BLIP_SUBMITTED) {
				++BLIP_COUNT;
				
				processBlip(e.getBlip(), wavelet);
				
				if (BLIP_COUNT == MAX_BLIPS + NUM_OF_VOTES) {
					log.log(Level.INFO, "Blip count is " + BLIP_COUNT + ", spawning a new wave.");
					
					wavelet.createWavelet(wavelet.getParticipants(), "ID").setTitle(getNewTitle(wavelet));
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
		
		if(!activeWavers.contains(blipAuthor)) {
			activeWavers.add(blipAuthor);
		}
		
		log.info("Wave Creator: "+ waveAuthor + "Blip from: " + blipAuthor+"\n");
		
		
		if (text.startsWith(CMD_OPEN_IDENT + FORCE_NEW_WAVE + CMD_CLOSE_IDENT) && privelegedWavers.contains(blipAuthor)) {
			/* Force a new Wave */
			
			makeNewWave(wavelet);
			
			log.info(blipAuthor + " forced a new wave.");
		} else if(text.startsWith(CMD_OPEN_IDENT + VOTE_NEW_WAVE + CMD_CLOSE_IDENT)) {
			/* Vote for new Wave */
			
			String voteCreator = blip.getCreator();
			blip.getDocument().append("\n" + NW_VOTE_QUOTE);
			votes.put(voteCreator, 1);

			NUM_OF_VOTES = votes.size();
			String rootText = wavelet.getRootBlip().getDocument().getText();
			
			int index = rootText.indexOf("Wave Max: ");
			if (index < 0) {
				String appendText = "\n\n" + "Wave Max: " + (NUM_OF_VOTES + MAX_BLIPS) + "\nNumber of votes for new wave: " + NUM_OF_VOTES;
				wavelet.getRootBlip().getDocument().append(appendText);
			} else {
				String newText = rootText.substring(0, index);
				wavelet.getRootBlip().getDocument().delete();
				wavelet.getRootBlip().getDocument().append(newText + "Wave Max: " + (NUM_OF_VOTES + MAX_BLIPS) + "\nNumber of votes for new wave: " + NUM_OF_VOTES);
			}
			
			if (NUM_OF_VOTES > ((1/3) * activeWavers.size())) {
				makeNewWave(wavelet);
			}
			
		}
		/* else if(text.startsWith("!@russian-roulette@!")) {
			int drop = generator.nextInt(wavelet.getParticipants().size());
			//Blip newBlip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.append("\nThanks for transforming " + wavelet.getParticipants().get(drop) + ".");
			wavelet.removeParticipant(wavelet.getParticipants().get(drop));
		}*/
		else if (text.startsWith(CMD_OPEN_IDENT + WEATHER)) {
			/* Request weather */
			
			Matcher mtchr = weatherPattern.matcher(text);
			mtchr.lookingAt();
			try {
				String current = "";
				String image = "";
				try {
					current = XMLParser.getLocation(Integer.parseInt(mtchr.group(1)));
					current += "\nNow - " + XMLParser.getTemp(Integer.parseInt(mtchr.group(1)));
					current += "\n" + XMLParser.getForecast(Integer.parseInt(mtchr.group(1)));
					image = XMLParser.getImage(Integer.parseInt(mtchr.group(1)));
				} catch (NumberFormatException e) {
					blip.getDocument().append("Encountered an error when requesting weather");
					
					log.log(Level.WARNING, "Caught NumberFormatException when requesting weather, message was: " + e.getLocalizedMessage());
					e.printStackTrace();
				} catch (IOException e) {
					blip.getDocument().append("Encountered an error when requesting weather");
					
					log.log(Level.WARNING, "Caught IOException when requesting weather, message was: " + e.getLocalizedMessage());
					e.printStackTrace();
				}
				
				blip.getDocument().replace(current);
				blip.getDocument().appendElement(new Image(image, 52,52,""));
			} catch (IllegalStateException e) {
				log.log(Level.WARNING, "Caught IllegalStateException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				blip.getDocument().append("Incorrect command form. Correct form is !@weather:<zip code, 5 digits>@!");
				
				log.log(Level.WARNING, "Caught IndexOutOfBoundsException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
		} else if (text.startsWith(CMD_OPEN_IDENT + VOTE_TO_BAN)) {
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
		} else if (text.startsWith(CMD_OPEN_IDENT + VOTE_TO_UNBAN)) {
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
		} else if (text.startsWith(CMD_OPEN_IDENT + AUTO_INVITE + CMD_CLOSE_IDENT)) {
			for (String usr : privelegedWavers) {
				wavelet.addParticipant(usr);
			}
		}
		
		consolidateBlips(blip);
	}

	private void makeNewWave(Wavelet wavelet) {
		log.log(Level.INFO, "Creating new wave");
		
		wavelet.appendBlip().getDocument().append(NEW_WAVE_INDICATOR);
		
		String title = getNewTitle(wavelet);
		Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
		newWave.setTitle(title);
		
		log.log(Level.INFO, "Created new wave: " + title);
	}

	private String getNewTitle(Wavelet wavelet) {
		int index;
		String title;
		
		if (WAVE_BASE_TITLE == null)
			WAVE_BASE_TITLE = "";
		index = WAVE_BASE_TITLE.indexOf(CONT_IDENT);
		if (index == -1) {
			title = WAVE_BASE_TITLE + CONT_IDENT + "2";
		} else {
			int count = Integer.parseInt(WAVE_BASE_TITLE.substring(index + CONT_IDENT.length()).trim());
			title = WAVE_BASE_TITLE.substring(0,index) + CONT_IDENT + (count + 1);
		}
		
		return title;
	}

	private void consolidateBlips(Blip blip) {
		return;
		/*if (blip.getCreator().equals(LAST_BLIP_CREATOR)) {
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
		
		return;*/
	}

    /**
     * Takes a list of all wavers subscribed to this wave, removes bots, and returns a list containing only human wavers
     */
    private List<String> getHumanWavers(List<String> wavers) {
        List<String> remList = new ArrayList<String>();

        for (String s : wavers) {
            if (s.contains("@appspot.com")) {
                remList.add(s);
            }
        }

        wavers.removeAll(remList);

        return wavers;
    }
}
