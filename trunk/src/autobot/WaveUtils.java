package autobot;

import java.util.logging.Level;

import com.google.wave.api.Wavelet;

public class WaveUtils {
	public static Wavelet makeNewWavelet(Wavelet wavelet) {
		final String NEW_WAVE_INDICATOR = "We're rolling out!";
		
		Auto_BotServlet.log.log(Level.INFO, "Creating new wave");
		
		wavelet.appendBlip().getDocument().append(NEW_WAVE_INDICATOR);
		
		String title = getNewTitle(wavelet);
		Wavelet newWave = wavelet.createWavelet(wavelet.getParticipants(), "ID");
		newWave.setTitle(title);
		
		Auto_BotServlet.log.log(Level.INFO, "Created new wave: " + title);
		
		return newWave;
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
