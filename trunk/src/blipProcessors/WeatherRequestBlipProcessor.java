package blipProcessors;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import waveutils.Utils;

import autobot.Auto_BotServlet;
import autobot.WeatherParser;

import com.google.wave.api.Blip;
import com.google.wave.api.Image;
import com.google.wave.api.Wavelet;

public class WeatherRequestBlipProcessor implements IBlipProcessor {
	public final static String WEATHER = "weather";
	
	final static Pattern weatherPattern = Pattern.compile(WEATHER + ":");//(\\d{5})" + CMD_CLOSE_IDENT);
	final static Logger log = Logger.getLogger(WeatherRequestBlipProcessor.class.getName());
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {
		Matcher mtchr = weatherPattern.matcher(blip.getContent());
		log.log(Level.INFO, "Test: " + blip.getContent());
		if (mtchr.lookingAt())
			log.log(Level.INFO, "Found a match");
		else
			log.log(Level.INFO, "No match");
		
		try {
			String current = "";
			String image = "";
			try {
				log.log(Level.INFO, "Getting weather for" + mtchr.group(1));
				current = WeatherParser.getLocation(mtchr.group(1));
				current += "\nNow - " + WeatherParser.getTemp(mtchr.group(1));
				current += "\n" + WeatherParser.getForecast(mtchr.group(1));
				image = WeatherParser.getImage(mtchr.group(1));
			} catch (NumberFormatException e) {
				//blip.getDocument().append("Encountered an error when requesting weather");
				Utils.appendLineToBlip(blip, "Encountered an error when requesting weather");
				
				Auto_BotServlet.log.log(Level.WARNING, "Caught NumberFormatException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			} catch (IOException e) {
				//blip.getDocument().append("Encountered an error when requesting weather");
				Utils.appendLineToBlip(blip, "Encountered an error when requesting weather");
				
				log.warning("Caught IOException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				log.warning("Could not construct xml parser");
			} catch (SAXException e) {
				log.warning("Unable to parse weather xml");
			}
			
			//blip.getDocument().replace(current);
			Utils.replaceBlip(blip, current);
			blip.append(new Image(image, 52,52,""));
		} catch (IllegalStateException e) {
			log.warning("Caught IllegalStateException when requesting weather, message was: " + e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			//blip.getDocument().append("Incorrect command form. Correct form is " + CMD_OPEN_IDENT + WEATHER + ":<zip code, 5 digits>" + CMD_CLOSE_IDENT);
			Utils.appendLineToBlip(blip, "Incorrect command form. Correct form is " + CMD_OPEN_IDENT + WEATHER + ":<zip code, 5 digits>" + CMD_CLOSE_IDENT);
			
			log.warning("Caught IndexOutOfBoundsException when requesting weather, message was: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return wavelet;
	}

}
