package stats;
import java.util.*;

public class Wave {
	
	private String Title;
	private int BlipCount = 0;
	private int EditCount = 0;
	private int DeleteCount = 0;
	
	private ArrayList<User> Users;
	
	public Wave(Wave wave){
		this.Title = wave.Title;
		this.BlipCount = wave.BlipCount;
		this.EditCount = wave.EditCount;
		this.DeleteCount = wave.DeleteCount;
	}
	
	public Wave(String Title,ArrayList<User> Users){
		this.Title = Title;
		this.Users.addAll(Users);
	}
	
	public Wave(String Title){
		this.Title = Title;
		this.Users = new ArrayList<User>();
	}
	
	public Wave(){
		this.Title = "DefaultTitle";
		this.Users = new ArrayList<User>();
	}
	
	public String getTitle(){
		return Title;
	}

	public int getBlipCount(){
		return BlipCount;
	}
	
	public void setBlipCount(int newBlipCount){
		BlipCount = newBlipCount;
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
		EditCount=newEditCount;
	}
	
	public void incrementEditCount(){
		EditCount++;
	}
	
	public void incrementEditCount(int numEdits){
		EditCount+=numEdits;
	}
	
	public void setDeleteCount(int newDeleteCount){
		if(newDeleteCount < 0)
			newDeleteCount = 0;
		DeleteCount=newDeleteCount;
	}
	
	public void incrementDeleteCount(){
		DeleteCount++;
	}
	
	public void incrementDeleteCount(int numDeletes){
		DeleteCount+=numDeletes;
	}
	
	public int getNumberOfUsers(){
		return Users.size();
	}
}
