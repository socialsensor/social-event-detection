package clustering;


import utils.IOUtils;

import java.util.Random;
import org.apache.mahout.math.*;

public class KMeansClusterer {
    
  
private static Integer[] k_means_pl_pl(Matrix data,int n_components){
  int n_data=data.numRows();
  int n_vars=data.numCols();
  
  Integer[] centers=new Integer[n_components];
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
        temp_dist=temp_dist*temp_dist*temp_dist;
        if(temp_dist<distances[j]) distances[j]=temp_dist;
      } 
    }
    double sum_d=0.;
    for(int j=0;j<n_data;j++) sum_d=sum_d+distances[j];
    for(int j=0;j<n_data;j++) probability[j]=distances[j]/sum_d;
    double gg=random.nextDouble();
    int curr=-1;
    double acc=0.0;
    while((acc<gg)&&(curr<n_data-1)){
      curr++;
      acc=acc+probability[curr];
    }
    centers[i]=curr;
  }
  IOUtils.write1dArray2File(centers, "D:\\mediaeval_c\\initialcenters.txt");
  return centers;
  
}

    
    public static Integer[] KMeansClustererFromData(Matrix data,int max_iterations,int n_clusters){
        int n_data=data.numRows();
        int n_cols=data.numCols();
        Integer[] class_assignments=new Integer[n_data];
        Double[][] means=new Double[n_clusters][n_cols];
        Integer[] n_counts=new Integer[n_clusters];
               
        int i,j,k,l,cur_class;
        
        
        Integer[] plus_plus_initialization=k_means_pl_pl(data,n_clusters);
        for(j=0;j<n_clusters;j++)
            for(k=0;k<n_cols;k++)
                means[j][k]=data.get(plus_plus_initialization[j], k);
        
        for(j=0;j<n_data;j++){
            int min_index=0;
            double min_dist=0;
            for(k=0;k<n_cols;k++) min_dist=min_dist+(data.get(j, k)-means[min_index][k])*(data.get(j, k)-means[min_index][k]);
            double tmp_dist;
            for(k=1;k<n_clusters;k++){
                tmp_dist=0;
                for(l=0;l<n_cols;l++) tmp_dist=tmp_dist+(data.get(j, l)-means[k][l])*(data.get(j, l)-means[k][l]);
                    if(tmp_dist<min_dist){
                        min_dist=tmp_dist;
                        min_index=k;
                }
            }
            class_assignments[j]=min_index;
        }
        
        
/*        //random initializations
        for(i=0;i<n_data;i++){
            class_assignments[i] =(int) Math.floor(rnd.nextDouble()*n_clusters);
        }
  */      
        for(i=0;i<max_iterations;i++){
//            if((i%10)==0) System.out.println("Iteration : "+i);
            //Compute mean vectors for current assignment of data to classes
            for(j=0;j<n_clusters;j++)
                for(k=0;k<n_cols;k++)
                    means[j][k]=0.;
            for(j=0;j<n_clusters;j++) n_counts[j]=0;
            for(j=0;j<n_data;j++){
                cur_class=class_assignments[j];
                n_counts[cur_class]++;
                for(k=0;k<n_cols;k++) means[cur_class][k]=means[cur_class][k]+data.get(j,k); 
            }
            for(j=0;j<n_clusters;j++)
                for(k=0;k<n_cols;k++)
                    means[j][k]=means[j][k]/n_counts[j];

/*            System.out.println("MEANS:");
            for(j=0;j<n_clusters;j++){
                for(k=0;k<n_cols;k++)
                    System.out.print(means[j][k]+"\t");
                System.out.println("\n");
            }
            System.out.println("COUNTS:");
            for(j=0;j<n_clusters;j++){
                    System.out.print(n_counts[j]+"\t");
            }
                System.out.println("\n");
*/
            
            
            
            //Compute new assignments according to newly computed means
            for(j=0;j<n_data;j++){
                int min_index=0;
                double min_dist=0;
                for(k=0;k<n_cols;k++) min_dist=min_dist+(data.get(j, k)-means[min_index][k])*(data.get(j, k)-means[min_index][k]);
                double tmp_dist;
                for(k=1;k<n_clusters;k++){
                        tmp_dist=0;
                        for(l=0;l<n_cols;l++) tmp_dist=tmp_dist+(data.get(j, l)-means[k][l])*(data.get(j, l)-means[k][l]);
                        if(tmp_dist<min_dist){
                            min_dist=tmp_dist;
                            min_index=k;
                    }
                }
                class_assignments[j]=min_index;
            }
        }
        return class_assignments;
    }
    
    public static DenseMatrix ComputeSimpleMatrixOfAllPairwiseEuclideanDistances(Matrix data){
        int n_data=data.numRows();
        int n_cols=data.numCols();
        DenseMatrix distances=new DenseMatrix(n_data,n_data);
        int i,j,k;
        double tmp_dist;
        for(i=0;i<n_data;i++)
            for(j=i+1;j<n_data;j++){
                tmp_dist=0;
                for(k=0;k<n_cols;k++) tmp_dist=tmp_dist+(data.get(i,k)-data.get(j,k))*(data.get(i,k)-data.get(j,k));
                tmp_dist=Math.sqrt(tmp_dist);
                distances.set(i,j,tmp_dist);
                distances.set(j,i,tmp_dist);
            }
        return distances;
    }

    
    
}
