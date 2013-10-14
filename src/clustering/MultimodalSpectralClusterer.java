/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustering;

import utils.IOUtils;
import utils.MatrixOps;

import org.apache.mahout.math.*;

public class MultimodalSpectralClusterer {
    
    public static Integer[] MultimodalSpectralClusteringFromAffinityMatrices(Matrix[] affinities,int max_iterations,int n_clusters,int k_knn,double alpha,SpectralClusterer.LaplacianType laplacian_mode,boolean useLanczos, int k_means_iterations,int max_opt_iterations){
        int n_modalities=affinities.length;
        int n_data=affinities[0].numRows();
        Matrix[] laplacianMatrices=new Matrix[n_modalities];
        int i,j,k;
        for(i=0;i<n_modalities;i++) laplacianMatrices[i]=SpectralClusterer.ComputeLaplacianFromAffinity(affinities[i], laplacian_mode);
        boolean nan_found=false;
        int modality_found=-1;
        for(i=0;i<n_modalities;i++)
            for(j=0;j<n_data;j++)
                for(k=0;k<n_data;k++)
                    if((new Double(laplacianMatrices[i].get(j,k))).isNaN()){
                        nan_found=true;
                        modality_found=i;
                        laplacianMatrices[i].set(j,k,0);
                    }
        if (nan_found) System.out.println("Nan found in computed single mode laplacians "+modality_found);
        System.out.println("Computing multimodal laplacian matrix");
        Matrix multimodalLaplacian=ComputeMultimodalLaplacian(laplacianMatrices, alpha);

        nan_found=false;
        for(j=0;j<n_data;j++)
            for(k=0;k<n_data;k++)
                if((new Double(multimodalLaplacian.get(j,k))).isNaN()){
                    nan_found=true;
                    multimodalLaplacian.set(j,k,0);
                }
        if (nan_found) System.out.println("Nan found after computation of multimodal laplacian");
        
        System.out.println("Computing eigenvectors of multimodal laplacian");
        Matrix eigs_n=MatrixOps.ComputeEigenvectors(multimodalLaplacian,n_clusters,useLanczos,false,false,"");

        nan_found=false;
        for(j=0;j<eigs_n.numRows();j++)
            for(k=0;k<eigs_n.numCols();k++)
                if((new Double(eigs_n.get(j,k))).isNaN()){
                    nan_found=true;
                    eigs_n.set(j,k,0);
                }
        if (nan_found) System.out.println("Nan found during computation of the eigenvectors");
        
        MatrixOps.NormalizeRows(eigs_n);
        Integer[] assignments;      
        
        System.out.println("Computing first assignment of items to clusters");
        assignments=KMeansClusterer.KMeansClustererFromData(eigs_n,k_means_iterations, n_clusters);
        Matrix clusteringIndicator=assignmentToClusteringIndicatorMatrix(assignments, n_clusters);
        
        IOUtils.write1dArray2File(assignments,"d:\\mediaeval_c\\locations_assignments_pre.txt");

        
        //iterate on clustering indicator matrix
        clusteringIndicator=clusteringIndicator.plus(0.2);
        double diff=Double.MAX_VALUE;
        double threshold=0.00;
        Matrix nom;
        Matrix denom;
        DenseMatrix clusteringIndicator_tmp=new DenseMatrix(n_data,n_clusters);
        int n_iterations=0;
        IOUtils.write2dArray2File(clusteringIndicator,"d:\\mediaeval_c\\indicator_matrix_0.txt","\t");
        
        System.out.println("Iterative optimization started");
        while((diff>threshold)&&(n_iterations<max_opt_iterations)){
            n_iterations++;
            System.out.println("Optimization iteration : "+n_iterations);
            nom=multimodalLaplacian.times(clusteringIndicator);
            denom=clusteringIndicator.times(clusteringIndicator.transpose()).times(nom);
            for(i=0;i<n_data;i++)
                for(j=0;j<n_clusters;j++)
                    nom.set(i,j,Math.sqrt(nom.get(i,j)/denom.get(i,j)));
            for(i=0;i<n_data;i++)
                for(j=0;j<n_clusters;j++)
                    clusteringIndicator_tmp.set(i,j,clusteringIndicator.get(i,j)*nom.get(i,j));
            diff=0.;
            for(i=0;i<n_data;i++)
                for(j=0;j<n_clusters;j++)
                    diff=diff+Math.abs(clusteringIndicator_tmp.get(i,j)-clusteringIndicator.get(i,j));
            clusteringIndicator=clusteringIndicator_tmp;
        }
        double max;
        int pos=0;
        for(i=0;i<n_data;i++){
            max=Double.MIN_VALUE;
            for(j=0;j<n_clusters;j++)
                if(clusteringIndicator.get(i,j)>max){
                    max=clusteringIndicator.get(i,j);
                    pos=j;
                }
            assignments[i]=pos;
        }
        System.out.println("Completed");
        
        return assignments;


    }

    public static Integer[] MultimodalSpectralClustering(Matrix[] modalities_data,int max_iterations,int n_clusters,int k_knn,double alpha){
        //parameters to be placed in the function header
        boolean useLanczos=true;
        int k_means_iterations=20;
        int max_opt_iterations=20;
                
        int n_modalities=modalities_data.length;
        int n_data=modalities_data[0].numRows();
        Matrix[] laplacianMatrices=new Matrix[n_modalities];
        int i,j;
        System.out.println("Computing individual laplacian matrices");
        for(i=0;i<n_modalities;i++) laplacianMatrices[i]=SpectralClusterer.ComputeLaplacianMatrixKnn(modalities_data[i],k_knn,SpectralClusterer.LaplacianType.UNNORMALIZED_LAPLACIAN);
        System.out.println("Computing multimodal laplacian matrix");
        Matrix multimodalLaplacian=ComputeMultimodalLaplacian(laplacianMatrices, alpha);
        System.out.println("Computing eigenvectors of multimodal laplacian");
        Matrix eigs_n=MatrixOps.ComputeEigenvectors(multimodalLaplacian,n_clusters,useLanczos,false,false,"");
        MatrixOps.NormalizeRows(eigs_n);
        Integer[] assignments;      
        
        System.out.println("Computing first assignment of items to clusters");
        assignments=KMeansClusterer.KMeansClustererFromData(eigs_n,k_means_iterations, n_clusters);
        Matrix clusteringIndicator=assignmentToClusteringIndicatorMatrix(assignments, n_clusters);
        
        IOUtils.write1dArray2File(assignments,"d:\\mediaeval_c\\locations_assignments_pre.txt");

        
        //iterate on clustering indicator matrix
        clusteringIndicator=clusteringIndicator.plus(0.2);
        double diff=Double.MAX_VALUE;
        double threshold=0.00;
        Matrix nom;
        Matrix denom;
        DenseMatrix clusteringIndicator_tmp=new DenseMatrix(n_data,n_clusters);
        int n_iterations=0;
        IOUtils.write2dArray2File(clusteringIndicator,"d:\\mediaeval_c\\indicator_matrix_0.txt","\t");
        
        System.out.println("Iterative optimization started");
        while((diff>threshold)&&(n_iterations<max_opt_iterations)){
            n_iterations++;
            System.out.println("Optimization iteration : "+n_iterations);
            nom=multimodalLaplacian.times(clusteringIndicator);
            denom=clusteringIndicator.times(clusteringIndicator.transpose()).times(nom);
            for(i=0;i<n_data;i++)
                for(j=0;j<n_clusters;j++)
                    nom.set(i,j,Math.sqrt(nom.get(i,j)/denom.get(i,j)));
            for(i=0;i<n_data;i++)
                for(j=0;j<n_clusters;j++)
                    clusteringIndicator_tmp.set(i,j,clusteringIndicator.get(i,j)*nom.get(i,j));
            diff=0.;
            for(i=0;i<n_data;i++)
                for(j=0;j<n_clusters;j++)
                    diff=diff+Math.abs(clusteringIndicator_tmp.get(i,j)-clusteringIndicator.get(i,j));
            clusteringIndicator=clusteringIndicator_tmp;
        }
        double max;
        int pos=0;
        for(i=0;i<n_data;i++){
            max=Double.MIN_VALUE;
            for(j=0;j<n_clusters;j++)
                if(clusteringIndicator.get(i,j)>max){
                    max=clusteringIndicator.get(i,j);
                    pos=j;
                }
            assignments[i]=pos;
        }
        System.out.println("Completed");
        
        return assignments;

    }

    public static Matrix ComputeMultimodalLaplacian(Matrix[] laplacians,double alpha){
        int n_modalities=laplacians.length;
        int n_data=laplacians[0].numRows();
        SparseMatrix multimodalLaplacian=new SparseMatrix(n_data,n_data);
        SparseMatrix tmpEyeMatrix=new SparseMatrix(n_data,n_data);
        for(int i=0;i<n_data;i++) 
        	tmpEyeMatrix.set(i,i,alpha);
        Matrix tmp_inv;
        for(int i=0;i<n_modalities;i++) {
            tmp_inv=MatrixOps.invertBySVD(laplacians[i].plus(tmpEyeMatrix));
            multimodalLaplacian=(SparseMatrix) multimodalLaplacian.plus(tmp_inv);
        }
        return multimodalLaplacian;
    }
    
    public static Matrix assignmentToClusteringIndicatorMatrix(Integer[] assignments,int n_clusters){
        int n_data=assignments.length;
        int i;
        SparseMatrix clusteringIndicator=new SparseMatrix(n_data,n_clusters);
        for(i=0;i<n_data;i++) clusteringIndicator.set(i,assignments[i],1.0);
        return clusteringIndicator;
    }
    
    public static Integer[] clusteringIndicatorMatrixToAssignments(Matrix clustering_indicator){
        int n_data=clustering_indicator.numRows();
        int n_clusters=clustering_indicator.numCols();
        int i,j;
        Integer[] assignments=new Integer[n_data];
        for(i=0;i<n_data;i++) 
            for(j=0;j<n_clusters;j++)
                if(clustering_indicator.get(j, j)!=0.0){
                    assignments[i]=j;
                    j=n_clusters;
                }
        return assignments;
    }

}
