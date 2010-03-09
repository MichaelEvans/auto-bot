package blipProcessors;

import java.util.Map;
import java.util.HashSet;

import waveutils.Utils;

//import autobot.Auto_BotProfileServlet;
import autobot.Auto_BotServlet;

import com.google.wave.api.Blip;
import com.google.wave.api.Element;
import com.google.wave.api.Image;
import com.google.wave.api.Wavelet;

public class LikeThisBlipProcessor implements IBlipProcessor {
	public final static String NAME = "like";
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet, Map<String, Object> dataMap) {
		String likeStr = " " + blip.getCreator() + " likes this blip!";
		blip.all().delete();
		blip.append("\n");
		Element like = new Image("https://wiki.endoftheinter.net/images/4/44/Like.png", 15, 15, "");
		blip.append(like);
		Utils.appendToBlip(blip, likeStr);
		
		return wavelet;
	}

}
