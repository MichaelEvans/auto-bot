package stats;

import com.google.appengine.api.datastore.Key;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserStats {
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
	@Persistent
	private String Name;
	
	@Persistent
	private int BlipCount = 0;
	
	@Persistent
	private int EditCount = 0;
	
	@Persistent
	private int DeleteCount = 0;
	
	@Persistent
	private Integer linkCount = 0;
	
	
	public UserStats(String Name){
		this.Name=Name;
		this.BlipCount = 0;
		this.EditCount = 0;
		this.DeleteCount = 0;
	}
	
	public String getName(){
		return Name;
	}
	
	public void setName(String Name){
		this.Name=Name;
	}
	
	public int getBlipCount(){
		return BlipCount;
	}
	
	public void incrementBlipCount(){
		BlipCount++;
	}
	
	public int getEditCount(){
		return EditCount;
	}
	
	public void incrementEditCount(){
		this.EditCount++;
	}
	
	public int getDeleteCount(){
		return DeleteCount;
	}
	
	public void incrementDeleteCount(){
		this.DeleteCount++;
	}
	
	public void incrementLinkCount() {
		linkCount++;
	}
	
	public int getLinkCount() {
		return linkCount;
	}
	
	public int getActionsCount() {
		return BlipCount + EditCount + DeleteCount;
	}

	public Key getKey() {
		return key;
	}

}
