package it.polito.tdp.dronedelivery.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Marco
 *
 */
public class Drone {

	private int maxBatteryRangeKM; //Range for a standard drone, 
	private List<Shipment> pathContainer;
	private Set<Shipment> assigned; 
	private int currentEnergy;
	
	/**
	 * @param batteryStatus remaining battery in KM 
	 * @param maxBatteryRangeKM maxFlayableKM, standard model 20 (default), powered 35  
	 */
	
	public Drone(int maxBatteryRangeKM) {
		super();
		if(maxBatteryRangeKM < 20) this.maxBatteryRangeKM = 20;
		this.maxBatteryRangeKM = maxBatteryRangeKM;
		this.pathContainer = new ArrayList<Shipment>();
		this.assigned = new HashSet<Shipment>();
		this.currentEnergy = maxBatteryRangeKM / 4;
	}



	public void setCurrentEnergy(int energy) { // used with negative numbers to drain the battery
		this.currentEnergy = energy;
	}
	
	public int getCurrentEnergy() {
		return this.currentEnergy;
	}



	public double getMaxBatteryRangeKM() {
		return maxBatteryRangeKM;
	}
	


	public List<Shipment> getPathContainer() {
		return pathContainer;
	}


	public void setPathContainer(List<Shipment> pathContainer) {
		this.pathContainer = pathContainer;
	}


	public Set<Shipment> getAssigned() {
		return assigned;
	}


	public void setAssigned(Set<Shipment> assigned) {
		this.assigned = assigned;
	}


	public void setMaxBatteryRangeKM(int maxBatteryRangeKM) {
		this.maxBatteryRangeKM = maxBatteryRangeKM;
	}
	
		
}
