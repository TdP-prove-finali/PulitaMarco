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
	private int energyFactor;
	
	public int getEnergyFactor() {
		return energyFactor;
	}



	public void setEnergyFactor(int energyFactor) {
		this.energyFactor = energyFactor;
	}



	/**
	 * @param batteryStatus remaining battery in KM 
	 * @param maxBatteryRangeKM maxFlayableKM, standard model 20 (default), powered 35  
	 */
	
	public Drone(int maxBatteryRangeKM) {
		super();
		if(maxBatteryRangeKM < 40) this.maxBatteryRangeKM = 40;
		this.maxBatteryRangeKM = maxBatteryRangeKM;
		this.pathContainer = new ArrayList<Shipment>();
		this.assigned = new HashSet<Shipment>();
		this.energyFactor = 12;
		this.currentEnergy = maxBatteryRangeKM / energyFactor; //the standard drone is fully charged after 3 time intervals. 
				//System.out.println("Drone energy: " + this.currentEnergy);
	}

	public int getMaxEnergy() {
		return this.maxBatteryRangeKM / this.energyFactor;
	}
	
	public int getUsedEnergy() {
		return (this.maxBatteryRangeKM / this.energyFactor) - this.currentEnergy;
	}
	
	public void setCurrentEnergy(int energy) { 
		this.currentEnergy = energy;
	}
	
	public int getCurrentEnergy() {
		return this.currentEnergy;
	}



	public int getMaxBatteryRangeKM() {
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
