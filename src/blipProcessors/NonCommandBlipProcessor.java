package blipProcessors;

import java.util.Map;

import waveutils.*;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class NonCommandBlipProcessor implements IBlipProcessor {

	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {

		//Utils.replaceBlipContent(blip, ":D", "D:");
		
		return wavelet;
	}

}
