/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author gpetkos
 */
import org.apache.mahout.math.*;
import org.apache.mahout.math.decomposer.*;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import com.google.common.collect.Maps;
import com.google.common.collect.Lists;

import utils.IOUtils;

import java.util.Comparator;


/**
 *
 * @author gpetkos
 */
public class EigenVerification {

  //default max-error : 0.05
  //default min-eigenvalue: 0.0
  private static List<Map.Entry<Vector, EigenStatus>> prunedEigenMeta;
  public static Vector[] clean_eigenvectors;
  public static double[] clean_eigenvalues;
  
  public EigenVerification(Vector[] eigenVectors,Matrix corpus,double minEigenValue,double maxError,boolean ascending,boolean output_eigs,String filename){
     
    SimpleEigenVerifier eigenVerifier = new SimpleEigenVerifier();
    
    Map<Vector, EigenStatus> eigenMetaData = Maps.newHashMap();
    int i;
    for (i=0;i<eigenVectors.length;i++) {
      EigenStatus status = eigenVerifier.verify(corpus, eigenVectors[i]);
      eigenMetaData.put(eigenVectors[i], status);
    }

    prunedEigenMeta = Lists.newArrayList();

    for (Map.Entry<Vector, EigenStatus> entry : eigenMetaData.entrySet()) {
//      if (Math.abs(1 - entry.getValue().getCosAngle()) < maxError && entry.getValue().getEigenValue() > minEigenValue) {
        prunedEigenMeta.add(entry);
  //    }
    }
    if(ascending)
    Collections.sort(prunedEigenMeta, new Comparator<Map.Entry<Vector, EigenStatus>>() {
      @Override
      public int compare(Map.Entry<Vector,EigenStatus> e1, Map.Entry<Vector,EigenStatus> e2) {
/*        int index1 = e1.getKey().index();
        int index2 = e2.getKey().index();
        if (index1 < index2) {
          return -1;
        }
        if (index1 > index2) {
          return 1;
        }*/
        double eig1=e1.getValue().getEigenValue();
        double eig2=e2.getValue().getEigenValue();
        if(eig1<eig2){
            return -1;
        }
        if(eig1>eig2){
            return 1;
        }
      
        return 0;
      }
    });
    else
            Collections.sort(prunedEigenMeta, new Comparator<Map.Entry<Vector, EigenStatus>>() {
      @Override
      public int compare(Map.Entry<Vector,EigenStatus> e1, Map.Entry<Vector,EigenStatus> e2) {
/*        int index1 = e1.getKey().index();
        int index2 = e2.getKey().index();
        if (index1 < index2) {
          return -1;
        }
        if (index1 > index2) {
          return 1;
        }*/
        double eig1=e1.getValue().getEigenValue();
        double eig2=e2.getValue().getEigenValue();
        if(eig1<eig2){
            return 1;
        }
        if(eig1>eig2){
            return -1;
        }
      
        return 0;
      }
    });

    
    
    
/*    for (Map.Entry<Vector, EigenStatus> entry : prunedEigenMeta.) {
        System.out.println(entry.getValue().getEigenValue()+"");
    }
*/

    int n_pruned=prunedEigenMeta.size();
    
    clean_eigenvectors=new Vector[n_pruned];
    clean_eigenvalues=new double[n_pruned];
    for(i=0;i<n_pruned;i++){
        clean_eigenvectors[i]=prunedEigenMeta.get(i).getKey();
        clean_eigenvalues[i]=prunedEigenMeta.get(i).getValue().getEigenValue();
    }
        
    if(output_eigs){
        IOUtils.write1dArray2File(clean_eigenvalues, filename);
       //double[] eigenvalues_a=new double[];
            
    }
        
    
    
  }
  
}
