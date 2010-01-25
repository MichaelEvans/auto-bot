package blipProcessors;

import java.util.Map;
import java.util.Random;

import com.google.wave.api.Blip;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

public class RussianRouletteBlipProcessor implements AbstractBlipProcessor {
	public final static String RUSSIAN_ROULETTE = "russian-roulette";
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {
		Random generator = new Random();
		int drop = generator.nextInt(wavelet.getParticipants().size());
		
		TextView textView = blip.getDocument();
		textView.append("\nThanks for transforming " + wavelet.getParticipants().get(drop) + ".");
		wavelet.removeParticipant(wavelet.getParticipants().get(drop));
		
		return wavelet;
	}

}
