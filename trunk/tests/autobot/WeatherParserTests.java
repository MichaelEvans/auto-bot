package autobot;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class WeatherParserTests extends TestCase {
	private static String ZIP = "20740";
	
	public void testImageURL() throws ParserConfigurationException, SAXException, IOException {
		assertNotNull(WeatherParser.getImage(ZIP));
	}
	
	public void testTemp() throws ParserConfigurationException, SAXException, IOException {
		assertNotSame("", WeatherParser.getTemp(ZIP));
	}
	
	public void testForecast() throws ParserConfigurationException, SAXException, IOException {
		assertNotSame("No Forecast Available", WeatherParser.getForecast(ZIP));
	}
	
	public void testLocation() throws ParserConfigurationException, SAXException, IOException {
		assertEquals("College Park, MD", WeatherParser.getLocation(ZIP));
	}
}
