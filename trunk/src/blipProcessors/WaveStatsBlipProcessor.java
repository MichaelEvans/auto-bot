package blipProcessors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

/**
 * 
 * @author n.lefler
 *
 */
public class WaveStatsBlipProcessor implements BlipProcessor {
	public final static String WAVE_STATS = "get-wave-stats";
	
	final static Pattern getWaveStatsPattern = Pattern.compile(CMD_OPEN_IDENT + WAVE_STATS + CMD_CLOSE_IDENT);
	
	final static SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {		
		StringBuffer responseBuffer = new StringBuffer();
		
		responseBuffer.append("\n\n");
		
		responseBuffer.append("Wave created: ");
		responseBuffer.append(sdf.format(new Date(wavelet.getCreationTime())));
		responseBuffer.append("\n\n");
		
		responseBuffer.append("Number of Human Wavers: ");
		responseBuffer.append(getHumanWavers(wavelet.getParticipants()).size());
		responseBuffer.append("\n\n");
		
		responseBuffer.append("Number of Bots: ");
		responseBuffer.append(getBots(wavelet.getParticipants()).size());
		responseBuffer.append("\n\n");
		
		responseBuffer.append("Number of Blips (According to Auto-Bot): ");
		responseBuffer.append(dataMap.get("numberOfBlips"));
		responseBuffer.append("\n");
		
		blip.getDocument().append(responseBuffer.toString());
		
		return wavelet;
	}

	/**
     * Takes a list of all wavers subscribed to this wave, removes bots, and returns a list containing only human wavers
     */
    private static List<String> getHumanWavers(List<String> wavers) {
        List<String> remList = new ArrayList<String>();

        for (String s : wavers) {
            if (s.contains("@appspot.com")) {
                remList.add(s);
            }
        }

        wavers.removeAll(remList);

        return wavers;
    }
    
    private static List<String> getBots(List<String> wavers) {
    	List<String> botsList = new ArrayList<String>();

        for (String s : wavers) {
            if (s.contains("@appspot.com")) {
            	botsList.add(s);
            }
        }

        return botsList;
    }
}
