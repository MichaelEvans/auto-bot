package blipProcessors;

import java.util.Map;
import java.util.HashSet;

import waveutils.Utils;

//import autobot.Auto_BotProfileServlet;
import autobot.Auto_BotServlet;
import autobot.Tools;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class ForceNewWaveBlipProcessor implements IBlipProcessor {
	public final static String FORCE_NEW_WAVE = "force-new-wave";
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		if (((HashSet<String>)dataMap.get("privelegedWavers")).contains(blip.getCreator())) {
			
			Auto_BotServlet.log.info(blip.getCreator() + " is forcing a new wave.");
			
			//TODO: Deal with NEW_WAVE_INDICATOR better
			Utils.reply(wavelet, Auto_BotServlet.NEW_WAVE_INDICATOR + "\n\n!{fuck_this_thread_im_outta_here}!");
			Utils.createWaveWithOther((Auto_BotServlet)dataMap.get("robot"), wavelet, Tools.newTitle(wavelet), wavelet.getDomain(), wavelet.getParticipants());
		}
		
		return wavelet;
	}

}
