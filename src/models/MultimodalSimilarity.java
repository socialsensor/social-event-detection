package models;

import java.io.PrintWriter;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import utils.Constants;

import utils.Constants.SIM_TYPES;

public class MultimodalSimilarity {

	public Instance similarities;

    public MultimodalSimilarity(MultimodalItem item1, MultimodalItem item2, ArrayList<Attribute> attributes) {
        similarities = computeSimilarities(item1, item2, attributes);
    }
    
    public MultimodalSimilarity(Event event1, Event event2, ArrayList<Attribute> attributes) {
        similarities = computeEventsSimilarities(event1, event2, attributes);
    }
    
    public static Instance computeSimilarities(MultimodalItem item1, MultimodalItem item2, ArrayList<Attribute> attributes) {
    	
//        int nDistances = Constants.usedSimTypes.length;
//        Instance sims = new DenseInstance(nDistances + 1);
    	Instance sims = new DenseInstance(attributes.size());
    	
        for(int i=0; i<attributes.size()-1; i++) {
        	String attrName = attributes.get(i).name();
        
        	/*
        	 * Location Attribute
        	 */
            if(attrName.equals(Constants.SIM_TYPES.LOCATION.name()))
                sims.setValue(i, item1.locationSimilarity(item2));
            
            /* 
             * User Attribute
             */
            if(attrName.equals(Constants.SIM_TYPES.SAME_USER.name()))
                sims.setValue(i, item1.sameUserSimilarity(item2));
            
            /*
             * Time Attributes
             */
            if(attrName.equals(Constants.SIM_TYPES.TIME_TAKEN.name()))
                sims.setValue(i, item1.timeTakenSimilarity(item2));
         
            if(attrName.equals(Constants.SIM_TYPES.TIME_UPLOADED.name()))
                sims.setValue(i, item1.timeUploadedSimilarity(item2));
            
            if(attrName.equals(Constants.SIM_TYPES.TIME_TAKEN_DAY_DIFF_3.name()))
                sims.setValue(i, item1.timeTakenDayDiff3(item2));
            
            if(attrName.equals(Constants.SIM_TYPES.TIME_TAKEN_HOUR_DIFF_12.name()))
                sims.setValue(i, item1.timeTakenHourDiff12(item2));
            
            if(attrName.equals(Constants.SIM_TYPES.TIME_TAKEN_HOUR_DIFF_24.name()))
                sims.setValue(i, item1.timeTakenHourDiff24(item2));
         
            
           
            /*
             * Textual Attributes
             */
            if(attrName.equals(Constants.SIM_TYPES.TITLE_BM25.name()))
                sims.setValue(i, item1.titleSimilarityBM25(item2));
            
            if(attrName.equals(Constants.SIM_TYPES.TITLE_COSINE.name()))
                sims.setValue(i, item1.titleSimilarityCosine(item2));
            
            if(attrName.equals(Constants.SIM_TYPES.TAGS_BM25.name()))
                sims.setValue(i, item1.tagsSimilarityBM25(item2));
            
            if(attrName.equals(Constants.SIM_TYPES.TAGS_COSINE.name()))
                sims.setValue(i, item1.tagsSimilarityCosine(item2));
           
        	if(attrName.equals(Constants.SIM_TYPES.DESCRIPTION_BM25.name()))
                sims.setValue(i, item1.descriptionSimilarityBM25(item2));
        	
            if(attrName.equals(Constants.SIM_TYPES.DESCRIPTION_COSINE.name()))
                sims.setValue(i, item1.descriptionSimilarityCosine(item2));
            
            /*
             * Visual Attribute
             */
            if(attrName.equals(Constants.SIM_TYPES.VLAD_SURF.name()))
                sims.setValue(i, item1.visualSimilarity(item2));
                
        }
        return sims;
    }
    
    public static Instance computeEventsSimilarities(Event event1, Event event2, ArrayList<Attribute> attributes) {
        int nDistances = Constants.usedEventSimTypes.length;
        Instance sims = new DenseInstance(nDistances + 1);

        for(int i=0; i<attributes.size()-1; i++) {
        	String attrName = attributes.get(i).name();
        
        	/*
        	 * Location Attribute
        	 */
            if(attrName.equals(Constants.SIM_TYPES.LOCATION.name()))
                sims.setValue(i, event1.locationSimilarity(event2));
            
            /* 
             * User Attribute
             */
            if(attrName.equals(Constants.SIM_TYPES.SAME_USER.name()))
                sims.setValue(i, event1.userSetSimilarity(event2));
            
            /*
             * Time Attributes
             */
            if(attrName.equals(Constants.SIM_TYPES.TIME_TAKEN.name()))
                sims.setValue(i, event1.timeTakenSimilarity(event2));
         
            if(attrName.equals(Constants.SIM_TYPES.TIME_UPLOADED.name()))
                sims.setValue(i, event1.timeUploadedSimilarity(event2));
         
            if(attrName.equals(Constants.SIM_TYPES.TIME_TAKEN_DAY_DIFF_3.name()))
                sims.setValue(i, event1.timeTakenDayDiff3(event2));
            
            if(attrName.equals(Constants.SIM_TYPES.TIME_TAKEN_HOUR_DIFF_12.name()))
                sims.setValue(i, event1.timeTakenHourDiff12(event2));
            
            if(attrName.equals(Constants.SIM_TYPES.TIME_TAKEN_HOUR_DIFF_24.name()))
                sims.setValue(i, event1.timeTakenHourDiff24(event2));
           
            /*
             * Textual Attributes
             */
            if(attrName.equals(Constants.SIM_TYPES.TITLE_BM25.name()))
                sims.setValue(i, event1.textSimilarityBM25(event2));
            
            if(attrName.equals(Constants.SIM_TYPES.TITLE_COSINE.name()))
                sims.setValue(i, event1.textSimilarityCosine(event2));
            
            if(attrName.equals(Constants.SIM_TYPES.TAGS_BM25.name()))
                sims.setValue(i, event1.textSimilarityBM25(event2));
            
            if(attrName.equals(Constants.SIM_TYPES.TAGS_COSINE.name()))
                sims.setValue(i, event1.textSimilarityCosine(event2));

            if(attrName.equals(Constants.SIM_TYPES.DESCRIPTION_BM25.name()))
                sims.setValue(i, event1.textSimilarityBM25(event2));
            
            if(attrName.equals(Constants.SIM_TYPES.DESCRIPTION_COSINE.name()))
                sims.setValue(i, event1.textSimilarityCosine(event2));
                
            /*
             * Visual Attribute
             */
            if(attrName.equals(Constants.SIM_TYPES.VLAD_SURF.name()))
                sims.setValue(i, -1);
            
        }
        return sims;
    }
    
    public void saveToFile(PrintWriter pw) {
        int n_similarities = similarities.numAttributes() - 1;
        int i;
        for(i=0; i<n_similarities-1; i++)
            pw.print(similarities.value(i)+" ");
        pw.println(similarities.value(i));
    }
    
    public static ArrayList<Attribute> getAttributes(SIM_TYPES[] simTypes) {
        ArrayList<Attribute> attributesList = new ArrayList<Attribute>();

		for(SIM_TYPES simType : simTypes) {
        	attributesList.add(new Attribute(simType.toString()));
        }

        ArrayList<String> fvClassVal = new ArrayList<String>(2);
        fvClassVal.add("negative");
        fvClassVal.add("positive");
        Attribute ClassAttribute = new Attribute("theClass", fvClassVal);

        attributesList.add(ClassAttribute);
        
        return attributesList;
    }
    
    public static ArrayList<Attribute> getEventAttributes() {
        ArrayList<Attribute> attributesList = new ArrayList<Attribute>();

        SIM_TYPES[] simTypes = Constants.usedEventSimTypes;
		for(SIM_TYPES simType : simTypes) {
        	attributesList.add(new Attribute(simType.toString()));
        }

        ArrayList<String> fvClassVal = new ArrayList<String>(2);
        fvClassVal.add("negative");
        fvClassVal.add("positive");
        Attribute ClassAttribute = new Attribute("theClass", fvClassVal);

        attributesList.add(ClassAttribute);
        
        return attributesList;
    }
}