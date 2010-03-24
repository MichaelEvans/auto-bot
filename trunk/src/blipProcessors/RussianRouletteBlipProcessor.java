package blipProcessors;

import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import autobot.Auto_BotServlet;
import waveutils.Utils;
import stats.*;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class RussianRouletteBlipProcessor implements IBlipProcessor {
	public final static String NAME = "russian-roulette";
	
	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {
		Random generator = new Random();
		int drop = generator.nextInt(2);
		Object participant;
		
		if (drop == 0)
			participant = blip.getCreator();
		else { 
			drop = generator.nextInt(wavelet.getParticipants().size());
			participant = wavelet.getParticipants().toArray()[drop];
		}

                if (participant.toString().equals("dforsyth@googlewave.com"))
                        return (wavelet);
		
		// TODO FIX THIS
		Auto_BotServlet.log.log(Level.INFO, "Kicking " + participant + " out of the wave.");
		try {
			Utils.appendLineToBlip(blip, "\nThanks for transforming " + participant + ".");
			wavelet.getParticipants().remove(participant);
		}
		catch (UnsupportedOperationException e) {
			WaveStats ws = (WaveStats)dataMap.get("WaveStats");
			if (ws.getMuted() == null || ws.getMuted().equals(""))
				ws.setMuted(participant.toString());
		}
		
		return wavelet;
	}

}
