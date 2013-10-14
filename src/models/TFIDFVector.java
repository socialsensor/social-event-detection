package models;

import java.util.*;
import java.util.Map.Entry;

public class TFIDFVector extends HashMap<String, FrequencyEntry> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static double BM25_K1 = 1.5;
    public static double BM25_B = 0.75;
    
   //public Map<String, FrequencyEntry> vector;
    double vectorLength;
    public int nTerms;
    boolean relative_tfs;
    
    
    public TFIDFVector() {
    	super();
        vectorLength = 0;
        relative_tfs = false;
    }

    public void addTerm(String term, double tf, double idf) {
    	FrequencyEntry freq = new FrequencyEntry();
    	freq.tf = tf;
    	freq.idf = idf;
    	freq.tfidf = tf * idf;
    	
    	put(term, freq);
    }
    
    public TFIDFVector aggregate(TFIDFVector v2) {
        for(Entry<String, FrequencyEntry> entry : v2.entrySet()) {
            String key = entry.getKey();
            FrequencyEntry value = entry.getValue();
            if(containsKey(key)) {
                FrequencyEntry value2 = get(key);
                value.tf += value2.tf;
                value.tfidf += value2.tfidf;
            }
            else {
                nTerms++;
            }
            put(key, value);
        }
        return this;
    } 
    
    public void computeLength() {
        vectorLength = 0;
        Collection<FrequencyEntry> freqs = values();
        for(FrequencyEntry freq : freqs) {
            vectorLength += Math.pow(freq.tfidf, 2);
        }
        vectorLength = Math.sqrt(vectorLength);
    }

    public double computeLengthSubset(Set<Word> subset) {
        double vectorLength = 0;
        Set<String> words = keySet();
        Set<Word> intersection = new HashSet<Word>(subset);
        intersection.retainAll(words);
        for(Word next_word : intersection) {
            FrequencyEntry freq = get(next_word);
            vectorLength = vectorLength + freq.tfidf * freq.tfidf;
        }
        return vectorLength = Math.sqrt(vectorLength);
    }
    
    
    public double cosineSimilarity(TFIDFVector vector2) {
        if(vectorLength == 0) 
        	computeLength();
        if(vectorLength == 0) 
        	return 0;
        if(vector2.vectorLength == 0) 
        	vector2.computeLength();
        if(vector2.vectorLength == 0) 
        	return 0;
        
        double similarity = 0;
        Set<String> words1 = keySet();
        Set<String> words2 = vector2.keySet();
        Set<String> intersection = new HashSet<String>(words1);
        intersection.retainAll(words2);
        
        for(String match : intersection) {
            FrequencyEntry fre1 = get(match);
            FrequencyEntry fre2 = vector2.get(match);
            similarity += (fre1.tfidf * fre2.tfidf);
        }
        similarity = similarity / (vectorLength * vector2.vectorLength);
        return similarity;
    }

    public double cosineSimilaritySubset(TFIDFVector vector2, Set<Word> subset) {
        double vectorLength1 = computeLengthSubset(subset);
        double vectorLength2 = vector2.computeLengthSubset(subset);
        if(vectorLength1==0) 
        	return 0;
        if(vectorLength2==0) 
        	return 0;

        double similarity = 0;
        
        Set<String> words1 = keySet();
        Set<String> words2 = vector2.keySet();
        Set<Word> intersection = new HashSet<Word>(subset);
        intersection.retainAll(words1);
        intersection.retainAll(words2);

        for(Word match : intersection){
            FrequencyEntry fre1 = get(match);
            FrequencyEntry fre2 = vector2.get(match);
            similarity = similarity+fre1.tfidf*fre2.tfidf;
        }
        similarity = similarity / (vectorLength1*vectorLength2);
        return similarity;
    }
    
    public double bm25Similarity(TFIDFVector vector2, double avgLength) {
        double similarity1 = 0, similarity2=0;
        double d1 = 0, d2=0;
        for(FrequencyEntry freq : values()) {
            d1 += freq.tf;
        }
        for(FrequencyEntry freq:vector2.values()) {
            d2 += freq.tf;
        }
        
        Set<String> words1 = keySet();
        Set<String> words2 = vector2.keySet();
        Set<String> intersection = new HashSet<String>(words1);
        intersection.retainAll(words2);
        
        for(String match : intersection) {
            FrequencyEntry fre1 = get(match);
            similarity1 += fre1.idf * (fre1.tf * (BM25_K1+1))/(fre1.tf + BM25_K1 * (1 - BM25_B + BM25_B * (d1/avgLength)));       
        }

        for(String match:intersection){
            FrequencyEntry fre2 = vector2.get(match);
            similarity2 += fre2.idf * (fre2.tf * (BM25_K1 + 1))/(fre2.tf + BM25_K1 * (1 - BM25_B + BM25_B * (d2/avgLength)));
        }
        
        return (similarity1 + similarity2)/2;
    }
    
}
