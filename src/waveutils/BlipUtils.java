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
		BlipUtils.replaceContent(b, b.getContent(), s);
	}	
	
	/**
	 * Replaces all instances of the <tt>needle</tt> string with <tt>replace</tt> inside a Blip <tt>b</tt>
	 * 
	 * @param b Blip to replace in
	 * @param needle String to match against
	 * @param replace Replacement string
	 */
	public static void replaceContent(Blip b, String needle, String replace) {
		BlipUtils.replace(b.all(needle), replace);
	}
	
	private static void replace(BlipContentRefs refs, String s) {
		refs.replace(s);
	}
	
	public static BlipContentRefs replaceContent(Blip b, String needle, BlipContent replace) {
		
		return b.all(needle).replace(replace);
	}
}
