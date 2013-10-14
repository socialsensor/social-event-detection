package indexing.visual;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import models.MultimodalItem;

import org.apache.commons.io.IOUtils;

import eu.socialsensor.visual.datastructures.VladArray;
import eu.socialsensor.visual.utilities.Result;
import eu.socialsensor.visual.vectorization.ImageVectorizer;

import utils.Constants;

public class VisualIndex {

	private ImageVectorizer vectorizer;
	private VladArray index;
	
	public VisualIndex(String indexFolder, String codebookFile, String PCAFile) throws Exception {
				
		vectorizer = new ImageVectorizer(codebookFile, PCAFile, true);
		index = new VladArray(Constants.VectorLength, 0, Constants.MaxIndexCapacity, indexFolder, true, true, true);
	}

	public int size() {
		return index.getLoadCounter();
	}
	
	public void loadVectorsFromFiles(String folder) {
		File featuresFolder = new File(folder);
		File[] featureFiles = featuresFolder.listFiles();
		for(File file : featureFiles) {
			try {
				String id = file.getName().replaceAll(".vlad", "");
				double[] vector = loadVector(file);
				index.indexVector(id, vector);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	public boolean index(String id, double[] vector) {
		try {
			return index.indexVector(id, vector);
		} catch (Exception e) {
			return false;
		}
	}
	
	public double[] loadVector(File file) {    	
    	if(!file.exists())
    		return null;
    	
    	BufferedReader reader = null;
    	try {
    		reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF8"));
        	String line = reader.readLine();
        	reader.close();
        	if(line == null)
        		return null;
        	
        	line = line.substring(1, line.length()-1);
        	String[] parts = line.split(",");
        	double[] vector = new double[parts.length];
        	
            for(int i=0; i<parts.length; i++) {
            	vector[i] = Double.parseDouble(parts[i].trim());
            }
            return vector;
        } catch (IOException e) {
            e.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        }
	}
	
	public Set<String> search(MultimodalItem item, int k) {
		Set<String> ids = new HashSet<String>();
		try {
			String id = item.id;
			Result[] nn = index.computeNearestNeighbors(k, id);
			for(Result result : nn) {
				String resultId = result.getExternalId();
				if(id.equals(resultId))
					continue;
				
				ids.add(resultId);
			}
		} catch (Exception e) { }
		
		return ids;
	}
	
	public double getDistance(MultimodalItem item1, MultimodalItem item2) {
		try {		
			int vId1 = index.getInternalId(item1.id, null);
			double[] vector1 = index.getVector(vId1);
			
			int vId2 = index.getInternalId(item2.id, null);
			double[] vector2 = index.getVector(vId2);
			
			if(vector1 == null || vector2 == null)
	    		return 2;
	    	
	    	double dif = 0;
	        for(int i=0; i<vector1.length; i++) {
	            dif += Math.pow(vector1[i] - vector2[i], 2);
	        }
	        return Math.sqrt(dif);
	        
		} catch (Exception e) { 
			return 2;
		}
	}
	
	public double[] getVector(MultimodalItem item) {
		return getVector(item.id);
	}
	
	public double[] getVector(String id) {
		try {
			int vId = index.getInternalId(id, null);
			double[] vector = index.getVector(vId);
			return vector;
		} catch (Exception e) { }
		return null;
	}
	
	public void createIndex(String imagesFolder, String vfFolder) {
		
		File folder = new File(imagesFolder);
		
		System.out.println(imagesFolder);
		System.out.println(vfFolder);
		
		File[] imageFiles = folder.listFiles();
		System.out.println(imageFiles.length + " images to process.");
		
		int numOfThreads = 32;
		int batchLength = imageFiles.length / numOfThreads;
		Thread[] threads = new Thread[numOfThreads];
		for(int i=0; i<numOfThreads; i++) {
			threads[i] = new IndexThread(i*batchLength, Math.min((i+1)*batchLength, imageFiles.length)
					, imageFiles, vfFolder);
			threads[i].start();
		}
	}
	
	private class IndexThread extends Thread {

		private int startIndex, endIndex;
		private File[] imageFiles;
		private String vfFolder;

		IndexThread(int startIndex, int endIndex, File[] imageFiles, String vfFolder) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.imageFiles = imageFiles;
			this.vfFolder = vfFolder;
		}
		
		@Override
		public void run() {
			System.out.println(this.getName() +" index from " + startIndex + " to " + endIndex);
			for(int i=startIndex ; i<endIndex; i++) {
				File imageFile = imageFiles[i];
				String id = imageFile.getName().replaceAll(".jpg", "");
				double[] vector;
				try {
					vector = vectorizer.transformToVector(imageFile.toString());
					index.indexVector(id, vector);
					
					String str = Arrays.toString(vector);
					IOUtils.write(str, new FileOutputStream(vfFolder + "/" + id + ".vlad"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		VisualIndex vIndex = new VisualIndex(
				Constants.testVisualIndex, 
				Constants.codebookFile, 
				Constants.pcaFile);
		
		vIndex.createIndex(Constants.TEST_IMAGES_DIR, Constants.TEST_VISUAL_FEAT_DIR);
	}
}
