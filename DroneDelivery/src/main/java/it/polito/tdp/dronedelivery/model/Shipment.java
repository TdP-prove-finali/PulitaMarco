package it.polito.tdp.dronedelivery.model;

import com.javadocmd.simplelatlng.LatLng;

public class Shipment {

	private int reqID;
	private String address;
	private String city;
	private String state;
	private int zip; 
	private LatLng coords;
	
	/**
	 * 
	 * @param reqID the shipment ID, used to identify the object
	 * @param address
	 * @param city
	 * @param state
	 * @param zip
	 * @param coords the coordinates as LatLng object
	 */

	public Shipment(int reqID, String address, String city, String state, int zip, LatLng coords) {
		this.reqID = reqID;
		this.address = address;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.coords = coords;
		
	}

	public int getReqID() {
		return reqID;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public int getZip() {
		return zip;
	}

	public LatLng getCoords() {
		return coords;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + reqID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Shipment other = (Shipment) obj;
		if (reqID != other.reqID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[reqID: " + reqID + " - " + address + ", " + zip + " " + city + "]";
	}
	
}


