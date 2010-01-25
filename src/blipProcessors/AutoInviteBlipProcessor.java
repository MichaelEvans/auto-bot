package blipProcessors;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class AutoInviteBlipProcessor implements BlipProcessor {
	public final static String AUTO_INVITE = "auto-invite";
	public final static String AUTO_INVITE_ADD = "auto-invite-add:";
	public final static String AUTO_INVITE_REMOVE = "auto-invite-remove:";
	
	final static Pattern autoInviteAddPattern = Pattern.compile(CMD_OPEN_IDENT + AUTO_INVITE_ADD + "(.+)" + CMD_CLOSE_IDENT);
	final static Pattern autoInviteRemovePattern = Pattern.compile(CMD_OPEN_IDENT + AUTO_INVITE_REMOVE + "(.+)" + CMD_CLOSE_IDENT);

	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {
		Set<String> privelegedWavers = (HashSet<String>)dataMap.get("privelegedWavers");
		
		for (String usr : privelegedWavers) {
			wavelet.addParticipant(usr);
		}
		
		return wavelet;
	}

}
