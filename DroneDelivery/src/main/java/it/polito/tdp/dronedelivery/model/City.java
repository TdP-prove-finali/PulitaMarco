package it.polito.tdp.dronedelivery.model;

public class City {


	
	public String cityName;
	public Shipment warehouse;
	public int availableShipments;
	
	/**
	 * Models a City object
	 * @param name of the city
	 * @param warehouse details as {@code Shipment} object
	 * @param numShipments available shipments in database
	 */
	
	public City (String name, Shipment warehouse, int numShipments) {
		this.cityName = name;
		this.warehouse = warehouse;
		this.availableShipments = numShipments;
	}

	public String getCityName() {
		return cityName;
	}

	public Shipment getWarehouse() {
		return warehouse;
	}

	public int getAvailableShipments() {
		return availableShipments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cityName == null) ? 0 : cityName.hashCode());
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
		City other = (City) obj;
		if (cityName == null) {
			if (other.cityName != null)
				return false;
		} else if (!cityName.equals(other.cityName))
			return false;
		return true;
	}
	
	
}
