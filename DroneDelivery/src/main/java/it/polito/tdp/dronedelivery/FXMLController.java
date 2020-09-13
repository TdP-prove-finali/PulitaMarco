package it.polito.tdp.dronedelivery;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.dronedelivery.model.City;
import it.polito.tdp.dronedelivery.model.Drone;
import it.polito.tdp.dronedelivery.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;


public class FXMLController  {

	
	private Model model;
	private String city="";
	private String totalShipments="";
	private Double rate=100.0;
	private Double variability=30.0;
	private Integer interval=30;
	private String dronesList="";
	private boolean logPath = false;
	private boolean buffer = false;
	private int logDrone = -1;
	private List<String> logDronesList = new ArrayList<String>();
	private LocalTime startFlights = LocalTime.of(9, 00);
	private LocalTime stopFlights = LocalTime.of(17, 30);

	@FXML
	private ComboBox<String> dropcity;

	@FXML
	private Slider sliderate;

	@FXML
	private Slider slidevar;

	@FXML
	private ComboBox<Integer> droprange;

	@FXML
	private ComboBox<Integer> dropInterval;
	
	@FXML
	private ComboBox<String> dropdronefilter;
	
	@FXML
	private ComboBox<String> dropfrom;
	
	@FXML
	private ComboBox<String> dropto;

	@FXML
	private CheckBox checkpath;
	
	@FXML
	private CheckBox checkbuffer;

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


	/**
	 * Add a drone in the {@code model}
	 */
	
	@FXML
	private void doAddDrone(ActionEvent event) {

		if(droprange.getValue() != null) {
			int droneNumber = model.addDrone(droprange.getValue());
			Drone added = model.getDrone(droneNumber);
			dronesList += "Drone "+droneNumber+ " - Range: " + 
					droprange.getValue() + "Km, Battery "+added.getCurrentEnergy()+"/"+added.getMaxEnergy()+"\n";
			detailsRefresh();
			logDronesList.add("Drone "+droneNumber);
			dropdronefilter.getItems().clear();
			dropdronefilter.getItems().addAll(logDronesList);
		}
		else txtlog.appendText("Add at least one drone to start the simulation\n");
	}

	/**
	 * Updates the {@code city} parameter and calls {@link detailsRefresh()} 
	 */
	
	@FXML
	private void doCitySelect(ActionEvent event) {
		if(dropcity.getValue() != null) {
			this.city = ( dropcity.getValue().split(":") )[0];
			this.totalShipments = ( dropcity.getValue().split(":") )[1];
			detailsRefresh();
		}
	}

	/**
	 * Updates the {@code rate} parameter and calls {@link detailsRefresh()} 
	 */
	
	@FXML
	private void doRateUpdate() {
		this.rate = sliderate.getValue();
		
		detailsRefresh();
	}
	
	/**
	 * Updates the {@code S_INT} parameter (simulation interval) and calls {@link detailsRefresh()} 
	 */
	@FXML
	private void doIntervalUpdate() {
	
			this.interval = dropInterval.getValue();
			detailsRefresh();
		
	}

	@FXML
	private void doSetFrom() {
		
			String startString = dropfrom.getValue();
			switch(startString) {
				case "8:00":
				this.startFlights=LocalTime.of(8, 00);	
				break;
				case "8:30":
				this.startFlights=LocalTime.of(8, 30);	
				break;
				case "9:00":
				this.startFlights=LocalTime.of(9, 00);	
				break;
			}
			detailsRefresh();
		
	}
	
	@FXML
	private void doSetTo() {
		
		
			String stopString = dropto.getValue();
			switch(stopString) {
				case "17:00":
				this.stopFlights=LocalTime.of(17, 00);	
				break;
				case "17:30":
				this.stopFlights=LocalTime.of(17, 30);	
				break;
				case "18:00":
				this.stopFlights=LocalTime.of(18, 00);	
				break;
			}
			detailsRefresh();
		
	}
	
	
	/**
	 * Updates the variability parameter and calls {@link detailsRefresh()}.
	 */
	
	@FXML
	private void doVarUpdate() {
		variability = slidevar.getValue();
		detailsRefresh();
	}


	/**
	 * Starts the simulation
	 * @param event
	 */
	
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
			detailsRefresh();
			model.init();
			model.setCity(city);
			model.resetDrones();
			model.setStart(this.startFlights);
			model.setStop(this.stopFlights);
			model.setCollectionFrequency(this.interval);
		    model.setBuffer(buffer);
			model.setRate(rate);
			model.setvar(variability);
			model.setLogPath(logPath);
			model.setLogDrone(logDrone);
			
			//txtlog.appendText("Sim started for " + model.getCity());
			model.run() ;
			txtlog.appendText(model.getCommon());
			
			if(logPath) {
				if(logDrone == -1 )txtlog.appendText(model.getFullAll());
				else txtlog.appendText(model.getDrone(logDrone).getFullLog());				
			} else {
				if(logDrone == -1 )txtlog.appendText(model.getAll());
				else txtlog.appendText(model.getDrone(logDrone).getLog());
			}
		}
	}

	
	/**
	 * Executes the reset of simulation parameters and information text boxes
	 * @param event
	 */
	
	@FXML
	private void doReset(ActionEvent event) {
		city = "";
		dropcity.valueProperty().set(null);
		droprange.valueProperty().set(null);
		if(dropInterval.valueProperty() != null) dropInterval.setValue(null);
		if(dropfrom.valueProperty() != null) dropfrom.valueProperty().set("");
		if(dropto.valueProperty() != null) dropto.valueProperty().set("");
		
		/*List<Integer> collectIntervals = new ArrayList<Integer>(); 
		collectIntervals.add(30); 
		collectIntervals.add(60); 
		collectIntervals.add(120); 		
		List<String> collectStart = new ArrayList<String>(); 
		collectStart.add("8:00"); 
		collectStart.add("9:30"); 
		collectStart.add("9:00"); 
		List<String> collectEnd = new ArrayList<String>(); 
		collectEnd.add("17:00"); 
		collectEnd.add("17:30"); 
		collectEnd.add("18:00"); 
		dropInterval.getItems().clear();
		dropInterval.getItems().addAll(collectIntervals);
		dropfrom.getItems().clear();
		dropfrom.getItems().addAll(collectStart);
		dropto.getItems().clear();
		dropto.getItems().addAll(collectEnd);*/
		model.initDrones();
		sliderate.setValue(100.0);
		slidevar.setValue(30.0);
		totalShipments="";
		dronesList="";
		logPath = false;
		checkpath.setSelected(false);
		buffer = true;
		checkbuffer.setSelected(false);
		interval = 30;
		dropdronefilter.getItems().clear();
		txtlog.clear();
		txtdetails.clear();
		logDronesList = new ArrayList<String>();
		logDronesList.add("All");
		dropdronefilter.getItems().addAll(logDronesList);
		logDrone = -1;
		startFlights = LocalTime.of(9, 00);
		stopFlights = LocalTime.of(17, 30);
				
				
	}

	/**
	 * Switches the logPath FXMLController class variable and calls {@link doCitySelect()} . 
	 * @param event
	 */
	@FXML
	private void doLogPath(ActionEvent event) {
		logPath = !logPath;

		detailsRefresh();
	}

	@FXML
	private void doBuffer(ActionEvent event) {
		buffer = !buffer;

		detailsRefresh();
	}
	
	/**
	 * Applies the Drone filtering and calls {@link detailsRefresh()}
	 * @param event
	 */
	
	@FXML
	private void doDroneFilter(ActionEvent event) {
		if(dropdronefilter.getValue() != null) {
			if(dropdronefilter.getValue().equals("All") ) logDrone = -1;
			else logDrone = Integer.valueOf(dropdronefilter.getValue().split(" ")[1]);
		}

		detailsRefresh();
	}


	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert dropcity != null : "fx:id=\"dropcity\" was not injected: check your FXML file 'Scene.fxml'.";
		assert sliderate != null : "fx:id=\"sliderate\" was not injected: check your FXML file 'Scene.fxml'.";
		assert slidevar != null : "fx:id=\"slidevar\" was not injected: check your FXML file 'Scene.fxml'.";
		assert droprange != null : "fx:id=\"droprange\" was not injected: check your FXML file 'Scene.fxml'.";
		assert dropInterval != null : "fx:id=\"dropInterval\" was not injected: check your FXML file 'Scene.fxml'.";
		assert btnadd != null : "fx:id=\"btnadd\" was not injected: check your FXML file 'Scene.fxml'.";
		assert btnreset != null : "fx:id=\"btnreset\" was not injected: check your FXML file 'Scene.fxml'.";
		assert txtlog != null : "fx:id=\"txtlog\" was not injected: check your FXML file 'Scene.fxml'.";
		assert txtdetails != null : "fx:id=\"txtdetails\" was not injected: check your FXML file 'Scene.fxml'.";
		assert btnstart != null : "fx:id=\"btnstart\" was not injected: check your FXML file 'Scene.fxml'.";
		assert dropdronefilter != null : "fx:id=\"dropdronefilter\" was not injected: check your FXML file 'Scene.fxml'.";
		assert dropfrom != null : "fx:id=\"dropFrom\" was not injected: check your FXML file 'Scene.fxml'.";
		assert dropto != null : "fx:id=\"dropTo\" was not injected: check your FXML file 'Scene.fxml'.";
		assert checkpath != null : "fx:id=\"checkpath\" was not injected: check your FXML file 'Scene.fxml'.";
		assert checkbuffer != null : "fx:id=\"checkbuffer\" was not injected: check your FXML file 'Scene.fxml'.";
	}

	
	// Support methods
	
	/**
	 * Refreshes the details box
	 */
	
	private void detailsRefresh() {
		txtdetails.clear();
		txtdetails.appendText("======PARAMETERS======\n");
		txtdetails.appendText("City: " + this.city+"\n");
		txtdetails.appendText("Total shipments available: " + totalShipments+"\n\n");
		txtdetails.appendText("Sampling rate: " + String.format("%.1f", this.rate) +"%\n");
		txtdetails.appendText("Collection variability: +/-" + String.format("%.1f", this.variability) +"%\n");
		txtdetails.appendText("Event collection interval: " + this.interval +" mins\n");
		txtdetails.appendText("PickUp start time: " +startFlights+ " \n");
		txtdetails.appendText("PickUp stop time: " +stopFlights+ " \n\n");
		txtdetails.appendText("========DRONES========\n");
		if(buffer) txtdetails.appendText("5Km return buffer: enabled\n\n");
		else txtdetails.appendText("5Km return buffer: disabled\n\n");
		txtdetails.appendText(this.dronesList+"\n");
		txtdetails.appendText("=========LOG==========\n");
		if(logPath) txtdetails.appendText("Show full path: enabled\n");
		else txtdetails.appendText("Show full path: disabled\n");
		if(logDrone == -1) txtdetails.appendText("Log not filtered\n");
		else txtdetails.appendText("Log filtered for drone "+logDrone);
	}

   /**
    * Init the controller linking the {@code model} passed
    * @param model
    */
	
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
		batteryRanges.add(90); 
		batteryRanges.add(100); 
		droprange.getItems().addAll(batteryRanges);
		
		logDronesList.add("All");
		dropdronefilter.getItems().addAll(logDronesList);

		List<Integer> collectIntervals = new ArrayList<Integer>(); 
		collectIntervals.add(30); 
		collectIntervals.add(60); 
		collectIntervals.add(120); 
		dropInterval.getItems().addAll(collectIntervals);
		
		List<String> collectStart = new ArrayList<String>(); 
		collectStart.add("8:00"); 
		collectStart.add("9:30"); 
		collectStart.add("9:00"); 
		dropfrom.getItems().addAll(collectStart);
		
		List<String> collectEnd = new ArrayList<String>(); 
		collectEnd.add("17:00"); 
		collectEnd.add("17:30"); 
		collectEnd.add("18:00"); 
		dropto.getItems().addAll(collectEnd);
		
		
	}

}
