package utils;

import java.util.ArrayList;
import java.io.*;
import java.util.Collection;
import java.util.List;
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SparseMatrix;

public class IOUtils {

    public static List<List<String>> loadArrayListOfArrayLists(String filename, String separator) {
        List< List<String>> result = new ArrayList<List<String>>();

        String line;
        int n_vars = 0;
        String[] parts;
        ArrayList<String> tmp_arraylist;
        int i;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), "UTF8"));
            line = null;
            while ((line = reader.readLine()) != null) {
                parts = line.split(separator);
                n_vars = parts.length;
                if (n_vars > 0) {
                    tmp_arraylist = new ArrayList<String>();
                    for (i = 0; i < n_vars; i++) {
                        tmp_arraylist.add(parts[i]);
                    }
                    result.add(tmp_arraylist);
                }
            }

            reader.close();
            return result;

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
        return null;
    }

    public static String[] loa1DArray(String filename) {
        String[] lines=null;
        String line;
        int n_lines = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), "UTF8"));
            line = null;
            while ((line = reader.readLine()) != null) {
                n_lines++;
            }
            lines=new String[n_lines];
            reader.close();
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), "UTF8"));
            line = null;
            n_lines=0;
            while ((line = reader.readLine()) != null) {
                lines[n_lines]=line;
                n_lines++;
            }
            reader.close();
            return lines;

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
        return null;
    }

    
    public static void write1dArray2File(String[] table, String textFile) {
        BufferedWriter writer = null;
        int dim1 = table.length;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(textFile), "UTF8"));
            for (int i = 0; i < dim1; i++) {
                writer.append(table[i]);
                writer.newLine();
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

    public static void write1dArray2File(double[] table, String textFile) {
        BufferedWriter writer = null;
        int dim1 = table.length;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(textFile), "UTF8"));
           
            for (int i = 0; i < dim1; i++) {
                writer.append(table[i]+"");
                writer.newLine();
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
    
    
    public static void readSiftFeatures(String filename,String separator){
        String line;
        int n_vars = 0;
        String[] parts;
        BufferedReader reader = null;
        int min_length=Integer.MAX_VALUE;
        int max_length=Integer.MIN_VALUE;
        
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename), "UTF8"));
            line = null;
            while ((line = reader.readLine()) != null) {
                parts = line.split(separator);
                n_vars = parts.length;
                if(n_vars<min_length) min_length=n_vars;
                if(n_vars>max_length) max_length=n_vars;
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

	public static void writeStringCollectionToFile(Collection<String> stringCollection, String textFile){
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(textFile), "UTF8"));
			for (String line : stringCollection){
				writer.append(line);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e){
			if (writer != null){
				e.printStackTrace();
				try {
					writer.close();
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		} 
	}
    
	public static List<String> readFileToStringList(String textFile){
		List<String> stringList = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(textFile), "UTF8"));
			String line = null;
			while ( (line = reader.readLine()) != null){
				stringList.add(line);
			}
			reader.close();
		} catch (IOException e){
			e.printStackTrace();
			if (reader != null){
				try {
					reader.close();
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		}
		return stringList;
	}
    
    public static Matrix loadDataToMatrix(String filename,String separator,boolean dense){
        Matrix data;
        String line="";
        int n_lines=0;
        int n_vars=0;
        String[] parts;
        BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename), "UTF8"));
			line = null;
                        if ( (line = reader.readLine()) != null){
                            n_lines++;
                            parts=line.split(separator);
                            n_vars=parts.length;
                        }
			while ( (line = reader.readLine()) != null) n_lines++;

                        reader.close();
			reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename), "UTF8"));

                        if(dense)
                            data=new DenseMatrix(n_lines,n_vars);
                        else
                            data=new SparseMatrix(n_lines,n_vars);
                        
                    
                        int n_counter=0;
                        int i;
			while ( (line = reader.readLine()) != null){
                            parts=line.split(separator);
                            for(i=0;i<n_vars;i++) 
                                data.set(n_counter,i,Double.parseDouble(parts[i]));
                            n_counter++;
                        }
                        
                        reader.close();
                        return data;

		} catch (IOException e){
			e.printStackTrace();
			if (reader != null){
				try {
					reader.close();
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		}
                return null;
    }

    	public static void write1dArray2File(Integer[] table, String textFile){
		BufferedWriter writer = null;
                int dim1=table.length;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(textFile), "UTF8"));
                        for(int i=0;i<dim1;i++) {
                            writer.append(Integer.toString(table[i]));
                            writer.newLine();
                        }
			writer.close();
		} catch (IOException e){
			if (writer != null){
				e.printStackTrace();
				try {
					writer.close();
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		} 
	}

    	public static void write2dArray2File(Matrix table, String textFile,String delimiter){
		BufferedWriter writer = null;
                int dim1=table.numRows();
                int dim2=table.numCols();
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(textFile), "UTF8"));
                        int i=0;
                        int j=0;
                        for(i=0;i<dim1;i++){
                            for(j=0;j<dim2-1;j++){
                                writer.append(table.get(i, j)+delimiter);
                            }
                            writer.append(table.get(i,j)+"");
                            writer.newLine();
                        }
			writer.close();
		} catch (IOException e){
			if (writer != null){
				e.printStackTrace();
				try {
					writer.close();
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		} 
	}

    	public static void write2dArray2File(double[][] table, String textFile,String delimiter){
		BufferedWriter writer = null;
                int dim1=table.length;
                int dim2=table[0].length;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(textFile), "UTF8"));
                        int i=0;
                        int j=0;
                        for(i=0;i<dim1;i++){
                            for(j=0;j<dim2-1;j++){
                                writer.append(table[i][j]+delimiter);
                            }
                            writer.append(table[i][j]+"");
                            writer.newLine();
                        }
			writer.close();
		} catch (IOException e){
			if (writer != null){
				e.printStackTrace();
				try {
					writer.close();
				} catch (IOException ex){
					ex.printStackTrace();
				}
			}
		} 
	}
        
        
    	public static void createDirectory(String directory) {
		File dir = new File(directory);
		if (!dir.exists()){
			if (!dir.mkdirs()){
				throw new IllegalStateException("Could not create directory: " + directory);
			}
		}
	}

    	public static String getFilePath(String rootDir, Long tempMember, String extension) {
    		String memberStringValue = tempMember.toString();

    		while(memberStringValue.length() < 12){
    			memberStringValue = "0" + memberStringValue;
    		}
    		
    		String filepath = rootDir + 
    						  memberStringValue.substring(0, 3) + 
    						  File.separator + memberStringValue.substring(3, 6) + 
    						  File.separator + memberStringValue.substring(6, 9) +  
    						  File.separator + tempMember.toString() + extension;
    		
    		return filepath;
    	}

		public static double[] readFeatureVectorFromFile(String featureFile, int featuresNum){
			//System.out.println(featureFile);
			double[] vector = new double[featuresNum];
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(featureFile)));
				String line = null;
				int i=0;
				while ( (line = reader.readLine()) != null){
					vector[i++] = Double.parseDouble(line);
					if(i==featuresNum)
						break;
				}
				reader.close();
			} catch (IOException e){
				e.printStackTrace();
				if (reader != null){
					try {
						reader.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			return vector;
		}



        
}