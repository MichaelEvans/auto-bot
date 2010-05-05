package com.trollhouse.wave.utils;

import java.util.Set;

import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

/**
* A utility class for modifying Wavelets in Google Wave.
* 
* @author Rob
* @version 0.1.2
*
*/
public class WaveletUtils {
	
	/**
	 * Creates a new reply to Wavelet <tt>w</tt> with string <tt>s</tt>
	 * 
	 * @param w Wavelet to reply to
	 * @param s Reply message
	 * @return Reply blip
	 */
	public static Blip reply(Wavelet w, String s) {
		return w.reply("\n" + s);
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
	 * @return The newly created Wavelet
	 */
	public static Wavelet create(AbstractRobot maker, Wavelet withWave, String title, String domain, Set<String> participants) {
		Wavelet newWavelet = maker.newWave(domain, participants);
		newWavelet.setTitle(title);
		newWavelet.submitWith(withWave);
		
		return newWavelet;
	}
	
	public static Wavelet create(AbstractRobot maker, String title, String domain, Set<String> participants, String rpc) {
		Wavelet newWavelet = maker.newWave(domain, participants);
		newWavelet.setTitle(title);
		try {
			maker.submit(newWavelet, rpc);
		}
		catch(Exception e) {
			
		}
		return newWavelet;
	}
}
