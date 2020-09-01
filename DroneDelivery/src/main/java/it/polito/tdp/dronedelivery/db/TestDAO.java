package it.polito.tdp.dronedelivery.db;

import java.sql.Connection;
import java.util.List;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.dronedelivery.db.DBConnect;
import it.polito.tdp.dronedelivery.db.DeliveryDAO;
import it.polito.tdp.dronedelivery.model.Shipment;

public class TestDAO {

	public static void main(String[] args) {
		
		try {
			Connection connection = DBConnect.getConnection();
			connection.close();
			System.out.println("Connection Test PASSED");
			
			DeliveryDAO dao = new DeliveryDAO() ;
			
			Shipment s1 = dao.getShipment(330);
			Shipment s2 = dao.getShipment(338);
			
			System.out.println(LatLngTool.distance(s1.getCoords(), s2.getCoords(), LengthUnit.KILOMETER));
			
			System.out.println(s1.toString()) ;
			System.out.println(s2.toString()) ;

		} catch (Exception e) {
			throw new RuntimeException("Connection Test FAILED", e);
		}
	}

}