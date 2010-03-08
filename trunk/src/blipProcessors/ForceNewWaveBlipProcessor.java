package blipProcessors;

import java.util.Map;
import java.util.HashSet;

//import autobot.Auto_BotProfileServlet;
import autobot.Auto_BotServlet;
import autobot.WaveUtils;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class ForceNewWaveBlipProcessor implements IBlipProcessor {
	public final static String FORCE_NEW_WAVE = "force-new-wave";
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		if (((HashSet<String>)dataMap.get("privelegedWavers")).contains(blip.getCreator())) {
			Auto_BotServlet.log.info(blip.getCreator() + " is forcing a new wave.");
			
			((Auto_BotServlet)dataMap.get("robot")).createNewWave(wavelet);
		}
		
		return wavelet;
	}

}
