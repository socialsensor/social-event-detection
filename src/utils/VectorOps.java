/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author gpetkos
 */
public class VectorOps {
    public static double computeVectorLength(double[] vector){
        double vectorLength=0.0;
        int n_length=vector.length;
        int i;
        for(i=0;i<n_length;i++) vectorLength=vectorLength+vector[i]*vector[i];
        vectorLength=Math.sqrt(vectorLength);
        return vectorLength;
    }

    public static double computeVectorLength(Integer[] vector){
        double vectorLength=0.0;
        int n_length=vector.length;
        int i;
        for(i=0;i<n_length;i++) vectorLength=vectorLength+vector[i]*vector[i];
        vectorLength=Math.sqrt(vectorLength);
        return vectorLength;
    }

    public static double innerProduct(Integer[] vector1,Integer[] vector2){
	double num=0.0;
        int n_length=vector1.length;
	for(int i = 0; i<n_length; i++){
            num=num+(vector1[i]*vector2[i]);
	}
        return num;
    }

    public static double innerProduct(double[] vector1,double[] vector2){
	double num=0.0;
        int n_length=vector1.length;
	for(int i = 0; i<n_length; i++){
            num=num+(vector1[i]*vector2[i]);
	}
        return num;
    }
    
    public static double cosineSimilarity(Integer[] vector1,Integer[] vector2){
        double length1=VectorOps.computeVectorLength(vector1);
        double length2=VectorOps.computeVectorLength(vector2);
        double den=length1*length2;
        if(den < 0.0000000001){
            return 0.0;
         }
        double num=innerProduct(vector1,vector2);
	return num / den;
    }

    public static double cosineSimilarity(double[] vector1,double[] vector2){
        double length1=VectorOps.computeVectorLength(vector1);
        double length2=VectorOps.computeVectorLength(vector2);
        double den=length1*length2;
        if(den < 0.0000000001){
            return 0.0;
         }
        double num=Math.sqrt(innerProduct(vector1,vector2));
	return num / den;
    }
    
    
}