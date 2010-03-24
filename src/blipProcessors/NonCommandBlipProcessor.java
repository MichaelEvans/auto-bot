package blipProcessors;

import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import waveutils.*;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class NonCommandBlipProcessor implements IBlipProcessor {

	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {

		Pattern weatherPattern = Pattern.compile("(\\^|\\n|\\A){1}>.*");
		Matcher mtchr = weatherPattern.matcher(blip.getContent());
		
		while(mtchr.find()) {
			autobot.Auto_BotServlet.log.log(Level.INFO, "Found something: " + mtchr.group());
			Utils.replaceBlip(blip, "This isn't 4chan.");
		}

		return wavelet;
	}

}
