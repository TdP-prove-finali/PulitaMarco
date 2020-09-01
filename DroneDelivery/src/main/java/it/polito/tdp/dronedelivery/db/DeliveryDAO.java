package it.polito.tdp.dronedelivery.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.dronedelivery.model.Shipment;

public class DeliveryDAO {


	public List<Shipment> getAllShipments() {

		final String sql = "SELECT REQID, Address, City, State, Zip, Latitude, Longitude " + 
		                   "FROM delivery where REQID = 23 ORDER BY City ASC";
		List<Shipment> shipments = new ArrayList<Shipment>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Shipment s = new Shipment(rs.getInt("REQID"), rs.getString("Address"),
						rs.getString("City"), rs.getString("State"), rs.getInt("Zip"),
						new LatLng(rs.getDouble("Latitude"), rs.getDouble("Longitude")));
				shipments.add(s);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("DB Connection Error!");
		}

		return shipments;
	}

	public Shipment getShipment(int reqID) {

		final String sql = "SELECT REQID, Address, City, State, Zip, Latitude, Longitude " + 
		                   "FROM delivery where REQID = ? ORDER BY City ASC";
		Shipment s;
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, reqID);
			ResultSet rs = st.executeQuery();

			rs.next();
			s = new Shipment(rs.getInt("REQID"), rs.getString("Address"),
					 rs.getString("City"), rs.getString("State"), rs.getInt("Zip"),
					 new LatLng(rs.getDouble("Latitude"), rs.getDouble("Longitude")));

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("DB Connection Error!");
		}

		return s;
	}
	

	
	/*public List<Fermata> fermateSuccessive(Fermata fp, Map<Integer, Fermata> fermateIdMap) {
		String sql = "SELECT DISTINCT id_stazA FROM connessione WHERE id_stazP = ?";
		List<Fermata> result = new ArrayList<>();
		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, fp.getIdFermata());
			ResultSet res = st.executeQuery();
			
			######################################################
			
			  USE MAP TO REMOVE DUPLICATES LIKE IN METRO EXAMPLE
			
			######################################################
			
			while(res.next()) {
				int id_fa = res.getInt("id_stazA"); //ID FERMATA
				result.add(fermateIdMap.get(id_fa));
			}
			
			
			conn.close();
			
			
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		
	} return result;
		
	}*/

}
