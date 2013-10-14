package models;

public class FrequencyEntry {
    public double tf;
    public double idf;
    public double tfidf;
    
    @Override
    public String toString() {
    	return "[tf="+tf+", idf="+idf+", tf*idf="+tfidf+"]";
    }
}
