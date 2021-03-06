package NGramSet;

import java.util.HashMap;
import java.util.List;

public interface Alliteration {
	public static boolean STRICT = false;
	
	String processWord(String word);
	
	void popFirstWord();

	int consume(HashMap<String, List<Alliteration>> map);
	
	void setDocument(List<String> words);
	
	int getCountOfWord(String key);
//	public List<String> getWordList();
	public List<String> getModifiedWordList();
	
	public int getMinSize();
	public int getMaxSize();
	
	int size();
	
	String toString();
	
	boolean hasMatches();
	boolean isStopWord(String word);
	
	List<String> getDocument();

	int getPosition();
//	int hashCode();
}