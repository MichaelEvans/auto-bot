package stats;

import java.util.HashSet;
import java.util.Set;

public abstract class WaverStats {
	String waverID, waverName;
	Set<String> blipsSet, topWordsSet;
	
	public WaverStats(String waverID, String waverName) {
		this.waverID = waverID;
		this.waverName = waverName;
		
		blipsSet = new HashSet<String>();
		topWordsSet = new HashSet<String>();
	}
	
	public void addBlip(String blipID) {
		blipsSet.add(blipID);
	}
	
	public void addTopWord(String word) {
		topWordsSet.add(word);
	}
	
	public String getWaverID() {
		return waverID;
	}

	public String getWaverName() {
		return waverName;
	}
	
	/**
	 * Returns a shallow copy of the blips set
	 * @return
	 */
	public Set<String> getBlipsSet() {
		Set<String> ret = new HashSet<String>();
		
		for (String s : blipsSet) {
			ret.add(s);
		}
		
		return ret;
	}
	
	/**
	 * Returns a shallow copy of the top words set
	 * @return
	 */
	public Set<String> getTopWordsSet() {
		Set<String> ret = new HashSet<String>();
		
		for (String s : topWordsSet) {
			ret.add(s);
		}
		
		return ret;
	}
}
