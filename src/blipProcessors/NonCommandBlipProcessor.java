package blipProcessors;

import java.util.Map;

import autobot.WaveUtils;
import waveutils.*;

import com.google.wave.api.Blip;
import com.google.wave.api.Image;
import com.google.wave.api.Wavelet;

public class NonCommandBlipProcessor implements IBlipProcessor {

	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {

		//BlipUtils.replaceBlipContent(blip, ":D", "D:");
		
		return wavelet;
	}

}
