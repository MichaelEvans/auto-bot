package autobot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;

import org.w3c.dom.*;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLParser{
	
	public static String getImage(int Zip) throws IOException{
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
		    }*/

		in.close();
		return image;
	}
	public static String getTemp(int Zip) throws IOException{
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
		    }*/

		in.close();
		return current;
	}
	public static String getForecast(int Zip) throws IOException{
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
		    }*/

		in.close();
		return forecast;
	}
	public static String getLocation(int Zip) throws IOException{
		URL url = new URL("http://weather.yahooapis.com/forecastrss?p=" + Zip);
		InputStream input = url.openStream();
		BufferedReader in = new BufferedReader (new InputStreamReader(input));
		String info = "";
		int startLine = 24;
		for (int i = 0; i < startLine; i++) { info = in.readLine(); }
		in.close();
		return info.substring(info.indexOf(">")+1, info.lastIndexOf("<"));
	}
	/*public static void main(String[] args){
		try {
			System.out.println(XMLParser.getLocation(20740));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}