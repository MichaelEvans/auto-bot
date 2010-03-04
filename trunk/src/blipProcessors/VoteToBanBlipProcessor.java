package blipProcessors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.wave.api.Blip;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

public class VoteToBanBlipProcessor implements IBlipProcessor {
	final static Logger log = Logger.getLogger(VoteNewWaveBlipProcessor.class.getName());

	public final static String VOTE_TO_BAN = "vote-to-ban:";
	public final static String VOTE_TO_UNBAN = "vote-to-unban:";

	private final static Pattern voteToBanPattern = Pattern.compile(CMD_OPEN_IDENT + VOTE_TO_BAN + "(.+)" + CMD_CLOSE_IDENT);
	private final static Pattern voteToUnbanPattern = Pattern.compile(CMD_OPEN_IDENT + VOTE_TO_UNBAN + "(.+)" + CMD_CLOSE_IDENT);

	private Map<String, Set<String>> banMap = new HashMap<String, Set<String>>();
	private Map<String, Long> cantBan = new HashMap<String, Long>();
	private Set<String> areBanned = new HashSet<String>();


	public Wavelet processBlip(Blip blip, Wavelet wavelet,
			Map<String, Object> dataMap) {
		TextView rootBlipDoc;
		
		String blipAuthor = blip.getCreator();
		rootBlipDoc = wavelet.getRootBlip().getDocument();
		
		if (((String)dataMap.get("banType")).contains("ban")) {
			if (cantBan.containsKey(blipAuthor)) {
				// Allow if user has been baned for longer than 10 minues
				if (cantBan.get(blipAuthor) < System.currentTimeMillis()) {
					blip.getDocument().append("Message from Auto-Bot: Your ban vote-to-ban privileges have been temporarily revoked.");

					return wavelet;
				} else {
					cantBan.remove(blipAuthor);
				}
			}

			Matcher mtchr = voteToBanPattern.matcher(blip.getDocument().getText());
			String usr;

			mtchr.lookingAt();

			try{
				usr = mtchr.group(1);

				if (usr.substring(0, usr.indexOf("@googlewave.com")).equalsIgnoreCase(blipAuthor)) {
					//cantBan.put(blipAuthor, System.currentTimeMillis() + 10*60*1000);

					return wavelet;
				}

				if (!rootBlipDoc.getText().contains("Motions to Ban")) {
					;
				}

				if (wavelet != null) {
					wavelet.getRootBlip().getDocument().append("\n" + blipAuthor + " motions to ban " + usr + ".");
				}

				if (!wavelet.getParticipants().contains(usr)) {
					throw new IllegalStateException();
				}

				if (banMap.containsKey(usr)) {
					banMap.get(usr).add(blipAuthor);
				} else {
					banMap.put(usr, new HashSet());
					banMap.get(usr).add(blipAuthor);
				}

				if (banMap.get(usr).size() >= ((2/3) * (Integer)dataMap.get("numberOfActiveWavers"))) {
					String message = "Motion to ban " + usr + " passed with " + banMap.get(usr).size() + " votes. Removing this user.";
					log.info(message);
					wavelet.getRootBlip().getDocument().append("\n" + message);
					wavelet.removeParticipant(usr);
				}
			}catch (IllegalStateException e) {
				wavelet.getRootBlip().getDocument().append("\n" + blipAuthor + " loses their ban vote privileges.");

				cantBan.put(blipAuthor, System.currentTimeMillis() + 10*60*1000);
			}catch (IndexOutOfBoundsException e) {
				//not this many matches
				String message = "Incorrect command form. Correct form is !@vote-to-ban:<user>@googlewave.com@!";
				wavelet.getRootBlip().getDocument().append("\n" + message);
			}
		} else {
			/* The following if block can be removed once removeParticipant() works */
			if (cantBan.containsKey(blipAuthor)) {
				if (cantBan.get(blipAuthor) + 10*60*1000 < System.currentTimeMillis()) {
					return wavelet;
				} else {
					cantBan.remove(blipAuthor);
				}
			}

			Matcher mtchr = voteToUnbanPattern.matcher(blip.getDocument().getText());
			String usr;

			mtchr.lookingAt();

			try{
				usr = mtchr.group(1);

				if (!rootBlipDoc.getText().contains("Motions to Unban")) {
					;
				}

				if (wavelet != null) {
					wavelet.getRootBlip().getDocument().append("\n" + blipAuthor + " motions to unban " + usr + ".");
				}

				if (!wavelet.getParticipants().contains(usr)) {
					throw new IllegalStateException();
				}

				if (banMap.containsKey(usr)) {
					banMap.get(usr).add(blipAuthor);
				} else {
					banMap.put(usr, new HashSet<String>());
					banMap.get(usr).add(blipAuthor);
				}

				if (banMap.get(usr).size() >= ((2/3) * (Integer)dataMap.get("numberOfActiveWavers"))) {
					String message = "Motion to unban " + usr + " passed with " + banMap.get(usr).size() + " votes. Unbanning this user.";
					log.info(message);
					wavelet.getRootBlip().getDocument().append("\n" + message);
					wavelet.addParticipant(usr);
					banMap.remove(usr);
					areBanned.remove(usr);
				}
			}catch (IllegalStateException e) {
				wavelet.getRootBlip().getDocument().append("\n" + blipAuthor + " loses their unban vote privileges.");

				cantBan.put(blipAuthor, System.currentTimeMillis());
			}catch (IndexOutOfBoundsException e) {
				//not this many matches
				String message = "Usage: !@vote-to-unban:user@googlewave.com@!";
				wavelet.getRootBlip().getDocument().append("\n" + message);
			}
		}

		return wavelet;
	}

}
