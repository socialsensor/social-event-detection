package clustering.scan;
 
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;

public class ScanCommunityStructure<V,E> {
	
	
	/* map of vertex-ID to community-ID */
	private Map<V, Integer> cMap = new HashMap<V, Integer>();
	
	/* list of communities */
	private Map<Integer, Community<V,E>> communities = new HashMap<Integer, Community<V,E>>();
	
	private List<V> hubs = new ArrayList<V>();
	private Map<V, Integer> availableHubs = new HashMap<V, Integer>();
	
	private List<V> outliers = new ArrayList<V>();
	private Set<V> availableOutliers = new HashSet<V>();
	
	private Graph<V, E> graph = null;
	
	public ScanCommunityStructure(Graph<V, E> g){
		this.graph = g;
	}
	
	
	public void setHubsAndOutliers(ScanCommunityStructure<V,E> structure){
		List<V> listOfHubs = structure.getHubs();
		for (int i = 0; i < listOfHubs.size(); i++){
			this.addHub(listOfHubs.get(i), structure.getHubAdjacentCommunities(listOfHubs.get(i)));
		}
		
		List<V> listOfOutliers = structure.getOutliers();
		for (int i = 0; i < listOfOutliers.size(); i++){
			this.addOutlier(listOfOutliers.get(i));
		}
	}
	
	public boolean addCommunity(Community<V,E> community){
		
		int maxID = -1;
		
		for (Integer cID: communities.keySet()){
			if (cID > maxID){
				maxID = cID;
			}
		}
		
		maxID++;
		
		communities.put(maxID, community);
		for (V member : community.getMembers()){
			cMap.put(member, maxID);
		}
		return true;
	}
	
	public boolean addVertexToCommunity(V toAdd, int cId){

		Community<V,E> c = communities.get(cId);
		if (c == null){
			c = new Community<V, E>(cId, graph);
			communities.put(cId, c);
		}
		c.addMember(toAdd);
		cMap.put(toAdd, cId);
		return true;
	}
	public int getCommunityIndex(V vertex){
		Integer cId = cMap.get(vertex);
		if (cId == null){
			return -1;
		}
		return cId;
	}
	
	
	
	public int getNumberOfMembers(){
		return cMap.size();
	}
	
	public int getNumberOfCommunities(){
		return communities.size();
	}
	public Community<V,E> getCommunity(int cId){
		return communities.get(cId);
	}
	
	public boolean addHub(V hubToAdd, int nrAdjacentCommunities){
		if (availableHubs.containsKey(hubToAdd) ||
				availableOutliers.contains(hubToAdd) ||
				cMap.containsKey(hubToAdd)){
			return false;
		}
		hubs.add(hubToAdd);
		availableHubs.put(hubToAdd, nrAdjacentCommunities);
		return true;
	}
	public List<V> getHubs(){
		return hubs;
	}
	public boolean isHub(V hubCandidate){
		return availableHubs.containsKey(hubCandidate);
	}
	
	
	public int getHubAdjacentCommunities(V hubIdx){
		if (availableHubs.containsKey(hubIdx)){
			return availableHubs.get(hubIdx);
		} else {
			return 0;
		}
	}
	
	public boolean addOutlier(V outlierToAdd){
		if (availableHubs.containsKey(outlierToAdd) ||
				availableOutliers.contains(outlierToAdd) ||
				cMap.containsKey(outlierToAdd)){
			return false;
		}
		outliers.add(outlierToAdd);
		availableOutliers.add(outlierToAdd);
		return true;
	}
	
	// this is slow
	public boolean removeOutlier(V outlierToRemove){
		if (! availableOutliers.contains(outlierToRemove)){
			return false;
		}
		availableOutliers.remove(outlierToRemove);
		return outliers.remove(outlierToRemove);
	}
	public boolean isOutlier(V candidateOutlier){
		return availableOutliers.contains(candidateOutlier);
	}
	
	
	public List<V> getOutliers(){
		return outliers;
	}

	
	public void printCommunitySummary(){
		
		System.out.println(communities.size() + " communities");
		System.out.println(graph.getVertexCount() - hubs.size() - outliers.size() + " members");
		System.out.println(hubs.size() + " hubs");
		System.out.println(outliers.size() + " outliers");
		
		int maxToShow = 10;
		Iterator<Community<V, E>> cIter = communities.values().iterator();
		int count = 0;
		while (cIter.hasNext()){
			if (++count > maxToShow){
				break;
			}
			Community<V, E> c = cIter.next();
			System.out.print(c.getId() + "(" + c.getNumberOfMembers() + " members): ");
			int Nmembers = Math.min(c.getNumberOfMembers(), 10);
			System.out.print("[");
			for (int i = 0; i < Nmembers; i++){
				V m = c.getMembers().get(i);
				int d = graph.degree(m);
				System.out.print("(" + m + ", " + d + ") ");
			}
			System.out.println("]");
			//System.out.println(c.getMembers().subList(0, Nmembers));
		}
		
		int Nhubs = Math.min(hubs.size(), 10);
		System.out.print("[");
		for (int i = 0; i < Nhubs; i++){
			System.out.print("(" + hubs.get(i) + "," + availableHubs.get(hubs.get(i))+"), ");
		}
		System.out.println("]");
		//System.out.println(hubs.subList(0, Nhubs).toString());
		int Noutliers = Math.min(outliers.size(), 10);
		System.out.println(outliers.subList(0, Noutliers).toString());
	}
	
	public void writeSizesToFile(String file) throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
		for (int i = 0; i < getNumberOfCommunities(); i++){
			writer.append(String.valueOf(getCommunity(i).getNumberOfMembers()));
			writer.newLine();
		}
		writer.close();
	}
	
	public void writeStructureToFile(String file) throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
                writer.append("--COMMNUNITIES--");
		writer.newLine();
		writer.append(String.valueOf(communities.size()));
		writer.newLine();
		for (Community<V,E> community : communities.values()) {
			writer.append(String.valueOf(community.getNumberOfMembers()));
			for (V member : community.getMembers()){
				writer.append("\t" + member.toString());
			}
			writer.newLine();
		}
		
                writer.append("--HUBS--");
		writer.newLine();
		writer.append(String.valueOf(hubs.size()));
		writer.newLine();
		for (V hub : hubs) {
			writer.append(hub.toString() + "\t" + getHubAdjacentCommunities(hub));
			writer.newLine();
		}
                writer.append("--OUTLIERS--");
		writer.newLine();
		writer.append(String.valueOf(outliers.size()));
		writer.newLine();
		for (V outlier : outliers){
			writer.append(outlier.toString());
			writer.newLine();
		}
		
		writer.close();
	}
}
