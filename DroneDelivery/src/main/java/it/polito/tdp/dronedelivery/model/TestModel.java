package it.polito.tdp.dronedelivery.model;

import org.jgrapht.alg.tour.NearestNeighborHeuristicTSP;
import org.jgrapht.graph.DefaultWeightedEdge;


public class TestModel {

	public static void main(String[] args) {
		Model model = new Model();
		model.graphBuilder("SAN FRANCISCO");
		
		
		System.out.println(model.vertexCount());
		System.out.println(model.edgeCount());
		
		
		//System.out.println(model);
	}	
}
