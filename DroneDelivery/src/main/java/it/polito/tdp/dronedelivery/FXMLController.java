package it.polito.tdp.dronedelivery;



import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import it.polito.tdp.dronedelivery.db.DeliveryDAO;
import it.polito.tdp.dronedelivery.model.City;
import it.polito.tdp.dronedelivery.model.Distance;
import it.polito.tdp.dronedelivery.model.Drone;
import it.polito.tdp.dronedelivery.model.Event;
import it.polito.tdp.dronedelivery.model.Model;
import it.polito.tdp.dronedelivery.model.Shipment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class FXMLController  {
    

	private Model model;
	private String city="";
	private String totalShipments="";
	private Double rate=100.0;
	private Double deviance=30.0;
	private String dronesList="";

	    @FXML
	    private ComboBox<String> dropcity;

	    @FXML
	    private Slider sliderate;

	    @FXML
	    private Slider slidedev;

	    @FXML
	    private ComboBox<Integer> droprange;

	    @FXML
	    private Button btnadd;

	    @FXML
	    private Button btnreset;

	    @FXML
	    private TextArea txtlog;

	    @FXML
	    private TextArea txtdetails;

	    @FXML
	    private Button btnstart;

	    @FXML
	    private void doAddDrone(ActionEvent event) {
	    	
	    	int droneNumber = model.addDrone(droprange.getValue());
	    	Drone added = model.getDrone(droneNumber);
	    	this.dronesList += "Drone "+(droneNumber + 1)+ " - Range: " + 
	    	                   droprange.getValue() + "Km, Battery "+added.getCurrentEnergy()+"/"+added.getMaxEnergy()+"\n";
	    	detailsRefresh();
	    }
	    
	    @FXML
	    private void doCitySelect(ActionEvent event) {
		    if(dropcity.getValue() != null) {
		    	this.city = ( dropcity.getValue().split(":") )[0];
		    	this.totalShipments = ( dropcity.getValue().split(":") )[1];
	
		    	detailsRefresh();
		    }
	    }
	
	    @FXML
	    private void doRateUpdate() {
	    	this.rate = sliderate.getValue();
	    	model.setRate(this.rate);
	    	detailsRefresh();
	    }
	    
	    @FXML
	    private void doDevUpdate() {
	    	this.deviance = slidedev.getValue();
	    	model.setDev(this.deviance);
	    	detailsRefresh();
	    }
	    

    @FXML
    private void doSimulationStart(ActionEvent event) {
    	
    	txtlog.clear();
    	if(dropcity.getValue() == null) {
    		txtlog.appendText("Select a city to start the simulation\n");
    	}
    	else if (model.getDronesNumber() == 0) {
    		txtlog.appendText("Add at least one drone to start the simulation\n");
    	}
    	else {
    		model.init();
        	model.setCity(city);
        	model.setRate(this.rate);
        	model.setDev(this.deviance);
	    		//txtdetails.appendText("Sampling Rate: 100%\n");
	    	
	    	
	        //this set the city parameter variable and fill model.shipmentCitySet with the related shipments from the DB
	    	;
	    	txtlog.appendText("Sim started for " + model.getCity());
			//model.setCity(this.city);
	    	model.run() ;
			System.out.println("");
    	}
    }
    
    @FXML
    private void doReset(ActionEvent event) {
    	this.city = "";
    	this.dropcity.valueProperty().set(null);
    	this.droprange.valueProperty().set(null);
    	model.initDrones();
    	this.sliderate.setValue(100.0);
    	this.slidedev.setValue(30.0);
    	this.totalShipments="";
    	this.dronesList="";
    	txtlog.clear();
    	txtdetails.clear();
    }

    
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert dropcity != null : "fx:id=\"dropcity\" was not injected: check your FXML file 'Scene.fxml'.";
        assert sliderate != null : "fx:id=\"sliderate\" was not injected: check your FXML file 'Scene.fxml'.";
        assert slidedev != null : "fx:id=\"slidedev\" was not injected: check your FXML file 'Scene.fxml'.";
        assert droprange != null : "fx:id=\"droprange\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnadd != null : "fx:id=\"btnadd\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnreset != null : "fx:id=\"btnreset\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtlog != null : "fx:id=\"txtlog\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtdetails != null : "fx:id=\"txtdetails\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnstart != null : "fx:id=\"btnstart\" was not injected: check your FXML file 'Scene.fxml'.";
        
        
    }
        
    private void detailsRefresh() {
    	txtdetails.clear();
    	
    	txtdetails.appendText("City: " + this.city+"\n");
    	txtdetails.appendText("Total shipments available: " + totalShipments+"\n\n");
    	txtdetails.appendText("Sampling rate: " + String.format("%.1f", this.rate) +"%\n");
    	txtdetails.appendText("Event collection deviance: +/-" + String.format("%.1f", this.deviance) +"%\n\n");
    	txtdetails.appendText("========DRONES========\n");
    	txtdetails.appendText(this.dronesList+"\n");
    	//txtdetails.appendText();
    	//txtdetails.appendText();
    	//txtdetails.appendText();
    	
    }
    
    //e'chiamata nella entrypoint
    public void setModel(Model model) {
    	this.model = model;
    	model.initDrones();
    	model.getCityList();
    	
    	for(City s: model.getCityDropDown()) {
    		dropcity.getItems().add(s.getCityName() + ":["+s.getAvailableShipments()+"]");
    	}
    	List<Integer> batteryRanges = new ArrayList<Integer>(); 
    	batteryRanges.add(20); 
    	batteryRanges.add(30); 
    	batteryRanges.add(40); 
    	batteryRanges.add(50); 
    	batteryRanges.add(60); 
    	batteryRanges.add(70); 
    	batteryRanges.add(80); 
    	droprange.getItems().addAll(batteryRanges);
    	
    }

}
