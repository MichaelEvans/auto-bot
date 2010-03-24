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

import com.google.wave.api.Blip;
import com.google.wave.api.Image;
import com.google.wave.api.Wavelet;

public class ChanCleanerBlipProcessor implements IBlipProcessor {
	public final static String TRIGGER = ">";

	final static Logger log = Logger.getLogger(WeatherRequestBlipProcessor.class.getName());

	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {
                String str = blip.getContent();
                if (str.matches("^>")) {
                        log.log(Level.INFO, "Someone thinks wave is 4chan.");
                        Utils.replaceBlip(blip, "This isn't 4chan.");
                }

                return (wavelet);
        }
}
