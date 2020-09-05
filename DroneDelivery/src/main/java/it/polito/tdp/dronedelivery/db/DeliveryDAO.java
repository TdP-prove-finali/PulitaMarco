package it.polito.tdp.dronedelivery.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.dronedelivery.model.Distance;
import it.polito.tdp.dronedelivery.model.Shipment;

/**
 * @author Marco
 *
 */
public class DeliveryDAO {


	public Set<Shipment> getAllShipments(String city, Map<Integer, Shipment> idMap) {

		final String sql = "SELECT REQID, Address, City, State, Zip, Latitude, Longitude " + 
		                   "FROM delivery where City = ?   ORDER BY City ASC";  //"  +"and REQID IN (319,320,322,323)"+  "


		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, city);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				
				if(!idMap.containsKey(rs.getInt("REQID"))) {
				
				Shipment s = new Shipment(rs.getInt("REQID"), rs.getString("Address"),
						rs.getString("City"), rs.getString("State"), rs.getInt("Zip"),
						new LatLng(rs.getDouble("Latitude"), rs.getDouble("Longitude")));
				
				idMap.put(s.getReqID(), s);
				}
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("DB Connection Error!");
		}

		return new HashSet<>(idMap.values());
	}
	
	/**
	 * 
	 * @param reqID The request ID that identify the Shipment
	 * @return The single Shipment object identified by the reqID
	 */
	
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
	
	public Set<Distance> getDistances(String city, Map<Integer, Shipment> idMap) {
		
		//Preparing the subset of objects to query. 
		//This is not a user input. It's randomly generated picking a number of shipments defined 
		//sampling rate, size might vary at each time slot.  
		//As the length is variable PreparedStatement won't cache them optimally rebuilding each time.
		//In this case I build the query setting the INT values directly in the string inside the IN statement.
		//Different is the case of "city" field. This is a string and needs to be filtered.
		Set<Integer> vertexes = idMap.keySet();
		String inStr="";
		Iterator<Integer> v = vertexes.iterator();
		while(v.hasNext()) {
			inStr = inStr+= v.next();
			if(v.hasNext()) inStr = inStr+= ",";
		}
		//System.out.println(inStr);
		// inStr = "319,320,322,323";
		
		
		
		String sql = "SELECT d1.REQID P1, d2.REQID P2, d1.city P1city, d1.address P1Address, " + 
		                   "d2.address P2Address, d1.Latitude la1, d2.Latitude la2, d1.Longitude lo1, d2.Longitude lo2 " + 
				           "FROM delivery d1, delivery d2 " +
		                   "WHERE d1.city = ? AND d2.city = ? " +
			               "AND d1.REQID < d2.REQID AND d1.REQID IN ("+inStr+") AND d2.REQID IN  ("+inStr+")"; 
		
		
		Set<Distance> distances = new HashSet<Distance>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, city);
			st.setString(2, city);
			//System.out.println(st);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				LatLng pc1 = new LatLng(rs.getDouble("la1"), rs.getDouble("lo1"));
				LatLng pc2 = new LatLng(rs.getDouble("la2"), rs.getDouble("lo2"));
				Double distP1P2 = LatLngTool.distance(pc1, pc2, LengthUnit.KILOMETER);
				Distance d = new Distance(rs.getInt("P1"), rs.getInt("P2"), distP1P2);			                 
				distances.add(d);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("DB Connection Error!");
		}

		return distances;
	}

}
