package it.polito.tdp.dronedelivery;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class FXMLController implements Initializable {
    
	
	
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
        System.out.println("You clicked me!");
        label.setText("Hello World!");
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
}
