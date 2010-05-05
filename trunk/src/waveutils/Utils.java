package waveutils;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import stats.*;

import autobot.Auto_BotServlet;

import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Blip;
import com.google.wave.api.BlipContent;
import com.google.wave.api.BlipContentRefs;
import com.google.wave.api.Element;
import com.google.wave.api.Wavelet;

/**
 * Utilities for performing common Wave actions. Generally, Utils provides a way for modifying Blips 
 * and Wavelets in one place and is a wrapper for other package classes like BlipUtils and WaveletUtils.
 * 
 * @author Rob Kiefer
 * @version 0.1.2
 *
 */
public class Utils {
	private static Logger log = Auto_BotServlet.log;
	
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
		log.log(Level.INFO, "Appending " + s + " to blip " + b);
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
	
	public static BlipContentRefs replaceBlipContent(Blip b, String needle, BlipContent replace) {
		return BlipUtils.replaceContent(b, needle, replace);
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
	
	/**
	 * Removes Blip from Wavelet
	 * 
	 * @param w Wavelet that contains Blip
	 * @param b Blip to be deleted
	 */
	public static void deleteBlip(Wavelet w, Blip b) {
		w.delete(b);
	}
	
	/**
	 * Creates a new Wave via AbstractRobot <tt>Maker</tt> through the submission of Wavelet <tt>withWave</tt> 
	 * with title <i>title</i> in domain <tt>domain</tt> with participants in the set <tt>participants</tt>.
	 *  
	 * @param maker AbstractRobot who will make the new wave
	 * @param withWave Wavelet that on submission will create the new Wavelet 
	 * @param title Title of new Wave
	 * @param domain Domain of new Wave
	 * @param participants Participants in the new Wave
	 * 
	 * @return The newly created wavelet.
	 */
	public static Wavelet createWaveWithOther(AbstractRobot maker, Wavelet withWave, String title, String domain, Set<String> participants) {
		return WaveletUtils.create(maker, withWave, title, domain, participants);
	}
	
	public static Wavelet createWave(AbstractRobot maker, String title, String domain, Set<String> participants, String rpc) {
		return WaveletUtils.create(maker, title, domain, participants, rpc);
	}
}
