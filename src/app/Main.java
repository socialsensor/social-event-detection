package app;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import clustering.SameClassLink;
import models.MultimodalItem;
import indexing.spatial.SpatialIndex;
import indexing.textual.TextualIndex;
import indexing.time.TimeIndex;
import indexing.visual.VisualIndex;
import utils.Constants;
import collections.EventCollection;
import collections.MultimediaCollection;
import edu.uci.ics.jung.graph.Graph;


public class Main {
	 
	private static TextualIndex textualIndex;
	private static VisualIndex visualIndex;
	private static SpatialIndex spatialIndex;
	private static TimeIndex timeIndex;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {	
		
		MultimediaCollection trainMultimediaCollection = new MultimediaCollection(Constants.trainMetadataFile);
		System.out.println(trainMultimediaCollection.size() + " documents loaded for training");	

		MultimediaCollection testMultimediaCollection = new MultimediaCollection(Constants.testMetadataFile);
		System.out.println(testMultimediaCollection.size() + " documents loaded for testing");	
		
		EventCollection trainEvents = new EventCollection();
		trainEvents.loadFromGt(Constants.trainEventsFile, trainMultimediaCollection);
		System.out.println(trainEvents.size() + " train events with " + trainEvents.getIds().size() + " images");
		
		loadIndexes(testMultimediaCollection, true);
		loadFeatureVectors(testMultimediaCollection);

		createCandidates(testMultimediaCollection, Constants.candidatesFolder + "Extended");
		
		EventCollection testEvents = new EventCollection();
		Run[] runs = Constants.runs;
		for(int i=0;i<runs.length; i++) {
			if(i==2)
				continue;
			
			System.out.println("Run: " + runs[i].run);
			//trainEvents.learnSameEventModels(Constants.nPositiveExamples, Constants.nNegativeExamples, runs[i].simTypes);
			//trainEvents.saveModel(Constants.modelDirectory, runs[i].run);
			
			Graph<MultimodalItem, SameClassLink> itemsGraph = null;
			
			testEvents.loadModel(Constants.modelDirectory, runs[i].run, runs[i].simTypes); 
			itemsGraph = testEvents.getItemsGraph(testMultimediaCollection, Constants.candidatesFolder);
			
			String graphFilename = Constants.GRAPH_DIR + File.separator + "graph_w" + runs[i].run + ".graphml";
			System.out.println("Save graph " + graphFilename);
			testEvents.saveGraph(itemsGraph, graphFilename);
			
			itemsGraph = null;
			itemsGraph = testEvents.loadItemsGraph(testMultimediaCollection, graphFilename);
			EventCollection events = testEvents.getEventsBySCAN(itemsGraph, testMultimediaCollection);
			
			events.saveEvents(Constants.rootDir + File.separator + "results_" + runs[i].run +".csv");
			events.save(Constants.rootDir + File.separator + "results_" + runs[i].run +"_s.csv");
		}
		
	}

	public static void loadFeatureVectors(MultimediaCollection collection) throws Exception {
		int cc = 0;
		for(MultimodalItem item : collection.values()) {
			if(++cc%10000==0) {
				System.out.println("Visual and tectual vectors loaded for " + cc + " items.");
			}
			item.titleTFIDF = textualIndex.getTFIDFVector(item.id, "title");
			item.tagsTFIDF = textualIndex.getTFIDFVector(item.id, "tags");
			item.descriptionTFIDF = textualIndex.getTFIDFVector(item.id, "description");
			
			item.vladSurfVector = visualIndex.getVector(item);
		}
	}
	
	public static void loadIndexes(MultimediaCollection eventsCollection, boolean test) throws Exception {
		
		String textualIndexFile = test ? Constants.testTextualIndex : Constants.trainTextualIndex;
		textualIndex = new TextualIndex(textualIndexFile, false);
		System.out.println("Textual Index Size: " + textualIndex.size());

		String visualIndexTestFile = test ? Constants.testVisualIndex : Constants.trainVisualIndex;
		visualIndex = new VisualIndex(visualIndexTestFile, Constants.codebookFile, Constants.pcaFile);	
		System.out.println("Visual Index Size: " + visualIndex.size());
		
		String spatialIndexFile = test ? Constants.testSpatialIndex : Constants.trainSpatialIndex;
		spatialIndex = new SpatialIndex(spatialIndexFile);
		System.out.println("Spatial Index Size: " + spatialIndex.size());
		
		timeIndex = new TimeIndex(eventsCollection);
		System.out.println("Time Index Size: " + timeIndex.size());
	}
	
	public static void createCandidates(MultimediaCollection collection, String candidatesFolder) {
    	File file = new File(candidatesFolder);
    	if(!file.exists())
    		file.mkdirs();
    	
    	System.out.print("Get candidate neighbours per item in test set");
    	
    	int i = 0;
    	for(MultimodalItem item : collection.values()) {
			if(++i%100==0)
				System.out.println(i + " items proccessed!");
		
			
			if(new File(candidatesFolder, item.id + ".txt").exists())
				continue;
			
			Set<String> ids = new HashSet<String>();
			Set<String> temp;
			try {
				temp = timeIndex.search(item, Constants.TIME_NN);
				ids.addAll(temp);
			}
			catch(Exception e) { }
			
			try {
				temp = textualIndex.search(item, Constants.TEXT_NN);
				ids.addAll(temp);
			} catch(Exception e){ }
			
			if(item.hasLocation()) {
				try {
					temp = spatialIndex.search(item, Constants.LOCATION_NN);
					ids.addAll(temp);
				}
				catch(Exception e) { }
			}
			
			try {
				temp = visualIndex.search(item, Constants.VISUAL_NN);
				ids.addAll(temp);
			}
			catch(Exception e) { }
			
			item.candidateNeighbours.addAll(ids);
			item.writeCandidateNeighboursToFile(candidatesFolder);
			
			item.candidateNeighbours = null;
		}
	}
    	
	

	
}
