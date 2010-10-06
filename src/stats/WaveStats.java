package stats;

import autobot.Auto_BotServlet;

import com.google.appengine.api.datastore.Key;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class WaveStats {
	
	public static final Logger log = Logger.getLogger(WaveStats.class.getName());
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
    @Persistent
    private String waveID;

    @Persistent
    private int blips;
    
    //@Persistent
    //public int bomb = -1;
    
    @Persistent
    private String nextWaveID;
    
    @Persistent
    private String nextWaveletID;
    
    @Persistent
    private String muted;
    
    @Persistent
    private List<String> links;
    
    @Persistent
    private List<UserStats> users;
    
    @Persistent
    private List<String> keywords;
    
    @Persistent
    private List<String> blipIDs;
    
    @Persistent
    private List<WordBag> wordBags;
    
    @Persistent
    private Long startTime;
    
    @Persistent
    private Long endTime = null;

    /** Constructor for a wave with a specified number of blips.
     * 
     * @param waveID Wave ID
     * @param blips Number of blips to start the count at
     */
    public WaveStats(String waveID, int blips) {
        this.waveID = waveID;
        this.blips = blips;
        this.users = new ArrayList<UserStats>();
      //  this.keywords = new ArrayList<String>();
      //  this.wordBags = new ArrayList<WordBag>();
        this.blipIDs = new ArrayList<String>();
        this.links = new ArrayList<String>();
        this.startTime = System.currentTimeMillis();
    }
    
    /** Constructor for new wave with no blips yet
     *  
     * @param waveID Wave ID
     */
    public WaveStats(String waveID) {
    	this(waveID, 0);
    }

    /**
     * Getter for the Datastore key associated with his object.
     * 
     * @return Datastore key
     */
	public Key getKey() {
		return key;
	}

	/**
	 * Getter for the WaveID associated with this object.
	 * 
	 * @return WaveID
	 */
	public String getWaveID() {
		return waveID;
	}

	/** Setter for the WaveID associated with this object.
	 * 
	 * @param waveID WaveID of the wave.
	 */
	public void setWaveID(String waveID) {
		this.waveID = waveID;
	}

	/**
	 * Getter for the number of blips associated with this object.
	 * 
	 * @return Number of blip submissions in the wave.
	 */
	public int getBlips() {
		return (blipIDs != null) ? getBlipCount() : blips;
	}

	/** Setter for the number of blips associated with this object.
	 * 
	 * @param blips Number of blip submissions
	 */
	public void setBlips(int blips) {
		this.blips = blips;
	}
	
	public UserStats getUser(String name){
		for (UserStats us : users)
			if (us.getName().equals(name))
				return us;
		
		return null;
	}
	
	public void addUser(UserStats name) {
		users.add(name);
	}
	
	public List<UserStats> getUsers() {
		return users;
	}
	
	public boolean addBlip(String blipID) {
		if (blipIDs != null && !blipIDs.contains(blipID)) {
			log.log(Level.INFO, "[WS] Adding a blip to list.");
			blipIDs.add(blipID);
			blips++;
			return true;
		}
		return false;
	}
	
	public boolean removeBlip(String blipID) {
		if (blipIDs != null) {
			blipIDs.remove(blipID);
			blips--;
			return true;
		}
		return false;
	}
	
	public boolean addLink(String link) {
		if (links != null && !links.contains(link)) {
			log.log(Level.INFO, "[WS] Adding a link.");
			links.add(link);
			return true;
		}
		return false;
	}
	
	public String getLinks() {
		if (links == null || links.size() == 0)
			return "No links in this wave!";
		else {
			String ret = "";
			for (String link : links)
				ret += "- " + link + "\n";
			
			return ret;
			
		}
	}
	
	public int getLinkCount() {
		return (links == null) ? 0 : links.size();
	}
	
	public int getBlipCount() {
		log.log(Level.INFO, "[WS] Getting blip count via list.");
		return blipIDs.size();
	}
	
	/*public void fillWordBags(String s) {
		String[] allWords = s.replace("\n", " ").split(" ");
		int i = 0;
		
		for (i = 0; i < allWords.length-1; i++) {
			String keyword = allWords[i].trim();
			String nextWord = allWords[i+1].trim();
			int keywordIdx = keywords.indexOf(keyword);
			if (keywordIdx != -1) {
				wordBags.get(keywordIdx).add(nextWord);
			}
			else {
				WordBag temp = new WordBag();
				temp.add(nextWord);
				keywords.add(keyword);
				wordBags.add(temp);
			}
		}
	}
	
	public String doMarkov(int times) {
		int idx, j, count;
		Random r;
		StringBuilder ret;
		WordBag temp;
		String next;
		
		idx = -1;
		r = new Random();
		idx = (idx != -1) ? idx : (r.nextInt(((int)(keywords.size() * 2.2))) % keywords.size());
		ret = new StringBuilder();
		j = 0;
		while(true) {
			temp = wordBags.get(idx);
			count = temp.count();
			next = temp.getWord(r.nextInt(((int)(count * 2.2))) % count);
			ret.append(next).append(" ");
			idx = keywords.indexOf(next);
			idx = (idx != -1) ? idx : (r.nextInt(((int)(count * 2.2))) % keywords.size());
			j++;
			if ((next.contains(".") && j > times) || (j > (2 * times)))
				break;
		}
		return ret.toString();
	}*/

	public String getNextWaveID() {
		return nextWaveID;
	}

	public void setNextWaveID(String nextWaveID) {
		this.nextWaveID = nextWaveID;
	}

	public String getNextWaveletID() {
		return nextWaveletID;
	}

	public void setNextWaveletID(String nextWaveletID) {
		this.nextWaveletID = nextWaveletID;
	}

	public String getMuted() {
		return muted;
	}

	public void setMuted(String muted) {
		this.muted = muted;
	}
	
	public void clearWordBag() {
		if (keywords != null)
			keywords.clear();
		if (wordBags != null)
			wordBags.clear();
	}
	
	public void setEndTime() {
		this.endTime = System.currentTimeMillis();
	}
	
	public long totalLength() {
		return (endTime != null) ? endTime - startTime : -1;
	}
	
	
}