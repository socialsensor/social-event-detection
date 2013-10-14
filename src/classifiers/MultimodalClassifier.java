package classifiers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import collections.MultimediaCollection;
import models.Event;
import models.MultimodalItem;
import models.MultimodalSimilarity;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import utils.Constants;
import utils.Constants.ClassifierTypes;
import utils.Constants.SIM_TYPES;

public class MultimodalClassifier {
    
	public static void main(String[] args) throws Exception {	
//		String modelFile = "/disk1_data/mediaEval SED 2013/task 1/train/sameClassModel/model.svm";
//		SMO m = (SMO) weka.core.SerializationHelper.read(modelFile);
	}
	
	private List<TrainingPair> positiveTrainingPairs;
	private List<TrainingPair> negativeTrainingPairs;
	
    private Instances dataTrain;
    private Classifier model;
    private ArrayList<Attribute> attributes;
    
    List<MultimodalSimilarity> positiveMultimodalSimilarities;
    List<MultimodalSimilarity> negativeMultimodalSimilarities;
	
    private SIM_TYPES[] usedSimTypes;

    public MultimodalClassifier(int nPositivePairs, int nNegativePairs, SIM_TYPES[] usedSimTypes) {
    	positiveTrainingPairs = new ArrayList<TrainingPair>(nPositivePairs);
    	negativeTrainingPairs = new ArrayList<TrainingPair>(nNegativePairs);
    	
    	this.usedSimTypes = usedSimTypes;
    }

    public void addPositiveTrainingPair(MultimodalItem item1, MultimodalItem item2) {
    	positiveTrainingPairs.add(new TrainingPair(item1, item2));	
    }
    
    public void addNegativeTrainingPair(MultimodalItem item1, MultimodalItem item2) {
    	negativeTrainingPairs.add(new TrainingPair(item1, item2));
    }

    public TrainingPair getPositiveTrainingPair(int index) {
    	return positiveTrainingPairs.get(index);	
    }
    
    public TrainingPair getNegativeTrainingPair(int index) {
    	return negativeTrainingPairs.get(index);
    }
    
    public void train(Constants.ClassifierTypes classifierType) {
    	
        String parameters = "";
        if(Constants.CLASSIFIER_TYPE == Constants.ClassifierTypes.decisionTree)
            parameters = Constants.DECISION_TREE_PARAMETERS;
        if(Constants.CLASSIFIER_TYPE == Constants.ClassifierTypes.jrip)
            parameters = Constants.JRIP_PARAMETERS;
        if(Constants.CLASSIFIER_TYPE == Constants.ClassifierTypes.MultilayerPerceptron)
            parameters = Constants.MULTILAYER_PERCEPTRON_PARAMETERS;
        if(Constants.CLASSIFIER_TYPE == Constants.ClassifierTypes.NaiveBayes)
            parameters = Constants.NAIVE_BAYES_PARAMETERS;
        if(Constants.CLASSIFIER_TYPE == Constants.ClassifierTypes.NearestNeighbour)
            parameters = Constants.NEAREST_NEIGHBOUR_PARAMETERS;
        if(Constants.CLASSIFIER_TYPE == Constants.ClassifierTypes.part)
            parameters = Constants.PART_PARAMETERS;
        if(Constants.CLASSIFIER_TYPE == Constants.ClassifierTypes.randomForest)
            parameters = Constants.RANDOM_FOREST_PARAMETERS;
        if(Constants.CLASSIFIER_TYPE == Constants.ClassifierTypes.randomTree)
            parameters = Constants.RANDOM_TREE_PARAMETERS;
        if(Constants.CLASSIFIER_TYPE == Constants.ClassifierTypes.svm)
            parameters = Constants.SVM_PARAMETERS;
        
        String[] options;
        System.out.println("Classifier type: " + classifierType.toString());
        try {
            options = weka.core.Utils.splitOptions(parameters);
            if(classifierType == Constants.ClassifierTypes.svm) {
                model = new SMO();
                ((SMO)model).setOptions(options);
            }
            if(classifierType == Constants.ClassifierTypes.decisionTree){
                model = new J48();
                System.out.println(options);
                ((J48)model).setOptions(options);
            }
            if(classifierType == Constants.ClassifierTypes.jrip){
                model = new JRip();
                ((JRip)model).setOptions(options);
            }
            if(classifierType == Constants.ClassifierTypes.MultilayerPerceptron){
                model = new MultilayerPerceptron();
                ((MultilayerPerceptron)model).setOptions(options);
            }
            if(classifierType == Constants.ClassifierTypes.NaiveBayes){
                model = new NaiveBayes();
                ((NaiveBayes)model).setOptions(options);
            }
            if(classifierType == Constants.ClassifierTypes.NearestNeighbour){
                model = new IBk();
                ((IBk)model).setOptions(options);
            }
            if(classifierType == Constants.ClassifierTypes.part){
                model = new PART();
                ((PART)model).setOptions(options);
            }
            if(classifierType == Constants.ClassifierTypes.randomForest){
                model = new RandomForest();
                ((RandomForest)model).setOptions(options);
            }
            if(classifierType == Constants.ClassifierTypes.randomTree){
                model = new RandomTree();
               ((RandomTree)model).setOptions(options);
            }
        } catch (Exception ex) {
            Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        int nPositiveExamples = positiveTrainingPairs.size();
        int nNegativeExamples = negativeTrainingPairs.size();
        
        attributes = MultimodalSimilarity.getAttributes(usedSimTypes);
        System.out.println("ATTRIBUTES : " + attributes);
        
        positiveMultimodalSimilarities = new ArrayList<MultimodalSimilarity>(nPositiveExamples);
        for(TrainingPair pair : positiveTrainingPairs) {
            positiveMultimodalSimilarities.add(new MultimodalSimilarity(pair.item1, pair.item2, attributes));
        }
        
        negativeMultimodalSimilarities = new ArrayList<MultimodalSimilarity>(nNegativeExamples);
        for(TrainingPair pair : negativeTrainingPairs) {
        	negativeMultimodalSimilarities.add(new MultimodalSimilarity(pair.item1, pair.item2, attributes));
    	}
        int n_points = nPositiveExamples/2 + nNegativeExamples/2;
        dataTrain = new Instances("train", attributes, n_points);
        Instances dataTestPos = new Instances("testPos", attributes, n_points/2);
        Instances dataTestNeg = new Instances("testNeg", attributes, n_points/2);
        dataTrain.setClassIndex(dataTrain.numAttributes() - 1);
        dataTestPos.setClassIndex(dataTrain.numAttributes() - 1);
        dataTestNeg.setClassIndex(dataTrain.numAttributes() - 1);

        int n_vars = usedSimTypes.length;
        
        System.out.println("n vars : " + n_vars);
        System.out.println("n dims : " + (positiveMultimodalSimilarities.get(0).similarities.numAttributes()-1));

        for(int i=0; i<nPositiveExamples/2; i++) {
            Instance instance = positiveMultimodalSimilarities.get(i).similarities;
            instance.setDataset(dataTrain);
            instance.setValue(instance.numAttributes()-1, "positive");
            dataTrain.add(instance);
        }
        for(int i=0; i<nNegativeExamples/2; i++){
            Instance instance = negativeMultimodalSimilarities.get(i).similarities;
            instance.setDataset(dataTrain);
            instance.setValue(instance.numAttributes()-1, "negative");
            dataTrain.add(instance);
        }

        System.out.println("No of items: " + dataTrain.numInstances());
        System.out.println("No of variables: " + dataTrain.numAttributes());
        System.out.println("No of classes: " + dataTrain.numClasses());
        
        try {
        	// build classifier
            model.buildClassifier(dataTrain);         
        } catch (Exception ex) {
            Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Trained");
        
        System.out.println("Positive test");
        for(int i = nPositiveExamples/2; i<nPositiveExamples; i++) {
            Instance instance = positiveMultimodalSimilarities.get(i).similarities;
            instance.setDataset(dataTestPos);
            instance.setValue(instance.numAttributes()-1, "positive");
            dataTestPos.add(instance);
        }
        for(int i = nNegativeExamples/2; i<nNegativeExamples; i++) {
            Instance instance = negativeMultimodalSimilarities.get(i).similarities;
            instance.setDataset(dataTestNeg);
            instance.setValue(instance.numAttributes()-1, "negative");
            dataTestNeg.add(instance);
        }
        dataTestPos.setClassIndex(dataTestPos.numAttributes() - 1);
        dataTestNeg.setClassIndex(dataTestNeg.numAttributes() - 1);

        Evaluation evalPos;
        try {
            evalPos = new Evaluation(dataTrain);
            if(model == null) 
            	System.out.println("model is null");
            evalPos.evaluateModel(model, dataTestPos);
            System.out.println(evalPos.toSummaryString("\nResults\n======\n", false));            
        } catch (Exception ex) {
            Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }

        Evaluation evalNeg;
        try {
            evalNeg = new Evaluation(dataTrain);
            if(model == null) 
            	System.out.println("model is null");
            evalNeg.evaluateModel(model, dataTestNeg);
            System.out.println(evalNeg.toSummaryString("\nResults\n======\n", false));            
        } catch (Exception ex) {
            Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
        
    
    public double sameClassScore(MultimodalItem item1, MultimodalItem item2) {
        if(model == null) 
        	return 0;
        
        if(attributes == null) {
            attributes = MultimodalSimilarity.getAttributes(usedSimTypes);
        }
        MultimodalSimilarity multimodalSimilarity=new MultimodalSimilarity(item1, item2, attributes);
        try {
            if(dataTrain == null) {
                dataTrain = new Instances("tr", attributes, attributes.size());
                dataTrain.setClassIndex(dataTrain.numAttributes() - 1);
            }
            multimodalSimilarity.similarities.setDataset(dataTrain);
            return model.classifyInstance(multimodalSimilarity.similarities);
        } catch (Exception ex) {
            Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    public double sameClassScore(Event event1, Event event2) {
        if(model == null) 
        	return 0;
        
        if(attributes == null)
            attributes = MultimodalSimilarity.getEventAttributes();
        MultimodalSimilarity multimodalSimilarity = new MultimodalSimilarity(event1, event2, attributes);
        try {
            if(dataTrain == null) {
                dataTrain = new Instances("tr", attributes, attributes.size());
                dataTrain.setClassIndex(dataTrain.numAttributes() - 1);
            }
            multimodalSimilarity.similarities.setDataset(dataTrain);
            return model.classifyInstance(multimodalSimilarity.similarities);
        } catch (Exception ex) {
            Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    public double[] testProbability(MultimodalItem item1, MultimodalItem item2) {
        if(model == null) 
        	return null;
        
        if(attributes == null)
            attributes = MultimodalSimilarity.getAttributes(usedSimTypes);
        MultimodalSimilarity multimodalSimilarity = new MultimodalSimilarity(item1, item2, attributes);
        double[] probs = new double[2];
        try {
        	if(dataTrain == null) {
                dataTrain = new Instances("tr",attributes,attributes.size());
                dataTrain.setClassIndex(dataTrain.numAttributes() - 1);
            }
            multimodalSimilarity.similarities.setDataset(dataTrain);
            probs = model.distributionForInstance(multimodalSimilarity.similarities);
        } catch (Exception ex) {
            Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return probs;
    }
    
    public void save(String modelFilename) {
        if(model != null) {
            System.out.println("saving model in " + modelFilename);
            try {
                weka.core.SerializationHelper.write(modelFilename, model);
            } catch (Exception ex) {
                Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }
    
    public void save(String modelDirectory, ClassifierTypes classifierType) {
        if(!modelDirectory.endsWith(File.separator))
            modelDirectory = modelDirectory + File.separator;
        
    	String modelFile = modelDirectory + File.separator + "model." + classifierType;
    	
        File dir = new File(modelDirectory);
        if((!dir.exists()) || (!dir.isDirectory()))
            dir.mkdirs();
        if(model != null) {
            System.out.println("saving model in " + modelDirectory);
            try {
                weka.core.SerializationHelper.write(modelFile, model);
            } catch (Exception ex) {
                Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        savePositivePairsIds(modelDirectory);
        saveNegativePairsIds(modelDirectory);
    }

    private void savePositivePairsIds(String directory) {
    	if(!directory.endsWith(File.separator))
    		directory = directory + File.separator;
    	
        String filename = directory + "positive_pairs.txt";
        int n_pairs = positiveTrainingPairs.size();
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename)));
            for(int i=0; i<n_pairs; i++) {
            	TrainingPair pair = positiveTrainingPairs.get(i);
                pw.println(pair.item1.id + " " + pair.item2.id);
            }
            pw.close();
        }
        catch(IOException e) {
        }
    }

    private void saveNegativePairsIds(String directory) {
    	if(!directory.endsWith(File.separator))
    		directory = directory + File.separator;
    	
        String filename = directory + "negative_pairs.txt";
        int n_pairs = negativeTrainingPairs.size();
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename)));
			for(int i=0; i<n_pairs; i++) {
				TrainingPair pair = negativeTrainingPairs.get(i);
                pw.println(pair.item1.id + " " + pair.item2.id);
			}
            pw.close();
        }
        catch(IOException e) {
        }
    }
   
    public void load(String modelFilename) {
        File file=new File(modelFilename);
        if(file.exists()) {
            try {
            	model = (Classifier) weka.core.SerializationHelper.read(modelFilename);
            	
            	System.out.println("Capabilities: " + model.getCapabilities());
            } catch (Exception ex) {
                Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("loading model from : "+modelFilename);
        }
        else {
            model = null;
        }
    }
    
    public void load(String modelDirectory, ClassifierTypes classifierType, MultimediaCollection collection) {
        
    	String modelFile = modelDirectory + File.separator + "model." + classifierType;
    	
        File file=new File(modelFile);
        if(file.exists()) {
            try {
            	model = (Classifier) weka.core.SerializationHelper.read(modelFile);
            	
            	System.out.println("Capabilities: " + model.getCapabilities());
            } catch (Exception ex) {
                Logger.getLogger(MultimodalClassifier.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("loading model from : "+modelFile);
        }
        else {
            model = null;
        }
        //loadPositivePairsIds(modelDirectory, collection);
        //loadNegativePairsIds(modelDirectory, collection);
    }
    
    public void loadPositivePairsIds(String directory, MultimediaCollection collection) {
        try{
            String filename = directory + File.separator + "positive_pairs.txt";
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            ArrayList<String> lines = new ArrayList<String>();
            while ((strLine = br.readLine()) != null) {
                if(!strLine.trim().equals(""))
                    lines.add(strLine);
            }
            int n_pairs = lines.size();
            positiveTrainingPairs = new ArrayList<TrainingPair>(n_pairs);
            for(int i=0; i<n_pairs; i++){
                String[] parts = lines.get(i).split(" ");
                MultimodalItem item1 = collection.get(parts[0]);
                MultimodalItem item2 = collection.get(parts[1]);
                
                positiveTrainingPairs.add(new TrainingPair(item1, item2));
            }
            br.close();
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(MultimediaCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(MultimediaCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadNegativePairsIds(String directory, MultimediaCollection collection){
        try {
            String filename = directory + File.separator + "negative_pairs.txt";
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            ArrayList<String> lines=new ArrayList<String>();
            while ((strLine = br.readLine()) != null) {
                if(!strLine.trim().equals(""))
                    lines.add(strLine);
            }
            int n_pairs = lines.size();
            negativeTrainingPairs = new ArrayList<TrainingPair>(n_pairs);
            for(int i=0; i<n_pairs; i++) {
                String[] parts = lines.get(i).split(" ");
                MultimodalItem item1 = collection.get(parts[0]);
                MultimodalItem item2 = collection.get(parts[1]);
                
                positiveTrainingPairs.add(new TrainingPair(item1, item2));
            }
            
            br.close();
        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(MultimediaCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(MultimediaCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void savePositiveMultimodalSimilarities(String modelDirectory) {
    	if(!modelDirectory.endsWith(File.separator))
    		modelDirectory = modelDirectory + File.separator;
    	
        if(positiveMultimodalSimilarities != null) {
            String filename = modelDirectory + "positive_similarities.txt";
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename)));
                for(MultimodalSimilarity positiveMultimodalSimilarity : positiveMultimodalSimilarities)
                	positiveMultimodalSimilarity.saveToFile(pw);
                pw.close();
            }
            catch(IOException e) {
            }
        }
    }

    public void saveNegativeMultimodalSimilarities(String modelDirectory) {
    	if(!modelDirectory.endsWith(File.separator))
    		modelDirectory = modelDirectory + File.separator;
    	
        if(negativeMultimodalSimilarities != null) {
            String filename=modelDirectory +"negative_similarities.txt";
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename)));
                for(MultimodalSimilarity negativeMultimodalSimilarity : negativeMultimodalSimilarities)
                	negativeMultimodalSimilarity.saveToFile(pw);
                pw.close();
            }
            catch(IOException e) {
            }
        }
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

}