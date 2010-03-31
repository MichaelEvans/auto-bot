package blipProcessors;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import waveutils.*;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class NonCommandBlipProcessor implements IBlipProcessor {
	private static Logger log = Logger.getLogger(NonCommandBlipProcessor.class.getName());

	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {

		/*String cont = blip.getContent().trim().toLowerCase();
		if (cont.matches("^\\^*this\\.*$")) {
			log.log(Level.INFO, "Post is "
					+ "useless " + cont);
			blip.all().delete();
			Utils.appendToBlip(blip, "This post was completely "
					+ "useless.  Instead, have a picture\n!{::random}!");
			return (wavelet);
		}

		Pattern chanQuote = Pattern.compile("(\\^|\\n|\\A){1}(\\.)?>.*");
		Matcher mtchr = chanQuote.matcher(blip.getContent());

		while(mtchr.find()) {
			log.log(Level.INFO, "Found something: " + mtchr.group());
			blip.all().delete();
			Utils.appendToBlip(blip, "This isn't 4chan.");
			blip.all().annotate("style/fontWeight", "bold");
			blip.all().annotate("style/color", "red");
		}*/

		return wavelet;
	}

}
