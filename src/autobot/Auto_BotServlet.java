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
import java.util.HashMap;
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
	Random generator = new Random();
	
	private int MAX_BLIPS = 150;
	private int NUM_OF_VOTES = 0;
	private int ACTIVE_WAVERS = 0;
	
	/* Command Strings. */
	final String CMD_OPEN_IDENT = "!@";
	final String CMD_CLOSE_IDENT = "@!";
	final String FORCE_NEW_WAVE = "force-new-wave";
	final String VOTE_NEW_WAVE = "roll-out"
	final String WEATHER = "weather"

	Map<String, Set<String>> bMap<K, V>ap = new HashMap<String, Set<String>>();
	Map<String, long> cantBan = new HashMap<String, long>();
		
	final Pattern weatherPattern = Pattern.compile("CMD_OPEN_IDENT" + WEATHER + ":(\\d{5})" + CMD_CLOSE_IDENT);
	final String NW_VOTE_QUOTE = "Before your president decides, please ask him this: What if we leave, and you're wrong?"
	final String WELCOME_SELF = "Autobots roll out.";

	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();

		if (bundle.wasSelfAdded()) {
			Image optimusTransform = new Image("http://imgur.com/m66zH.gif", 160, 120, "");
			Blip blip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.append(WELCOME_SELF);
			blip.getDocument().appendElement(optimusTransform);
		}

		int NUM_OF_PARTICIPANTS = wavelet.getParticipants().size();

		for (Event e: bundle.getEvents()) {
			if (e.getType() == EventType.WAVELET_PARTICIPANTS_CHANGED) {    
			}

			if (e.getType() == EventType.BLIP_SUBMITTED){
				Blip root = wavelet.getRootBlip();
				List<String> childrenIds = root.getChildBlipIds(); 
				//List<Blip> children = new ArrayList<Blip>(); 
				/*for (String id: childrenIds) { 
					children.add(bundle.getBlip(wavelet.getWaveId(), wavelet.getWaveletId(), id)); 
				}*/
				processBlip(e.getBlip(), wavelet);
				if(childrenIds.size()==MAX_BLIPS+NUM_OF_VOTES){
					Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
					String title = getNewTitle(wavelet);
					newWave.setTitle(title);
				}

			}
			/*if (e.getType() == EventType.BLIP_DELETED){
			}*/
		}
	}

	private void processBlip(Blip blip, Wavelet wavelet) {
		// TODO Auto-generated method stub
		String text = blip.getDocument().getText();
		String author = wavelet.getCreator();
		String authorRequest = blip.getCreator();
		if(!activeWavers.contains(authorRequest)){
			ACTIVE_WAVERS++;
			activeWavers.add(authorRequest);
		}
		log.info("Wave Creator: "+ author + "Blip from: " + authorRequest+"\n");
		if (text.startsWith(CMD_OPEN_IDENT + FORCE_NEW_WAVE + CMD_CLOSE_IDENT) && author.equals(authorRequest)) {
			log.info("Forced a new wave.");
			Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
			String title = getNewTitle(wavelet);
			newWave.setTitle(title);
		}
		if(text.startsWith(CMD_OPEN_IDENT + VOTE_NEW_WAVE + CMD_CLOSE_IDENT)){
			String voteCreator = blip.getCreator();
			blip.getDocument().append("\n" + NW_VOTE_QUOTE);
			votes.put(voteCreator, 1);
			int i = 0;
			Set<String> users = votes.keySet();
			/*for(String user:users){
				if(votes.get(user)==1)
					i++;
			}*/
			NUM_OF_VOTES = votes.size();
			String rootText = wavelet.getRootBlip().getDocument().getText();
			int index = rootText.indexOf("Wave Max: ");
			if(index<0){
				String appendText = "\n\n" + "Wave Max: " + (NUM_OF_VOTES + MAX_BLIPS) + "\nNumber of votes for new wave: "+NUM_OF_VOTES;
				//wavelet.getRootBlip().getDocument().delete();
				wavelet.getRootBlip().getDocument().append(appendText);
			}else{
				String newText = rootText.substring(0,index);
				wavelet.getRootBlip().getDocument().delete();
				wavelet.getRootBlip().getDocument().append(newText + "Wave Max: " + (NUM_OF_VOTES + MAX_BLIPS) + "\nNumber of votes for new wave: "+NUM_OF_VOTES);
			}
			if(NUM_OF_VOTES>((1/3)*ACTIVE_WAVERS)&&(ACTIVE_WAVERS>=4))
				makeNewWave(wavelet);
		}
		/*if(text.startsWith("!@russian-roulette@!")){
			int drop = generator.nextInt(wavelet.getParticipants().size());
			//Blip newBlip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.append("\nThanks for transforming " + wavelet.getParticipants().get(drop) + ".");
			wavelet.removeParticipant(wavelet.getParticipants().get(drop));
		}*/
		
		if(text.startsWith(CMD_OPEN_IDENT + WEATHER)){
			Matcher mtchr = weatherPattern.matcher(text);
			mtchr.lookingAt();
			try{
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
			}catch (IllegalStateException e){
				//blip.getDocument().replace("Degrees");
			}catch (IndexOutOfBoundsException e){
				//not this many matches
			}
		}
		if(text.startsWith("!@vote-to-ban:")){
			if (cantBan.containsKey(blip.getCreator())) {
				if (cantBan.get(blip.getCreator()) + 10(60)(1000) < System.currentTimeMillis()) {
					return;
				} else {
					cantBan.remove(blip.getCreator());
				}
			}
			
			Pattern banP = Pattern.compile("!@vote-to-ban:(.+)@!");
			Matcher mtchr = banP.matcher(text);
			
			mtchr.lookingAt();
			
			try{
				wavelet.getRootBlip().getDocument().append("\n" + blip.getCreator() + " motions to ban " + mtchr.group(1) + ".");
				
				if (banMap.contains(mtchr.group(1))) {
					banMap.get(mtchr.group(1)).add(blip.getCreator());
				} else {
					banMap.put(mtchr.group(1), new HashSet());
					banMap.get(mtchr.group(1)).add(blip.getCreator());
				}
			}catch (IllegalStateException e){
				wavelet.getRootBlip().getDocument().append("\n" + blip.getCreator() + " loses their ban vote privileges.");
				
				cantBan.put(blip.getCreator(), System.currentTimeMillis());
			}catch (IndexOutOfBoundsException e){
				//not this many matches
			}
			
			if (banMap.get(mtchr.group(1)).size() >= ((2/3) * ACTIVE_WAVERS)) {
				wavelet.removeParticipant(mtchr.group(1));
			}
		}
	}

	private void makeNewWave(Wavelet wavelet) {
		String title = getNewTitle(wavelet);
		Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
		newWave.setTitle(title);
	}

	private String getNewTitle(Wavelet wavelet) {
		// TODO Auto-generated method stub
		String Title = wavelet.getTitle();
		/*int indexMax = wavelet.getRootBlip().getDocument().getText().indexOf("Wave Max: ");
		if(indexMax>-1){
			Title = wavelet.getRootBlip().getDocument().getText().substring(0,indexMax);
		}*/
		int index = Title.indexOf("//Part");
		if(index == -1){
			Title = Title + " //Part 2";
		}else{
			int count = Integer.parseInt(Title.substring(index+6).trim());
			count++;
			Title = Title.substring(0,index) + " //Part " + count;
		}
		return Title;
	}

	private void consolidateBlips(Wavelet wavelet, Blip latestBlip) {
		/* do nada. */
		return;
	}

}
