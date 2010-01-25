package blipProcessors;

import java.util.Map;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

/**
 * 
 * @author n.lefler
 *
 */
public abstract class AbstractBlipProcessor {
	public static final String CMD_OPEN_IDENT = "!@";
	public static final String CMD_CLOSE_IDENT = "@!";
	
	public abstract Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap);
}
