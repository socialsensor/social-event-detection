package clustering.louvain;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import models.MultimodalItem;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.plugin.NodeColorTransformer;

import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;

import clustering.SameClassLink;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class LouvainClustering {

	@SuppressWarnings("unused")
	private Workspace workspace;
	private ProjectController pc;
	
	private GraphModel graphModel;
	private Map<String, Node> nodes = new HashMap<String, Node>();
	private GraphFactory factory;
	private PartitionController partitionController;

	
	public LouvainClustering() {
		this.pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		this.workspace = pc.getCurrentWorkspace();
		 
		this.graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		this.factory = graphModel.factory();
		
		this.partitionController = Lookup.getDefault().lookup(PartitionController.class);
	}
	
	private UndirectedGraph getGraph(Graph<MultimodalItem, SameClassLink> itemsGraph) {
    	UndirectedGraph graph = graphModel.getUndirectedGraph();
    	
		int v = 0;
    	Collection<MultimodalItem> vertices = itemsGraph.getVertices();
    	for(MultimodalItem vertex : vertices) {
    		if(!nodes.containsKey(vertex.id)) {
    			Node node = factory.newNode(vertex.id);
    			nodes.put(vertex.id, node);
    			graph.addNode(node);
    		}
    		if(++v%10000==0) {
    			System.out.println(v + " nodes inserted into graph");
    		}
    	}
    	
    	int e = 0;
    	long t1 = 0, t2 = 0;
    	Collection<SameClassLink> edges = itemsGraph.getEdges();
    	for(SameClassLink edge : edges) {
    		long t = System.currentTimeMillis();
    		Pair<MultimodalItem> endpoints = itemsGraph.getEndpoints(edge);
    		
    		MultimodalItem item1 = endpoints.getFirst();
    		MultimodalItem item2 = endpoints.getSecond();
    		t1 += (System.currentTimeMillis() - t);
    		
    		
    		Node n1 = nodes.get(item1.id);
    		Node n2 = nodes.get(item2.id);
    		
    		if(edge.weight == 1) {
    			t = System.currentTimeMillis();
    			graph.addEdge(factory.newEdge(n1, n2, edge.weight, false));
    			t2 += (System.currentTimeMillis() - t);
    		}
    		
    		if(++e%100000==0) {
    			System.out.println(e + " edges inserted into graph. Load:" + t1 + ", Insert:" + t2);
    		}
    	}
    	
    	return graph;
	}
	
	public Partition<Node> partition(Graph<MultimodalItem, SameClassLink> itemsGraph) {

		UndirectedGraph graph = getGraph(itemsGraph);
		
		System.out.println("Nodes: " + graph.getNodeCount());
		System.out.println("Edges: " + graph.getEdgeCount());

		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		
		// Run modularity algorithm - community detection
		Modularity modularity = new Modularity();
		modularity.setUseWeight(true);
		modularity.setResolution(1.);
		modularity.setRandom(true);
		modularity.execute(graphModel, attributeModel);
		
		AttributeColumn modColumn = attributeModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);

		@SuppressWarnings("unchecked")
		Partition<Node> p = partitionController.buildPartition(modColumn, graph);
		
		return p;
	}
	
	public void save(String filename, Partition<Node> p) {
		NodeColorTransformer nodeColorTransformer = new NodeColorTransformer();
		nodeColorTransformer.randomizeColors(p);
		partitionController.transform(p, nodeColorTransformer);
		
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);	
		// Export
		try {
			ec.exportFile(new File(filename));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
}