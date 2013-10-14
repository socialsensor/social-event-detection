package collections;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;

import java.io.*;
import java.util.*;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.io.IOUtils;
import org.gephi.graph.api.Node;
import org.gephi.partition.api.Part;
import org.gephi.partition.api.Partition;

import models.Event;
import models.MultimodalItem;
import models.MultimodalSimilarity;

import classifiers.MultimodalClassifier;
import classifiers.TrainingPair;
import clustering.SameClassLink;
import clustering.louvain.LouvainClustering;
import clustering.scan.Community;
import clustering.scan.ScanCommunityDetector;
import clustering.scan.ScanCommunityStructure;
import utils.Constants;
import utils.Constants.SIM_TYPES;

public class EventCollection extends ArrayList<Event> {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MultimodalClassifier _sameEventModel;
    
    private MultimediaCollection _multimediaCollection;
    private Set<String> ids = new HashSet<String>();
	
    private static String separator = "[,\\s]";
    
    private Random RND;
    
    public EventCollection() {
        super();
        RND = new Random();
    }
    
    /*
     * Load Events from file
     */
    public void load(String filename, MultimediaCollection multimediaCollection) {
    	_multimediaCollection = new MultimediaCollection();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), "UTF8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Event event = new Event();
                String[] parts = line.split(separator);
                int nVars = parts.length;
                if (nVars > 0) {
                    for (String id : parts) {
                    	id = id.trim();
                    	ids.add(id);
                        MultimodalItem item = multimediaCollection.get(id);
                        event.addItem(item);
                        _multimediaCollection.put(id, item);
                    }
                    add(event);
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
    
    /*
     * Load Events from file
     */
    public void loadFromGt(String filename, MultimediaCollection multimediaCollection) {
    	_multimediaCollection = new MultimediaCollection();
        BufferedReader reader = null;
		try {
        	reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), "UTF8"));
        	
        	List<String> lines = IOUtils.readLines(reader);
        	reader.close();
        	
        	Map<String, Event> temp = new HashMap<String, Event>();
        	for (int i=1; i<lines.size(); i++) {
            	String line = lines.get(i);
            	
            	String[] parts = line.split(" ");
            	Event event = temp.get(parts[0]);
            	if(event == null) {
            		event = new Event();
            		temp.put(parts[0], event);
            	}
            	String id = parts[1];
            	id = id.trim();
            	ids.add(id);
            	MultimodalItem item = multimediaCollection.get(id);
            	event.addItem(item);
            	_multimediaCollection.put(id, item);
            }
            
        	this.addAll(temp.values());
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
    
    public void mergeEvents() {
    	Set<Event> merged = new HashSet<Event>();
    	int i=0;
		for(Event e1 : this) {
			System.out.println(i++);
			if(merged.contains(e1))
				continue;
			for(Event e2 : this) {
				if(e1 == e2)
					continue;
				if(merged.contains(e2))
				continue;
				double score = _sameEventModel.sameClassScore(e1, e2);
				System.out.println(score);
				if(score > 0) {
					e1.merge(e2);
					merged.add(e2);
				}
				
			}
		}
		this.removeAll(merged);
    }
    
    public Set<String> getIds() {
    	return ids;
    }
    
    public MultimediaCollection getMultimediaCollction() {
    	return this._multimediaCollection;
    }
    
    public void testSameEventModel(int nPositiveExamples, int nNegativeExamples, boolean loadModel, SIM_TYPES[] usedSimTypes) {
        int nEvents = size();
        _sameEventModel = new MultimodalClassifier(nPositiveExamples, nNegativeExamples, usedSimTypes);
        
        if(loadModel) {	
            _sameEventModel.load(Constants.modelDirectory, Constants.CLASSIFIER_TYPE,
            		_multimediaCollection);
            System.out.println("Loaded model");
        }
        
        int wrong_pos = 0;
        int wrong_neg = 0;
        
        int count_pos = 0;
        int count_neg = 0;
        
        System.out.println("Sampling positive examples");
        System.out.println("# positive examples: " + nPositiveExamples);
        
        for(int i=0; i<nPositiveExamples; i++) {
            int event1 = RND.nextInt(nEvents);
            while(get(event1).size()<2) {
                event1 = RND.nextInt(nEvents);    
            }
            MultimodalItem item1 = get(event1).sampleItem();
            MultimodalItem item2 = get(event1).sampleItem();
            while(item1 == item2)
                item2 = get(event1).sampleItem();
            
            _sameEventModel.addPositiveTrainingPair(item1, item2);
          
            double result = _sameEventModel.sameClassScore(item1, item2);

            // ===================
            double[] probs = _sameEventModel.testProbability(item1, item2);
            if(probs[1]>0.2 && probs[1]<0.8)
            	System.out.println("Class: " + result + " Probabilities: " + Arrays.toString(probs));
            // ====================
            
            if(result == 0)		wrong_pos++;
            
            if(result == 1) 	count_pos++;
            if(result == 0) 	count_neg++;
        }
        
        
        System.out.println("Sampling negative examples");
        System.out.println("# negative examples: " + nNegativeExamples);
        
        for(int i=0; i<nNegativeExamples; i++) {
        	int event1 = RND.nextInt(nEvents);
            while(get(event1).size()<1) {
                event1 = RND.nextInt(nEvents);    
            }          
            MultimodalItem item1 = get(event1).sampleItem();
            int event2 = RND.nextInt(nEvents);
            while((event1 == event2) || (get(event2).size() < 1))
                event2 = RND.nextInt(nEvents);
            MultimodalItem item2 = get(event2).sampleItem();
            
            _sameEventModel.addNegativeTrainingPair(item1, item2);
            
            double result = _sameEventModel.sameClassScore(item1, item2);
            
            if(result==1)	wrong_neg++;
            
            if(result==1) 	count_pos++;
            if(result==0) 	count_neg++;
        }

        System.out.println("Wrong positives: " + wrong_pos);
        System.out.println("Positives test size: " + nPositiveExamples);
        System.out.println("Positives precision: " + (nPositiveExamples-wrong_pos)/(float)nPositiveExamples);
        
        System.out.println("Wrong negatives: " + wrong_neg);
        System.out.println("Neggatives test size: " + nNegativeExamples);
        System.out.println("Neggatives precision: " + (nNegativeExamples-wrong_neg)/(float)nNegativeExamples);
        
        System.out.println("Count positives: " + count_pos);
        System.out.println("Count negatives: " + count_neg);
               
    }

    /*
     * 
     */
    public void sampleDataAndSaveARFF(int nPositiveExamples, int nNegativeExamples, SIM_TYPES[] usedSimTypes, String filenameTrain, String filenameTest, String filenameAll) {
    	int nEvents = size();
        
        System.out.println("Sampling positive examples");
        _sameEventModel=new MultimodalClassifier(nPositiveExamples, nNegativeExamples, usedSimTypes);
        for(int i=0; i<nPositiveExamples; i++) {
            int event1 = RND.nextInt(nEvents);
            while(get(event1).size()<2) {
                event1 = RND.nextInt(nEvents);    
            }
            MultimodalItem item1 = get(event1).sampleItem();
            MultimodalItem item2 = get(event1).sampleItem();
            while(item1 == item2)
                item2 = get(event1).sampleItem();
            
            _sameEventModel.addPositiveTrainingPair(item1, item2);
        }

        System.out.println("Sampling negative examples");
        for(int i=0; i<nNegativeExamples; i++) {
        	int event1 = RND.nextInt(nEvents);
            while(get(event1).size()<1) {
                event1 = RND.nextInt(nEvents);    
            }          
            MultimodalItem item1 = get(event1).sampleItem();
            int event2 = RND.nextInt(nEvents);
            while((event1 == event2) || (get(event2).size() < 1))
                event2 = RND.nextInt(nEvents);
            MultimodalItem item2 = get(event2).sampleItem();
            
            _sameEventModel.addNegativeTrainingPair(item1, item2);
        }

        String datasetLine;
        //First create file for train data (first half of the data)
        datasetLine="@relation dataTrain" + nNegativeExamples;
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filenameTrain), "UTF8"));
            writer.append(datasetLine);
            writer.newLine();
            writer.newLine();
            for(int i=0; i<usedSimTypes.length; i++) {
                writer.append("@attribute "+usedSimTypes[i].name()+" numeric");
                writer.newLine();
            }
            writer.append("@ATTRIBUTE class {+1,-1}");
            writer.newLine();
            writer.newLine();
            writer.append("@data");
            writer.newLine();
            writer.newLine();
            
            for(int i=0; i<nPositiveExamples/2; i++) {
            	TrainingPair pair = _sameEventModel.getPositiveTrainingPair(i);
                MultimodalSimilarity tmp_sim=new MultimodalSimilarity(
                		pair.item1, pair.item2,
                		_sameEventModel.getAttributes());
                for(int j=0;j<tmp_sim.similarities.numAttributes();j++)
                    writer.append(tmp_sim.similarities.value(j) +",");
                writer.append("+1");
                writer.newLine();
            }
            for(int i=0; i<nNegativeExamples/2; i++) {
            	TrainingPair pair = _sameEventModel.getNegativeTrainingPair(i);
                MultimodalSimilarity tmp_sim=new MultimodalSimilarity(
                		pair.item1, pair.item2,
                		_sameEventModel.getAttributes());
                for(int j=0;j<tmp_sim.similarities.numAttributes();j++)
                    writer.append(tmp_sim.similarities.value(j) +",");
                writer.append("-1");
                writer.newLine();
            }
            writer.close();

        } catch (IOException e) {
        }
        catch (Exception ex) {
            ex.printStackTrace();
        } 
        
        //Then create file for test data (second half of the data)
        datasetLine="@relation dataTest" + nNegativeExamples;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filenameTest), "UTF8"));
            writer.append(datasetLine);
            writer.newLine();
            writer.newLine();
            for(int i=0; i<usedSimTypes.length; i++) {
                writer.append("@attribute "+usedSimTypes[i].name()+" numeric");
                writer.newLine();
            }
            writer.append("@ATTRIBUTE class {+1,-1}");
            writer.newLine();
            writer.newLine();
            writer.append("@data");
            writer.newLine();
            writer.newLine();
            
            for(int i=(nPositiveExamples/2)+1; i<nPositiveExamples; i++) {
            	TrainingPair pair = _sameEventModel.getPositiveTrainingPair(i);
                MultimodalSimilarity tmp_sim=new MultimodalSimilarity(
                		pair.item1, pair.item2,
                		_sameEventModel.getAttributes());
                
                for(int j=0;j<tmp_sim.similarities.numAttributes();j++)
                    writer.append(tmp_sim.similarities.value(j) +",");
                writer.append("+1");
                writer.newLine();
            }
            
            for(int i=(nNegativeExamples/2)+1; i<nNegativeExamples; i++) {
            	TrainingPair pair = _sameEventModel.getNegativeTrainingPair(i);
                MultimodalSimilarity tmp_sim=new MultimodalSimilarity(
                		pair.item1, pair.item2,
                		_sameEventModel.getAttributes());
                for(int j=0;j<tmp_sim.similarities.numAttributes();j++)
                    writer.append(tmp_sim.similarities.value(j) +",");
                writer.append("-1");
                writer.newLine();
            }
            writer.close();

        } catch (IOException e) {
        }
        catch (Exception ex) {
            ex.printStackTrace();
        } 
        
        //Finally create an aggregate file (all pairs, both train and test)
        datasetLine="@relation dataAll" + nNegativeExamples;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filenameAll), "UTF8"));
            writer.append(datasetLine);
            writer.newLine();
            writer.newLine();
            for(int i=0; i<usedSimTypes.length; i++){
                writer.append("@attribute "+usedSimTypes[i].name()+" numeric");
                writer.newLine();
            }
            writer.append("@ATTRIBUTE class {+1,-1}");
            writer.newLine();
            writer.newLine();
            writer.append("@data");
            writer.newLine();
            writer.newLine();
            
            for(int i=0; i<nPositiveExamples; i++) {
            	TrainingPair pair = _sameEventModel.getPositiveTrainingPair(i);
                MultimodalSimilarity tmp_sim=new MultimodalSimilarity(
                		pair.item1, pair.item2,
                		_sameEventModel.getAttributes());
                for(int j=0; j<tmp_sim.similarities.numAttributes(); j++)
                    writer.append(tmp_sim.similarities.value(j)+",");
                writer.append("+1");
                writer.newLine();
            }
            for(int i=0; i<nNegativeExamples; i++) {
            	TrainingPair pair = _sameEventModel.getNegativeTrainingPair(i);
                MultimodalSimilarity tmp_sim=new MultimodalSimilarity(
                		pair.item1, pair.item2,
                		_sameEventModel.getAttributes());
                for(int j=0; j<tmp_sim.similarities.numAttributes(); j++)
                    writer.append(tmp_sim.similarities.value(i) +",");
                writer.append("-1");
                writer.newLine();
            }
            writer.close();

        } catch (IOException e) {
        }
        catch (Exception ex) {
            ex.printStackTrace();
        } 

    }
    
    /*
     * Learn same event model using a set of positive and negative examples
     */
    public void learnSameEventModels(int nPositiveExamples, int nNegativeExamples, SIM_TYPES[] usedSimTypes) {

        int nEvents = size();
        
        _sameEventModel = new MultimodalClassifier(nPositiveExamples, nNegativeExamples, usedSimTypes);
        
        System.out.println("Sampling positive examples");
        for(int i=0; i<nPositiveExamples; i++) {
        	int event1 = RND.nextInt(nEvents);
        	while(get(event1).size()<2) {
        		event1 = RND.nextInt(nEvents);    
        	}
        	MultimodalItem item1 = get(event1).sampleItem();
        	MultimodalItem item2 = get(event1).sampleItem();
        	while(item1 == item2)
        		item2=get(event1).sampleItem();
        	
        	_sameEventModel.addPositiveTrainingPair(item1, item2);
        }
        
        System.out.println("Sampling negative examples");
        for(int i=0; i<nNegativeExamples; i++) {
        	int event1 = RND.nextInt(nEvents);
        	while(get(event1).size()<1) {
        		event1 = RND.nextInt(nEvents);    
        	}          
        	MultimodalItem item1 = get(event1).sampleItem();
        	
        	int event2 = RND.nextInt(nEvents);
        	while((event1 == event2) || (get(event2).size() < 1))
        		event2 = RND.nextInt(nEvents);
        	MultimodalItem item2 = get(event2).sampleItem();
        	
        	_sameEventModel.addNegativeTrainingPair(item1, item2);
        }
        
        _sameEventModel.train(Constants.CLASSIFIER_TYPE);	
              
    }
          
    public void saveModel(String dir, int run) {
    	if(_sameEventModel != null) {
    		String filename = dir + File.separator + "model_" + run + "." + Constants.CLASSIFIER_TYPE;
    		_sameEventModel.save(filename);
    	}
    }
    
    public Graph<MultimodalItem, SameClassLink> getItemsGraph(MultimediaCollection collection, String nnFolder) {
    	Graph<MultimodalItem, SameClassLink> itemsGraph = new UndirectedSparseGraph<MultimodalItem, SameClassLink>();
        //First add the nodes
        long t = System.currentTimeMillis();
        for(MultimodalItem item : collection.values())
            itemsGraph.addVertex(item);
        t = System.currentTimeMillis() - t;
        System.out.println("Nodes added in " + t + " msecs!");
        
        //Then add the links
        t = System.currentTimeMillis();
        int ll = 0;
        for(MultimodalItem item : collection.values()) {
            if(++ll%100==0)
            	System.out.println(ll + " processed.");
            Set<MultimodalItem> candidates = new HashSet<MultimodalItem>();
            item.loadCandidateNeighboursFromFile(nnFolder);               
            for(String candidateId : item.candidateNeighbours) {
            	MultimodalItem candidateItem = collection.get(candidateId);
            	candidates.add(candidateItem);
            }
            item.candidateNeighbours = null;
            
            for(MultimodalItem candidateItem : candidates) {
                try {
                	
                	double sameClassResult = 0;
                	
					if(Constants.SAME_CLASS_HARD_ASSIGNMENT)
                		sameClassResult = _sameEventModel.sameClassScore(item, candidateItem);
                	else {
                		double[] probs = _sameEventModel.testProbability(item, candidateItem);
                		sameClassResult = probs[1];
                	}
					
                	if((sameClassResult > Constants.CONNECTION_THRESHOLD) && (itemsGraph.findEdge(item, candidateItem)==null)) {	
                		itemsGraph.addEdge(new SameClassLink((float) sameClassResult), item, candidateItem);
                	}	
                }
                catch(Exception e) {
                	e.printStackTrace();
                	return null;
                }
            }
        }
        t = System.currentTimeMillis() - t;
        System.out.println("Edges added in " + t + " msecs!");
        

        
        System.out.println("N nodes : " + itemsGraph.getVertices().size());
        System.out.println("N edges : " + itemsGraph.getEdgeCount(EdgeType.UNDIRECTED));
        
        return itemsGraph;
    }

    public void saveGraph(Graph<MultimodalItem, SameClassLink> itemsGraph, String graphFilename) {
        ////SAVE GRAPH
        if(itemsGraph != null) {
            GraphMLWriter<MultimodalItem, SameClassLink> graphWriter = new GraphMLWriter<MultimodalItem, SameClassLink> ();
            
            graphWriter.addEdgeData("weight", null, "1", new Transformer<SameClassLink, String>() {
                public String transform(SameClassLink link) {
                    return Float.toString(link.weight);
                }
            });
            
            PrintWriter out = null;
            try {
                out = new PrintWriter(
                new BufferedWriter(
                    new FileWriter(graphFilename)));
                graphWriter.save(itemsGraph, out);        
            } catch (IOException ex) {
            }
            out.close();
        }        
        ////GRAPH SAVED 
    }
    
    public Graph<String, SameClassLink> loadIdsGraph(String graphFilename) {
        Graph<String, SameClassLink> itemsGraph = new UndirectedSparseGraph<String, SameClassLink>();

        ////LOAD GRAPH
        Transformer<GraphMetadata, Graph<String, SameClassLink>> graphTransformer = new Transformer<GraphMetadata,
	                          Graph<String, SameClassLink>>() {
	 
        	public Graph<String, SameClassLink> transform(GraphMetadata metadata) {
        		return new UndirectedSparseGraph<String, SameClassLink>();
        	}
        };
        
        
        @SuppressWarnings("unused")
		final Factory<String> vertexFactory = new Factory<String>() {
        	int n = 0;
        	public String create(String id) { 
        		return id; 
        	}

        	@Override
        	public String create() {
        		return "";
        	}
        	
        };

        Transformer<NodeMetadata, String> vertexTransformer	= new Transformer<NodeMetadata, String>() {
        	public String transform(NodeMetadata metadata) {
        		String id = metadata.getId();
        		return id;
        	}
        };        

        final Factory<SameClassLink> edgeFactory = new Factory<SameClassLink>() {
        	@SuppressWarnings("unused")
			int n = 0;
        	public SameClassLink create() { return new SameClassLink(); }
        };


        Transformer<EdgeMetadata, SameClassLink> edgeTransformer =
        		new Transformer<EdgeMetadata, SameClassLink>() {
        	public SameClassLink transform(EdgeMetadata metadata) {
        		String w = metadata.getProperty("weight");
        		float weight = w==null ? 0.0f : Float.parseFloat(w);
        		
        		SameClassLink e = edgeFactory.create();
        		
        		e.weight = weight;
        		
        		return e;
        	}
        };
            
        Transformer<HyperEdgeMetadata, SameClassLink> hyperEdgeTransformer = new Transformer<HyperEdgeMetadata, SameClassLink>() {
        	public SameClassLink transform(HyperEdgeMetadata metadata) {
        		String w = metadata.getProperty("weight");
        		float weight = w==null ? 0.0f : Float.parseFloat(w);
        		
        		SameClassLink e = edgeFactory.create();
        		e.weight = weight;
        		
        		System.out.println("Edge: " + metadata.getEndpoints().get(0).getNode().toString() + 
        				" - " + metadata.getEndpoints().get(1).getNode().toString());
        		return e;
        	}
        };          

        BufferedReader fileReaderG = null;
        try {
            fileReaderG = new BufferedReader(new FileReader(graphFilename));
        } catch (FileNotFoundException ex) {
        	ex.printStackTrace();
        }
        
        GraphMLReader2<Graph<String, SameClassLink>, String, SameClassLink> graphReader 
        	= new GraphMLReader2<Graph<String, SameClassLink>, String, SameClassLink>(
        		fileReaderG, graphTransformer, vertexTransformer, edgeTransformer, hyperEdgeTransformer);

        try {
        	//Get the new graph object from the GraphML file 
        	itemsGraph = graphReader.readGraph();
        } catch (GraphIOException ex) {}         
        // GRAPG LOADED
        
        System.out.println("N nodes : " + itemsGraph.getVertices().size());
        System.out.println("N edges : " + itemsGraph.getEdgeCount(EdgeType.UNDIRECTED));

        return itemsGraph;
    }
    
    public Graph<MultimodalItem, SameClassLink> loadItemsGraph(final MultimediaCollection collection, String graphFilename) {
        Graph<MultimodalItem, SameClassLink> itemsGraph = new UndirectedSparseGraph<MultimodalItem, SameClassLink>();

        ////LOAD GRAPH
        Transformer<GraphMetadata, Graph<MultimodalItem, SameClassLink>> graphTransformer = new Transformer<GraphMetadata,
	                          Graph<MultimodalItem, SameClassLink>>() {
	 
        	public Graph<MultimodalItem, SameClassLink> transform(GraphMetadata metadata) {
        		return new UndirectedSparseGraph<MultimodalItem, SameClassLink>();
        	}
        };
        
        
        @SuppressWarnings("unused")
		final Factory<MultimodalItem> vertexFactory = new Factory<MultimodalItem>() {
        	int n = 0;
        	public MultimodalItem create(MultimodalItem it) { 
        		return collection.get(it.id); }
        	public MultimodalItem create(String id) { 
        		return collection.get(id); 
        	}

        	@Override
        	public MultimodalItem create() {
        		return new MultimodalItem();
        	}
        	
        };

        Transformer<NodeMetadata, MultimodalItem> vertexTransformer	= new Transformer<NodeMetadata, MultimodalItem>() {
        	public MultimodalItem transform(NodeMetadata metadata) {
        		String id = metadata.getId();
        		return collection.get(id);
        	}
        };        

        final Factory<SameClassLink> edgeFactory = new Factory<SameClassLink>() {
        	@SuppressWarnings("unused")
			int n = 0;
        	public SameClassLink create() { return new SameClassLink(); }
        };


        Transformer<EdgeMetadata, SameClassLink> edgeTransformer =
        		new Transformer<EdgeMetadata, SameClassLink>() {
        	public SameClassLink transform(EdgeMetadata metadata) {
        		String w = metadata.getProperty("weight");
        		float weight = w==null ? 0.0f : Float.parseFloat(w);
        		
        		SameClassLink e = edgeFactory.create();
        		
        		e.weight = weight;
        		
        		return e;
        	}
        };
            
        Transformer<HyperEdgeMetadata, SameClassLink> hyperEdgeTransformer = new Transformer<HyperEdgeMetadata, SameClassLink>() {
        	public SameClassLink transform(HyperEdgeMetadata metadata) {
        		String w = metadata.getProperty("weight");
        		float weight = w==null ? 0.0f : Float.parseFloat(w);
        		
        		SameClassLink e = edgeFactory.create();
        		e.weight = weight;
        		
        		System.out.println("Edge: " + metadata.getEndpoints().get(0).getNode().toString() + 
        				" - " + metadata.getEndpoints().get(1).getNode().toString());
        		return e;
        	}
        };          

        BufferedReader fileReaderG = null;
        try {
            fileReaderG = new BufferedReader(new FileReader(graphFilename));
        } catch (FileNotFoundException ex) {
        	ex.printStackTrace();
        }
        
        GraphMLReader2<Graph<MultimodalItem, SameClassLink>, MultimodalItem, SameClassLink> graphReader 
        	= new GraphMLReader2<Graph<MultimodalItem, SameClassLink>, MultimodalItem, SameClassLink>(
        		fileReaderG, graphTransformer, vertexTransformer, edgeTransformer, hyperEdgeTransformer);

        try {
        	//Get the new graph object from the GraphML file 
        	itemsGraph = graphReader.readGraph();
        } catch (GraphIOException ex) {}         
        // GRAPG LOADED
        
        System.out.println("N nodes : " + itemsGraph.getVertices().size());
        System.out.println("N edges : " + itemsGraph.getEdgeCount(EdgeType.UNDIRECTED));

        return itemsGraph;
    }
    
    
    public EventCollection getEventsBySCAN(Graph<MultimodalItem, SameClassLink> itemsGraph, MultimediaCollection collection) {
    	ScanCommunityDetector<MultimodalItem, SameClassLink> detector =
        		new ScanCommunityDetector<MultimodalItem, SameClassLink>(Constants.SCAN_EPSILON, Constants.SCAN_MU);

        ScanCommunityStructure<MultimodalItem, SameClassLink> structure = detector.getCommunityStructure(itemsGraph);
        int nCommunities = structure.getNumberOfCommunities();
        
        EventCollection eventCollection = new EventCollection();
        for(int i=0; i<nCommunities; i++) {
        	Community<MultimodalItem,SameClassLink> community = structure.getCommunity(i);
        	if(community != null) {		 
                Event event = new Event();
                event.addItems(community.getMembers());
                eventCollection.add(event); 
        	}
        }

        int nHubs = structure.getHubs().size();
        System.out.println("N_hubs: " + nHubs);
        
        // SAVE HUBS
        BufferedWriter writer = null;
        try { 
        	writer = new BufferedWriter(
                                new OutputStreamWriter(new FileOutputStream(Constants.HUBS_FILENAME), "UTF8"));
        	for(int i=0; i<nHubs; i++) {
        		MultimodalItem hub = structure.getHubs().get(i);
        		writer.append(hub.id + " ");
        		writer.newLine();
        	}
        	writer.close();
        } catch (IOException e) {
        	e.printStackTrace();
        	if (writer != null) {
        		try {
        			writer.close();
        		} catch (IOException ex) {
        			ex.printStackTrace();
        		}
        	}
        }
        
        if(Constants.UsetHubsOutliers) {
        	Set<Event> singeItemEvents = new HashSet<Event>();

        	List<MultimodalItem> hubs = structure.getHubs();
        	List<MultimodalItem> outliers = structure.getOutliers();
       
        	int nEvents = eventCollection.size();
        	for(MultimodalItem candidateItem : hubs) {
        		int[] adjacentCommunities = new int[nEvents];
        		for(int i=0; i<nEvents; i++) {
        			Event event = eventCollection.get(i);
        			for(MultimodalItem eventItem : event) {
        				if(itemsGraph.findEdge(candidateItem, eventItem) != null)
       						adjacentCommunities[i] = adjacentCommunities[i] + 1;
        			}
        		}
        		int max = Integer.MIN_VALUE, pos = -1;            
        		for(int i=0; i<nEvents; i++) {
        			if(adjacentCommunities[i] > max) {
        				max = adjacentCommunities[i];
        				pos = i;
        			}
        		}
        		if(max > Constants.ATTACHMENT_THRESHOLD) {
        			eventCollection.get(pos).addItem(candidateItem);
        		}
        		else {
        			Event singeItemEvent = new Event();
        			singeItemEvent.addItem(candidateItem);
        			singeItemEvents.add(singeItemEvent);
        		}
        	}
        	
        	for(MultimodalItem candidateItem : outliers) {
        		Event singeItemEvent = new Event();
    			singeItemEvent.addItem(candidateItem);
    			singeItemEvents.add(singeItemEvent);
        	}
        	
        	eventCollection.addAll(singeItemEvents);
        }
        
        if(Constants.PostProcessing) {
        	postProcess(eventCollection, collection, itemsGraph);
        }

        return eventCollection;
    }
    
    private void postProcess(EventCollection eventCollection, MultimediaCollection collection, 
    		Graph<MultimodalItem, SameClassLink> itemsGraph) {
    	
    	Set<Event> singeItemEvents = new HashSet<Event>();

    	Set<MultimodalItem> allItems = new HashSet<MultimodalItem>(collection.values());
    	Set<MultimodalItem> itemsInClusters = new HashSet<MultimodalItem>();
    	for(int i=0; i<eventCollection.size(); i++) {
    		itemsInClusters.addAll(eventCollection.get(i));
    	}
    	
    	Set<MultimodalItem> itemsNotInClusters = new HashSet<MultimodalItem>(allItems);
    	itemsNotInClusters.removeAll(itemsInClusters);
   
    	int nEvents = eventCollection.size();
    	for(MultimodalItem candidateItem : itemsNotInClusters) {
    		int[] adjacentCommunities = new int[nEvents];
    		for(int i=0; i<nEvents; i++) {
    			Event event = eventCollection.get(i);
    			for(MultimodalItem eventItem : event) {
    				if(itemsGraph.findEdge(candidateItem, eventItem) != null)
    					adjacentCommunities[i] = adjacentCommunities[i] + 1;
    			}
    		}
    		int max = Integer.MIN_VALUE, pos = -1;            
    		for(int i=0; i<nEvents; i++) {
    			if(adjacentCommunities[i] > max) {
    				max = adjacentCommunities[i];
    				pos = i;
    			}
    		}
    		if(max > Constants.ATTACHMENT_THRESHOLD) {
    			eventCollection.get(pos).addItem(candidateItem);
    		}
    		else {
    			Event singeItemEvent = new Event();
    			singeItemEvent.addItem(candidateItem);
    			singeItemEvents.add(singeItemEvent);
    		}
    	}
    	eventCollection.addAll(singeItemEvents);
    }
    
    public EventCollection getEventsByLouvain(Graph<MultimodalItem, SameClassLink> itemsGraph, MultimediaCollection collection) {
    	EventCollection eventCollection = new EventCollection();
    	
    	LouvainClustering louvain = new LouvainClustering();

    	System.out.println("Start graph partitioning.");
    	Partition<Node> partition = louvain.partition(itemsGraph);
    	System.out.println(partition.getPartsCount() + " partitions found");
        
    	Part<Node>[] parts = partition.getParts();
		for (Part<Node> part : parts) {
			Node[] nodes = part.getObjects();
			Event event = new Event();
			for (Node node : nodes) {
				String itemId = node.toString();
				MultimodalItem item = collection.get(itemId);
				event.addItem(item);
			}
			eventCollection.add(event);
		}
		
		if(Constants.PostProcessing) {
			postProcess(eventCollection, collection, itemsGraph);
		}
        
    	return eventCollection;
    }
    
    
    public void save(String filename) {
        BufferedWriter writer = null;
        try { 
        	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));
        	for(Event event : this) {
        		String line = event.getAsString(",");
        		writer.append(line);
        		writer.newLine();
        	}
        	writer.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        finally {
        	if (writer != null) {
        		try {
        			writer.close();
        		} catch (IOException ex) {
        			ex.printStackTrace();
        		}
        	}
        }
    }

	public void saveEvents(String filename) {
		BufferedWriter writer = null;
        try { 
        	writer = new BufferedWriter(new OutputStreamWriter(
        			new FileOutputStream(filename), "UTF-8"));
        	
    		for(int i=0 ; i<this.size();i++) {
    			Event event = this.get(i);
    			for(int j=0; j<event.size(); j++) {
    				MultimodalItem item = event.get(j);
    				writer.append(item.id + " " + (i+1));
    	        	writer.newLine();
    			}
    		}
    		
        	writer.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
	}
    
    public void loadModel(String directory, int run, SIM_TYPES[] usedSimTypes) {
    	_sameEventModel = new MultimodalClassifier(1,1, usedSimTypes);
    	//_sameEventModel.load(directory, Constants.CLASSIFIER_TYPE, _multimediaCollection);
    	String filename = directory + File.separator + "model_" + run + "." + Constants.CLASSIFIER_TYPE;
    	_sameEventModel.load(filename);
    }
    
    public void saveGraphLinks(MultimediaCollection collection, String filename, String nnFolder) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));
            
            //Then add the links
            for(MultimodalItem currentItem : collection.values()) {
                currentItem.loadCandidateNeighboursFromFile(nnFolder);
                Set<MultimodalItem> candidates = new HashSet<MultimodalItem>();
                for(String id : currentItem.candidateNeighbours){
                    MultimodalItem item = collection.get(id);
                    candidates.add(item);
                }
    
                for(MultimodalItem currentCandidate : candidates){
                    double same_class_result = 0;
                    same_class_result = _sameEventModel.sameClassScore(currentItem, currentCandidate);
                    if((same_class_result>Constants.CONNECTION_THRESHOLD)) {
                    	writer.append(currentItem+" "+currentCandidate);
                        writer.newLine();
                    }
                }
                currentItem.candidateNeighbours = null;
            }
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

    public void sample(int nSamples, int nEventsTraining, int n_minimum_training_size, String directory) {
        String directoryBase = directory;
        if(!directoryBase.endsWith(File.separator))
            directoryBase = directoryBase + File.separator;
        
        Random rnd = new Random();
        for(int i=0; i<nSamples; i++) {
            String directoryNextSample = directoryBase + "run" + (i+1) + File.separator;
            File tmp_f = new File(directoryNextSample);
            if(!tmp_f.exists()) 
            	tmp_f.mkdirs();
            int n_cand_events = size();
            boolean[] selected = new boolean[n_cand_events];
            for(int j=0; j<n_cand_events; j++)
                selected[j] = false;
            List<Event> trainEvents = new ArrayList<Event>();
            while(trainEvents.size() < nEventsTraining) {
                int nextCand = rnd.nextInt(n_cand_events);
                if((!selected[nextCand])&&(get(nextCand).size() > n_minimum_training_size)
                		&&(get(nextCand).size()>n_minimum_training_size)) {
                    trainEvents.add(get(nextCand));
                    selected[nextCand] = true;
                }
            }
            List<Event> testEvents = new ArrayList<Event>();
            for(int j=0; j<n_cand_events; j++) {
                if(!selected[j]) 
                	testEvents.add(get(j));
            }
            
            EventCollection trainCollection = new EventCollection();
            trainCollection.addAll(trainEvents);
            trainCollection.save(directoryNextSample+"trainEvents.txt");
            PrintWriter out = null;
            int count_tr = 0;
            try {
                out = new PrintWriter(
                new BufferedWriter(
                    new FileWriter(directoryNextSample + "trainIds.txt")));
                for(int j=0; j<trainEvents.size(); j++) {
                    Event nextEvent = trainEvents.get(j);
                    for(MultimodalItem nextItem : nextEvent) {
                        count_tr++;
                        out.println(nextItem.id);
                    }
                }
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            EventCollection testCollection = new EventCollection();
            testCollection.addAll(testEvents);
            testCollection.save(directoryNextSample + "testEvents.txt");
            int count_te = 0;
            try {
                out = new PrintWriter(
                new BufferedWriter(
                    new FileWriter(directoryNextSample+"testIds.txt")));
                for(int j=0; j<testEvents.size(); j++) {
                    Event nextEvent = testEvents.get(j);
                    for(MultimodalItem nextItem : nextEvent) {
                        count_te++;
                        out.println(nextItem.id);
                    }
                }
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println(i+" : "+count_tr+" / "+count_te);
        }
    }
}