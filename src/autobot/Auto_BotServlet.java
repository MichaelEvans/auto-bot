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
import java.util.logging.Logger;
import java.util.regex.*;
import java.util.Random;

import sun.util.calendar.BaseCalendar.Date;

import com.google.wave.api.*;

public class Auto_BotServlet extends AbstractRobotServlet {
	static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName()); 
	public HashMap<String, Integer> votes = new HashMap<String, Integer>();
	public ArrayList<String> activeWavers = new ArrayList<String>();
	public Set<String> autoInviteWavers = new HashSet<String> {{
		this.add("n.lefler@googlewave.com");
		this.add("bmwracer0@googlewave.com");
		this.add("dforsythe@googlewave.com");
		this.add("themagnum@googlewave.com");
		this.add("twyphoon@googlewave.com");
		this.add("claudio.sayan@googlewave.com");
		this.add("rgalginaitis@googlewave.com");
		this.add("rob.kiefer@googlewave.com");
	}};
	Random generator = new Random();
	
	private int MAX_BLIPS = 150;
	private int NUM_OF_VOTES = 0;
	private int ACTIVE_WAVERS = 0;
	private int	WAVE_NUMBER = 1;
	
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
			WAVE_BASE_TITLE = wavelet.getTitle();
			
			Image optimusTransform = new Image("http://imgur.com/m66zH.gif", 160, 120, "");
			Blip blip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.append(WELCOME_SELF);
			blip.getDocument().appendElement(optimusTransform);
		}

		/* Should Auto-Bot be included in this? */
		int NUM_OF_PARTICIPANTS = wavelet.getParticipants().size();

		for (Event e : bundle.getEvents()) {
			if (e.getType() == EventType.WAVELET_PARTICIPANTS_CHANGED) {
				for (String usr : e.getRemovedParticipants()) {
					activeWavers.remove(usr);
					/* banMap.remove(usr);
					for (Set s : banMap.values()) {
						s.remove(usr);
					} */
				}
				for (String usr : e.getAddedParticipants()) {
					if (areBanned.contains(usr)) {
						wavelet.removeParticipant(usr);
					}
				}
			}

			if (e.getType() == EventType.BLIP_SUBMITTED) {
				//Blip root = wavelet.getRootBlip();
				//List<String> childrenIds = root.getChildBlipIds(); 
				//List<Blip> children = new ArrayList<Blip>(); 
				/*for (String id: childrenIds) { 
					children.add(bundle.getBlip(wavelet.getWaveId(), wavelet.getWaveletId(), id)); 
				}*/
				processBlip(e.getBlip(), wavelet);
				if (wavelet.getRootBlip().getChildBlipIds().size() == MAX_BLIPS + NUM_OF_VOTES) {
					Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
					String title = getNewTitle(wavelet);
					newWave.setTitle(title);
				}
			}
			/*if (e.getType() == EventType.BLIP_DELETED) {
			}*/
		}
	}

	private void processBlip(Blip blip, Wavelet wavelet) {
		String text = blip.getDocument().getText();
		String author = wavelet.getCreator();
		String authorRequest = blip.getCreator();
		
		if(!activeWavers.contains(authorRequest)) {
			ACTIVE_WAVERS++;
			activeWavers.add(authorRequest);
		}
		
		log.info("Wave Creator: "+ author + "Blip from: " + authorRequest+"\n");
		
		
		if (text.startsWith(CMD_OPEN_IDENT + FORCE_NEW_WAVE + CMD_CLOSE_IDENT) && author.equals(authorRequest)) {
			/* Force a new Wave */
			
			makeNewWave(wavelet);
			log.info("Forced a new wave.");

		} else if(text.startsWith(CMD_OPEN_IDENT + VOTE_NEW_WAVE + CMD_CLOSE_IDENT)) {
			/* Vote for new Wave */
			
			String voteCreator = blip.getCreator();
			blip.getDocument().append("\n" + NW_VOTE_QUOTE);
			votes.put(voteCreator, 1);
			int i = 0;
			Set<String> users = votes.keySet();
			/*for(String user:users) {
				if(votes.get(user)==1)
					i++;
			}*/
			NUM_OF_VOTES = votes.size();
			String rootText = wavelet.getRootBlip().getDocument().getText();
			int index = rootText.indexOf("Wave Max: ");
			if (index < 0) {
				String appendText = "\n\n" + "Wave Max: " + (NUM_OF_VOTES + MAX_BLIPS) + "\nNumber of votes for new wave: "+NUM_OF_VOTES;
				//wavelet.getRootBlip().getDocument().delete();
				wavelet.getRootBlip().getDocument().append(appendText);
			} else {
				String newText = rootText.substring(0,index);
				wavelet.getRootBlip().getDocument().delete();
				wavelet.getRootBlip().getDocument().append(newText + "Wave Max: " + (NUM_OF_VOTES + MAX_BLIPS) + "\nNumber of votes for new wave: " + NUM_OF_VOTES);
			}
			if (NUM_OF_VOTES > ((1/3) * ACTIVE_WAVERS) && (ACTIVE_WAVERS >= 4))
				makeNewWave(wavelet);
			
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				blip.getDocument().replace(current);
				blip.getDocument().appendElement(new Image(image, 52,52,""));
			} catch (IllegalStateException e) {
				//blip.getDocument().replace("Degrees");
			} catch (IndexOutOfBoundsException e) {
				//not this many matches
			}
		} else if (text.startsWith(CMD_OPEN_IDENT + VOTE_TO_BAN)) {
			/* Vote to ban user */
			
			if (cantBan.containsKey(authorRequest)) {
				if (cantBan.get(authorRequest) + 10*60*1000 < System.currentTimeMillis()) {
					return;
				} else {
					cantBan.remove(authorRequest);
				}
			}
			
			Matcher mtchr = voteToBanPattern.matcher(text);
			TextView rootBlipDoc = wavelet.getRootBlip().getDocument();
			String usr;
			
			mtchr.lookingAt();
			
			try{
				usr = mtchr.group(1);
				
				if (!rootBlipDoc.getText().contains("Motions to Ban")) {
					;
				}
				
				if (wavelet) {
					wavelet.getRootBlip().getDocument().append("\n" + authorRequest + " motions to ban " + usr + ".");
				}
				
				if (!wavelet.getParticipants().contains(usr)) {
					throw new IllegalStateException();
				}
				
				if (banMap.containsKey(usr)) {
					banMap.get(usr).add(authorRequest);
				} else {
					banMap.put(usr, new HashSet());
					banMap.get(usr).add(authorRequest);
				}
				
				if (banMap.get(usr).size() >= ((2/3) * ACTIVE_WAVERS)) {
					String message = "Motion to ban " + usr + " passed with " banMap.get(usr).size() + " votes. Removing this user.";
					log.info(message);
					wavelet.getRootBlip().getDocument().append("\n" + message);
					wavelet.removeParticipant(usr);
				}
			}catch (IllegalStateException e) {
				wavelet.getRootBlip().getDocument().append("\n" + authorRequest + " loses their ban vote privileges.");
				
				cantBan.put(authorRequest, System.currentTimeMillis());
			}catch (IndexOutOfBoundsException e) {
				//not this many matches
				String message = "Usage: !@vote-to-ban:user@googlewave.com@!";
				wavelet.getRootBlip().getDocument().append("\n" + message);
			}	
		} else if (text.startsWith(CMD_OPEN_IDENT + VOTE_TO_UNBAN)) {
			/* Vote to unban user */
			
			/* The following if block can be removed once removeParticipant() works */
			if (cantBan.containsKey(authorRequest)) {
				if (cantBan.get(authorRequest) + 10*60*1000 < System.currentTimeMillis()) {
					return;
				} else {
					cantBan.remove(authorRequest);
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
				
				if (wavelet) {
					wavelet.getRootBlip().getDocument().append("\n" + authorRequest + " motions to unban " + usr + ".");
				}
				
				if (!wavelet.getParticipants().contains(usr)) {
					throw new IllegalStateException();
				}
				
				if (banMap.containsKey(usr)) {
					banMap.get(usr).add(authorRequest);
				} else {
					banMap.put(usr, new HashSet());
					banMap.get(usr).add(authorRequest);
				}
				
				if (banMap.get(usr).size() >= ((2/3) * ACTIVE_WAVERS)) {
					String message = "Motion to unban " + usr + " passed with " banMap.get(usr).size() + " votes. Unbanning this user.";
					log.info(message);
					wavelet.getRootBlip().getDocument().append("\n" + message);
					wavelet.addParticipant(usr);
					banMap.remove(usr);
					areBanned.remove(usr);
				}
			}catch (IllegalStateException e) {
				wavelet.getRootBlip().getDocument().append("\n" + authorRequest + " loses their unban vote privileges.");
				
				cantBan.put(authorRequest, System.currentTimeMillis());
			}catch (IndexOutOfBoundsException e) {
				//not this many matches
				String message = "Usage: !@vote-to-unban:user@googlewave.com@!";
				wavelet.getRootBlip().getDocument().append("\n" + message);
			}
		} else if (text.startsWith(CMD_OPEN_IDENT + AUTO_INVITE + CMD_CLOSE_IDENT) {
			for (String usr : autoInviteWavers) {
				wavelet.addParticipant(usr);
			}
		}
		
		consolidateBlips(blip);
	}

	private void makeNewWave(Wavelet wavelet) {
		Blip rollOut = new Blip();
		
		rollOut.getDocument().append(NEW_WAVE_INDICATOR);
		wavelet.appendBlip(rollOut);
		
		String title = getNewTitle(wavelet);
		Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
		newWave.setTitle(title);
	}

	private String getNewTitle(Wavelet wavelet) {
		// TODO Auto-generated method stub
		String title;// = wavelet.getTitle();
		/*int indexMax = wavelet.getRootBlip().getDocument().getText().indexOf("Wave Max: ");
		if(indexMax>-1) {
			Title = wavelet.getRootBlip().getDocument().getText().substring(0,indexMax);
		}*/
		int index = WAVE_BASE_TITLE.indexOf(CONT_IDENT);
		if (index == -1) {
			title = WAVE_BASE_TITLE + CONT_IDENT + "2";
		} else {
			int count = Integer.parseInt(WAVE_BASE_TITLE.substring(index + CONT_IDENT.length()).trim());
			title = WAVE_BASE_TITLE.substring(0,index) + CONT_IDENT + (count + 1);
		}
		return title;
	}

	private void consolidateBlips(Blip blip) {
		if (blip.getCreator().equals(LAST_BLIP_CREATOR)) {
			/* Consolidate blips */
			int prevBlipIndex = blip.getParent().getChildren().indexOf(blip) - 1;
			Blip prevBlip = blip.getParent().getChild(prevBlipIndex);
			TextView prevBlipText = prevBlip.getDocument();
			
			log.info("Consilidating blips " + prevBlip.getBlipId() + " and " + blip.getBlipId());
			
			prevBlipText.append("\n");
			prevBlipText.append(Calendar.HOUR + ":" + Calendar.MINUTE + ":" + Calendar.SECOND);
			prevBlipText.append(blip.getDocument().toString());
			
			blip.getDocument().append("Consilidating blips " + prevBlip.getBlipId() + " and " + blip.getBlipId());
			//blip.getParent().deleteInlineBlip(blip);
		} else {
			LAST_BLIP_CREATOR = blip.getCreator();
		}
		
		return;
	}

}