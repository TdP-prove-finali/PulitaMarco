package it.polito.tdp.dronedelivery.model;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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



import it.polito.tdp.dronedelivery.db.DeliveryDAO;
import it.polito.tdp.dronedelivery.model.Event.EventType;


public class Model {
	private Graph<Shipment, DefaultWeightedEdge> graph;
	private Map<Integer, Shipment> idMap;	
	private Set<Shipment> shipmentCitySet; //filled by setCity()
	
	// Coda degli eventi
	private PriorityQueue<Event> queue = new PriorityQueue<>();

	// Parametri di simulazione
	private int sampleRate = 100; //definire totale sample e variazione a time slot: es 60% su 4 time slot, 15 a time slot sul time slot inserire % es +-33% tra 10 e 20
	private int nDrones = 3;
	private Double timeslotDeviation = 30.0;
	private Duration S_INT = Duration.of(30, ChronoUnit.MINUTES); // clock sampling interval
	// private final LocalTime firstCollect = LocalTime.of(8, 30);
	private final LocalTime startFlights = LocalTime.of(9, 00);
	private final LocalTime stopFlights = LocalTime.of(17, 30);
    private DeliveryDAO dao;
    private int collectEventsNumber;
    private int toDeliverSubsetSize;

	// modello del mondo
	private List<Drone> drones = new ArrayList<Drone>();
	// valori da calcolare

	//private Set<Shipment> delivered = new HashSet<Shipment>();
	private List<Shipment> toDeliverSubset = new ArrayList<Shipment>(); //using list (slow removal) to force random index selection calling Math.random 
	//private List<Set<Shipment>> assigned = new ArrayList<Set<Shipment>>();  //cercare di rimuovere inserendo una variabile di tipo set o lista in drone
	
	
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
		
	}
	
	public void setToDeliverSubset() {
		
		this.toDeliverSubset.addAll(this.shipmentCitySet);
		int size = this.shipmentCitySet.size();
		int numberToRemove = (int) Math.round( (Double.valueOf(100 - this.sampleRate)/100)*size);
				
		
		String removed = "[";
		int i;
		for(i=0; i<numberToRemove; i++) {
        	int randomPick = new Random().nextInt(this.toDeliverSubset.size());
        	removed += this.toDeliverSubset.get(randomPick).getReqID() + " ";
        	this.toDeliverSubset.remove(randomPick);
        }
        removed += "]";
        
        //System.out.println("Old size: "+shipmentCitySet.size());
        //System.out.println("New size: "+toDeliverSubset.size());
        //System.out.println("Removed shipments : "+ removed);
        System.out.println("Rimossi : "+ numberToRemove);
        
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
		
		this.dao = new DeliveryDAO();
		this.shipmentCitySet = dao.getAllShipments(city, idMap);
		//System.out.println(city+" "+dao+" "+idMap.hashCode());
	}
	
	

	// metodi di impostzione parametri

	public void setNumDrones(int n) {
		this.nDrones = n;
	}


	public void startDrones() {
		int i;
		this.drones.clear();
		for(i=0; i<this.nDrones; i++) {
			drones.add(i, new Drone(20));
			this.drones.get(i).setAssigned(new HashSet<Shipment>());
			//this.assigned.add(i, new HashSet<Shipment>());
		}
	}
	
	
	public void distributeToDeliverSubset(int shipmentsToDistribute) {
		
		
			
		//System.out.println(this.toDeliverSubset.size());
		ListIterator<Shipment> lIt = this.toDeliverSubset.listIterator();
		Set<Shipment> toRemove = new HashSet<Shipment>();
		Shipment s;
		while (lIt.hasNext()) {
            int j;
			for(j=0; j<this.nDrones; j++) {
				if(lIt.hasNext() && shipmentsToDistribute > 0) {
					s = lIt.next();
					this.drones.get(j).getAssigned().add(s); // && this.drones.get(j).getCellsToCharge() == 0 controllo batteria da spostare in event process, se non si possono deliverare li rimetto in toDeliverSubset
					toRemove.add(s);
					shipmentsToDistribute--;
				}
					//this.assigned.get(j).add(lIt.next());
				//lIt.remove();
				//-->> mettere remove all alla fine toDeliverSubset.remove(lIt.next());
			}
        	//lIt.next();
			if(shipmentsToDistribute <= 0) break;
        }
        this.toDeliverSubset.removeAll(toRemove);
	}
	
	public void setConnectionFrequency(Duration d) {
		this.S_INT = d;
	}
	/*
	 * public Object getDronesPaths() { return dronesPaths; }
	 */

	     //parametri simulazione  settati nella controller, vanno pulite le code e fatta partire la simulazione
	
		public void run() {

			/*this.dronesPaths.get(0);
			
			Model model = new Model();
			
			String city = "SAN FRANCISCO";
			
			
			
			model.graphBuilder("SAN FRANCISCO"); */
			//this.assigned.clear();
			
			
			//System.out.println(shipmentCitySet);
			
			//preparazione simulazione (mondo+coda eventi)
		
			//this.nDrones = 0; da settare nell'xml controller model.setndroni()
			//this.delivered.clear();
			this.toDeliverSubset.clear();
			
			startDrones(); 
			
			//coda eventi
			this.queue.clear();
			
			setToDeliverSubset(); // pick a portion based on the 
			
			
			
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
			this.toDeliverSubsetSize = this.toDeliverSubset.size();
			this.collectEventsNumber = this.queue.size();
			
			//esecuzione simulazione 
			while(!this.queue.isEmpty()) {
				Event e = this.queue.poll();
				System.out.println(e);
				processEvent(e);

			} 

		}

		private void processEvent(Event e) {
			
			
			int toDeliverNumber = (int) Math.round(((( this.timeslotDeviation / 100.0 ) * 
			           ( Math.random() - 0.5) * 2.0  ) + 1.0) * (this.toDeliverSubsetSize/(this.collectEventsNumber -1) ) );
			if(this.queue.size()==0) toDeliverNumber = this.toDeliverSubset.size();
			distributeToDeliverSubset(toDeliverNumber); 
			System.out.println("----------");
			System.out.println(this.queue.size());
			System.out.println("collected: " + toDeliverNumber);
			System.out.println("In queue: " + this.toDeliverSubset.size());
			
			
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
		
		
		
		
		
		//cammino hamiltoniano !! DA IMPLEMENTARE!! IL DRONE DEVE SEMPRE TORNARE ALLA BASE PER PRENDERE NUOVE BUSTE. PRIMO E ULTIMO VERTICE SEMPRE BASE
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
	
	

