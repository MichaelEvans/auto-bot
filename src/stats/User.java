package Stats;

public class User {
	
	private String Name;
	private int BlipCount = 0;
	private int EditCount = 0;
	private int DeleteCount = 0;
	private WordCount WordCounts;
	
	
	public User(){
		this.Name="DefaultUser";
	}
	
	public User(String Name){
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
	
	public void updateFrequency(String message){
		WordCounts.addMessage(message);
		
	}
	public String getNextFreqentWord(){
		return "getNextFreqentWord Not Implemented";
	}

}
