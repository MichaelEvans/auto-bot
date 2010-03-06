package stats;

import com.google.appengine.api.datastore.Key;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class WaveStats implements Comparable {
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
    @Persistent
    private String waveID;

    @Persistent
    private int blips;

    public WaveStats(String waveID, int blips) {
        this.waveID = waveID;
        this.blips = blips;
    }

	public Key getKey() {
		return key;
	}

	public String getWaveID() {
		return waveID;
	}

	public void setWaveID(String waveID) {
		this.waveID = waveID;
	}

	public int getBlips() {
		return blips;
	}

	public void setBlips(int blips) {
		this.blips = blips;
	}

	public int compareTo(Object o) {
		return 1; //TODO Make this better
	}
    
}