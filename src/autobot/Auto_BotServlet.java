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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.wave.api.*;

public class Auto_BotServlet extends AbstractRobotServlet {
	static final Logger log = Logger.getLogger(Auto_BotServlet.class.getName()); 
	public String COMMAND = "!@force_new_wave@!";
	public int MAX_BLIPS = 150;
	public HashMap<String, Integer> votes = new HashMap<String, Integer>();
	public void processEvents(RobotMessageBundle bundle) {
		Wavelet wavelet = bundle.getWavelet();
		if (bundle.wasSelfAdded()) {
			Blip blip = wavelet.appendBlip();
			TextView textView = blip.getDocument();
			textView.append("Autobots roll out.");
		}
		for (Event e: bundle.getEvents()) {
			if (e.getType() == EventType.WAVELET_PARTICIPANTS_CHANGED) {    
				//Blip blip = wavelet.appendBlip();
				//TextView textView = blip.getDocument();
				//textView.append("Participant Added.");
			}
			if (e.getType() == EventType.BLIP_SUBMITTED){
				Blip root = wavelet.getRootBlip();
				List<String> childrenIds = root.getChildBlipIds(); 
				//List<Blip> children = new ArrayList<Blip>(); 
				/*for (String id: childrenIds) { 
					children.add(bundle.getBlip(wavelet.getWaveId(), wavelet.getWaveletId(), id)); 
				}*/
				processBlip(e.getBlip(), wavelet);
				if(childrenIds.size()==MAX_BLIPS){
					Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
					String title = getNewTitle(wavelet);
					newWave.setTitle(title);
				}
				
			}
			/*if (e.getType() == EventType.BLIP_DELETED){
				Blip blip = wavelet.appendBlip();
				TextView textView = blip.getDocument();
				textView.append("Blip Deleted");
				Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
				Blip newBlip = newWave.appendBlip();
				TextView text = newBlip.getDocument();
				text.append("New Wave Created");
				//log.warning("Title Changed");
				//Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "DataDocument");
			}*/
		}
	}

	private void processBlip(Blip blip, Wavelet wavelet) {
		// TODO Auto-generated method stub
		String text = blip.getDocument().getText();
		String author = wavelet.getCreator();
		String authorRequest = blip.getCreator();
		log.info("Wave Creator: "+ author + "Blip from: " + authorRequest+"\n");
		if (text.startsWith(COMMAND) && author.equals(authorRequest)) {
			log.info("Forced a new wave.");
			Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
			String title = getNewTitle(wavelet);
			newWave.setTitle(title);
		}
		if(text.startsWith("!@add_vote@!")){
			//Gadget voter = new Gadget("http://wave-poll.googlecode.com/svn/trunk/src/poll.xml");
			String voteCreator = blip.getCreator();
			votes.put(voteCreator, 1);
			int i = 0;
			Set<String> users = votes.keySet();
			for(String user:users){
				if(votes.get(user)==1)
					i++;
			}
			String rootText = wavelet.getRootBlip().getDocument().getText();
			//String newText = rootText.substring(0,rootText.indexOf("Number of votes for new wave: "));
			//wavelet.getRootBlip().getDocument().delete();
			wavelet.getRootBlip().getDocument().append("Number of votes for new wave: "+i);
		}
	}

	private String getNewTitle(Wavelet wavelet) {
		// TODO Auto-generated method stub
		String Title = wavelet.getTitle();
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
