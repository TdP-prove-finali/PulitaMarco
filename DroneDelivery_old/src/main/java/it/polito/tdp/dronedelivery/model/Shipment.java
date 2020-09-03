package it.polito.tdp.dronedelivery.model;

import com.javadocmd.simplelatlng.LatLng;

public class Shipment {

	private int reqID;
	private String address;
	private String city;
	private String state;
	private int zip; 
	private LatLng coords;
	
	public Shipment(int reqID, String address, String city, String state, int zip, LatLng coords) {
		this.reqID = reqID;
		this.address = address;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.coords = coords;
	}

	@Override
	public String toString() {
		return "Shipment [reqID=" + reqID + ", address=" + address + ", city=" + city + ", state=" + state + ", zip="
				+ zip + ", coords=" + coords + "]";
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


}


