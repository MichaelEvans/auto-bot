package blipProcessors;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import autobot.Auto_BotServlet;
import autobot.XMLParser;

import com.google.wave.api.Blip;
import com.google.wave.api.Image;
import com.google.wave.api.Wavelet;

public class WeatherRequestBlipProcessor extends AbstractBlipProcessor {

	@Override
	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {
		final String WEATHER = "weather";
		final Pattern weatherPattern = Pattern.compile(CMD_OPEN_IDENT + WEATHER + ":(\\d{5})" + CMD_CLOSE_IDENT);
		final Logger log = Logger.getLogger(WeatherRequestBlipProcessor.class.getName());
		
		Matcher mtchr = weatherPattern.matcher(blip.getDocument().getText());
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
				blip.getDocument().append("Encountered an error when requesting weather");
				
				Auto_BotServlet.log.log(Level.WARNING, "Caught NumberFormatException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			} catch (IOException e) {
				blip.getDocument().append("Encountered an error when requesting weather");
				
				log.warning("Caught IOException when requesting weather, message was: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
			
			blip.getDocument().replace(current);
			blip.getDocument().appendElement(new Image(image, 52,52,""));
		} catch (IllegalStateException e) {
			log.warning("Caught IllegalStateException when requesting weather, message was: " + e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			blip.getDocument().append("Incorrect command form. Correct form is " + CMD_OPEN_IDENT + WEATHER + ":<zip code, 5 digits>" + CMD_CLOSE_IDENT);
			
			log.warning("Caught IndexOutOfBoundsException when requesting weather, message was: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return wavelet;
	}

}
