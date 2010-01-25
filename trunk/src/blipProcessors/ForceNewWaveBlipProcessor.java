package blipProcessors;

import java.util.Map;

import autobot.Auto_BotProfileServlet;
import autobot.Auto_BotServlet;
import autobot.WaveUtils;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class ForceNewWaveBlipProcessor extends AbstractBlipProcessor {

	@Override
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		Auto_BotServlet.log.info(blip.getCreator() + " is forcing a new wave.");
		
		return WaveUtils.makeNewWavelet(wavelet);
	}

}
