package models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.GeodesicDistanceCalculator;

public class MultimodalItem {

    public static double AVG_TITLE = 1;
    public static double AVG_DESCRIPTION = 1;
    public static double AVG_TAGS = 1;
    
    public String id;
    public String username;

    public Date date_taken;
    public Long timestamp_taken;
    
    public Date date_uploaded;
    public Long timestamp_uploaded;
    public Double latitude;
    public Double longitude;
    public String title;
    public String description;
    public List<String> tags;
    
    public TFIDFVector titleTFIDF;
    public TFIDFVector tagsTFIDF;
    public TFIDFVector descriptionTFIDF;
    
    public double[] vladSurfVector = null;
	public Set<String> candidateNeighbours;
    
    public MultimodalItem() {
    	candidateNeighbours = new HashSet<String>();
    }
    
    public String print() {
    	StringBuffer strBuf = new StringBuffer();
    	strBuf.append(id + ", ");
    	strBuf.append(username + ", ");
    	strBuf.append(title + ", ");
    	strBuf.append(date_taken + ", ");
    	strBuf.append(date_uploaded + ", ");
    	strBuf.append(date_uploaded + ", ");
    	strBuf.append(date_uploaded + ", ");
    	
    	return strBuf.toString();
    }
    
    public String toString() {
    	return id;
    }
    
    public double locationSimilarity(MultimodalItem item2) {
        if(this.hasLocation() && item2.hasLocation())
            return GeodesicDistanceCalculator.vincentyDistance(
            		latitude, longitude, item2.latitude, item2.longitude);
        else 
            return -1;
    }
  

    public double descriptionSimilarityCosine(MultimodalItem item2) {
        return descriptionTFIDF.cosineSimilarity(item2.descriptionTFIDF);
    }
    
    public double titleSimilarityCosine(MultimodalItem item2) {
        return titleTFIDF.cosineSimilarity(item2.titleTFIDF);
    }
    
    public double tagsSimilarityCosine(MultimodalItem item2) {
        return tagsTFIDF.cosineSimilarity(item2.tagsTFIDF);
    }

    public double descriptionSimilarityBM25(MultimodalItem item2) {
        return descriptionTFIDF.bm25Similarity(item2.descriptionTFIDF, AVG_DESCRIPTION);
    }
    
    public double titleSimilarityBM25(MultimodalItem item2) {
        return titleTFIDF.bm25Similarity(item2.titleTFIDF, AVG_TITLE);
    }
    
    public double tagsSimilarityBM25(MultimodalItem item2) {
        return tagsTFIDF.bm25Similarity(item2.tagsTFIDF, AVG_TAGS);
    }
    
    
    public double timeUploadedSimilarity(MultimodalItem item2) {
        if(timestamp_uploaded != null && item2.timestamp_uploaded != null) {
            double divisor = 1000.0 * 60 * 60;
            return Math.abs(timestamp_uploaded-item2.timestamp_uploaded)/divisor;
        }
        else
            return -1;
    }

    public double timeTakenSimilarity(MultimodalItem item2) {
    	if(timestamp_taken != null && item2.timestamp_taken != null) {
    		double divisor = 1000.0 * 60 * 60;
        	return Math.abs(timestamp_taken-item2.timestamp_taken)/divisor;
    	}
    	else
    		return -1;
    }

    public double timeTakenHourDiff12(MultimodalItem item2) {
        double divisor = 1000.0;
        divisor = divisor * 60 * 60;
        double hours = (timestamp_taken - item2.timestamp_taken) / divisor;
        if(hours<12)
            return 1;
        else 
            return 0;
    }
    
    public double timeTakenHourDiff24(MultimodalItem item2){
        double divisor = 1000.0;
        divisor = divisor * 60 * 60;
        double hours=(timestamp_taken - item2.timestamp_taken) / divisor;
        if(hours < 24)
            return 1;
        else 
            return 0;
    }

    public double timeTakenDayDiff3(MultimodalItem item2){
        double divisor = 1000.0;
        divisor = divisor * 60* 60 * 24;
        double days = (timestamp_taken - item2.timestamp_taken) / divisor;
        if(days < 3)
            return 1;
        else 
            return 0;
    }
    
    
    public double sameUserSimilarity(MultimodalItem item2) {
        if(this.username.equals(item2.username))
            return 1;
        return 0;
    }    
    
    // This is L2 distance. 
    public double visualSimilarity(MultimodalItem item) {  	
    	if(vladSurfVector == null || item.vladSurfVector == null) {
    		return 2;
    	}
    	
    	double dif = 0;
    	for(int i=0; i<vladSurfVector.length; i++) {
    		dif += Math.pow(vladSurfVector[i] - item.vladSurfVector[i], 2);
    	}
    	return Math.sqrt(dif);
    }
    
    public boolean hasLocation() {
        if(latitude != null && longitude != null)
            return true;
        else 
            return false;
    }
    
    public double VladSurfLength() {
    	double length = 0;
    	if(vladSurfVector != null) {
    		for(double f : vladSurfVector) {
    			length += (f*f);
    		}
    	}
        return Math.sqrt(length);
    }
    
    public void addCandidateNeighbour(String itemId) {
    	candidateNeighbours.add(itemId);
    }
    
    public void writeCandidateNeighboursToFile(String candidatesFolder) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
            		new FileOutputStream(new File(candidatesFolder, id + ".txt"))));
            for(String itemId : candidateNeighbours)                
                writer.append(itemId + " ");
            writer.close();
        } catch (IOException e) {
            if (writer != null) {
                e.printStackTrace();
                try {
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void loadCandidateNeighboursFromFile(String dir) {
        candidateNeighbours = new HashSet<String>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(dir + File.separator + id + ".txt")));
            
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                for(int i=0; i<parts.length; i++) {
                    String itemId = parts[i].trim();
                    if(itemId.length()>0)
                        candidateNeighbours.add(itemId);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
 
}
