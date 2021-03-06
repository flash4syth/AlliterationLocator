package NGramSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class AlliterationImpl implements Alliteration {
	protected static int minSize;
	protected static int maxSize;
	protected static boolean matchCase = false;
	protected static boolean useStopWords = true;
	protected static boolean useSrictMatching = true;
	
	protected int position = 0;
	
	protected List<String> document;
	
	protected List<String> words;
	protected List<String> modifiedWords;
	
	protected HashMap<String, Integer> wordCounts;
	protected HashMap<Alliteration, Integer> matches;
	
	public AlliterationImpl(int size) {initialize(size);}
	public AlliterationImpl(Alliteration other) {
		initialize(other.size());
		
		document = other.getDocument();
		position = 0;
		
//		for(String word : other.getWordList()) {
//			addWord(word);
//		}
		for(String word : other.getModifiedWordList()) {
			processModifiedWord(word);
		}
		position = other.getPosition();
	}
	
	protected void initialize(int size) {
		words = new ArrayList<String>(size);
		modifiedWords = new ArrayList<String>(size);
		wordCounts = new HashMap<String, Integer>(size);
		matches = new HashMap<Alliteration, Integer>();
	}
	
//	protected void addWord(String word) {words.add(word);}
	
	public String processWord(String word) {
//		addWord(word);
		
		String modifiedWord = modifyWord(word);
		
		return processModifiedWord(modifiedWord);
	}
	
	protected String modifyWord(String word) {
		String modifiedWord;
		if(!matchCase) {
			modifiedWord = new String(word).toLowerCase();
		}
		else {
			modifiedWord = word;
		}
		return modifiedWord;
	}

	protected String processModifiedWord(String word) {
		addProcessedWord(word);
		incrementPosition();
		
		return incrementWordCount(word);
	}
	
	protected void addProcessedWord(String stemmedWord) {modifiedWords.add(stemmedWord);}
	
	public int consume(HashMap<String, List<Alliteration>> map) {return consume(map, wordCounts);}
	
	protected int consume(HashMap<String, List<Alliteration>> map, HashMap<String, Integer> wordCounts) {
		assert(minSize != 0);
		assert(maxSize != 0);
		assert(minSize <= maxSize);
		
//		System.out.print("\nSearching for matches for:\n\n" + toString());
		
		HashMap<Alliteration, Integer> potentialMatches = new HashMap<Alliteration, Integer>();
		
		// accumulate all possible matches
		// non-strict matching
		if(useSrictMatching) {
			for(Entry<String, Integer> e : wordCounts.entrySet()) {
				String s = e.getKey();
				
				List<Alliteration> oneWordMatches = map.get(s);
				
				if(oneWordMatches == null)
					continue;
				
				Set<Alliteration> oneWordMatchesUnique = new HashSet<Alliteration>(oneWordMatches);

				assert(oneWordMatches != null);
				assert(oneWordMatches.size() > 0);
				assert(oneWordMatchesUnique.size() > 0);
				assert(oneWordMatchesUnique.size() <= oneWordMatches.size());
				
				if(oneWordMatches != null) {
					for(Alliteration possibleMatchNGram : oneWordMatchesUnique) {
						int currentCount = 0;
						int increment = 1;
						
						if(potentialMatches.containsKey(possibleMatchNGram)) {
							currentCount = potentialMatches.get(possibleMatchNGram);
						}
						increment = (e.getValue() > possibleMatchNGram.getCountOfWord(s)) ? possibleMatchNGram.getCountOfWord(s) : e.getValue(); 
						
						increment += currentCount;
						
						potentialMatches.put(possibleMatchNGram, increment);
					}
				}
			}
		}
		else {
			// non-STRICT matching
			for(Entry<String, Integer> e : wordCounts.entrySet()) {
				String s = e.getKey();
				
				for(int j = e.getValue(); j > 0; j--) {
					List<Alliteration> oneWordMatches = map.get(s);
					
					if(oneWordMatches == null)
						break;
					
					for(Alliteration possibleMatchNGram : oneWordMatches) {
						int currentCount = 0;
						int increment = 1;
						
						if(potentialMatches.containsKey(possibleMatchNGram)) {
							currentCount = potentialMatches.get(possibleMatchNGram);
						}
						increment += currentCount;
						increment = (increment > maxSize) ? maxSize : increment;
						
						potentialMatches.put(possibleMatchNGram, increment);
					}
				}
			}
		}
		
		// accumulate matches that meet minimum requirement
		int c = 0;
		for(Entry<Alliteration, Integer> e : potentialMatches.entrySet()) {
			if(e.getValue() >= minSize) {
				matches.put(e.getKey(), e.getValue());
				
//				System.out.println("Found Match: " + e.getKey().toString());
//				potentialMatches.remove(e.getValue());
				c++;
			}
		}
//		if(c > 0) System.out.println("Size: " + c);
		
		potentialMatches = null;
		return c;
	}
	
	protected void incrementPosition() {
		assert(position < document.size());
		position++;
		
//		System.out.println("Position: " + position);
//		if(position - maxSize > 0)
//			System.out.println("New Word: " + document.get(position - maxSize));
	}
	
	protected String incrementWordCount(String word) {
		if(isStopWord(word))
			return null;
 
		if(wordCounts.containsKey(word)) {
			wordCounts.put(word, wordCounts.get(word)+1);
		}
		else {
			wordCounts.put(word, 1);
		}
		return word;
	}
	
	public void popFirstWord() {
//		assert(words.size() > 0);
		assert(modifiedWords.size() > 0);
		assert(wordCounts != null);
		//assert(words.get(0).equals(document.get(position - maxSize)));

		String firstWord = modifiedWords.get(0);
		
		decrementWordCount(firstWord);
		
//		words.remove(0);
		modifiedWords.remove(0);
	}
	
	protected void decrementWordCount(String word) {
		assert(wordCounts != null);
		if(isStopWord(word))
			return;
		
		assert(wordCounts.get(word) >= 1);
		assert(wordCounts.containsKey(word));
		
		if(wordCounts.get(word) > 1) {
			wordCounts.put(word , wordCounts.get(word)-1);
		}
		else {
			wordCounts.remove(word);
		}
	}
	
	public int getCountOfWord(String key) {
		if(wordCounts.containsKey(key)) {
			return wordCounts.get(key);
		}
		return 0;
	}

	public boolean isStopWord(String word) {
		if(useStopWords == false)
			return false;
		return 
			word.equals("The")
			||
			word.equals("the")
			||
			word.equals("And")
			||
			word.equals("and")
			||
			word.equals("Of")
			||
			word.equals("of")
			||
			word.equals("That")
			||
			word.equals("that")
			||
			word.equals("To")
			||
			word.equals("to")
			||
			word.equals("They")
			||
			word.equals("they")
			||
			word.equals("Unto")
			||
			word.equals("unto")
			||
			word.equals("I")
			||
			word.equals("i")
			||
			word.equals("He")
			||
			word.equals("he")
			||
			word.equals("It")
			||
			word.equals("it")
		;
	}
	
	public String toString() {
		StringBuilder st = new StringBuilder();
		st.append("Primary Match: " + leftToString() + "\n");
		
		appendRightToStringBuilder(st);
		
		return st.toString();
	}
	
	protected void appendRightToStringBuilder(StringBuilder st) {
		for(Entry<Alliteration, Integer> e : matches.entrySet()) {
			AlliterationImpl s = (AlliterationImpl) e.getKey();
			
			st.append("Secondary Match (" + e.getValue() + " words match):" + s.leftToString());
			st.append("\n");
		}
	}
	
	public String leftToString() {
		StringBuilder st = new StringBuilder();
		st.append(" Position: " + (position-maxSize) + "/" + document.size());
		st.append(' ');
		
		st.append(' ');st.append(' ');
		for(int i = position - maxSize; i < position; i++) {
			assert(i < document.size());
			st.append(document.get(i));
			st.append(' ');
		}
//		st.append("|||| ");
//		
//		for(String word : words) {
//			st.append(word);
//			st.append(' ');
//		}
		
		st.append("\tbasis [ ");
		for(String word : modifiedWords) {
			st.append(word);
			st.append(' ');
		}
		st.append("]");

		return st.toString();
	}
	
	public boolean hasMatches() {return matches.entrySet().size() > 0;}
	
//	public List<String> getWordList() {return words;}
	public List<String> getModifiedWordList() {return modifiedWords;}
	
	public boolean getStrictness() {return STRICT;}

	public static void setMatchCase(boolean mc) {matchCase = mc;}
	public int size() {return matches.entrySet().size();}

	public static void setMinSize(int min) {minSize = min;}
	public static void setMaxSize(int size) {maxSize = size;}
	public static void setUseStopWords(boolean USESTOPWORDS) {useStopWords  = USESTOPWORDS;}
	public void setDocument(List<String> words) {document = words;}
	
	public int getPosition() {return position;}
	
	
	public int getMinSize() {return minSize;}
	public int getMaxSize() {return maxSize;}
	public List<String> getDocument() {return document;}
//	public int hashCode() {return wordCounts.hashCode();}
	public static void setStrictness(boolean STRICT) {useSrictMatching = STRICT;}
}
