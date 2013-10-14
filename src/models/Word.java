package models;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;


public class Word {

    public String _id;
    public int _df;
    public double _idf;

    public Word(String id) {
        _id = id;
        _df = 0;
        _idf = 0;
    }

    public Word() {
        _df = 0;
        _idf = 0;
    }

    public void computeIDF(int nDocs) {
        _idf = Math.log(((double) nDocs)/((double) _df+1));
    }

    public int getDf() {
        return _df;
    }

    public double getIdf() {
        return _idf;
    }
    
    public double getIdf(int nDocs) {
        if(_idf == 0) 
        	computeIDF(nDocs);
        return _idf;
    }


    public void increaseDF() {
        _df++;
    }

    public void writeToFile(PrintWriter pw) {
    	pw.print(_id);
    	pw.print(" ");
    	pw.print(_df);
    	pw.print(" ");
    	pw.println(_idf);
   }

   public boolean loadFromFile(BufferedReader reader) {
       try {
           String line=reader.readLine();
           if((line==null)||(line.trim().equals(""))) return false;
           String[] parts=line.split(" ");
           _id = parts[0];
           _df = Integer.parseInt(parts[1]);
           _idf = Double.parseDouble(parts[2]);
           return true;
       }
       catch(IOException e) {
           
       }
       return false;
   }

    public String getId() {
        return _id;
    }
    
}