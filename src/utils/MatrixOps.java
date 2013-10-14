/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import org.apache.mahout.math.*;
import org.apache.mahout.math.decomposer.lanczos.LanczosSolver;
import org.apache.mahout.math.decomposer.lanczos.LanczosState;

public class MatrixOps {
    
    public static void NormalizeRows(Matrix matrix){
        int n_rows=matrix.numRows();
        int n_cols=matrix.numCols();
        int i,j;
        double tmp_sum;
        for(i=0;i<n_rows;i++){
            tmp_sum=0;
            for(j=0;j<n_cols;j++) tmp_sum=tmp_sum+matrix.get(i, j);
            for(j=0;j<n_cols;j++) matrix.set(i, j,matrix.get(i,j)/tmp_sum);
        }
    }

    public static void NormalizeCols(Matrix matrix){
        int n_rows=matrix.numRows();
        int n_cols=matrix.numCols();
        int i,j;
        double tmp_sum;
        for(i=0;i<n_cols;i++){
            tmp_sum=0;
            for(j=0;j<n_rows;j++) tmp_sum=tmp_sum+matrix.get(j, i);
            for(j=0;j<n_rows;j++) matrix.set(j, i,matrix.get(j,i)/tmp_sum);
        }
    }

    public static Double[][] ComputeArrayOfAllPairwiseEuclideanDistances(Matrix data){
        int n_data=data.numRows();
        int n_cols=data.numCols();
        Double[][] distances=new Double[n_data][n_data];
        int i,j,k;
        double tmp_dist;
        for(i=0;i<n_data;i++)
            for(j=i+1;j<n_data;j++){
                tmp_dist=0;
                for(k=0;k<n_cols;k++) tmp_dist=tmp_dist+(data.get(i,k)-data.get(j,k))*(data.get(i,k)-data.get(j,k));
                tmp_dist=Math.sqrt(tmp_dist);
                distances[i][j]=tmp_dist;
                distances[j][i]=tmp_dist;
            }
        return distances;
    }
    
    //Multiplies (pointwise) all rows of the data by the vector scaling_factos
    //and returns a new matrix
    public static Matrix scaleColumnsMult(Matrix data,double[] scaling_factors){
        int n_data=data.numRows();
        int n_cols=data.numCols();
        DenseMatrix output_matrix=new DenseMatrix(n_data,n_cols);
        if(n_cols!=scaling_factors.length) return null;
        int i,j;
        for(i=0;i<n_data;i++)
            for(j=0;j<n_cols;j++)
                output_matrix.set(i,j,data.get(i,j)*scaling_factors[j]);
        return output_matrix;
    }
    
    //Divides (pointwise) all rows of the data by the vector scaling_factos
    //and returns a new matrix
    public static Matrix scaleColumnsDiv(Matrix data,double[] scaling_factors){
        int n_data=data.numRows();
        int n_cols=data.numCols();
        DenseMatrix output_matrix=new DenseMatrix(n_data,n_cols);
        if(n_cols!=scaling_factors.length) return null;
        int i,j;
        for(i=0;i<n_data;i++)
            for(j=0;j<n_cols;j++)
                output_matrix.set(i,j,data.get(i,j)/scaling_factors[j]);
        return output_matrix;
    }
    
    public static Matrix invertBySVD(Matrix input){
        if(input.numCols()!=input.numCols()) return null;
        SingularValueDecomposition svd=new SingularValueDecomposition(input);
        int n_dim=input.numCols();
        Matrix S=svd.getS();
        Matrix U=svd.getU();
        Matrix V=svd.getV();
        int i;
        for(i=0;i<n_dim;i++) S.set(i,i,1.0/S.get(i, i));
        return V.times(S).times(U.transpose());
        
    }
    
    public static Matrix ComputeEigenvectors(Matrix laplacian,int n_clusters,boolean useLanczos,boolean get_min_eigenvectors,boolean output_eigs,String filename){
        int n_data=laplacian.numRows();
        for(int i=0;i<laplacian.numRows();i++)
            for(int j=0;j<laplacian.numCols();j++)
                if((new Double(laplacian.get(i,j))).isNaN()) {
                    System.out.println("SVD: input matrix has NaN");
                    return null;
                }
        DenseMatrix eigs_n=new DenseMatrix(n_data,n_clusters);

        if(useLanczos){
            int n_eigs_to_retrieve=n_data;
            //THE NEXT LINE IS EXPERIMENTAL
            //WE MAY HAVE A SMALL SPEED-UP
            //BUT MAY NOT RETRIEVE ALL TOP n_clusters EIGENVECTORS
            if(!get_min_eigenvectors) n_eigs_to_retrieve=n_data;
            if(n_eigs_to_retrieve>n_data) n_eigs_to_retrieve=n_data;
            LanczosSolver lanczos_solver=new LanczosSolver();
            DenseVector initial_state=new DenseVector(n_data);
            initial_state.assign((1.0/(Math.sqrt((double) n_data))));
            LanczosState state = new LanczosState(laplacian,n_data,n_eigs_to_retrieve,initial_state);
            lanczos_solver.solve(state,n_eigs_to_retrieve,true);
    
            Vector[] eigenvectors=new Vector[n_eigs_to_retrieve];
            int i;
            for(i=0;i<n_eigs_to_retrieve;i++) eigenvectors[i]=state.getRightSingularVector(i);
            //EigenVerification ver = 
            new EigenVerification(eigenvectors, laplacian, 0.000,0.00,get_min_eigenvectors,output_eigs,filename);
            for(i=0;i<n_clusters;i++) eigs_n.assignColumn(i, EigenVerification.clean_eigenvectors[i]);
            
        }
        else{
            SingularValueDecomposition svd=new SingularValueDecomposition(laplacian);
//            for(int k=0;k<n_clusters;k++)
  //              System.out.println(svd.getSingularValues()[n_data-1-k]+" ");
            if(get_min_eigenvectors)
                for(int k=0;k<n_clusters;k++)
                    for(int l=0;l<n_data;l++)
                        eigs_n.set(l,k,svd.getV().get(l,n_data-1-k));
            else
                for(int k=0;k<n_clusters;k++)
                    for(int l=0;l<n_data;l++)
                        eigs_n.set(l,k,svd.getV().get(l,k));
            
        }
        return eigs_n;
    }
    
}