package autobot;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public abstract class AbstractBlipProcessor {
	protected abstract Wavelet processBlip(Blip blip, Wavelet wavelet);
}
