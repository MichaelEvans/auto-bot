package autobot;

import java.util.logging.Level;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class WaveUtils {
	
	public static void appendToBlip(Blip b, String s) {
		b.append("\n" + s);
	}
	
	public static void replaceBlip(Blip b, String s) {
		b.all().delete();
		b.append(s);
		//Auto_BotServlet.log.log(Level.INFO, "Replacing content to:" + b.getContent());
	}
	
	public static Blip reply(Wavelet w, String s) {
		return w.reply("\n" + s);
	}
	
	public static String getNewTitle(Wavelet wavelet) {
		final String CONT_IDENT = " // Part ";
		
		int index;
		String title, waveBaseTitle = wavelet.getTitle();;
		
		if (waveBaseTitle == null) {
			waveBaseTitle = "";
		}
		
		index = waveBaseTitle.indexOf(CONT_IDENT);
		if (index == -1) {
			title = waveBaseTitle + CONT_IDENT + "2";
		} else {
			int count = Integer.parseInt(waveBaseTitle.substring(index + CONT_IDENT.length()).trim());
			title = waveBaseTitle.substring(0,index) + CONT_IDENT + (count + 1);
		}
		
		return title;
	}
}
