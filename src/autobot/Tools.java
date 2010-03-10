package autobot;

import stats.WaveStats;

import com.google.wave.api.Wavelet;

public class Tools {
	
	public static String newTitle(Wavelet wavelet) {
		final String CONT_IDENT = " // Part ";
		
		int index;
		String title, waveBaseTitle = wavelet.getTitle();
		
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
	
	public static String newTitle(WaveStats ws) {
		String start = "Wave of " + ws.doMarkov(10);
		return start;
	}
}
