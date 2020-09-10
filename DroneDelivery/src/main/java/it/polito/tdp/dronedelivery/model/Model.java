package it.polito.tdp.dronedelivery.model;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.tour.NearestNeighborHeuristicTSP;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.dronedelivery.db.DeliveryDAO;
import it.polito.tdp.dronedelivery.model.Event.EventType;


public class Model {
	private Map<Integer, Shipment> idMap;
	private Map<String, City> cityMap;
	
	private Set<Shipment> shipmentCitySet; //filled by setCity()
	//private Set<Distance> shipmentCitySetDistances; //filled by setCity()
	
	// Coda degli eventi
	private PriorityQueue<Event> queue = new PriorityQueue<>();

	// Parametri di simulazione
	private Double sampleRate = 100.0; //definire totale sample e variazione a time slot: es 60% su 4 time slot, 15 a time slot sul time slot inserire % es +-33% tra 10 e 20
	//private int nDrones = 10;
	double returnBuffer = 5.0;
	private Double timeslotDeviation = 30.0;
	private Duration S_INT = Duration.of(30, ChronoUnit.MINUTES); // clock sampling interval
	// private final LocalTime firstCollect = LocalTime.of(8, 30);
	private final LocalTime startFlights = LocalTime.of(9, 00);
	private final LocalTime stopFlights = LocalTime.of(17, 30);
    private DeliveryDAO dao;
    private int collectEventsNumber;
    private int toDeliverSubsetSize;
    private String city;

	// modello del mondo
	private List<Drone> drones = new ArrayList<Drone>();
	private Shipment warehouse;
	// valori da calcolare

	//private Set<Shipment> delivered = new HashSet<Shipment>();
	private List<Shipment> toDeliverSubset1 = new ArrayList<Shipment>(); //using list (slow removal) to force random index selection calling Math.random 
	private Map<Integer, Shipment> toDeliverSubset2 = new HashMap<Integer, Shipment>();
	
	
	
	//private List<Set<Shipment>> assigned = new ArrayList<Set<Shipment>>();  //cercare di rimuovere inserendo una variabile di tipo set o lista in drone
	
	// pulire il database da citta'con lo stesso nome, esempio DALLAS
	
	
	//assigned(drone0)-->SET[]
	//assigned(drone1)-->SET[]
	//delivered[.... Shipment_X(deliveredtime,deliveringdroneID)  ....  ]
	/*
	 V 1) seleziona tutte le spedizioni
	 * 2) ad ogni evento:
	 *        - divide i nodi tra droni crea il grafo delle consegne (li aggiunge alla lista(d1,d1) di set di nodi todeliver) List<set> = new ArrayList<Set>
	 *        - crea i grafi delle consegne inclusivi degli eventuali nodi precedentemente non consegnati
	 *        - trova il cammino di consegna
	 *        - itera il path delle consegne e
	 *             - verifica la batteria corrente se > peso consegna + buffer ritorno
	 *               SE OK
	 *                 - imposta il valore del timeslot per la spedizione
	 *                 - rimuove il nodo dal set toDeliver e lo aggiunge in delivered
	 *               SE NONOK
	 *                 - ? nulla?
	 *             
	 * 
	 */
	
	//chiamata nella entrypoint
	public Model() {  
		idMap = new HashMap<Integer,Shipment>();
		cityMap = new HashMap<String,City>();
		
	}
	
	public void setToDeliverSubset() {
		
		List<Shipment> randomPick = new ArrayList<Shipment>(); //+
		for(Shipment s: idMap.values()) {
			if(s.getReqID() != warehouse.getReqID()) this.toDeliverSubset2.put(s.getReqID(), s);
		    randomPick.add(s); //avoid to call addAll later
		}
		
		
		//this.toDeliverSubset.addAll(this.shipmentCitySet); -
		int size = randomPick.size();// = this.shipmentCitySet.size();
		int numberToRemove = (int) Math.round( (Double.valueOf(100 - this.sampleRate)/100)*size);
		String removed = "[";
		int i;
		for(i=0; i<numberToRemove; i++) {
        
			int random = new Random().nextInt(size);
        	removed += randomPick.get(random).getReqID() + " ";
        	this.toDeliverSubset2.remove(randomPick.get(random).getReqID());//.remove(randomPick);
        	//toDeliverSubset2.
		}
        removed += "]";
        
        //System.out.println("Old size: "+shipmentCitySet.size());
        //System.out.println("New size: "+toDeliverSubset.size());
        //System.out.println("Removed shipments : "+ removed);
        //System.out.println("Rimossi : "+ numberToRemove);
        
	}
	
	
 /*	public List<Shipment> getRandomSets() {
		List<Shipment> shuffle = new ArrayList<Shipment>();
		shuffle.addAll(shipmentCitySet);
		int size = shipmentCitySet.size();
		int numberToRemove = (int) Math.round( (Double.valueOf(100 - sampleRate)/100)*size);
		
		
		int randomPick = new Random().nextInt(size);
		
		int numberToRemove = Double.valueOf(100 - 93);
		int i = 0;
		for(Shipment s : shipmentCitySet)
		{
			if (i == item)
				return obj;
			i++;
		}
		return 
	}*/
	
	
	public void modelReset() {
		//initialize all queues and vars
	}
	
	public void setCity(String city) {
		this.city = city;
		this.dao = new DeliveryDAO();
		this.shipmentCitySet = dao.getAllShipments(city, idMap);
		this.warehouse = cityMap.get(city).warehouse; // generare dao.getWarehouseID
		System.out.println("Setting city:" + city);
		System.out.println("Warehouse ID: " + this.warehouse.getReqID());
	}
	
	

	// metodi di impostzione parametri

	/*public void setNumDrones(int n) {
		this.nDrones = n;
	}*/


	public void initDrones() {
		this.drones = new ArrayList<Drone>();
		//this.nDrones = 0;
		}

	
	public int addDrone(int range) {
			int position = drones.size();
			this.drones.add(position, new Drone(range));
			this.drones.get(position).setAssigned(new HashSet<Shipment>());
			return position;
			
		
	}
	public int getDronesNumber() {
		return this.drones.size();
	}
	
	public Drone getDrone(int num) {
		return this.drones.get(num);
	}
	
	public void distributeToDeliverSubset(int shipmentsToDistribute) {
		List<Integer> toDeliverIdS = new ArrayList<Integer>(toDeliverSubset2.keySet());
		ListIterator<Integer> lIt = toDeliverIdS.listIterator();
		Integer idREQ;
		while (lIt.hasNext()) {
          int j;
			for(j=0; j<this.drones.size(); j++) {
				
					if(lIt.hasNext() && shipmentsToDistribute > 0) {
						idREQ = lIt.next();
						if (idREQ != this.warehouse.getReqID()){
							if(this.drones.get(j).getCurrentEnergy() == this.drones.get(j).getMaxEnergy()) {
								this.drones.get(j).getAssigned().add(this.toDeliverSubset2.get(idREQ)); 
								toDeliverSubset2.remove(idREQ);
								shipmentsToDistribute--;
							}
					    }
					}
			}
			if(shipmentsToDistribute <= 0) break;
        }
	}
	
	public void setConnectionFrequency(Duration d) {
		this.S_INT = d;
	}
	/*
	 * public Object getDronesPaths() { return dronesPaths; }
	 */

	     //parametri simulazione  settati nella controller, vanno pulite le code e fatta partire la simulazione
	
		public void run() {
			System.out.println(this.sampleRate);
			System.out.println(this.timeslotDeviation);
			/*this.dronesPaths.get(0);
			
			Model model = new Model();
			
			String city = "SAN FRANCISCO";
			
			
			
			model.graphBuilder("SAN FRANCISCO"); */
			//this.assigned.clear();
			
			
			//System.out.println(shipmentCitySet);
			
			//preparazione simulazione (mondo+coda eventi)
		
			//this.nDrones = 0; da settare nell'xml controller model.setndroni()
			//this.delivered.clear();
			this.toDeliverSubset2.clear();
			
			
			
			//coda eventi
			this.queue.clear();
			
			setToDeliverSubset(); // pick a portion based on the 
			
			System.out.println("Simulation started. " + this.toDeliverSubset2.size() + " shipments to deliver.");
			
			//System.out.println("orig: " + shipmentCitySet.size());
			//System.out.println("new:  " + toDeliverSubset.size());
			/*
			//DEBUG: Print assigned(i) size and the content
			 
			int i; 
			
			for(i=0; i<assigned.size(); i++) {
				System.out.println("\nAssigned["+i+"]: " + assigned.get(i).size());
				System.out.println(assigned.get(i));
			}
			
			*/
			LocalTime collectTime = this.startFlights;
			do {
				Event e = new Event(collectTime, EventType.NEW_COLLECT);
				this.queue.add(e);
				collectTime = collectTime.plus(this.S_INT);
			} while(collectTime.isBefore(this.stopFlights.plus(this.S_INT)));
			this.toDeliverSubsetSize = this.toDeliverSubset2.size();
			this.collectEventsNumber = this.queue.size();
			
			//esecuzione simulazione 
			while(!this.queue.isEmpty()) {
				Event e = this.queue.poll();
				//System.out.println(e);
				processEvent(e);

			} 
			int toDeliver = 0;
			int i;
			for(i=0; i<this.drones.size(); i++) {
				toDeliver += this.drones.get(i).getAssigned().size();
				if(this.drones.get(i).getAssigned().contains(warehouse)) toDeliver--;
			}
			System.out.println("Simulation ended. " + this.toDeliverSubset2.size()  + " shipments not delivered.");
			
			
		}

		private void processEvent(Event e) {
			
			
			int toDeliverNumber;
			System.out.println("Average delivery per event: " + ((int) (this.toDeliverSubsetSize/(this.collectEventsNumber -1) )));
			System.out.println(this.timeslotDeviation);
			
			
			
			toDeliverNumber = (int) Math.round(((( this.timeslotDeviation / 100.0 ) * 
			           ( Math.random() - 0.5) * 2.0  ) + 1.0) * (this.toDeliverSubsetSize/(this.collectEventsNumber -1) ) );
			System.out.println("Deviated number of assigned shipments: "+toDeliverNumber);
			if(this.queue.size()==0) toDeliverNumber = this.toDeliverSubset2.size(); //rimasti in coda alla fine della simulazione
			distributeToDeliverSubset(toDeliverNumber); 
			//System.out.println("----------");
			//System.out.println(this.queue.size());
			//System.out.println("collected: " + toDeliverNumber);
			//System.out.println("In queue: " + this.toDeliverSubset.size());
			Double edgeWeight;
			int previousID;
			int currentID;
			int i;
			String nodelist="";
			for(i=0; i<this.drones.size(); i++) {
	
				//  FOR
				
				System.out.println("---------- DRONE "+(i+1)+" ----------");
				System.out.println(e);
				
				
				if(this.drones.get(i).getCurrentEnergy() == this.drones.get(i).getMaxEnergy()) {
				
				GraphPath<Shipment, DefaultWeightedEdge> path = searchPath(drones.get(i).getAssigned());
				Double pathLength = path.getWeight();
				Double actualPathLength = 0.0;
				int origPathSize = path.getVertexList().size();
				System.out.println("Battery autonomy: " + drones.get(i).getMaxBatteryRangeKM());
				System.out.println("Path length:" + pathLength + " + " + this.returnBuffer + " (Return buffer)");
				System.out.println("Path hops:");
				//System.out.println(path.getVertexList());
				edgeWeight = 0.0;
				previousID = 0;
				
				for(Shipment s: path.getVertexList()) {
			     currentID = s.getReqID();
				 if(previousID != 0) {
					 edgeWeight = LatLngTool.distance(idMap.get(previousID).getCoords(), idMap.get(currentID).getCoords(), LengthUnit.KILOMETER);
					 System.out.println(s.getReqID()+" L "+ edgeWeight);
				 }
				 else {
					 System.out.println(s.getReqID()+" L ");
				 }
				 previousID = currentID;
				}
				 //int pathSize = path.getVertexList().size();
				
				List<Shipment> actualPath = executeDronePath(path.getVertexList(), i);
				
				//if()actualPath.remove(idMap.get(10000));
				
				edgeWeight = 0.0;
				previousID = 0;
				if(actualPath.size() > 0) {
				for(Shipment s: actualPath) {
				     currentID = s.getReqID();
				     if (previousID ==0) {
				    	 nodelist += currentID+" ";
				    	 edgeWeight = LatLngTool.distance(this.warehouse.getCoords(), idMap.get(currentID).getCoords(), LengthUnit.KILOMETER);
				     } else  {
						 nodelist += currentID+" ";
						 edgeWeight = LatLngTool.distance(idMap.get(previousID).getCoords(), idMap.get(currentID).getCoords(), LengthUnit.KILOMETER);
					 } 
					 previousID = currentID;
					 actualPathLength += edgeWeight;
					 
					}
				  actualPathLength += LatLngTool.distance(idMap.get(   actualPath.get( actualPath.size()-1).getReqID()     ).getCoords(),
						  this.warehouse.getCoords(), LengthUnit.KILOMETER);
				}
				System.out.println("Length of actual delivery path: "+actualPathLength);
				System.out.println("Delivered IDs: [ "+ nodelist +"]");
				nodelist = "";
				System.out.println("actualPath  " + actualPath);  //.size());
				System.out.println("actualPath size  " + actualPath.size());
				//System.out.println("actualPath length  " + actualPathLength);
				if(actualPath.size() > 0 ) {
					System.out.println("Successfully delivered: " + (actualPath.size())); 
					System.out.println("Not delivered: " + (origPathSize - actualPath.size() -2 ));

				} else if(pathLength > 0 && origPathSize == 3) {
					System.out.println("This drone model can't deliver. Too far. ");
				} else if(pathLength > 0 && origPathSize > 3 ) {
					System.out.println("Path need to be reviewed. Can't delivery with this path.");	
				}
				else {
					System.out.println("No shipments collected");

				}
				System.out.println("Energy used: " + drones.get(i).getUsedEnergy() + "/" + drones.get(i).getMaxEnergy());
				//removeall vertex
			
			//                FOR
				}
				else {
					System.out.println("Drone is at the warehouse. RECHARGING... ");
					System.out.println("Released to warehouse: "+this.drones.get(i).getAssigned().size());
					System.out.println("Shippments in warehouse: "+this.toDeliverSubset2.size());
					this.drones.get(i).setCurrentEnergy(this.drones.get(i).getCurrentEnergy() + 1);
					//System.out.println(this.drones.get(i).getAssigned());
					
					for(Shipment s: this.drones.get(i).getAssigned() ) {
						this.toDeliverSubset2.put(s.getReqID(),s);
					}
					
					this.drones.get(i).getAssigned().clear();
					//this.toDeliverSubsetSize--;
				}
					
			}
			/*
			 * switch(e.getType()) { case NEW_CLIENT: if(this.nAuto > 0) { // cliente
			 * servito, auto noleggiata this.nAuto--; //modello del mondo //aggiornamento
			 * risultati this.clienti++; 
			 * 
			 * 
			 * 
			 * 
			 * //generazione nuovi eventi double num =
			 * Math.random(); //[0,1) Duration travel; if(num<1.0/3.0) travel =
			 * 
			 * 
			 * 
			 * Duration.of(1, ChronoUnit.HOURS); else if (num<2.0/3.0) travel =
			 * Duration.of(2, ChronoUnit.HOURS); else travel = Duration.of(3,
			 * ChronoUnit.HOURS); Event nuovo = new Event(e.getTime().plus(travel),
			 * EventType.CAR_RETURNED); this.queue.add(nuovo); }else { //cliente
			 * insoddisfatto this.clienti++; this.insoddisfatti++;
			 * 
			 * } break;
			 * 
			 * case CAR_RETURNED:
			 * 
			 * this.nAuto++;
			 * 
			 * 
			 * break; 
			 */
		}
	// ---------------------------------------------------------------------------------------
	
	public Set<Shipment> getShipmentCitySet() {
		return this.shipmentCitySet;
	}

	public GraphPath<Shipment, DefaultWeightedEdge> searchPath(Set<Shipment> vertexes) {
		vertexes.add(this.warehouse);
		Graph<Shipment, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
				
		//Vertex add
		Graphs.addAllVertices(graph, vertexes);
		
		
		//Edge add
		for(Distance d : dao.getDistances(this.city, vertexes)) {
			Graphs.addEdge(graph, idMap.get(d.getObj1()), idMap.get(d.getObj2()), d.getWeight());
			//vertexes.remove(idMap.get(10000));
			//System.out.println("-->"+d+"<--");
		}	
	 // graph.toString();
		//System.out.println(graph.toString());
		
		
		
		
		
		//cammino hamiltoniano !! DA IMPLEMENTARE!! IL DRONE DEVE SEMPRE TORNARE ALLA BASE PER PRENDERE NUOVE BUSTE. PRIMO E ULTIMO VERTICE SEMPRE BASE
		NearestNeighborHeuristicTSP<Shipment, DefaultWeightedEdge> hamCycle = new NearestNeighborHeuristicTSP<Shipment, DefaultWeightedEdge>(this.warehouse);
		GraphPath<Shipment,DefaultWeightedEdge> path = hamCycle.getTour(graph);
		
		return path;
		

		
		
	}

	public List<Shipment> executeDronePath(List<Shipment> path, int droneID) {
		double partialPathLength = 0.0;
		int previousID = 0;
		int currentID;
		boolean incompletePath = false;
		Double edgeWeight = 0.0;
		Double returnWeight = 0.0;
		Set<Shipment> notDelivered = new HashSet<Shipment>();
		for(Shipment s: path) {
			currentID = s.getReqID();
		    if(previousID!=0) {
		    	
		    	// ----- System.out.println(previousID + " " + currentID);
		      edgeWeight = LatLngTool.distance(idMap.get(previousID).getCoords(), idMap.get(currentID).getCoords(), LengthUnit.KILOMETER); 
		      returnWeight = LatLngTool.distance(idMap.get(currentID).getCoords(), this.warehouse.getCoords(), LengthUnit.KILOMETER);
		      //System.out.println("Partial: "+partialPathLength +" Vertex: "+ edgeWeight);
		      if(partialPathLength + edgeWeight + returnWeight > ( this.drones.get(droneID).getMaxBatteryRangeKM() - this.returnBuffer )  || incompletePath) {
		    	  // aggiungere + distanza per tornare a casa
		    	  notDelivered.add(s);  
		    	  incompletePath = true;
		    	 // System.out.println((partialPathLength + edgeWeight));
		      } else {
		    	  partialPathLength += edgeWeight;
		    	  
		      }
		    }
		    previousID = currentID;
			
		}
		
		
		System.out.println("ND:"+notDelivered);
		System.out.println("PT:"+path);
		
		//If not delivered is NOT empty it containg the warehouse node too. The remove all clean it from the delivery path.
		path.removeAll(notDelivered);
		System.out.println("PT size:"+path.size());
		if(path.size() > 1) path.remove(this.warehouse); // remove warehouse - start point
		if(path.size() > 1) path.remove(this.warehouse); // remove warehouse - end point
		
		int energyUsed = (int) Math.round(partialPathLength / drones.get(droneID).getEnergyFactor());
		int initialEnergy = drones.get(droneID).getMaxBatteryRangeKM() / drones.get(droneID).getEnergyFactor(); 
		drones.get(droneID).getAssigned().removeAll(path);
		drones.get(droneID).setCurrentEnergy(initialEnergy - energyUsed);
		
		if(path.size() == 1 && path.get(0).getReqID() == this.warehouse.getReqID())			
			return new ArrayList<Shipment>(); // only warehouse in the list, return an empty one.
			//if(path.size() == 1) path.remove(0);
		 //if(path.size() > 1) path.remove(this.warehouse); //first occurrence 
		 //if(path.size() > 1) path.remove(this.warehouse); //last occurrence
		  
		 return path;
	}

	public void getCityList() {
		this.dao = new DeliveryDAO();
		dao.getCities(this.cityMap);
	}
	
	public List<City> getCityDropDown() {
		
		List<City> list = new ArrayList<City>();
		list.addAll(this.cityMap.values());
		return list;
	}

	public String getCity() {
		return this.city;
	}


	public void init() {
    	this.idMap.clear();
    	//this.shipmentCitySet.clear();
    	//this.shipmentCitySetDistances.clear();
    	collectEventsNumber = 0;
        toDeliverSubsetSize = 0;
	}

	public void setRate(Double rate) {
		this.sampleRate = rate;
		
	}

	public void setDev(Double deviance) {
		this.timeslotDeviation = deviance;
		
	}


}
	
	

