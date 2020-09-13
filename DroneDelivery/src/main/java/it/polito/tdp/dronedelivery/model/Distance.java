package it.polito.tdp.dronedelivery.model;

public class Distance {

	
	public Integer obj1;
	public Integer obj2;
	public Double weight;
	
	/**
	 * Generic distance object
	 * @param obj1 as LatLng object
	 * @param obj2 as LatLng object
	 * @param weight distance between obj1 and obj2
	 */
	
	public Distance(Integer obj1, Integer obj2, double weight) {
		super();
		this.obj1 = obj1;
		this.obj2 = obj2;
		this.weight = weight;
	}

	public Integer getObj1() {
		return obj1;
	}

	public void setObj1(Integer obj1) {
		this.obj1 = obj1;
	}

	public Integer getObj2() {
		return obj2;
	}

	public void setObj2(Integer obj2) {
		this.obj2 = obj2;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
	
	
}