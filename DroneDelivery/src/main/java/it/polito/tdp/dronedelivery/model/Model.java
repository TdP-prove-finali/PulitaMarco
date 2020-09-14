package it.polito.tdp.dronedelivery.model;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.dronedelivery.db.DeliveryDAO;
import it.polito.tdp.dronedelivery.model.Event.EventType;


public class Model {
	private Map<Integer, Shipment> idMap;
	private Map<String, City> cityMap;

	//Full Shipments list from DB filled by setCity()
	private Set<Shipment> shipmentCitySet; 

	// Events queue
	private PriorityQueue<Event> queue = new PriorityQueue<>();

	// Simulation parameters
	// ----------------------------------------------
	private Double sampleRate; 
	private double returnBuffer = 5.0;
	private Double timeslotVariability;

	// Sampling interval
	private Duration S_INT = Duration.of(30, ChronoUnit.MINUTES); 

	private LocalTime startFlights = LocalTime.of(9, 00);
	private LocalTime stopFlights = LocalTime.of(17, 30);
	private DeliveryDAO dao;
	private int collectEventsNumber;
	private String city;

	private int logDrone = -1; //-1 log all drones
	private boolean logPath = false;

	// -----------------------------------------------
	// World model

	private List<Drone> drones = new ArrayList<Drone>();
	private Shipment warehouse;
	private Map<Integer, Shipment> toDeliverMap = new HashMap<Integer, Shipment>();
	private Map<Integer, Shipment> delivered = new HashMap<Integer, Shipment>();
	private int initialToDeliverSize = 0;
	


	// -----------------------------------------------

	boolean debug = false;

	private String logCommon = "";
	private String logAll = "";
	private String fullLogAll = "";

	/**
	 *New Model(), {@code idMap} (map of city shipment) and {@code cityMap} (map of cities) are set
	 */

	public Model() {  
		idMap = new HashMap<Integer,Shipment>();
		cityMap = new HashMap<String,City>();
	}

	/**
	 * Fills the map of actual shipments scheduled and removes from it a number of
	 * randomly picked object defined with the {@code sampleRate} parameter 
	 */
	public void setToDeliverSubset() {
		List<Shipment> randomPick = new LinkedList<Shipment>();

		for(Shipment s: idMap.values()) {
			if(s.getReqID() != warehouse.getReqID()) { 
				this.toDeliverMap.put(s.getReqID(), s);
				randomPick.add(s);
			}
		}
		int size = randomPick.size();
		int numberToRemove = (int) Math.round( (Double.valueOf(100 - this.sampleRate) / 100 ) * size);

		String removed = "";
		removed = "[";
		int i;
		for(i=0; i<numberToRemove; i++) {
			int random = new Random().nextInt(randomPick.size());

			removed += randomPick.get(random).getReqID() + " ";
			this.toDeliverMap.remove(randomPick.get(random).getReqID());
			randomPick.remove(random);
			if(debug)System.out.println("Random() - removed the shipment at the position: " +random +". Shipment map size: "+ this.toDeliverMap.size());

		}
		removed += "]";
		appendCommon("The DB contains "+(idMap.keySet().size() - 1 )+" shipments");  //not considering warehouse
		appendCommon("With the sample rate of " + String.format("%.1f",this.sampleRate) + "%, " + numberToRemove +" shipments have beed removed");
		if(debug) System.out.println("Removed objects: " + removed);
		appendCommon("Actual shipments to deliver: " + this.toDeliverMap.size());
		initialToDeliverSize = this.toDeliverMap.size();

	}


	/**
	 * Set the {@code city} parameter for the model and load the related shipments
	 * from the database in to the {@code idMap} map
	 * @param city
	 */
	public void setCity(String city) {
		this.city = city;
		this.dao = new DeliveryDAO();
		this.shipmentCitySet = dao.getAllShipments(city, idMap);
		this.warehouse = cityMap.get(city).warehouse;
		appendCommon("Setting city: " + city);
		appendCommon("Warehouse ID: " + this.warehouse.getReqID());
	}

	/**
	 * Initialize the drones list
	 */
	public void initDrones() {
		this.drones = new ArrayList<Drone>();
		if(debug) System.out.println("Drone list wiped");
	}

	public void resetDrones() {
		for(Drone d: this.drones) {
			d.reset();
		}
		if(debug) System.out.println("Drone list resetd");
	}


	/**
	 * Add a {@code Drone} with a {@code maxBatteryRangeKM} specified with {@code range} 
	 * @param range
	 * @return the position in the list where the drone is added AKA drone number
	 */

	public int addDrone(int range) {
		int position = drones.size();
		this.drones.add(position, new Drone(range));
		this.drones.get(position).setAssigned(new HashSet<Shipment>());
		if(debug) System.out.println("Drone added at position "+position);
		return position;
	}

	/**
	 * Rerurn the number of drones in the simulation
	 * @return number of drones
	 */

	public int getDronesNumber() {
		return this.drones.size();
	}

	/**
	 * Return the {@code Drone} object in the Drones list at the given position 
	 * @param num
	 * @return Drone object at the position {@code num}
	 */

	public Drone getDrone(int num) {
		return this.drones.get(num);
	}


	/**
	 * Distributes in each drone shipment list the items to deliver 
	 * @param shipmentsToDistribute the list of object IDs to distribute 
	 */

	public void distributeToDeliverSubset(int shipmentsToDistribute) {
		List<Integer> toDeliverIdS = new ArrayList<Integer>(toDeliverMap.keySet());
		ListIterator<Integer> lIt = toDeliverIdS.listIterator();
		Integer idREQ;
		int dronesCharging = 0;
		while (lIt.hasNext()) {
			int j;

			for(j=0; j<this.drones.size(); j++) {

				if(lIt.hasNext() && shipmentsToDistribute > 0) {
					idREQ = lIt.next();
					if (idREQ != this.warehouse.getReqID()){ // avoid to distribute the warehouse id

						// the shipments are distributed only if at the specific time event the battery is fully charged
						if(this.drones.get(j).getCurrentEnergy() == this.drones.get(j).getMaxEnergy()) {
							this.drones.get(j).getAssigned().add(this.toDeliverMap.get(idREQ)); 
							toDeliverMap.remove(idREQ); // when assigned the shipment is removed from delivery queue
							shipmentsToDistribute--;
							if(debug) System.out.println("Assigned shipment "+idREQ+ " to drone " + j 
									+ ". Shipments to distribute are now " + shipmentsToDistribute );
						}
						else { 
							dronesCharging++;
							if(debug) System.out.println("Drone "+ j + 
									" is charging. Skipped. Shipments to distribute at this timeslot are " + shipmentsToDistribute);
							lIt.previous();
						}
					}
				}
			}
			if(shipmentsToDistribute <= 0 || dronesCharging==this.drones.size()) break;
		}
	}


	/**
	 * Set the interval between collection/pickup events in minutes
	 * @param minutes
	 */

	public void setCollectionFrequency(int minutes) {
		this.S_INT = Duration.of(minutes, ChronoUnit.MINUTES);
	}

	public void run() {
		//clearing list of deliveries
		this.toDeliverMap = new HashMap<Integer, Shipment>();
		this.delivered = new HashMap<Integer, Shipment>();

		//clearing events queue
		this.queue.clear();

		setToDeliverSubset(); // pick a portion based on the sampling rate

		// -----  FILLS THE EVENTS PRIORITY QUEUE -----
		LocalTime collectTime = this.startFlights;
		do {
			Event e = new Event(collectTime, EventType.NEW_COLLECT);
			this.queue.add(e);
			collectTime = collectTime.plus(this.S_INT);
		} while(collectTime.isBefore(this.stopFlights.plus(Duration.of(15, ChronoUnit.MINUTES))));

		Event e = new Event(collectTime, EventType.LAST_DELIVERY);
		this.queue.add(e);

		this.collectEventsNumber = this.queue.size();

		appendCommon("There are "+this.collectEventsNumber+" Events");

		//executing simulation polling events queue
		while(!this.queue.isEmpty()) {
			Event eq = this.queue.poll();
			processEvent(eq);
		} 

		appendCommon("--- Simulation ended ---");
		appendCommon("Delivered: " + this.delivered.size() + " - Not Delivered:" + this.toDeliverMap.size()  + "/"+this.initialToDeliverSize+ "\n\n");

		if(debug) System.out.println(this.toDeliverMap.keySet());
		if(debug) System.out.println(this.delivered.keySet());
	}

	/**
	 * Execute the simulation for a single event
	 * @param e, the event to process
	 */

	private void processEvent(Event e) {

		int toDeliverNumber;
		/*toDeliverNumber = (int) Math.round(((( this.timeslotVariability / 100.0 ) * 
				( Math.random() - 0.5) * 2.0  ) + 1.0) * (this.initialToDeliverSize/(this.collectEventsNumber -1) ) );*/
		toDeliverNumber = (int) Math.round(((( this.timeslotVariability / 100.0 ) * 
		( Math.random() - 0.5) * 2.0  ) + 1.0) * (this.toDeliverMap.size()/(this.queue.size()+1) ) );
		String eventLog = ("\n"+ e + "\nAvailable Shipments in this time slot: "+toDeliverNumber+", avg: "+(this.initialToDeliverSize/this.collectEventsNumber));
		appendAll(eventLog);

		if(e.getType().name().equals("LAST_DELIVERY")) {
			eventLog = ("Last collect, emtying the warehouse. Shipments on this time slot: " + this.toDeliverMap.size());
			appendAll(eventLog);
			toDeliverNumber = this.toDeliverMap.size();
		}
		distributeToDeliverSubset(toDeliverNumber); 

		Double edgeWeight;
		int previousID;
		int currentID;
		int i;
		String nodelist="";
		String log;
		for(i=0; i<this.drones.size(); i++) {

			this.drones.get(i).appendLog(eventLog);
			this.drones.get(i).appendFullLog(eventLog);
			log = "---------- DRONE "+i+" ---------- ";
			appendDroneAndCommonLog(i,log);
			appendDroneAndCommonLog(i,e.toString());
			if(debug) System.out.println(log);

			if(this.drones.get(i).getCurrentEnergy() == this.drones.get(i).getMaxEnergy()) {


				/* Search for a Hamiltonian path and return the related GraphPath object.
				 * It uses the NearestNeighborHeuristicTSP algorithm.
				 */
				GraphPath<Shipment, DefaultWeightedEdge> path = searchPath(drones.get(i).getAssigned());  //path contains always warehouse!!

				Double pathLength = path.getWeight();
				Double actualPathLength = 0.0;

				Set<Shipment> pathHops = new HashSet<Shipment>(path.getVertexList());
				if(pathHops.contains(warehouse) && pathHops.size() == 1) pathHops = new HashSet<Shipment>(); //wipe the path list if only warehouse is inside
				else pathHops.remove(warehouse);
				int origPathSize = pathHops.size();

				log = ("Battery autonomy: " + drones.get(i).getMaxBatteryRangeKM());
				appendDroneAndCommonLog(i,log);


				log = ("Original path length:" + String.format("%.1f",pathLength) + " Km + " + this.returnBuffer + " Km (Return buffer)");
				appendDroneAndCommonLog(i,log);

				edgeWeight = 0.0;
				previousID = 0;

				// ******* BUILD THE PATH STRING FOR LOGGING ****
				String pathHopsStr = "Assigned Path hops:\n";
				for(Shipment s: path.getVertexList()) {
					currentID = s.getReqID();
					if(previousID != 0) {
						edgeWeight = LatLngTool.distance(idMap.get(previousID).getCoords(), idMap.get(currentID).getCoords(), LengthUnit.KILOMETER);
						pathHopsStr += s.getReqID()+" \\ "+ String.format("%.1f",edgeWeight)+" Km\n";
					}
					else {
						pathHopsStr += s.getReqID()+" \\\n";
					}
					previousID = currentID;
				}
				// **********************************************
				if(logPath) {
					appendFullAll(pathHopsStr);
					this.drones.get(i).appendFullLog(pathHopsStr);
				}	

				//Assign the shipments to the drones queue
				List<Shipment> actualPath = executeDronePath(path.getVertexList(), i, e);

				edgeWeight = 0.0;
				currentID = previousID = 0;

				String actualPathHops = "Actual Path hops (reachable with the battery charge):\n"+this.warehouse.getReqID()+" \\\n";

				nodelist = "";

				// ******* BUILD THE ACTUAL PATH STRING FOR LOGGING ****
				if(actualPath.size() > 0) {
					for(Shipment s: actualPath) {
						currentID = s.getReqID();
						if (previousID ==0) {
							nodelist += currentID+" ";
							edgeWeight = LatLngTool.distance(this.warehouse.getCoords(), idMap.get(currentID).getCoords(), LengthUnit.KILOMETER);
							actualPathHops += s.getReqID()+" \\ "+ String.format("%.1f",edgeWeight)+" Km\n";
						} else  {
							nodelist += currentID+" ";
							edgeWeight = LatLngTool.distance(idMap.get(previousID).getCoords(), idMap.get(currentID).getCoords(), LengthUnit.KILOMETER);
							actualPathHops += s.getReqID()+" \\ "+ String.format("%.1f",edgeWeight)+" Km\n";
						} 
						previousID = currentID;
						actualPathLength += edgeWeight;

					}
					edgeWeight =  LatLngTool.distance(idMap.get(currentID).getCoords(), this.warehouse.getCoords(),LengthUnit.KILOMETER);
					actualPathHops += warehouse.getReqID()+" \\ "+ String.format("%.1f",edgeWeight)+" Km\n";
					actualPathLength += edgeWeight;
				}
				// ****************************************************

				log = "Length of actual delivery path: "+String.format("%.1f",actualPathLength)+" Km";
				log += "\nActual Path size (without warehouse hops)  " + actualPath.size();
				appendDroneAndCommonLog(i,log);


				if(actualPath.size() > 0 ) {
					appendFullAll(actualPathHops);
					this.drones.get(i).appendFullLog(actualPathHops);

					log = "Delivered IDs: [ "+ nodelist +"]";

					//delivered += actualPath.size();
					log += "\nSuccessfully delivered: " + (actualPath.size()); 

					this.drones.get(i).setHasDelivered(actualPath.size() + this.drones.get(i).getHasDelivered());

					log += "\nNot delivered: " + (origPathSize - actualPath.size() );

					this.drones.get(i).setMissedDelivery(origPathSize - actualPath.size() -2 + this.drones.get(i).getMissedDelivery());

					log += "\nEnergy used: " + drones.get(i).getUsedEnergy() + "/" + drones.get(i).getMaxEnergy() + 
							"\nShipments in warehouse: "+this.toDeliverMap.size();

					appendDroneAndCommonLog(i,log);
					if(debug) System.out.println(log);

					//actual path is 0 but the calculated is 3 including the warehouse.
					//There was only one shipment but the battery range was not enough. 
				} else if(pathLength > 0 && origPathSize == 3) { 
					log = "This drone model can't deliver. Too far. ";
					appendDroneAndCommonLog(i,log);
				} else if(pathLength > 0 && origPathSize > 3 ) {
					log = "Path need to be reviewed. Can't delivery with this path.";
					appendDroneAndCommonLog(i,log);
				}
				else {
					log = "No shipments collected";
					appendDroneAndCommonLog(i,log);

				}

			}
			else {
				log = "Drone is at the warehouse. RECHARGING... \n" +
						"Shippments in warehouse: "+this.toDeliverMap.size() + " - "+this.toDeliverMap.keySet()+"\n";
				appendDroneAndCommonLog(i,log);
				if(debug) System.out.println(log);
				if(debug) System.out.println(this.toDeliverMap);
				//recharge the drone battery
				this.drones.get(i).setCurrentEnergy(this.drones.get(i).getCurrentEnergy() + 1);
				this.drones.get(i).setTimeslotsInCharge(this.drones.get(i).getTimeslotsInCharge() + 1);
				//this.drones.get(i).getAssigned().clear();

			}

		}

	}



	public void appendDroneAndCommonLog(int drone, String log) {
		appendAll(log);
		appendFullAll(log);
		this.drones.get(drone).appendLog(log);
		this.drones.get(drone).appendFullLog(log);
	}

	public Set<Shipment> getShipmentCitySet() {
		return this.shipmentCitySet;
	}

	/**
	 * Searches a Hamiltonian path in the given graph
	 * @param vertexes
	 * @return the GraphPath object related to the cycle found
	 */
	public GraphPath<Shipment, DefaultWeightedEdge> searchPath(Set<Shipment> vertexes) {
		vertexes.add(this.warehouse);
		Graph<Shipment, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		//Vertex add
		Graphs.addAllVertices(graph, vertexes);
		//Edge add
		for(Distance d : dao.getDistances(this.city, vertexes)) {
			Graphs.addEdge(graph, idMap.get(d.getObj1()), idMap.get(d.getObj2()), d.getWeight());
		}	
		/*
		 * * * * CORE PART - HAMILTONIAN PATH GENERATION USING NearestNeighborHeuristicTSP * * *
		 */
		NearestNeighborHeuristicTSP<Shipment, DefaultWeightedEdge> hamCycle = new NearestNeighborHeuristicTSP<Shipment, DefaultWeightedEdge>(this.warehouse);
		GraphPath<Shipment,DefaultWeightedEdge> path = hamCycle.getTour(graph);
		/*
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
		 */

		return path;
	}

/**
 * 
 * @param path the path from the Hamiltonian cycle algorithm that the drone need to follow
 * @param droneID 
 * @param e the event when the collection happens
 * @return actual executed path
 */
	public List<Shipment> executeDronePath(List<Shipment> path, int droneID, Event e) {
		if(path.size() == 1 && path.contains(warehouse)) return new ArrayList<Shipment>();
		double partialPathLength = 0.0;
		int previousID = 0;
		int currentID;
		boolean incompletePath = false;
		boolean leftWarehouse = false;
		Double edgeWeight = 0.0;
		Double returnWeight = 0.0;
		Set<Shipment> notDelivered = new HashSet<Shipment>();
		for(Shipment s: path) {
			currentID = s.getReqID();
			if(previousID!=0) {
				edgeWeight = LatLngTool.distance(idMap.get(previousID).getCoords(), idMap.get(currentID).getCoords(), LengthUnit.KILOMETER); 
				returnWeight = LatLngTool.distance(idMap.get(currentID).getCoords(), this.warehouse.getCoords(), LengthUnit.KILOMETER);
				if(partialPathLength + edgeWeight + returnWeight > ( this.drones.get(droneID).getMaxBatteryRangeKM() - this.returnBuffer )  || incompletePath) {
					if(s.getReqID() != warehouse.getReqID()) {
						notDelivered.add(s);					
						incompletePath = true;
						this.toDeliverMap.put(s.getReqID(),s);
						
					}
				} else {
					if(s.getReqID() != warehouse.getReqID()) {
						partialPathLength += edgeWeight;
						delivered.put(s.getReqID(),s);
						leftWarehouse = true;
						if(debug) System.out.println(s.getReqID()+" delivered at "+e+" with drone "+droneID);

					}

				}
			}
			previousID = currentID;

		}

		if(debug) System.out.println("Full Path:"+path);
		if(debug) System.out.println("Not delivered list: "+notDelivered);
		if(debug) System.out.println("Path contains warehouse: "+path.contains(warehouse));
		if(debug) System.out.println("notDelivered contains warehouse: "+notDelivered.contains(warehouse));

		path.removeAll(notDelivered);
		
		if(path.size() > 1) path.remove(this.warehouse); // remove warehouse - first occurrence is start point
		if(path.size() > 1) path.remove(this.warehouse); // remove warehouse - last occurrence is end point
		if(path.size() == 1 && path.contains(warehouse) ) path = new ArrayList<Shipment>(); //in case only of one element, then new list;

		int energyUsed = (int) Math.round(partialPathLength / drones.get(droneID).getEnergyFactor());
		if(leftWarehouse && energyUsed==0) energyUsed++;  
		int initialEnergy = drones.get(droneID).getMaxBatteryRangeKM() / drones.get(droneID).getEnergyFactor(); 
		drones.get(droneID).getAssigned().removeAll(path);
		drones.get(droneID).setCurrentEnergy(initialEnergy - energyUsed);

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
		collectEventsNumber = 0;
		logCommon = "";
		logAll = "";

	}

	public void setRate(Double rate) {
		this.sampleRate = rate;

	}

	public void setvar(Double variance) {
		this.timeslotVariability = variance;

	}

	public int getLogDrone() {
		return this.logDrone;
	}

	public void setLogDrone(int logDrone) {
		this.logDrone = logDrone;
	}

	public boolean isLogPath() {
		return this.logPath;
	}

	public void setLogPath(boolean logPath) {
		this.logPath = logPath;
	}

	public void setBuffer(boolean returnBuffer) {
		if(returnBuffer) this.returnBuffer=5.0;
		else this.returnBuffer=0.0;
	}

	public void setStart(LocalTime of) {
		this.startFlights = of;

	}

	public void setStop(LocalTime of) {
		this.stopFlights = of;

	}

	public void appendCommon(String log) {
		this.logCommon = this.logCommon += log + "\n";
	}

	public void appendAll(String log) {
		this.logAll = this.logAll += log + "\n";
	}

	public void appendFullAll(String log) {
		this.fullLogAll = this.fullLogAll += log + "\n";
	}

	public String getCommon() {
		return this.logCommon;
	}

	public String getAll() {
		return this.logAll;
	}

	public String getFullAll() {
		return this.fullLogAll;
	}

}



