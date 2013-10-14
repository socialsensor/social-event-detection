package utils;

import java.io.File;

import app.Run;

public class Constants {
	
	public static String rootDir = "/disk1_data/mediaEval/2013/SED/challenge1";
	
	public static String testDir = rootDir + File.separator + "test";
	public static String testMetadataFile = testDir + File.separator + "sed2013_dataset_test.xml";
	
	public static String trainDir = rootDir + File.separator + "train";	
	public static String trainMetadataFile = trainDir + File.separator + "sed2013_dataset_train.xml";
	
	public static String resultsDir = rootDir + File.separator + "results";
	
	public static String trainEventsFile = trainDir + File.separator + "sed2013_dataset_train_gs.csv";
	//public static String testEventsFile = eventsDir + File.separator + "sed2013_events_test.txt";	
	
	/**
	 * Indexes
	 */
	public static String testIndicesDir = testDir + File.separator + "indices";
	public static String testTextualIndex = testIndicesDir + File.separator + "textual";
	public static String testSpatialIndex = testIndicesDir + File.separator + "spatial";
	public static String testVisualIndex = testIndicesDir + File.separator + "visual";
	
	public static String trainIndicesDir = trainDir + File.separator + "indices";
	public static String trainTextualIndex = trainIndicesDir + File.separator + "textual";
	public static String trainSpatialIndex = trainIndicesDir + File.separator + "spatial";
	public static String trainVisualIndex = trainIndicesDir + File.separator + "visual";
	
	// Visual Index parameters
	public static String codebookFile = rootDir + File.separator + "visual_index_learning_files/codebook.txt";
	public static String pcaFile = rootDir + File.separator + "visual_index_learning_files/pca.txt";
	public static String productQuantizerFile = rootDir + File.separator + "visual_index_learning_files/qproduct_adc_256x1024.txt"; 
    public static int VectorLength = 1024;
    public static int subVectorLength = 4;
    public static int numProductCentroids = 1024;
    public static int MaxIndexCapacity = 400000;
    
    public static String TRAIN_VISUAL_FEAT_DIR = trainDir + File.separator + "images/visualFeatures";
    public static String TEST_VISUAL_FEAT_DIR = testDir + File.separator + "images/visualFeatures";
    public static String TRAIN_IMAGES_DIR = trainDir + File.separator + "images/jpeg";
    public static String TEST_IMAGES_DIR = testDir + File.separator + "images/jpeg";
    
	public static String candidatesFolder = rootDir + File.separator + "CandidateNeighbours";
    


    public static int TEXT_NN = 150;
    public static int TIME_NN = 500;
    public static int LOCATION_NN = 150;
    public static int USER_NN = 100;
    public static int VISUAL_NN = 50;
    
    public static String GRAPH_DIR = rootDir + File.separator + "graphs";
    public static String HUBS_FILENAME = GRAPH_DIR + File.separator + "hubs.txt";
    
    public static double CONNECTION_THRESHOLD = 0.5;
    
	
    /**
     * Clustering
     */
    public static final double SCAN_EPSILON = 0.7;
    public static final int SCAN_MU = 3;
    
    public static int ATTACHMENT_THRESHOLD = 5;
    public static boolean PostProcessing = true;
    public static boolean UsetHubsOutliers = true;
    
    
    /**
     *  Classification
     */
    public static String modelDirectory = rootDir + File.separator + "sameClassModel";
    public static ClassifierTypes CLASSIFIER_TYPE = ClassifierTypes.svm;
    
    public static int nNegativeExamples = 20000;
	public static int nPositiveExamples = 20000;
	
	
    //This option determines if the same cluster predictions are discrete (0/1) or continuous
    public static boolean SAME_CLASS_HARD_ASSIGNMENT = true;
    
    public static enum ClassifierTypes {
    	svm,
    	decisionTree,
    	part,
    	jrip,
    	randomTree,
    	randomForest,
    	MultilayerPerceptron,
    	NaiveBayes,
    	NearestNeighbour
    };
    
    public static String SVM_PARAMETERS = "-C 1 -M";
    public static String DECISION_TREE_PARAMETERS = "-M 5";
    public static String PART_PARAMETERS = "";
    public static String JRIP_PARAMETERS = "";
    public static String RANDOM_TREE_PARAMETERS = "";
    public static String RANDOM_FOREST_PARAMETERS = "";
    public static String MULTILAYER_PERCEPTRON_PARAMETERS = "";
    public static String NAIVE_BAYES_PARAMETERS = "";
    public static String NEAREST_NEIGHBOUR_PARAMETERS = "";
    
    //What types of similarity there are between multimodal items
    public static enum SIM_TYPES {
    	TIME_TAKEN,
    	TIME_UPLOADED,
    	TIME_TAKEN_HOUR_DIFF_12,
    	TIME_TAKEN_HOUR_DIFF_24,
    	TIME_TAKEN_DAY_DIFF_3,
    	LOCATION,
    	SAME_USER,
    	VLAD_SURF,
    	TITLE_COSINE,
    	TITLE_BM25,
    	TAGS_COSINE,
    	TAGS_BM25,
    	DESCRIPTION_COSINE,
    	DESCRIPTION_BM25
    };  

    public static SIM_TYPES[] usedEventSimTypes = {
    	SIM_TYPES.SAME_USER,
    	SIM_TYPES.TIME_TAKEN,
    	SIM_TYPES.TIME_UPLOADED,
    	SIM_TYPES.TIME_TAKEN_DAY_DIFF_3,
    	SIM_TYPES.TIME_TAKEN_HOUR_DIFF_12,
    	SIM_TYPES.TIME_TAKEN_HOUR_DIFF_24,
    	SIM_TYPES.TITLE_COSINE,
    	SIM_TYPES.TITLE_BM25,
    	SIM_TYPES.TAGS_COSINE,
    	SIM_TYPES.TAGS_BM25,
    	SIM_TYPES.DESCRIPTION_COSINE,
    	SIM_TYPES.DESCRIPTION_BM25,
    	SIM_TYPES.VLAD_SURF
    	
    };  
    
    public static SIM_TYPES[] usedSimTypes1 = {
    	SIM_TYPES.SAME_USER,
    	SIM_TYPES.TIME_TAKEN,
    	SIM_TYPES.TIME_UPLOADED,
    	SIM_TYPES.TIME_TAKEN_DAY_DIFF_3,
    	SIM_TYPES.TIME_TAKEN_HOUR_DIFF_12,
    	SIM_TYPES.TIME_TAKEN_HOUR_DIFF_24,
    	SIM_TYPES.TITLE_COSINE,
    	SIM_TYPES.TITLE_BM25,
    	SIM_TYPES.TAGS_COSINE,
    	SIM_TYPES.TAGS_BM25,
    	SIM_TYPES.DESCRIPTION_COSINE,
    	SIM_TYPES.DESCRIPTION_BM25,
    };
    
    public static SIM_TYPES[] usedSimTypes2 = {
    	Constants.SIM_TYPES.SAME_USER
    };
    
    public static SIM_TYPES[] usedSimTypes3 = {
    	SIM_TYPES.TIME_TAKEN,
    	SIM_TYPES.TIME_UPLOADED,
    	SIM_TYPES.TIME_TAKEN_DAY_DIFF_3,
    	SIM_TYPES.TIME_TAKEN_HOUR_DIFF_12,
    	SIM_TYPES.TIME_TAKEN_HOUR_DIFF_24,
    	SIM_TYPES.TITLE_COSINE,
    	SIM_TYPES.TITLE_BM25,
    	SIM_TYPES.TAGS_COSINE,
    	SIM_TYPES.TAGS_BM25,
    	SIM_TYPES.DESCRIPTION_COSINE,
    	SIM_TYPES.DESCRIPTION_BM25,
    };
    
    public static SIM_TYPES[] usedSimTypes4 = {
    	SIM_TYPES.TIME_TAKEN,
    	SIM_TYPES.TIME_UPLOADED,
    	SIM_TYPES.TIME_TAKEN_DAY_DIFF_3,
    	SIM_TYPES.TIME_TAKEN_HOUR_DIFF_12,
    	SIM_TYPES.TIME_TAKEN_HOUR_DIFF_24, 
    	SIM_TYPES.VLAD_SURF
    };
    
    public static SIM_TYPES[] usedSimTypes5 = {
    	SIM_TYPES.SAME_USER,
    	SIM_TYPES.TIME_TAKEN,
    	SIM_TYPES.TIME_UPLOADED,
    	SIM_TYPES.TIME_TAKEN_DAY_DIFF_3,
    	SIM_TYPES.TIME_TAKEN_HOUR_DIFF_12,
    	SIM_TYPES.TIME_TAKEN_HOUR_DIFF_24,
    	SIM_TYPES.TITLE_COSINE,
    	SIM_TYPES.TITLE_BM25,
    	SIM_TYPES.TAGS_COSINE,
    	SIM_TYPES.TAGS_BM25,
    	SIM_TYPES.DESCRIPTION_COSINE,
    	SIM_TYPES.DESCRIPTION_BM25,
    	SIM_TYPES.VLAD_SURF
    };
    
    public static Run[] runs = {
			new Run(1, usedSimTypes1),
			new Run(2, usedSimTypes2),
			new Run(3, usedSimTypes2),
			new Run(4, usedSimTypes4),
			new Run(5, usedSimTypes5)
	};

}


