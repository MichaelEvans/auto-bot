package blipProcessors;

import java.util.Map;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class RunTestsBlipProcessor implements IBlipProcessor {
	static String RUN_TESTS = "run-tests";
	private static String ACKNOWLEDGE_TEXT = "Running Auto-Bot Tests\n" +
		"Will append blips in groups of 50, getting wave stats after each group," +
		"and will compare these results to the expected results.";
		
	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {
		wavelet.reply(ACKNOWLEDGE_TEXT);
		//acknowledge.getDocument().append(ACKNOWLEDGE_TEXT);
		
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 50; ++j) {
				wavelet.reply(String.valueOf(j));
			}
		}
		
		return wavelet;
	}

}
