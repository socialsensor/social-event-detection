package models;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import utils.GeodesicDistanceCalculator;

@SuppressWarnings("serial")
public class Event extends ArrayList<MultimodalItem> {
	
    private Long meanTakenTime;
	private Long medianTakenTime;
	private Long meanUploadedTime;
	private Long medianUploadedTime;
	
	private Long maxUploadedTime = 0L;
	private Long minUploadedTime = Long.MAX_VALUE;

	private Long maxTakenTime = 0L;
	private Long minTakenTime = Long.MAX_VALUE;
	
    private Point meanGeo = null;
    private Point medianGeo = null;
    
    private TFIDFVector aggregatedTFIDF = null;
	private Double eventGeoRange = 0.0;
    
	private Set<String> users = new HashSet<String>();
	
    public Random RND;
    
    public Event() {
        super();
        RND = new Random();
    }
    
    public void addItem(MultimodalItem item) { 	
    	if(this.contains(item))
    		return;
    	
    	this.add(item); 	
    	
    	if(item.timestamp_taken < minTakenTime)
    		minTakenTime = item.timestamp_taken;
    	
    	if(item.timestamp_taken > maxTakenTime)
    		maxTakenTime = item.timestamp_taken;
    	
    	if(item.timestamp_uploaded < minUploadedTime)
    		minUploadedTime = item.timestamp_uploaded;
    	
    	if(item.timestamp_uploaded > maxUploadedTime)
    		maxUploadedTime = item.timestamp_uploaded;
    	
    	users.add(item.username);
    }
    
    public void addItems(List<MultimodalItem> items) { 	
    	for(MultimodalItem item : items)
    		addItem(item);
    }
    
    public long getMinTakenTime() {
    	return minTakenTime;
    }
    
    public long getMaxTakenTime() {
    	return maxTakenTime;
    }
    
    public long getTimeTakenRange() {
    	return this.isEmpty() ? -1 : (maxTakenTime - minTakenTime);
    }
    
    public long getMedianTimeRange() {
    	long medianTimeTaken = getMedianTakenTime();
    	List<Long> diffs = new ArrayList<Long>();
    	for(MultimodalItem item : this) {
    		long diff = Math.abs(item.timestamp_taken - medianTimeTaken);
    		diffs.add(diff);
    	}
    	Collections.sort(diffs);
    	if(diffs.size() == 1)
			return diffs.get(0);
		
		Long lower = diffs.get(diffs.size()/2-1);
		Long upper = diffs.get(diffs.size()/2);
	 
		return (lower + upper) / 2;
    }
    
    public double getGeoRange() {
    	return eventGeoRange;
    }
    
    public double getMinRadious() {
    	Point medianGeo = getMedianGeo();
    	
    	if(medianGeo != null){
    		Double minDistance = Double.MAX_VALUE;
    		for(MultimodalItem item : this) {
    			if(item.hasLocation()){
    				Point p = new Point(item.longitude, item.latitude);
    				Double distance = GeodesicDistanceCalculator.vincentyDistance(
						p.getLatitude(), p.getLongitude(), 
						medianGeo.getLatitude(), medianGeo.getLongitude());
    				if(minDistance > distance)
    					minDistance = distance;
    			}
    		}
    		return minDistance;
    	}
    	return -1;
    }
    
    public double getMaxRadious() {
    	Point medianGeo = getMedianGeo();
    	
    	if(medianGeo!=null){
    		Double maxDistance = 0.0;
    		for(MultimodalItem item : this) {
    			if(item.hasLocation()){
    				Point p = new Point(item.longitude, item.latitude);
    				Double distance = GeodesicDistanceCalculator.vincentyDistance(
						p.getLatitude(), p.getLongitude(), 
						medianGeo.getLatitude(), medianGeo.getLongitude());
    				if(maxDistance < distance)
    					maxDistance = distance;
    			}
    		}
    		return maxDistance;
    	}
    	return -1;
    }
    
    public double getMedianRadious() {
    	Point medianGeo = getMedianGeo();
    	if(medianGeo != null) {
    		List<Double> distances = new ArrayList<Double>();
    		for(MultimodalItem item : this) {
    			if(item.hasLocation()){
    				Point p = new Point(item.longitude, item.latitude);
    				Double distance = GeodesicDistanceCalculator.vincentyDistance(
						p.getLatitude(), p.getLongitude(), 
						medianGeo.getLatitude(), medianGeo.getLongitude());
    				distances.add(distance);
    			}
    		}
    		Collections.sort(distances);
    		if(distances.isEmpty())
    			return -1;
    		if(distances.size()==1)
    			return distances.get(0);
    		
    		Double lower = distances.get(distances.size()/2-1);
    		Double upper = distances.get(distances.size()/2);
    	 
    		return (lower + upper) / 2;
    		
    	}
    	return -1;
    }
    
    public TFIDFVector getEventTFIDF() {
    	aggregatedTFIDF = new TFIDFVector();
    	for(MultimodalItem item : this) {
    		TFIDFVector tagsTFIDF = item.tagsTFIDF;
    		TFIDFVector descriptionTFIDF = item.descriptionTFIDF;
    		TFIDFVector titleTFIDF = item.titleTFIDF;
    		aggregatedTFIDF.aggregate(titleTFIDF).aggregate(descriptionTFIDF)
    			.aggregate(tagsTFIDF);	
    	}
		return aggregatedTFIDF;
    }

    public boolean hasLocation() {
    	if(getMedianGeo()==null) {
    		return false;
    	}
    	return true;
    }
    
    public Point getMeanGeo() {
    	double latitude = 0, longitude = 0;
    	int geoLocatedItems = 0;
    	for(MultimodalItem item : this) {
    		if(item.hasLocation()) {
    			geoLocatedItems++;
    			latitude += item.latitude;
    			longitude += item.longitude;
    		}
    	}
    	if(geoLocatedItems > 0)
    		meanGeo = new Point(longitude/geoLocatedItems, latitude/geoLocatedItems);
  
    	return meanGeo;
    }
    
    public Point getMedianGeo() {
    	medianGeo = null;
		Double min_total_distance = Double.MAX_VALUE;
    	for(MultimodalItem item1 : this) {
    		if(!item1.hasLocation())
    			continue;
    		Point p1 = new Point(item1.longitude, item1.latitude);
    		Double total_distance = 0.0;
			for(MultimodalItem item2 : this) {
    			if(!item2.hasLocation())
        			continue;
    			Point p2 = new Point(item2.longitude, item2.latitude);
    			Double distance = GeodesicDistanceCalculator.vincentyDistance(
    					p1.getLatitude(), p1.getLongitude(), 
    					p2.getLatitude(), p2.getLongitude());
    			total_distance += distance;
    			if(eventGeoRange < distance)
    				eventGeoRange = distance;
        	}
			if(total_distance < min_total_distance) {
				medianGeo = p1;
    			min_total_distance = total_distance;
    		}
    	}
    	return medianGeo;
    }
    
    public long getMeanTakenTime() {
    	meanTakenTime = 0L;
    	for(MultimodalItem item : this) {
    		meanTakenTime += item.timestamp_taken;
    	}
    	meanTakenTime /= this.size();
    	return meanTakenTime;
    }
    
    public long getMedianTakenTime() {
    	if(this.isEmpty())
    		return 0;
    	if(this.size()==1) {
    		medianTakenTime = this.iterator().next().timestamp_taken;
    		return medianTakenTime;
    	}
    	List<Long> times = new ArrayList<Long>();
    	for(MultimodalItem item : this) {
    		times.add(item.timestamp_taken);
    	}
    	Collections.sort(times);
    	Long lower = times.get(times.size()/2-1);
    	Long upper = times.get(times.size()/2);
    	medianTakenTime = (lower + upper) / 2;
    
    	return medianTakenTime;
    }
    
    public long getMeanUploadedTime() {
    	meanUploadedTime = 0L;
    	for(MultimodalItem item : this)
    		meanUploadedTime += item.timestamp_uploaded;
    	meanUploadedTime /= this.size();
    	return meanUploadedTime;
    }
    
    public long getMedianUploadedTime() {
    	if(this.isEmpty())
    		return 0;
    	if(this.size()==1){
    		medianUploadedTime = this.iterator().next().timestamp_taken;
    		return medianUploadedTime;
    	}
    	List<Long> times = new ArrayList<Long>();
    	for(MultimodalItem item : this) {
    		times.add(item.timestamp_uploaded);
    	}
    	Collections.sort(times);
    		
    	Long lower = times.get(times.size()/2-1);
    	Long upper = times.get(times.size()/2);
    	 
    	medianUploadedTime = (lower + upper) / 2;
    	return medianUploadedTime;
    }
    
    
    public String toString() {
    	String str = "";
    	for(MultimodalItem item : this) {
    		str += (item.id+",");
    	}
    	return str;
    }
    
    public MultimodalItem sampleItem() {
        int nItems = this.size();
        int nextItem = RND.nextInt(nItems);
        MultimodalItem item = this.get(nextItem);
        
        return item;
    }

    public static List<Event> loadEventsFromFile(String eventsFile, Map<String, MultimodalItem> items) {
    	List<Event> events = new ArrayList<Event>();
        List<String> lines;
		try {
			lines = IOUtils.readLines(new FileInputStream(eventsFile));
		} catch (Exception e) {
			e.printStackTrace();
			return events;
		} 
        for(String line : lines) {
        	Event event = new Event();
        	String[] ids = line.split(" ");
        	for(String id : ids) {
        		MultimodalItem item = items.get(id);
        		event.addItem(item);
        	}
        	events.add(event);
        }
        return events;
    }
    
    public void merge(Event e) {
    	for(MultimodalItem item : e) {
    		this.addItem(item);
    	}
	}
    
	public boolean isSameEvent(Event e, long timeDiff, double distanceDiff) {
    	Point geo1 = getMeanGeo();
    	Point geo2 = e.getMeanGeo();
    	if(geo1 == null || geo2 == null)
    		return false;
    	
    	if(Math.abs(getMedianTakenTime()-e.getMedianTakenTime())<timeDiff) {
    		double distance = GeodesicDistanceCalculator.vincentyDistance(
    				geo1.getLatitude(), geo1.getLongitude(), geo1.getLatitude(), geo1.getLongitude());
    		if(distance < distanceDiff)
    			return true;
    	}
    	return false;
    }
    
	public boolean isSameEvent(Event e, long timeDiff) {
    	if(Math.abs(getMedianTakenTime()-e.getMedianTakenTime())<timeDiff) 
    			return true;
    	if(Math.abs(getMedianUploadedTime()-e.getMedianUploadedTime())<timeDiff) 
			return true;

		if(userSetSimilarity(e)>0.5)
			return true;
		
    	return false;
    }
	
	public boolean isSameEvent(Event e) {
		if(userSetSimilarity(e)>0.5)
			return true;
    	return false;
    }
	
    public String getAsString(String separator) {
    	String[] ids = new String[this.size()];
        for(int i=0; i<ids.length; i++) {
        	ids[i] = this.get(i).id;
        }
        String result = StringUtils.join(ids, separator);
        return result;
    }
    
	public void print() {
		StringBuffer strBfr = new StringBuffer();
		strBfr.append(this.getMinTakenTime() + ", ");
		strBfr.append(this.getMaxTakenTime() + ", ");
		strBfr.append(this.getMedianTakenTime() + ", ");
		strBfr.append(this.getTimeTakenRange() + ", ");
		strBfr.append(this.getMedianTimeRange() + ", ");
		strBfr.append(this.getMedianGeo() + ", ");
		strBfr.append(this.getMinRadious() + ", ");
		strBfr.append(this.getMaxRadious() + ", ");
		strBfr.append(this.getMedianRadious() + ", ");
		strBfr.append(this.getGeoRange());
		
		System.out.println(strBfr.toString());
	}

	public double locationSimilarity(Event event2) {
		Point geo1 = this.getMedianGeo();
		Point geo2 = event2.getMedianGeo();
		if(geo1!=null && geo2!=null) {
            return GeodesicDistanceCalculator.vincentyDistance(
            		geo1.getLatitude(), geo1.getLongitude(), geo2.getLatitude(), geo2.getLongitude());
		}
        else 
            return -1;
	}

	public double userSetSimilarity(Event event2) {
		Set<String> intersection = new HashSet<String>(users);
		intersection.retainAll(event2.users);
		
		return intersection.size() / Math.min(users.size(), event2.users.size());
	}

	public double timeTakenSimilarity(Event event2) {
		
		long timeTaken1 = this.getMedianTakenTime();
		long timeTaken2 = event2.getMedianTakenTime();
    	
		double divisor = 1000.0 * 60 * 60;
        return Math.abs(timeTaken1-timeTaken2)/divisor;
    	
	}

	public double timeUploadedSimilarity(Event event2) {
		long timeUploaded1 = this.getMedianUploadedTime();
		long timeUploaded2 = event2.getMedianUploadedTime();
    	
		double divisor = 1000.0 * 60 * 60;
        return Math.abs(timeUploaded1-timeUploaded2)/divisor;
	}

	public double textSimilarityBM25(Event event2) {
		TFIDFVector tdidfVector1 = this.getEventTFIDF();
		TFIDFVector tdidfVector2 = event2.getEventTFIDF();
		
		return tdidfVector1.bm25Similarity(tdidfVector2, 1);
	}

	public double textSimilarityCosine(Event event2) {
		TFIDFVector tdidfVector1 = this.getEventTFIDF();
		TFIDFVector tdidfVector2 = event2.getEventTFIDF();
		
		return tdidfVector1.cosineSimilarity(tdidfVector2);
	}

	public double timeTakenDayDiff3(Event event2) {
		long timeTaken1 = this.getMedianTakenTime();
		long timeTaken2 = event2.getMedianTakenTime();
		
		double divisor = 1000.0;
        divisor = divisor * 60 * 60 * 24;
        double days = (timeTaken1 - timeTaken2) / divisor;
        if(days<3)
            return 1;
        else 
            return 0;
	}

	public double timeTakenHourDiff12(Event event2) {
		long timeTaken1 = this.getMedianTakenTime();
		long timeTaken2 = event2.getMedianTakenTime();
		
		double divisor = 1000.0;
        divisor = divisor * 60 * 60;
        double hours = (timeTaken1 - timeTaken2) / divisor;
        if(hours<12)
            return 1;
        else 
            return 0;
	}

	public double timeTakenHourDiff24(Event event2) {
		long timeTaken1 = this.getMedianTakenTime();
		long timeTaken2 = event2.getMedianTakenTime();
		
		double divisor = 1000.0;
        divisor = divisor * 60 * 60;
        double hours = (timeTaken1 - timeTaken2) / divisor;
        if(hours<24)
            return 1;
        else 
            return 0;
	}
	
}