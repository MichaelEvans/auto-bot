package stats;

import com.google.appengine.api.datastore.Key;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.List;
import java.util.ArrayList;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class WaveStats {
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
    @Persistent
    private String waveID;

    @Persistent
    private int blips;
    
    @Persistent
    private ArrayList<UserStats> users;

    /** Constructor for a wave with a specified number of blips.
     * 
     * @param waveID Wave ID
     * @param blips Number of blips to start the count at
     */
    public WaveStats(String waveID, int blips) {
        this.waveID = waveID;
        this.blips = blips;
        this.users = new ArrayList<UserStats>();
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
		return blips;
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
}