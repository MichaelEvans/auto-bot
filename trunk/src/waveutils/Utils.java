package waveutils;

import stats.*;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

/**
 * Utilities for performing common Wave actions. Generally, Utils provides a way for modifying Blips 
 * and Wavelets in one place and is a wrapper for other package classes like BlipUtils and WaveletUtils.
 * 
 * @author Rob Kiefer
 * @version 0.1.0
 *
 */
public class Utils {
	
	/**
	 * Appends the string <i>s</i> on a new line to Blip <i>b</i>
	 *  
	 * @param b Blip to append 
	 * @param s String to append to Blip
	 */
	public static void appendLineToBlip(Blip b, String s) {
		BlipUtils.appendLine(b, s);
	}
	
	/**
	 * Appends the string <i>s</i> to the Blip <i>b</i> (no new line)
	 * 
	 * @param b Blip to append
	 * @param s String to append to Blip
	 */
	public static void appendToBlip(Blip b, String s) {
		BlipUtils.append(b, s);
	}
	
	/**
	 * Replaces all the content of Blip <i>b</i> with the string <i>s</i>
	 * 
	 * @param b Blip to replace
	 * @param s String to set Blip content to
	 */
	public static void replaceBlip(Blip b, String s) {
		BlipUtils.replace(b, s);
	}
	
	/**
	 * Replaces all instances of the <i>needle</i> string inside a Blip
	 * 
	 * @param b Blip to replace in
	 * @param needle String to match against
	 * @param replace Replacement string
	 */
	public static void replaceBlipContent(Blip b, String needle, String replace) {
		BlipUtils.replaceContent(b, needle, replace);
	}
	
	/**
	 * Creates a new reply to Wavelet <i>w</i> with string <i>s</i>
	 * 
	 * @param w Wavelet to reply to
	 * @param s Reply message
	 * @return Reply blip
	 */
	public static Blip reply(Wavelet w, String s) {
		return WaveletUtils.reply(w, s);
	}
	
	// TODO : Move this
	public static String getNewTitle(Wavelet wavelet) {
		final String CONT_IDENT = " // Part ";
		
		int index;
		String title, waveBaseTitle = wavelet.getTitle();
		
		if (waveBaseTitle == null) {
			waveBaseTitle = "";
		}
		
		index = waveBaseTitle.indexOf(CONT_IDENT);
		if (index == -1) {
			title = waveBaseTitle + CONT_IDENT + "2";
		} else {
			int count = Integer.parseInt(waveBaseTitle.substring(index + CONT_IDENT.length()).trim());
			title = waveBaseTitle.substring(0,index) + CONT_IDENT + (count + 1);
		}
		
		return title;
	}
	
	// TODO: Move this
	public static String markovTitle(WaveStats ws) {
		String start = "Wave of " + ws.doMarkov(10);
		return start;
	}
}
