/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;

import java.util.Random;
import org.apache.mahout.math.*;

public class KMedoidsClusterer {

    public static Integer[] KMedoidsClustererFromData(Matrix data,int max_iterations,int n_clusters){

//    int[] k_medoids(float[][] data,int iterations,int n_components){
  int n_data=data.numRows();
  int n_vars=data.numCols();
  int[] medoids=new int[n_clusters];  
  //initialize assignments of data
  Integer[] assignments=new Integer[n_data];
  
//  Double[][] distances=MatrixOps.ComputeArrayOfAllPairwiseEuclideanDistances(data);

  medoids=k_means_pl_pl(data,n_clusters);
 
  int k;
  for(int i=0;i<max_iterations;i++){
     
    for(int j=0;j<n_data;j++){
       double tmp_dist=0.0;
       for(k=0;k<n_vars;k++) tmp_dist=tmp_dist+(data.get(j, k)-data.get(medoids[0], k))*(data.get(j, k)-data.get(medoids[0], k));
       double min_el=tmp_dist;
       assignments[j]=0;
       for(k=1;k<n_clusters;k++){
         tmp_dist=0.0;
         for(int l=0;l<n_vars;l++) tmp_dist=tmp_dist+(data.get(j, l)-data.get(medoids[k], l))*(data.get(j, l)-data.get(medoids[k], l));
         if(tmp_dist<min_el){
           min_el=tmp_dist;
           assignments[j]=k;
         }  
       }
    }    


     for(int j=0;j<n_clusters;j++){
      double min_dist=Double.MAX_VALUE;
      for(k=0;k<n_data;k++)
       if(assignments[k]==j){
         double medoid_dist=0;
         for(int l=0;l<n_data;l++)
           if(assignments[l]==j){
               double tmp_dist=0;
               for(int m=0;m<n_vars;m++) tmp_dist=tmp_dist+(data.get(k, m)-data.get(l, m))*(data.get(k, m)-data.get(l, m));
               medoid_dist=tmp_dist;
           }
         if(medoid_dist<min_dist){
          min_dist=medoid_dist;
          medoids[j]=k; 
         }  
       }       
     }    
   
  }
  
  return assignments;
  
}

private static int[] k_means_pl_pl(Matrix data,int n_components){
  int n_data=data.numRows();
  int n_vars=data.numCols();
  
  int[] centers=new int[n_components];
  Random random=new Random();
  centers[0]=(int) Math.floor(random.nextDouble()*n_components);
  double[] distances=new double[n_data];
  double[] probability=new double[n_data];
  
  
  for(int i=1;i<n_components;i++){
    //compute_distances
    for(int j=0;j<n_data;j++){
      distances[j]=Double.MAX_VALUE;
      for(int k=0;k<i;k++){
        double temp_dist=0.;
        for(int l=0;l<n_vars;l++) temp_dist=temp_dist+(data.get(centers[k],l)-data.get(j,l))*(data.get(centers[k],l)-data.get(j,l));
        if(temp_dist<distances[j]) distances[j]=temp_dist;
      } 
    }
    double sum_d=0.;
    for(int j=0;j<n_data;j++) sum_d=sum_d+distances[j];
    for(int j=0;j<n_data;j++) probability[j]=distances[j]/sum_d;
    double gg=random.nextDouble();
    double acc=0.;
    int curr=0;
    while(acc<gg){
      acc=acc+probability[curr];
      curr++;
    }
    centers[i]=curr;
  }
  return centers;
  
}
    
    
    
}
