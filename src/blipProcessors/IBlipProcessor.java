package blipProcessors;

import java.io.Serializable;
import java.util.Map;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public interface IBlipProcessor extends Serializable {
	public static final String CMD_OPEN_IDENT = "!@";
	public static final String CMD_CLOSE_IDENT = "@!";
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap);
}
