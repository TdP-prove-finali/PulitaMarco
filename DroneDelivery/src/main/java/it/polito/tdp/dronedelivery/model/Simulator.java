package it.polito.tdp.dronedelivery.model;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import it.polito.tdp.dronedelivery.model.Event.EventType;

public class Simulator {

	// Coda degli eventi
	private PriorityQueue<Event> queue = new PriorityQueue<>();

	// Parametri di simulazione
	private int sampleRate = 100; //definire totale sample e variazione a time slot: es 60% su 4 time slot, 15 a time slot sul time slot inserire % es +-33% tra 10 e 20
	private int timeSlotVariability =30;
	private int nDrones;
	private Duration S_INT = Duration.of(30, ChronoUnit.MINUTES); // clock sampling interval
	// private final LocalTime firstCollect = LocalTime.of(8, 30);
	private final LocalTime startFlights = LocalTime.of(9, 00);
	private final LocalTime stopFlights = LocalTime.of(18, 00);
	

	// modello del mondo
	private List<Drone> drones;

	// valori da calcolare

	private Set<Shipment> shipmentsAll = new HashSet<Shipment>();
	private Set<Shipment> delivered = new HashSet<Shipment>();
	private Set<Shipment> toDeliver = new HashSet<Shipment>();

	// metodi di impostzione parametri

	public void setNumDrones(int n) {
		this.nDrones = n;
	}


	public void setConnectionFrequency(Duration d) {
		this.S_INT = d;
	}
	/*
	 * public Object getDronesPaths() { return dronesPaths; }
	 */

	public void run() {

		/*this.dronesPaths.get(0);
		
		Model model = new Model();
		
		String city = "SAN FRANCISCO";
		
		
		
		model.graphBuilder("SAN FRANCISCO"); */
		

		
		//preparazione simulazione (mondo+coda eventi)
	
		this.nDrones = 0;
		this.delivered.clear();
		this.toDeliver.clear();

		//coda eventi
		this.queue.clear();

		LocalTime collectTime = this.startFlights;
		do {
			Event e = new Event(collectTime, EventType.NEW_COLLECT);
			this.queue.add(e);
			collectTime = collectTime.plus(this.S_INT);
		} while(collectTime.isBefore(this.stopFlights.plus(this.S_INT)));

		//esecuzione simulazione 
		while(!this.queue.isEmpty()) {
			Event e = this.queue.poll();
			System.out.println(e);
			processEvent(e);

		} 

	}

	private void processEvent(Event e) {
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
		 * break; }
		 */
	}
}