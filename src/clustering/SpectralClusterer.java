package clustering;

import utils.IOUtils;
import utils.MatrixOps;

import org.apache.mahout.math.*;


public class SpectralClusterer {
    
    public static enum LaplacianType {
    	UNNORMALIZED_LAPLACIAN, 
    	NORMALIZED_LAPLACIAN_SYM,
    	NORMALIZED_LAPLACIAN_SYM_2_JORDAN,
    	NORMALIZED_LAPLACIAN_RW
    };

    public static enum NoOfClustersSelectionType {NONE, Q_BASED};

    public static Integer[] SpectralClusteringFromAffinity(Matrix affinity, int n_clusters, boolean useLanczos,
    		LaplacianType l_type, NoOfClustersSelectionType sel_type, int k_means_iterations, boolean output_eigs, String filename) {
        
    	Integer[] class_assignments=new Integer[0];
        if(sel_type==NoOfClustersSelectionType.NONE){
            Matrix laplacian=null;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN)
                laplacian= (SparseMatrix) ComputeUnnormalizedLaplacianFromSimilarityMatrix(affinity);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_SYM)
                laplacian= (SparseMatrix)ComputeNormalizedSymLaplacianFromSimilarityMatrix(affinity);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_SYM_2_JORDAN)
                laplacian= (SparseMatrix)ComputeNormalizedSym2LaplacianFromSimilarityMatrix(affinity);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_RW)
                laplacian= (SparseMatrix)ComputeNormalizedRwLaplacianFromSimilarityMatrix(affinity);
            
            
            boolean get_min_eigenvectors=false;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN) get_min_eigenvectors=true;
            Matrix eigs_n=MatrixOps.ComputeEigenvectors(laplacian,n_clusters,useLanczos,get_min_eigenvectors,output_eigs,filename);
            MatrixOps.NormalizeRows(eigs_n);
            class_assignments=KMeansClusterer.KMeansClustererFromData(eigs_n,k_means_iterations, n_clusters);
            IOUtils.write2dArray2File(eigs_n, "d:\\mediaeval_c\\eigs_n.txt", " ");
        }
        if(sel_type==NoOfClustersSelectionType.Q_BASED){
            int n_data=affinity.numRows();
//            SparseMatrix affinity=ComputeSimilarityMatrixKnn(data, k_knn);
            SparseMatrix dmatrix=new SparseMatrix(n_data,n_data);
            int i,j;
            for(i=0;i<n_data;i++) 
                for(j=0;j<n_data;j++) 
                    dmatrix.set(i,i,affinity.get(i,j));
            SparseMatrix laplacian=new SparseMatrix(n_data, n_data);
            laplacian=(SparseMatrix) affinity.clone();
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN)
                laplacian= (SparseMatrix) ComputeUnnormalizedLaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_SYM)
                laplacian= (SparseMatrix)ComputeNormalizedSymLaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_SYM_2_JORDAN)
                laplacian= (SparseMatrix)ComputeNormalizedSym2LaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_RW)
                laplacian= (SparseMatrix)ComputeNormalizedRwLaplacianFromSimilarityMatrix(laplacian);
            boolean get_min_eigenvectors=false;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN) get_min_eigenvectors=true;
            Matrix eigs_n=MatrixOps.ComputeEigenvectors(laplacian,n_clusters,useLanczos,get_min_eigenvectors,false,"");
            Integer[] class_assignments_tmp;
            double best_q_value=0;
            double q_value;
            int best_i=0;
            //the correct number of clusters is the value of i 
            //for which the q_value computed on the (i-1) 
            //is maximum
            for(i=3;i<=n_clusters+1;i++){
                DenseMatrix eigs_n_tmp=new DenseMatrix(n_data,i-1);
                for(j=0;j<i-1;j++) eigs_n_tmp.assignColumn(j, eigs_n.getColumn(j));
                MatrixOps.NormalizeRows(eigs_n_tmp);
                if(i-1>1)
                    class_assignments_tmp=KMeansClusterer.KMeansClustererFromData(eigs_n_tmp,k_means_iterations, i-1);
                else
                    {
                        class_assignments_tmp=new Integer[n_data];
                        for(j=0;j<n_data;j++) class_assignments_tmp[j]=0;
                    }
                q_value=ComputeQValue(affinity,dmatrix,class_assignments_tmp,i-1);
                System.out.println("Q value for "+i+" clusters is : "+q_value);
                if(q_value>best_q_value) {
                    best_q_value=q_value;
                    best_i=i;
                    class_assignments=class_assignments_tmp.clone();
                }
                
            }
            System.out.println("Best k is : "+best_i);
        }        
        return class_assignments;
    }
    
    
    
    //in the case that Q_VALUE is chosen as the method for selecting the
    //correct number of clusters, then n_clusters is treated as 
    //the maximum number of clusters to examine
    public static Integer[] SpectralClusteringKnnAffinity(Matrix data,int k_knn,int n_clusters,boolean useLanczos,LaplacianType l_type,NoOfClustersSelectionType sel_type,int k_means_iterations){
        Integer[] class_assignments=new Integer[0];
        if(sel_type==NoOfClustersSelectionType.NONE){
            Matrix laplacian=ComputeLaplacianMatrixKnn(data,k_knn, l_type);
            boolean get_min_eigenvectors=false;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN) get_min_eigenvectors=true;
            Matrix eigs_n=MatrixOps.ComputeEigenvectors(laplacian,n_clusters,useLanczos,get_min_eigenvectors,false,"");
            MatrixOps.NormalizeRows(eigs_n);
            class_assignments=KMeansClusterer.KMeansClustererFromData(eigs_n,k_means_iterations, n_clusters);
            IOUtils.write2dArray2File(eigs_n, "d:\\mediaeval_c\\eigs_n.txt", " ");
        }
        if(sel_type==NoOfClustersSelectionType.Q_BASED){
            int n_data=data.numRows();
            SparseMatrix affinity=ComputeSimilarityMatrixKnn(data, k_knn);
            SparseMatrix dmatrix=new SparseMatrix(n_data,n_data);
            int i,j;
            for(i=0;i<n_data;i++) 
                for(j=0;j<n_data;j++) 
                    dmatrix.set(i,i,affinity.get(i,j));
            SparseMatrix laplacian=new SparseMatrix(n_data, n_data);
            laplacian=(SparseMatrix) affinity.clone();
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN)
                laplacian= (SparseMatrix) ComputeUnnormalizedLaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_SYM)
                laplacian= (SparseMatrix)ComputeNormalizedSymLaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_SYM_2_JORDAN)
                laplacian= (SparseMatrix)ComputeNormalizedSym2LaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_RW)
                laplacian= (SparseMatrix)ComputeNormalizedRwLaplacianFromSimilarityMatrix(laplacian);
            boolean get_min_eigenvectors=false;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN) get_min_eigenvectors=true;
            Matrix eigs_n=MatrixOps.ComputeEigenvectors(laplacian,n_clusters,useLanczos,get_min_eigenvectors,false,"");
            Integer[] class_assignments_tmp;
            double best_q_value=0;
            double q_value;
            int best_i=0;
            //the correct number of clusters is the value of i 
            //for which the q_value computed on the (i-1) 
            //is maximum
            for(i=3;i<=n_clusters+1;i++){
                DenseMatrix eigs_n_tmp=new DenseMatrix(n_data,i-1);
                for(j=0;j<i-1;j++) eigs_n_tmp.assignColumn(j, eigs_n.getColumn(j));
                MatrixOps.NormalizeRows(eigs_n_tmp);
                if(i-1>1)
                    class_assignments_tmp=KMeansClusterer.KMeansClustererFromData(eigs_n_tmp,k_means_iterations, i-1);
                else
                    {
                        class_assignments_tmp=new Integer[n_data];
                        for(j=0;j<n_data;j++) class_assignments_tmp[j]=0;
                    }
                q_value=ComputeQValue(affinity,dmatrix,class_assignments_tmp,i-1);
                System.out.println("Q value for "+i+" clusters is : "+q_value);
                if(q_value>best_q_value) {
                    best_q_value=q_value;
                    best_i=i;
                    class_assignments=class_assignments_tmp.clone();
                }
                
            }
            System.out.println("Best k is : "+best_i);
        }        
        return class_assignments;
    }

    public static Integer[] SpectralClusteringEpsilonAffinity(Matrix data,double epsilon,int n_clusters,boolean useLanczos,LaplacianType l_type,NoOfClustersSelectionType sel_type,int k_means_iterations){
        Integer[] class_assignments=new Integer[0];
        if(sel_type==NoOfClustersSelectionType.NONE){
            Matrix laplacian=ComputeLaplacianMatrixEpsilon(data,epsilon, l_type);
            boolean get_min_eigenvectors=false;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN) get_min_eigenvectors=true;
            Matrix eigs_n=MatrixOps.ComputeEigenvectors(laplacian,n_clusters,useLanczos,get_min_eigenvectors,false,"");
            MatrixOps.NormalizeRows(eigs_n);
            class_assignments=KMeansClusterer.KMeansClustererFromData(eigs_n,k_means_iterations, n_clusters);
        }
        if(sel_type==NoOfClustersSelectionType.Q_BASED){
            int n_data=data.numRows();
            SparseMatrix affinity=ComputeSimilarityMatrixEpsilon(data, epsilon);
            SparseMatrix dmatrix=new SparseMatrix(n_data,n_data);
            int i,j;
            for(i=0;i<n_data;i++) 
                for(j=0;j<n_data;j++) 
                    dmatrix.set(i,i,affinity.get(i,j));
            SparseMatrix laplacian=new SparseMatrix(n_data, n_data);
            laplacian=(SparseMatrix) affinity.clone();
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN)
                laplacian= (SparseMatrix) ComputeUnnormalizedLaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_SYM)
                laplacian= (SparseMatrix)ComputeNormalizedSymLaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_SYM_2_JORDAN)
                laplacian= (SparseMatrix)ComputeNormalizedSym2LaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_RW)
                laplacian= (SparseMatrix)ComputeNormalizedRwLaplacianFromSimilarityMatrix(laplacian);
            boolean get_min_eigenvectors=false;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN) get_min_eigenvectors=true;
            Matrix eigs_n=MatrixOps.ComputeEigenvectors(laplacian,n_clusters,useLanczos,get_min_eigenvectors,false,"");
            Integer[] class_assignments_tmp;
            double best_q_value=0;
            double q_value;
            int best_i=0;
            //the correct number of clusters is the value of i 
            //for which the q_value computed on the (i-1) 
            //is maximum
            for(i=3;i<=n_clusters+1;i++){
                DenseMatrix eigs_n_tmp=new DenseMatrix(n_data,i-1);
                for(j=0;j<i-1;j++) eigs_n_tmp.assignColumn(j, eigs_n.getColumn(j));
                MatrixOps.NormalizeRows(eigs_n_tmp);
                if(i-1>1)
                    class_assignments_tmp=KMeansClusterer.KMeansClustererFromData(eigs_n_tmp,k_means_iterations, i-1);
                else
                    {
                        class_assignments_tmp=new Integer[n_data];
                        for(j=0;j<n_data;j++) class_assignments_tmp[j]=0;
                    }
                q_value=ComputeQValue(affinity,dmatrix,class_assignments_tmp,i-1);
                System.out.println("Q value for "+i+" clusters is : "+q_value);
                if(q_value>best_q_value) {
                    best_q_value=q_value;
                    best_i=i;
                    class_assignments=class_assignments_tmp.clone();
                }
                
            }
            System.out.println("Best k is : "+best_i);
        }        
        return class_assignments;

        
/*        Integer[] class_assignments=new Integer[0];
        if(sel_type==NoOfClustersSelectionType.NONE){
            Matrix laplacian=ComputeLaplacianMatrixEpsilon(data,epsilon, l_type);
            boolean get_min_eigenvectors=false;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN) get_min_eigenvectors=true;
            Matrix eigs_n=ComputeEigenvectors(laplacian,n_clusters,useLanczos,get_min_eigenvectors);
            MatrixOps.NormalizeRows(eigs_n);
            class_assignments=KMeansClusterer.KMeansClustererFromData(eigs_n,k_means_iterations, n_clusters);
        }
        if(sel_type==NoOfClustersSelectionType.NONE){
            
        }        
        return class_assignments;*/
    }

    public static Integer[] SpectralClusteringFullGaussianAffinity(Matrix data,double sigma,int n_clusters,boolean useLanczos,LaplacianType l_type,NoOfClustersSelectionType sel_type,int k_means_iterations){
        Integer[] class_assignments=new Integer[0];
        if(sel_type==NoOfClustersSelectionType.NONE){
            Matrix laplacian=ComputeLaplacianMatrixFullGaussian(data,sigma, l_type);
            boolean get_min_eigenvectors=false;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN) get_min_eigenvectors=true;
            Matrix eigs_n=MatrixOps.ComputeEigenvectors(laplacian,n_clusters,useLanczos,get_min_eigenvectors,false,"");
            MatrixOps.NormalizeRows(eigs_n);
            class_assignments=KMeansClusterer.KMeansClustererFromData(eigs_n,k_means_iterations, n_clusters);
            IOUtils.write2dArray2File(eigs_n, "d:\\mediaeval_c\\eigs_n.txt", " ");
        }
        if(sel_type==NoOfClustersSelectionType.Q_BASED){
            int n_data=data.numRows();
            DenseMatrix affinity=ComputeSimilarityMatrixFullGaussian(data, sigma);
            SparseMatrix dmatrix=new SparseMatrix(n_data,n_data);
            int i,j;
            for(i=0;i<n_data;i++) 
                for(j=0;j<n_data;j++) 
                    dmatrix.set(i,i,affinity.get(i,j));
            SparseMatrix laplacian=new SparseMatrix(n_data, n_data);
            laplacian=(SparseMatrix) affinity.clone();
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN)
                laplacian= (SparseMatrix) ComputeUnnormalizedLaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_SYM)
                laplacian= (SparseMatrix)ComputeNormalizedSymLaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_SYM_2_JORDAN)
                laplacian= (SparseMatrix)ComputeNormalizedSym2LaplacianFromSimilarityMatrix(laplacian);
            if(l_type==LaplacianType.NORMALIZED_LAPLACIAN_RW)
                laplacian= (SparseMatrix)ComputeNormalizedRwLaplacianFromSimilarityMatrix(laplacian);
            boolean get_min_eigenvectors=false;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN) get_min_eigenvectors=true;
            Matrix eigs_n=MatrixOps.ComputeEigenvectors(laplacian,n_clusters,useLanczos,get_min_eigenvectors,false,"");
            Integer[] class_assignments_tmp;
            double best_q_value=0;
            double q_value;
            int best_i=0;
            //the correct number of clusters is the value of i 
            //for which the q_value computed on the (i-1) 
            //is maximum
            for(i=3;i<=n_clusters+1;i++){
                DenseMatrix eigs_n_tmp=new DenseMatrix(n_data,i-1);
               
                for(j=0;j<i-1;j++) eigs_n_tmp.assignColumn(j, eigs_n.getColumn(j));
                MatrixOps.NormalizeRows(eigs_n_tmp);
                if(i-1>1)
                    class_assignments_tmp=KMeansClusterer.KMeansClustererFromData(eigs_n_tmp,k_means_iterations, i-1);
                else
                    {
                        class_assignments_tmp=new Integer[n_data];
                        for(j=0;j<n_data;j++) class_assignments_tmp[j]=0;
                    }
                q_value=ComputeQValue(affinity,dmatrix,class_assignments_tmp,i-1);
                System.out.println("Q value for "+i+" clusters is : "+q_value);
                if(q_value>best_q_value) {
                    best_q_value=q_value;
                    best_i=i;
                    class_assignments=class_assignments_tmp.clone();
                }
                
            }
            System.out.println("Best k is : "+best_i);
        }        
        return class_assignments;
/*        Integer[] class_assignments=new Integer[0];
        if(sel_type==NoOfClustersSelectionType.NONE){
            Matrix laplacian=ComputeLaplacianMatrixFullGaussian(data,sigma, l_type);
            boolean get_min_eigenvectors=false;
            if(l_type==LaplacianType.UNNORMALIZED_LAPLACIAN) get_min_eigenvectors=true;
            Matrix eigs_n=ComputeEigenvectors(laplacian,n_clusters,useLanczos,get_min_eigenvectors);
            MatrixOps.NormalizeRows(eigs_n);
            IOUtils.write2dArray2File(eigs_n, "D:\\mediaeval_c\\eigs_n.txt"," ");
            class_assignments=KMeansClusterer.KMeansClustererFromData(eigs_n,k_means_iterations, n_clusters);
        }
        if(sel_type==NoOfClustersSelectionType.NONE){
            
        }        
        return class_assignments;
        
*/
    }

    public static Matrix ComputeLaplacianFromAffinity(Matrix affinity, LaplacianType laplacian_mode){
        Matrix laplacian=null;
        if(laplacian_mode==LaplacianType.UNNORMALIZED_LAPLACIAN)
            laplacian=ComputeUnnormalizedLaplacianFromSimilarityMatrix(affinity);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_SYM)
            laplacian=ComputeNormalizedSymLaplacianFromSimilarityMatrix(affinity);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_SYM_2_JORDAN)
            laplacian=ComputeNormalizedSym2LaplacianFromSimilarityMatrix(affinity);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_RW)
            laplacian=ComputeNormalizedRwLaplacianFromSimilarityMatrix(affinity);
        return laplacian;
    }
    
    
    public static Matrix ComputeLaplacianMatrixKnn(Matrix data, int k_knn, LaplacianType laplacian_mode){
        Matrix laplacian=ComputeSimilarityMatrixKnn(data,k_knn);
        if(laplacian_mode==LaplacianType.UNNORMALIZED_LAPLACIAN)
            laplacian=ComputeUnnormalizedLaplacianFromSimilarityMatrix(laplacian);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_SYM)
            laplacian=ComputeNormalizedSymLaplacianFromSimilarityMatrix(laplacian);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_SYM_2_JORDAN)
            laplacian=ComputeNormalizedSym2LaplacianFromSimilarityMatrix(laplacian);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_RW)
            laplacian=ComputeNormalizedRwLaplacianFromSimilarityMatrix(laplacian);
        return laplacian;
    }

    public static SparseMatrix ComputeSimilarityMatrixKnn(Matrix data, int k_knn){
        int n_data=data.numRows();
        int n_vars=data.numCols();
        SparseMatrix similarity=new SparseMatrix(n_data,n_data);
        int i,j,k;
        double[] nearest_neighbours_dists=new double[k_knn];
        int[] nearest_neighbours_indexes=new int[k_knn];
        double tmp_dist;
        int position_to_insert;
        for(i=0;i<n_data;i++) {
        	for(j=0;j<k_knn;j++) 
        		nearest_neighbours_dists[j]=Double.MAX_VALUE;
            for(j=0;j<n_data;j++) {
                if(j!=i) {
                    tmp_dist=0;
                    for(k=0;k<n_vars;k++) 
                    	tmp_dist = tmp_dist+(data.get(i,k)-data.get(j,k))*(data.get(i,k)-data.get(j,k));
                    position_to_insert=k_knn;
                    while((position_to_insert>0)&&(tmp_dist<nearest_neighbours_dists[position_to_insert-1]))
                        position_to_insert--;
                    if(position_to_insert<k_knn) {
                        for(k=k_knn-1;k>position_to_insert;k--) {
                            nearest_neighbours_dists[k]=nearest_neighbours_dists[k-1];
                            nearest_neighbours_indexes[k]=nearest_neighbours_indexes[k-1];
                        }
                        nearest_neighbours_dists[position_to_insert]=tmp_dist;
                        nearest_neighbours_indexes[position_to_insert]=j;
                    }
                }
            }
            
            Double tmp_m;
            for(j=0;j<k_knn;j++){
                tmp_m=Math.exp(-nearest_neighbours_dists[j]/nearest_neighbours_dists[k_knn-1]);
//                tmp_m=Math.exp(-nearest_neighbours_dists[j]);
                similarity.set(i,nearest_neighbours_indexes[j],tmp_m);
                similarity.set(nearest_neighbours_indexes[j],i,tmp_m);
            }          
            IOUtils.write2dArray2File(similarity, "D:\\mediaeval_c\\similarity.txt", "\t");
        }
        return similarity;
    }
    
    public static Matrix ComputeLaplacianMatrixEpsilon(Matrix data, double epsilon,LaplacianType laplacian_mode){
        Matrix laplacian=ComputeSimilarityMatrixEpsilon(data,epsilon);
        if(laplacian_mode==LaplacianType.UNNORMALIZED_LAPLACIAN)
            laplacian=ComputeUnnormalizedLaplacianFromSimilarityMatrix(laplacian);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_SYM)
            laplacian=ComputeNormalizedSymLaplacianFromSimilarityMatrix(laplacian);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_SYM_2_JORDAN)
            laplacian=ComputeNormalizedSym2LaplacianFromSimilarityMatrix(laplacian);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_RW)
            laplacian=ComputeNormalizedRwLaplacianFromSimilarityMatrix(laplacian);
        return laplacian;
    }
    
    public static SparseMatrix ComputeSimilarityMatrixEpsilon(Matrix data, double epsilon){
        int n_data=data.numRows();
        int n_vars=data.numCols();
        SparseMatrix similarity=new SparseMatrix(n_data,n_data);
        int i,j,k;
        double tmp_dist;
        for(i=0;i<n_data;i++)
            for(j=i;j<n_data;j++){
                tmp_dist=0;
                for(k=0;k<n_vars;k++) tmp_dist=tmp_dist+(data.get(i,k)-data.get(j,k))*(data.get(i,k)-data.get(j,k));
                if(tmp_dist<epsilon){
                    similarity.set(i, j,1.0);
                    similarity.set(j, i,1.0);
                }
                else
                {
                    similarity.set(i, j, 0.0);
                    similarity.set(j, i, 0.0);
                }
            }
        return similarity;
    }

    public static Matrix ComputeLaplacianMatrixFullGaussian(Matrix data,double sigma,LaplacianType laplacian_mode){
        Matrix laplacian=ComputeSimilarityMatrixFullGaussian(data,sigma);
        if(laplacian_mode==LaplacianType.UNNORMALIZED_LAPLACIAN)
            laplacian=ComputeUnnormalizedLaplacianFromSimilarityMatrix(laplacian);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_SYM)
            laplacian=ComputeNormalizedSymLaplacianFromSimilarityMatrix(laplacian);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_SYM_2_JORDAN)
            laplacian=ComputeNormalizedSym2LaplacianFromSimilarityMatrix(laplacian);
        if(laplacian_mode==LaplacianType.NORMALIZED_LAPLACIAN_RW)
            laplacian=ComputeNormalizedRwLaplacianFromSimilarityMatrix(laplacian);
        return laplacian;
    }

    
    public static DenseMatrix ComputeSimilarityMatrixFullGaussian(Matrix data,double sigma){
        int n_data=data.numRows();
        int n_vars=data.numCols();
        DenseMatrix similarity=new DenseMatrix(n_data,n_data);
        int i,j,k;
        double tmp_dist;
        double sigma_squared=2.0*sigma*sigma;
        for(i=0;i<n_data;i++)
            for(j=i;j<n_data;j++){
                tmp_dist=0;
                for(k=0;k<n_vars;k++) tmp_dist=tmp_dist+(data.get(i,k)-data.get(j,k))*(data.get(i,k)-data.get(j,k));
                tmp_dist=Math.exp(-tmp_dist/sigma_squared);
                similarity.set(i, j,tmp_dist);
                similarity.set(j, i,tmp_dist);
            }
        return similarity;
    }
    
    public static Matrix ComputeUnnormalizedLaplacianFromSimilarityMatrix(Matrix similarityMatrix){
        int i,j;
        int n_data=similarityMatrix.numRows();
        double[] sums=new double[n_data];
        for(i=0;i<n_data;i++) 
            for(j=0;j<n_data;j++) 
                sums[i]=sums[i]+similarityMatrix.get(i,j);
        similarityMatrix=similarityMatrix.times(-1.0);
        for(i=0;i<n_data;i++) similarityMatrix.set(i, i, sums[i]+similarityMatrix.get(i,i));
        return similarityMatrix;
    }

    public static Matrix ComputeNormalizedSymLaplacianFromSimilarityMatrix(Matrix similarityMatrix){
        int i,j;
        int n_data=similarityMatrix.numRows();
        double[] sums=new double[n_data];
        for(i=0;i<n_data;i++) 
            for(j=0;j<n_data;j++) 
                sums[i]=sums[i]+similarityMatrix.get(i,j);
        for(i=0;i<n_data;i++) sums[i]=1.0/Math.sqrt(sums[i]);
        
        for(i=0;i<n_data;i++) 
            for(j=0;i<n_data;i++) 
                similarityMatrix.set(i, j, similarityMatrix.get(i,j)*sums[i]);
        for(i=0;i<n_data;i++) 
            for(j=0;i<n_data;i++) 
                similarityMatrix.set(i, j, similarityMatrix.get(i,j)*sums[j]);
        
        similarityMatrix=similarityMatrix.times(-1.0);
        for(i=0;i<n_data;i++) 
            similarityMatrix.set(i,i,1.0+similarityMatrix.get(i,i));
        
        return similarityMatrix;
    }

    public static Matrix ComputeNormalizedSym2LaplacianFromSimilarityMatrix(Matrix similarityMatrix){
        int i,j;
        int n_data=similarityMatrix.numRows();
//        double[] sums=new double[n_data];
        SparseMatrix roots=new SparseMatrix(n_data,n_data);
        double tmp_sum;
        for(i=0;i<n_data;i++){
            tmp_sum=0.0;
            for(j=0;j<n_data;j++) 
                tmp_sum=tmp_sum+similarityMatrix.get(i,j);
            roots.set(i,i,1.0/Math.sqrt(tmp_sum));
        }
        return roots.times(similarityMatrix).times(roots);
/*        for(i=0;i<n_data;i++) 
            for(j=0;i<n_data;i++) 
                similarityMatrix.set(i, j, similarityMatrix.get(i,j)*sums[i]);
        for(i=0;i<n_data;i++) 
            for(j=0;i<n_data;i++) 
                similarityMatrix.set(i, j, similarityMatrix.get(i,j)*sums[j]);
  */             
//        return similarityMatrix;
    }
    
    
    public static Matrix ComputeNormalizedRwLaplacianFromSimilarityMatrix(Matrix similarityMatrix){
        int i,j;
        int n_data=similarityMatrix.numRows();
        double[] sums=new double[n_data];
        for(i=0;i<n_data;i++) 
            for(j=0;j<n_data;j++) 
                sums[i]=sums[i]+similarityMatrix.get(i,j);

        for(i=0;i<n_data;i++) sums[i]=1.0/sums[i];
        
        for(i=0;i<n_data;i++) 
            for(j=0;i<n_data;i++) 
                similarityMatrix.set(i, j, similarityMatrix.get(i,j)*sums[i]);
        for(i=0;i<n_data;i++) 
            for(j=0;i<n_data;i++) 
                similarityMatrix.set(i, j, similarityMatrix.get(i,j)*sums[j]);
        similarityMatrix=similarityMatrix.times(-1.0);
        for(i=0;i<n_data;i++) 
            similarityMatrix.set(i,i,1.0+similarityMatrix.get(i,i));
        
        return similarityMatrix;
    }
    
    
    public static double ComputeQValue(Matrix affinity,Matrix d_matrix,Integer[] class_labelings,int k){
        double q_value=0.0;
        int n_data=affinity.numRows();
        SparseMatrix labelings_tmp=new SparseMatrix(n_data,k);
        int i;
        for(i=0;i<n_data;i++) labelings_tmp.set(i,class_labelings[i],1.0);
        Matrix tmp_matrix=labelings_tmp.transpose().times(affinity.minus(d_matrix)).times(labelings_tmp);
/*        System.out.println("Q matrix for : "+k);
        for(i=0;i<tmp_matrix.numRows();i++){
            for(int j=0;j<tmp_matrix.numCols();j++) System.out.print(tmp_matrix.get(i,j)+" ");
            System.out.println("");
        }*/
        for(i=0;i<k;i++) q_value=q_value+tmp_matrix.get(i,i);
        return q_value;
    }
    
    public static Matrix computeLocallyScaledAffinity(Matrix affinity,int k_knn){
        int n_data=affinity.numRows();
        Matrix scaledAffinity=new DenseMatrix(n_data,n_data);
        int i,j,k;
        double[] kth_nearest_neighbours=new double[n_data];
        double[] nearest_neighbours_dists=new double[k_knn];
        double tmp_dist;
        int position_to_insert;
        for(i=0;i<n_data;i++){
            for(j=0;j<k_knn;j++) nearest_neighbours_dists[j]=Double.MIN_VALUE;
            for(j=0;j<n_data;j++){
                if(j!=i){
                    tmp_dist=affinity.get(i,j);
                    position_to_insert=k_knn;
                    while((position_to_insert>0)&&(tmp_dist>nearest_neighbours_dists[position_to_insert-1]))
                        position_to_insert--;
                    if(position_to_insert<k_knn){
                        for(k=k_knn-1;k>position_to_insert;k--)
                            nearest_neighbours_dists[k]=nearest_neighbours_dists[k-1];
                        nearest_neighbours_dists[position_to_insert]=tmp_dist;
                    }
                }
            }
            kth_nearest_neighbours[i]=nearest_neighbours_dists[k_knn-1];
        }

        double tmp_val;
        for(i=0;i<n_data;i++)
            for(j=i+1;j<n_data;j++){
//                tmp_val=(affinity.get(i,j)*affinity.get(i,j))/()
                tmp_val=affinity.get(i,j)*kth_nearest_neighbours[i]*kth_nearest_neighbours[j];
                scaledAffinity.set(i,j,tmp_val);
                scaledAffinity.set(j,i,tmp_val);
            }
        
        return scaledAffinity;
    }
    
    
}
