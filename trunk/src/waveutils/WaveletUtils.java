package waveutils;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

/**
* A utility class for modifying Wavelets in Google Wave.
* 
* @author Rob
* @version 0.1.0
*
*/
public class WaveletUtils {
	
	/**
	 * Creates a new reply to Wavelet <i>w</i> with string <i>s</i>
	 * 
	 * @param w Wavelet to reply to
	 * @param s Reply message
	 * @return Reply blip
	 */
	public static Blip reply(Wavelet w, String s) {
		return w.reply("\n" + s);
	}
}
