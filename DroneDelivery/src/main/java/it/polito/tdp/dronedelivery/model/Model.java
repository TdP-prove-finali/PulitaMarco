package it.polito.tdp.dronedelivery.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.tour.NearestNeighborHeuristicTSP;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;



import it.polito.tdp.dronedelivery.db.DeliveryDAO;


public class Model {
	private Graph<Shipment, DefaultWeightedEdge> graph;
	private Map<Integer, Shipment> idMap;	
	
	public Model() {
		idMap = new HashMap<Integer,Shipment>();
	}
	
	
	public void graphBuilder(String city) {
		this.graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		DeliveryDAO dao = new DeliveryDAO();
		dao.getAllShipments(city, idMap);
		
		//Vertex add
		Graphs.addAllVertices(this.graph, idMap.values());
		
		//Edge add
		for(Distance d : dao.getDistances(city, idMap)) {
			Graphs.addEdge(this.graph, idMap.get(d.getObj1()), idMap.get(d.getObj2()), d.getWeight());
		}	
	  
		System.out.println(idMap.get(319));
		
		
		
		
		
		//cammino hamiltoniano
		NearestNeighborHeuristicTSP<Shipment, DefaultWeightedEdge> hamCycle = new NearestNeighborHeuristicTSP<Shipment, DefaultWeightedEdge>(idMap.get(319));
		GraphPath<Shipment,DefaultWeightedEdge> path = hamCycle.getTour(graph);
		System.out.println(path.getWeight());
		List<Shipment> vertexList = path.getVertexList();
		for(Shipment s: vertexList) System.out.println(s.getReqID());
		
	}
	
	
	
	public int vertexCount() {
		return this.graph.vertexSet().size();
	}
	
	
	public int edgeCount() {
		return this.graph.edgeSet().size();
		
	}
   
	

	@Override
	public String toString() {
		return "Model [graph=" + graph + "]";
	}
	
	
}
	
	

