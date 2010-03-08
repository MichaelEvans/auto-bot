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
	
	//private WordCount WordCounts;
	
	
	/*public UserStats(){
		this.Name="DefaultUser";
	}*/
	
	public UserStats(String Name){
		this.Name=Name;
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
	
	public void setBlipCount(int newBlipCount){
		if(newBlipCount<0)
			newBlipCount = 0;
		this.BlipCount=newBlipCount;
	}
	
	public void incrementBlipCount(){
		BlipCount++;
	}
	
	public void incrementBlipCount(int numBlips){
		BlipCount+=numBlips;
	}
	
	public int getEditCount(){
		return EditCount;
	}
	
	public void setEditCount(int newEditCount){
		if(newEditCount<0)
			newEditCount = 0;
		this.EditCount=newEditCount;
	}
	
	public void incrementEditCount(){
		this.EditCount++;
	}
	
	public void incrementEditCount(int numEdits){
		this.EditCount+=numEdits;
	}
	
	public int getDeleteCount(){
		return DeleteCount;
	}
	
	public void setDeleteCount(int newDeleteCount){
		if(newDeleteCount < 0)
			newDeleteCount = 0;
		this.DeleteCount=newDeleteCount;
	}
	
	public void incrementDeleteCount(){
		this.DeleteCount++;
	}
	
	public void incrementDeleteCount(int numDeletes){
		this.DeleteCount+=numDeletes;
	}
	
	//public void updateFrequency(String message){
	//	WordCounts.addMessage(message);
		
	//}
	public String getNextFreqentWord(){
		return "getNextFreqentWord Not Implemented";
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

}
