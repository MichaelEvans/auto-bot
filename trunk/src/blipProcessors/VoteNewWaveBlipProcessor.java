package blipProcessors;

import java.util.HashMap;
import java.util.Map;


import autobot.Auto_BotServlet;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;
import com.trollhouse.wave.utils.Utils;

public class VoteNewWaveBlipProcessor implements IBlipProcessor {
	public final static String VOTE_NEW_WAVE = "roll-out";
	
	final static String NW_VOTE_QUOTE = "Before your president decides, please ask him this: What if we leave, and you're wrong?";
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String voteCreator, rootText;
		int numVotes;
		HashMap<String, Integer> votes;
		
		//votes = new HashMap<String, Integer>();
		//numVotes  = votes.size();
		//voteCreator = blip.getCreator();
/*		TODO
		blip.getDocument().append("\n" + NW_VOTE_QUOTE);
		votes.put(voteCreator, 1);

		numVotes = votes.size();
		rootText = wavelet.getRootBlip().getDocument().getText();
		
		int index = rootText.indexOf("Wave Max: ");
		if (index < 0) {
			String appendText = "\n\n" + "Wave Max: " + (numVotes + Auto_BotServlet.MAX_BLIPS) + "\nNumber of votes for new wave: " + numVotes;
			wavelet.getRootBlip().getDocument().append(appendText);
		} else {
			String newText = rootText.substring(0, index);
			wavelet.getRootBlip().getDocument().delete();
			wavelet.getRootBlip().getDocument().append(newText + "Wave Max: " + (numVotes + Auto_BotServlet.MAX_BLIPS) + "\nNumber of votes for new wave: " + numVotes);
		}
		
		if (numVotes > ((1/3) * (Integer)dataMap.get("numberOfActiveWavers"))) {
			return WaveUtils.makeNewWavelet(wavelet);
		}*/
		
		return null;// wavelet;
	}

}
