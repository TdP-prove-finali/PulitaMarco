package it.polito.tdp.dronedelivery.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Marco Pulita - s242664
 *
 */
public class Drone {


	private int maxBatteryRangeKM; //Range for a standard drone, 
	private List<Shipment> pathContainer;
	private Set<Shipment> assigned; 
	private int currentEnergy;
	private int energyFactor;
	private int hasDelivered = 0;
	private int missedDelivery = 0;
	private String log = "";
	private String fullLog = "";
	private int timeSlotsInCharge = 0;


	/**
	 * Creates a new object Drone with {@code maxBatteryRangeKM} as maximum length of flyable path.
	 * @param maxBatteryRangeKMs
	 */

	public Drone(int maxBatteryRangeKM) {
		super();
		this.maxBatteryRangeKM = maxBatteryRangeKM;
		this.pathContainer = new ArrayList<Shipment>();
		this.assigned = new HashSet<Shipment>();
		this.energyFactor = 12; // maxBatteryRangeKM / this value gives the number of "cells" of the battery.   
		this.currentEnergy = maxBatteryRangeKM / energyFactor; //a drone that can fly 40KM is fully charged after 3 time intervals. 
	}

	public int getEnergyFactor() {
		return energyFactor;
	}

	public void setEnergyFactor(int energyFactor) {
		this.energyFactor = energyFactor;
	}

	public void reset() {
		this.pathContainer = new ArrayList<Shipment>();
		this.assigned = new HashSet<Shipment>();
		this.currentEnergy = maxBatteryRangeKM / energyFactor;
		this.missedDelivery = 0;
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
		return this.assigned;
	}

	public void setAssigned(Set<Shipment> assigned) {
		this.assigned = assigned;
	}

	public void setMaxBatteryRangeKM(int maxBatteryRangeKM) {
		this.maxBatteryRangeKM = maxBatteryRangeKM;
	}

	public int getHasDelivered() {
		return hasDelivered;
	}

	public void setHasDelivered(int hasDelivered) {
		this.hasDelivered = hasDelivered;
	}
	
	public int getMissedDelivery() {
		return missedDelivery;
	}

	public void setMissedDelivery(int missedDelivery) {
		this.missedDelivery = missedDelivery;
	}

	public String getLog() {
		return log;
	}

	public String getFullLog() {
		return fullLog;
	}

	
	public int getTimeslotsInCharge() {
		return timeSlotsInCharge;
	}

	public void setTimeslotsInCharge(int timeSlotsInCharge) {
		this.timeSlotsInCharge = timeSlotsInCharge;
	}

	public void  appendLog(String log) {
		this.log = this.log += log+"\n";
	}
	
	public void  appendFullLog(String log) {
		this.fullLog = this.fullLog += log+"\n";
	}
	

}
