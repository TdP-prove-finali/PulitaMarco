package it.polito.tdp.dronedelivery.model;

import java.time.LocalTime;

public class Event implements Comparable<Event>{

	//definizione classe pubblica enumerazione
	
	public enum EventType {
		NEW_COLLECT 
	}
	
	private LocalTime time;
	
	private EventType type;

	/**
	 * @param time
	 * @param type
	 */
	public Event(LocalTime time, EventType type) {
		super();
		this.time = time;
		this.type = type;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	@Override
	public int compareTo(Event other) {
		// TODO Auto-generated method stub
		return this.time.compareTo(other.time);
	}

	@Override
	public String toString() {
		return "Event [time=" + time + ", type=" + type + "]";
	}
	
	
	
	
}