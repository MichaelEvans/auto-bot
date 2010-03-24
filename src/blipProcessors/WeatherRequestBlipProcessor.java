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
	private final String REGEX = "weather:([0-9]{5})";
	
	final static Logger log = Logger.getLogger(WeatherRequestBlipProcessor.class.getName());
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		Pattern weatherPattern = Pattern.compile(REGEX);
		Matcher mtchr = weatherPattern.matcher(blip.getContent());
		String current = "";
		String imageURL = "";
		while(mtchr.find()) {
			log.log(Level.INFO, "Found something: " + mtchr.group());
			String zip = mtchr.group(1);
		
			try {
				try {
					log.log(Level.INFO, "Getting weather for " + zip);
					
					current = WeatherParser.getLocation(zip);
					current += "\nNow - " + WeatherParser.getTemp(zip);
					current += "\n" + WeatherParser.getForecast(zip);
					imageURL = WeatherParser.getImage(zip);
					
					log.log(Level.INFO, "This is something? " + current);
				} catch (NumberFormatException e) {
					Utils.appendLineToBlip(blip, "Encountered an error when requesting weather");
				
					Auto_BotServlet.log.log(Level.WARNING, "Caught NumberFormatException when requesting weather, message was: " + e.getLocalizedMessage());
					e.printStackTrace();
				} catch (IOException e) {
					Utils.appendLineToBlip(blip, "Encountered an error when requesting weather");
				
					log.warning("Caught IOException when requesting weather, message was: " + e.getLocalizedMessage());
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					log.warning("Could not construct xml parser");
				} catch (SAXException e) {
					log.warning("Unable to parse weather xml");
				}
			
				log.log(Level.INFO, "Replacing blip with: " + current);

			} catch (IllegalStateException e) {
				log.warning("Caught IllegalStateException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				Utils.appendLineToBlip(blip, "Incorrect command form. Correct form is " + CMD_OPEN_IDENT + WEATHER + ":<zip code, 5 digits>" + CMD_CLOSE_IDENT);
			
				log.warning("Caught IndexOutOfBoundsException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		Utils.appendLineToBlip(blip, "\n" + current);
		//blip.append(new Image(imageURL,15,15,""));

		return wavelet;
	}

}
