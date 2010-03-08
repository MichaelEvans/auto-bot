package blipProcessors;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import autobot.Auto_BotServlet;
import autobot.WaveUtils;
import autobot.XMLParser;

import com.google.wave.api.Blip;
import com.google.wave.api.Image;
import com.google.wave.api.Wavelet;

public class WeatherRequestBlipProcessor implements IBlipProcessor {
	public final static String WEATHER = "weather";
	
	final static Pattern weatherPattern = Pattern.compile(CMD_OPEN_IDENT + WEATHER + ":(\\d{5})" + CMD_CLOSE_IDENT);
	final static Logger log = Logger.getLogger(WeatherRequestBlipProcessor.class.getName());
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {
		Matcher mtchr = weatherPattern.matcher(blip.getContent());
		mtchr.lookingAt();
		
		try {
			String current = "";
			String image = "";
			try {
				current = XMLParser.getLocation(Integer.parseInt(mtchr.group(1)));
				current += "\nNow - " + XMLParser.getTemp(Integer.parseInt(mtchr.group(1)));
				current += "\n" + XMLParser.getForecast(Integer.parseInt(mtchr.group(1)));
				image = XMLParser.getImage(Integer.parseInt(mtchr.group(1)));
			} catch (NumberFormatException e) {
				//blip.getDocument().append("Encountered an error when requesting weather");
				WaveUtils.appendToBlip(blip, "Encountered an error when requesting weather");
				
				Auto_BotServlet.log.log(Level.WARNING, "Caught NumberFormatException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			} catch (IOException e) {
				//blip.getDocument().append("Encountered an error when requesting weather");
				WaveUtils.appendToBlip(blip, "Encountered an error when requesting weather");
				
				log.warning("Caught IOException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
			
			//blip.getDocument().replace(current);
			WaveUtils.replaceBlip(blip, current);
			//TODO: blip.appendElement(new Image(image, 52,52,""));
		} catch (IllegalStateException e) {
			log.warning("Caught IllegalStateException when requesting weather, message was: " + e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			//blip.getDocument().append("Incorrect command form. Correct form is " + CMD_OPEN_IDENT + WEATHER + ":<zip code, 5 digits>" + CMD_CLOSE_IDENT);
			WaveUtils.appendToBlip(blip, "Incorrect command form. Correct form is " + CMD_OPEN_IDENT + WEATHER + ":<zip code, 5 digits>" + CMD_CLOSE_IDENT);
			
			log.warning("Caught IndexOutOfBoundsException when requesting weather, message was: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return wavelet;
	}

}
