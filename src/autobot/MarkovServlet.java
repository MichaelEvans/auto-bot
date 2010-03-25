package autobot;

import java.io.IOException;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;
import java.util.List;
import java.util.Map;

import org.waveprotocol.wave.model.id.*;

import stats.WaveStats;

import com.google.wave.api.*;
import com.google.wave.api.event.OperationErrorEvent;

@SuppressWarnings("serial")
public class MarkovServlet extends HttpServlet {
	public static final Logger log = Logger.getLogger(MarkovServlet.class.getName());
	private PersistenceManager pm;

	Map<String, WaveStats> waveStatsMap;

	private void makeBlipsMap(String id) {
		waveStatsMap = new HashMap<String, WaveStats>();
		List<WaveStats> tempList;
		String query = "select from " + stats.WaveStats.class.getName() + " WHERE waveID == '" + id + "'";
		
		if (pm == null) {
			log.log(Level.INFO,"pm is null");
		}
		else if (query == null) {
			log.log(Level.INFO, "query is null");

		}
		else {
		//	log.log(Level.INFO, "AUTO-BOT: Creating blips map.");
			
			tempList = (List<WaveStats>) pm.newQuery(query).execute();
			for (WaveStats ws : tempList) {
				waveStatsMap.put(ws.getWaveID(), ws);
			}
		}		
	}
	
	private void openPM() {
		try {
			pm = PMF.get().getPersistenceManager();
		}
		catch (Exception e) {
			log.log(Level.INFO, "Error opening PersistenceManager");
		}
		//makeBlipsMap();
	}
	
	private void closePM() {
		try {
			pm.close();
		}
		catch (Exception e) {
			log.log(Level.INFO, "Error closing PersistenceManager");
		}
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
		log.log(Level.INFO, "Markov data for: " + req.getParameter("waveID") + "is: " + req.getParameter("text") + "action: " + req.getParameter("action"));
		String id = req.getParameter("waveID");
		
		openPM();
		makeBlipsMap(id);
		WaveStats waveStats = waveStatsMap.get(id);
		if ((req.getParameter("action") != null && !req.getParameter("action").equals("clear")) || req.getParameter("action") == null) {
			waveStats.fillWordBags(req.getParameter("text"));
			log.log(Level.INFO, "Filling bag");
		}
		else
			waveStats.clearWordBag();
		closePM();
	}
}
