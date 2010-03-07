package stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

/* WordCount is a container for a TreeMap of Words. It allows us to
 * add messages, splitting them into Words, and adding them to the tree.
 */

public class WordCount {

	private TreeMap<String,Word> Words;
	private int mostFreqArrPos = 0;
	
	static class WordComparator implements Comparator<Word> {
		public int compare(Word word1, Word word2) {
	         return word2.count - word1.count;
		}
		
	}
	
	public WordCount(){
		Words= new TreeMap<String,Word>();
	}
	
	public WordCount(TreeMap<String,Word> map){
		Iterator<String> it=map.keySet().iterator();
		
		while(it.hasNext()){
			String str = it.next();
			if(Words.containsKey(str)){
				Word word = Words.get(str);
				word.count+= map.get(str).count;
				Words.put(str,word);
			}
		}	
	}


	public void addMessage(String message){
		StringTokenizer t = new StringTokenizer(message);
		while(t.hasMoreTokens()){
			String word = t.nextToken();
			if(word.length()>3){
				word=word.toLowerCase();
				Word value = Words.get(word);
				if(value == null)
					value = new Word(word,0);
				value.count++;
				
				Words.put(word, value);
			}
		}
	}
	
	public Word getMostFrequent(){
		if(Words.isEmpty())
			return new Word("WordTreeMapIsEmpty",0);
		
		ArrayList<Word> frequency = new ArrayList<Word>(Words.values());
		Collections.sort(frequency, new WordComparator());
		mostFreqArrPos = 0;
		return frequency.get(mostFreqArrPos);
		
	}
	
	public Word getFreqencyPos(int pos){
		ArrayList<Word> frequency = new ArrayList<Word>(Words.values());
		Collections.sort(frequency, new WordComparator());
		return frequency.get(mostFreqArrPos);
	}
	
}
