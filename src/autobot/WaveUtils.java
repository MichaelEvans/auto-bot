package autobot;

import stats.*;

import com.google.wave.api.Blip;
import com.google.wave.api.BlipContentRefs;
import com.google.wave.api.Wavelet;

/**
 * Utilities for performing common Wave actions. Generally, WaveUtils provides a way for modifying Blips 
 * and Wavelets in one place.
 * 
 * @author Auto-Bot team
 * @version 0.1.0
 *
 */
public class WaveUtils {
	
	/**
	 * Appends the string <i>s</i> on a new line to Blip <i>b</i>
	 *  
	 * @param b Blip to append 
	 * @param s String to append to Blip
	 */
	public static void appendToBlip(Blip b, String s) {
		b.append("\n" + s);
	}
	
	/**
	 * Appends the string <i>s</i> to the Blip <i>b</i> (no new line)
	 * 
	 * @param b Blip to append
	 * @param s String to append to Blip
	 */
	public static void appendToBlipNoLine(Blip b, String s) {
		b.append(s);
	}
	
	/**
	 * Replaces all the content of Blip <i>b</i> with the string <i>s</i>
	 * 
	 * @param b Blip to replace
	 * @param s String to set Blip content to
	 */
	public static void replaceBlip(Blip b, String s) {
		b.all().delete();
		b.append(s);
	}
	
	/**
	 * Replaces all instances of the <i>needle</i> string inside a Blip
	 * 
	 * @param b Blip to replace in
	 * @param needle String to match against
	 * @param replace Replacement string
	 */
	public static void replaceBlipContent(Blip b, String needle, String replace) {
		BlipContentRefs refs = b.all(needle);
		refs.replace(replace);
	}
	
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
	
	/**
	 * Generates a non-markov title by taking the previous title and incrementing the part
	 * number.
	 * 
	 * @param wavelet Wavelet to draw original title from
	 * @return New title with correct part number
	 */
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
	
	/**
	 * Generates a markov title by performing markov actions on a given WaveStats WordBags.
	 * @param ws WaveStats to use as training set for markov
	 * @return New markov title
	 */
	public static String markovTitle(WaveStats ws) {
		String start = "Wave of " + ws.doMarkov(10);
		return start;
	}
}
