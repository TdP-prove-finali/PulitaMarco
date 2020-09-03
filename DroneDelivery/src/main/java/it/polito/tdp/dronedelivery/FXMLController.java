package it.polito.tdp.dronedelivery;

import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

import it.polito.tdp.dronedelivery.model.Model;
import it.polito.tdp.dronedelivery.model.Simulator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;


public class FXMLController implements Initializable {
    

	private Model model;

	
	//
	
	@FXML private Canvas img ;

    private GraphicsContext gc ;

    @FXML private void drawCanvas(ActionEvent event) {
        gc.setFill(Color.AQUA);
        gc.fillRect(10,10,100,100);
    }
    
    //
	
	
    @FXML
    private Label label;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        String city = "DALLAS";
    	System.out.println("Setting city:" + city);
        //this set the city parameter variable and fill model.shipmentCitySet with the related shipments from the DB
    	model.setCity(city); //convertire in initialize e chiamare setCity dal model.initialize(simulation parameters);
        label.setText("Sim started for " + city);
		model.run() ;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    	gc = img.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        System.out.println("color set to black");
        gc.fillRect(50, 50, 100, 100);
        System.out.println("draw rectangle");
    	//

    }  
    
    //e'chiamata nella entrypoint
    public void setModel(Model model) {
    	this.model = model;
        
    }

}
