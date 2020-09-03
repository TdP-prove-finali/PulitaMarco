package it.polito.tdp.dronedelivery.db;

import java.sql.Connection;

import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;


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
			
			System.out.println(s1) ;
			System.out.println(s2) ;

			
			
			
		} catch (Exception e) {
			throw new RuntimeException("Connection Test FAILED", e);
		}
	}

}
