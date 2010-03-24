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
		dBF.setCoalescing(true);
		builder = dBF.newDocumentBuilder();
		doc = builder.parse(uri);

		return doc;
	}

	public static String getImage(String zip) throws ParserConfigurationException, SAXException, IOException {
		String yahooAPILoc = "http://weather.yahooapis.com/forecastrss?p=";
		Document yahooDoc;
		NodeList imageNodeList, imageNodeChildren;

		log.log(Level.INFO, "Locating image url");

		yahooDoc = getDocument(yahooAPILoc + zip);
		imageNodeList = yahooDoc.getElementsByTagNameNS("*", "description");


		for (int j = 0; j < imageNodeList.getLength(); ++j) {
			if (imageNodeList.item(j).getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			imageNodeChildren = imageNodeList.item(j).getChildNodes();
			for (int i = 0; i < imageNodeChildren.getLength(); ++i) {
				Node child;

				child = imageNodeChildren.item(i);
				if (child.getNodeType() == Node.TEXT_NODE) {
					// I don't feel like doing this correctly right now
					try {
						String ret =  child.getTextContent();
						ret = ret.substring(ret.indexOf("<img src=\"") + 10, ret.length());
						ret = ret.substring(0, ret.indexOf("\"/>"));

						return ret;
					} catch (Exception e) {
						continue;
					}
				}
			}
		}

		log.log(Level.INFO, "Unable to find image url");
		return "";
	}

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

	public static String getForecast(String zip) throws ParserConfigurationException, SAXException, IOException {
		String yahooAPILoc = "http://weather.yahooapis.com/forecastrss?p=";
		Document yahooDoc;
		Node forecast;
		NodeList forecastNodes;
		String ret;

		log.log(Level.INFO, "Constructing forecast");

		ret = null;
		yahooDoc = getDocument(yahooAPILoc + zip);
		forecastNodes = yahooDoc.getElementsByTagNameNS("*", "forecast");

		log.log(Level.INFO, "Found " + forecastNodes.getLength() + " forecast nodes");
		for (int i = 0; i < forecastNodes.getLength(); ++i) {
			forecast = forecastNodes.item(i);

			if (forecast.getNodeType() == Node.ELEMENT_NODE) {
				log.log(Level.INFO, "Found node of type ELEMENT, named " + 
						forecast.getLocalName());
				if (forecast.getLocalName().equals("forecast")) {
					log.log(Level.INFO, "Found forecast node");
					ret = ret == null ? "" : ret;
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

		return ret == null ? "No Forecast Available" : ret;
	}

	public static String getLocation(String zip) throws ParserConfigurationException, SAXException, IOException {
		String yahooAPILoc = "http://weather.yahooapis.com/forecastrss?p=";
		Document yahooDoc;
		NodeList locationNodes;

		log.log(Level.INFO, "Determining location");

		yahooDoc = getDocument(yahooAPILoc + zip);
		locationNodes = yahooDoc.getElementsByTagNameNS("*", "location");

		for (int i = 0; i < locationNodes.getLength(); ++i) {
			Node locationNode;

			locationNode = locationNodes.item(i);
			if (locationNode.getNodeType() == Node.ELEMENT_NODE) {
				log.log(Level.INFO, "Found location: " + ((Element)locationNode).getAttribute("city") +
						((Element)locationNode).getAttribute("region"));
				return ((Element)locationNode).getAttribute("city") + ", " + 
				((Element)locationNode).getAttribute("region");
			}
		}

		return "No Location Available";
	}
}