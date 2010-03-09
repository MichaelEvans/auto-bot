package stats;

import java.util.Set;
import java.util.TreeSet;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

import java.util.List;
import java.util.ArrayList;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class WordBag {
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

	@Persistent
	private List<String> words;
	
	@Persistent
	private List<Integer> counts;
	
	public WordBag() {
		this.words = new ArrayList<String>();
		this.counts = new ArrayList<Integer>();
	}
	
	public boolean equals(Object o) {
		if (o == null)
			return false;
		return true;
	}

	public Key getKey() {
		return key;
	}
	
	public void add(String word) {
		int idx = words.indexOf(word);
		if (idx == -1) {
			words.add(word);
			counts.add(1);
		}
		else {
			counts.add(idx, counts.get(idx));
			counts.remove(idx+1);
		}
	}
}
