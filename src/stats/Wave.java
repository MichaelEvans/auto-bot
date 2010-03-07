package stats;
import java.util.*;

public class Wave {
	
	private String Title;
	private int BlipCount = 0;
	private int EditCount = 0;
	private int DeleteCount = 0;
	
	private TreeMap<String,User> Users;
	
	public Wave(Wave wave){
		this.Title = wave.Title;
		this.BlipCount = wave.BlipCount;
		this.EditCount = wave.EditCount;
		this.DeleteCount = wave.DeleteCount;
	}
	
	public Wave(String Title,TreeMap<String,User> Users){
		this.Title = Title;
		this.Users.putAll(Users);
	}
	
	public Wave(String Title){
		this.Title = Title;
		this.Users = new TreeMap<String,User>();
	}
	
	public Wave(){
		this.Title = "DefaultTitle";
		this.Users = new TreeMap<String,User>();
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
	
	public User getUser(String name){
		return Users.get(name);
	}
	
	public void addUser(User user){
		Users.put(user.getName(),user);
	}
	
	public void updateBlipTotalsFromUsers(){
		int updatedBlipCount=0;
		for(User u: Users.values()){
			updatedBlipCount+=u.getBlipCount();			
		}
		BlipCount=updatedBlipCount;
	}
	
	public void updateDeleteTotalsFromUsers(){
		int updatedDeleteCount=0;
		for(User u: Users.values()){
			updatedDeleteCount+=u.getDeleteCount();			
		}
		DeleteCount=updatedDeleteCount;
	}
	
	public void updateEditTotalsFromUsers(){
		int updatedEditCount=0;
		for(User u: Users.values()){
			updatedEditCount+=u.getEditCount();			
		}
		EditCount=updatedEditCount;
	}
	
	public void updateStatTotalsFromUsers(){
		updateBlipTotalsFromUsers();
		updateDeleteTotalsFromUsers();
		updateEditTotalsFromUsers();

	}
	
}
