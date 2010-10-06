package stats;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ImgTag {

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
    @Persistent
    private String tag;
    
    @Persistent
    private List<Key> urls;
    
    public ImgTag(String tag) {
    	this.tag = tag;
    	this.urls = new ArrayList<Key>();
    }
    
    public ImgTag(String tag, Key url) {
    	this(tag);
    	urls.add(url);
    }
    
    public void addUrl(Key url) {
    	if (!urls.contains(url))
    		urls.add(url);
    }
    
    public List<Key> getUrls() {
    	return urls;
    }
    
    public String getTag() {
    	return tag;
    }
}
