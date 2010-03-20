package blipProcessors;

import java.util.Map;
import java.util.logging.Level;

import waveutils.Utils;

//import autobot.Auto_BotProfileServlet;
import autobot.Auto_BotServlet;

import org.waveprotocol.wave.model.id.*;

import com.google.wave.api.Blip;
import com.google.wave.api.Gadget;
import com.google.wave.api.Wavelet;

public class SpoilerBlipProcessor implements IBlipProcessor {
	public final static String NAME = "spoiler";
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		Utils.replaceBlip(blip, "\n\nSpoiler: ");
		Auto_BotServlet.log.log(Level.INFO, "WaveDomain: " + wavelet.getWaveId().getDomain() + " | WaveID: " + wavelet.getWaveId().getId());
		Auto_BotServlet.log.log(Level.INFO, "WaveletDomain: " + wavelet.getWaveletId().getDomain() + " | WaveletID: " + wavelet.getWaveletId().getId());
		blip.append(new Gadget("http://spoil-bot.appspot.com/spoil.xml?wave=" + blip.serialize().getWaveId() + "&wavelet=" + blip.serialize().getWaveletId() + "&blip=" + blip.getBlipId()));
		
		
		return wavelet;
	}

}
