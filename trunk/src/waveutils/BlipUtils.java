package waveutils;

import com.google.wave.api.*;

/**
 * A utility class for modifying Blips in Google Wave.
 * 
 * @author Rob
 * @version 0.1.0
 *
 */
public class BlipUtils {
	
	/**
	 * Appends the string <i>s</i> on a new line to Blip <i>b</i>
	 *  
	 * @param b Blip to append 
	 * @param s String to append to Blip
	 */
	public static void appendLine(Blip b, String s) {
		BlipUtils.append(b, "\n" + s);
	}
	
	/**
	 * Appends the string <i>s</i> to the Blip <i>b</i> with no new line preceeding <i>s</i>.
	 * 
	 * @param b Blip to append
	 * @param s String to append to Blip
	 */
	public static void append(Blip b, String s) {
		b.append(s);
	}
	
	/**
	 * Replaces all the content of Blip <i>b</i> with the string <i>s</i>
	 * 
	 * @param b Blip to replace
	 * @param s String to set Blip content to
	 */
	public static void replace(Blip b, String s) {
		b.all().delete();
		BlipUtils.append(b, s);
	}
	
	/**
	 * Replaces all instances of the <i>needle</i> string inside a Blip <i>b</i>
	 * 
	 * @param b Blip to replace in
	 * @param needle String to match against
	 * @param replace Replacement string
	 */
	public static void replaceBlipContent(Blip b, String needle, String replace) {
		BlipContentRefs refs = b.all(needle);
		refs.replace(replace);
	}
}