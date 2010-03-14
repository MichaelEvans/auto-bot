package autobot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.*;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.appengine.repackaged.org.apache.commons.logging.LogFactory;

public class WeatherParser{
	private static Logger log = Logger.getLogger(WeatherParser.class.getName());
	
	public static Document getDocument(String uri) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dBF;
		DocumentBuilder builder;
		Document doc;

		log.log(Level.INFO, "Building document from uri: " + uri);
		
		dBF = DocumentBuilderFactory.newInstance();
		dBF.setNamespaceAware(true);
		dBF.setValidating(true);
		builder = dBF.newDocumentBuilder();
		doc = builder.parse(uri);

		return doc;
	}

	/*public static String getImage(int Zip) throws IOException{
		URL url = new URL("http://weather.yahooapis.com/forecastrss?p=" + Zip);
		InputStream input = url.openStream();
		BufferedReader in = new BufferedReader (new InputStreamReader(input));
		String info = "";
		int startLine = 31;
		int endLine = 22;
		// read prior junk
		for (int i = 0; i < startLine; i++) { info = in.readLine(); }
		String image = info;

		info = in.readLine();
		String weather = in.readLine();
		info = in.readLine();
		String forecast = in.readLine();



		image = image.substring(image.indexOf("\"")+1, image.lastIndexOf("\""));
		System.out.println(image);
		String current = weather.substring(0, weather.lastIndexOf("<"));
		System.out.println(current);
		forecast = forecast.substring(0, forecast.lastIndexOf("<"));
		System.out.println(forecast);
		//System.out.println(info);
		/*for (int i = startLine; i < endLine + 1; i++) {
		        info = in.readLine();
		        System.out.println(info);
		    }//

		in.close();
		return image;
	}*/

	public static String getImage(String zip) throws ParserConfigurationException, SAXException, IOException {
		String yahooAPILoc = "http://weather.yahooapis.com/forecastrss?p=";
		Document yahooDoc;
		Node imageNode;
		NodeList imageNodeChildren;

		log.log(Level.INFO, "Locating image url");
		
		yahooDoc = getDocument(yahooAPILoc + zip);
		imageNode = yahooDoc.getElementsByTagName("image").item(0);
		imageNodeChildren = imageNode.getChildNodes();

		for (int i = 0; i < imageNodeChildren.getLength(); ++i) {
			Node child;

			child = imageNodeChildren.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals("url")) {
					log.log(Level.INFO, "Image url found: " + child.getNodeValue());
					
					return child.getNodeValue();
				}
			}
		}

		log.log(Level.INFO, "Unable to find image url");
		return "";
	}

	/*public static String getTemp(int Zip) throws IOException{
		URL url = new URL("http://weather.yahooapis.com/forecastrss?p=" + Zip);
		InputStream input = url.openStream();
		BufferedReader in = new BufferedReader (new InputStreamReader(input));
		String info = "";
		int startLine = 31;
		int endLine = 22;
		// read prior junk
		for (int i = 0; i < startLine; i++) { info = in.readLine(); }
		String image = info;

		info = in.readLine();
		String weather = in.readLine();
		info = in.readLine();
		String forecast = in.readLine();



		image = image.substring(image.indexOf("\"")+1, image.lastIndexOf("\""));
		System.out.println(image);
		String current = weather.substring(0, weather.lastIndexOf("<"));
		System.out.println(current);
		forecast = forecast.substring(0, forecast.lastIndexOf("<"));
		System.out.println(forecast);
		//System.out.println(info);
		/*for (int i = startLine; i < endLine + 1; i++) {
		        info = in.readLine();
		        System.out.println(info);
		    }//

		in.close();
		return current;
	}*/

	public static String getTemp(String zip) throws ParserConfigurationException, SAXException, IOException {
		String yahooAPILoc = "http://weather.yahooapis.com/forecastrss?p=";
		Document yahooDoc;
		Node conditionsNode;

		log.log(Level.INFO, "Getting temperature");
		
		yahooDoc = getDocument(yahooAPILoc + zip);
		conditionsNode = yahooDoc.getElementsByTagNameNS("*", "condition").item(0);

		log.log(Level.INFO, "Got temp: " + ((Element)conditionsNode).getAttribute("temp"));
		return ((Element)conditionsNode).getAttribute("temp");
	}

	/*public static String getForecast(int Zip) throws IOException{
		URL url = new URL("http://weather.yahooapis.com/forecastrss?p=" + Zip);
		InputStream input = url.openStream();
		BufferedReader in = new BufferedReader (new InputStreamReader(input));
		String info = "";
		int startLine = 31;
		int endLine = 22;
		// read prior junk
		for (int i = 0; i < startLine; i++) { info = in.readLine(); }
		String image = info;

		info = in.readLine();
		String weather = in.readLine();
		info = in.readLine();
		String forecast = in.readLine();



		image = image.substring(image.indexOf("\"")+1, image.lastIndexOf("\""));
		System.out.println(image);
		String current = weather.substring(0, weather.lastIndexOf("<"));
		System.out.println(current);
		forecast = forecast.substring(0, forecast.lastIndexOf("<"));
		System.out.println(forecast);
		//System.out.println(info);
		/*for (int i = startLine; i < endLine + 1; i++) {
		        info = in.readLine();
		        System.out.println(info);
		    }//

		in.close();
		return forecast;
	}*/

	public static String getForecast(String zip) throws ParserConfigurationException, SAXException, IOException {
		String yahooAPILoc = "http://weather.yahooapis.com/forecastrss?p=";
		Document yahooDoc;
		Node forecast;
		NodeList forecastNodes;
		String ret;

		log.log(Level.INFO, "Constructing forecast");
		
		ret = "No Forecast Available";
		yahooDoc = getDocument(yahooAPILoc + zip);
		forecastNodes = yahooDoc.getElementsByTagNameNS("*", "forcast");

		for (int i = 0; i < forecastNodes.getLength(); ++i) {
			forecast = forecastNodes.item(i);

			if (forecast.getNodeType() == Node.ELEMENT_NODE) {
				if (forecast.getNodeName().equals("forecast")) {
					ret += ((Element)forecast).getAttribute("day");
					ret += " - ";
					ret += ((Element)forecast).getAttribute("text");
					ret += ". High: ";
					ret += ((Element)forecast).getAttribute("high");
					ret += " Low: ";
					ret += ((Element)forecast).getAttribute("low");
					ret += "\n";
				}
			}
		}
		
		log.log(Level.INFO, "Got forcast: " + ret);

		return ret;
	}



	/*public static String getLocation(int Zip) throws IOException{
		URL url = new URL("http://weather.yahooapis.com/forecastrss?p=" + Zip);
		InputStream input = url.openStream();
		BufferedReader in = new BufferedReader (new InputStreamReader(input));
		String info = "";
		int startLine = 24;
		for (int i = 0; i < startLine; i++) { info = in.readLine(); }
		in.close();
		return info.substring(info.indexOf(">")+1, info.lastIndexOf("<"));
	}*/

	public static String getLocation(String zip) throws ParserConfigurationException, SAXException, IOException {
		String yahooAPILoc = "http://weather.yahooapis.com/forecastrss?p=";
		Document yahooDoc;
		Node locationNode;

		log.log(Level.INFO, "Determining location");
		
		yahooDoc = getDocument(yahooAPILoc + zip);
		locationNode = yahooDoc.getElementsByTagName("title").item(0);


		if (locationNode.getNodeType() == Node.ELEMENT_NODE) {
			if (locationNode.getNodeName().equals("title")) {
				log.log(Level.INFO, "Found location: " + locationNode.getNodeValue());
				return locationNode.getNodeValue();
			}
		}

		return "No Location Available";
	}
}