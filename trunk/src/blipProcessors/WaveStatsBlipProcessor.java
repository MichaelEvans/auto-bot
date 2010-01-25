package blipProcessors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

/**
 * 
 * @author n.lefler
 *
 */
public class WaveStatsBlipProcessor extends AbstractBlipProcessor {
	SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	
	@Override
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {		
		StringBuffer responseBuffer = new StringBuffer();
		
		responseBuffer.append("Wave created: ");
		responseBuffer.append(sdf.format(new Date(wavelet.getCreationTime())));
		responseBuffer.append("\n\n");
		
		responseBuffer.append("Number of Human Wavers: ");
		responseBuffer.append(getHumanWavers(wavelet.getParticipants()).size());
		responseBuffer.append("\n\n");
		
		responseBuffer.append("Number of Bots: ");
		responseBuffer.append(getBots(wavelet.getParticipants()).size());
		responseBuffer.append("\n\n");
		
		responseBuffer.append("Number of Blips: ");
		responseBuffer.append(wavelet.getRootBlip().getChildBlipIds().size());
		responseBuffer.append("\n");
		
		blip.getDocument().append(responseBuffer.toString());
		
		return wavelet;
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
    
    private List<String> getBots(List<String> wavers) {
    	List<String> botsList = new ArrayList<String>();

        for (String s : wavers) {
            if (s.contains("@appspot.com")) {
            	botsList.add(s);
            }
        }

        return botsList;
    }
}
