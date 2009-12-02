/* 
  Copyright (c) 2009 Michael Evans
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
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.*;
import java.util.Random;

import com.google.wave.api.*;

public class Auto_BotServlet extends AbstractRobotServlet {
	static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName()); 
	public String COMMAND = "!@force-new-wave@!";
	public int MAX_BLIPS = 150;
	public HashMap<String, Integer> votes = new HashMap<String, Integer>();
	int NUM_OF_VOTES = votes.size();
	public ArrayList<String> activeWavers = new ArrayList<String>();
	int ACTIVE_WAVERS = 0;
	Image optimusTransform = new Image("http://imgur.com/m66zH.gif", 160, 120, "");
	Random generator = new Random();
	String weatherURL = "http://weather.yahooapis.com/forecastrss?p=";
		
	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();
		if (bundle.wasSelfAdded()) {
			Blip blip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.append("Autobots roll out.");
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
		if (text.startsWith(COMMAND) && author.equals(authorRequest)) {
			log.info("Forced a new wave.");
			Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
			String title = getNewTitle(wavelet);
			newWave.setTitle(title);
		}
		if(text.startsWith("!@roll-out@!")){
			String voteCreator = blip.getCreator();
			blip.getDocument().append("\nFreedom is your right. If you make that request we will honor it.");
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
		//
		if(text.startsWith("!@weather")){
			Pattern banP = Pattern.compile("!@weather:(\\d{5})@!");
			Matcher mtchr = banP.matcher(text);
			mtchr.lookingAt();
			try{
				//ban
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
			Pattern banP = Pattern.compile("!@vote-to-ban:(.+)@!");
			Matcher mtchr = banP.matcher(text);
			mtchr.lookingAt();
			try{
				//ban
				wavelet.getRootBlip().getDocument().append("\n" + blip.getCreator() + " motions to ban " + mtchr.group(1) + ".");
			}catch (IllegalStateException e){
				wavelet.getRootBlip().getDocument().append("\n" + blip.getCreator() + " loses their ban vote privileges.");
			}catch (IndexOutOfBoundsException e){
				//not this many matches
			}
			//wavelet.removeParticipant(mtchr.group(1));
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

}