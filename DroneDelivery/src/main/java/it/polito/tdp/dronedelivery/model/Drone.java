package it.polito.tdp.dronedelivery.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco
 *
 */
public class Drone {

	private double batteryStatus;
	private double maxBatteryRangeKM = 20; //Range for a standard drone, 
	private List<Shipment> pathContainer;
	
	/**
	 * @param batteryStatus remaining battery in KM 
	 * @param maxBatteryRangeKM maxFlayableKM, standard model 20 (default), powered 35  
	 */
	
	public Drone(double batteryStatus, double maxBatteryRangeKM) {
		super();
		this.maxBatteryRangeKM = maxBatteryRangeKM;
		this.batteryStatus = maxBatteryRangeKM;
		this.pathContainer = new ArrayList<Shipment>();
		
	}


	public double getBatteryStatus() {
		return batteryStatus;
	}


	public void setBatteryStatus(int batteryStatus) {
		this.batteryStatus = batteryStatus;
	}


	public double getMaxBatteryRangeKM() {
		return maxBatteryRangeKM;
	}
	
	public void setPathContainer(List l) {
		this.pathContainer = l;
	}
	
		
}
