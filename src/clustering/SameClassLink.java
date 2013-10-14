package clustering;

public class SameClassLink {
    public float weight;
    public String id;
    public static int counter;
    
    public SameClassLink() {
    }

    public SameClassLink(float weight) {
        this.weight = weight;
        counter += 1;
        id = Integer.toString(counter);
    }
    
    public String toString() {
    	 return "E" + id;
    }
    
}