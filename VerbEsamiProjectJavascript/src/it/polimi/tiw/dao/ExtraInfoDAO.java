package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.beans.Round;
import it.polimi.tiw.beans.Verbal;

public class ExtraInfoDAO {
	private Connection connection;

	public ExtraInfoDAO(Connection connection) {
		this.connection = connection;
	}
	
	
	
	public String getClassName(int classId)throws SQLException {
		String className = "error";
		
		String query = "SELECT * FROM class WHERE idclass = ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, classId);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {  //this should only loop once if the db is created correctly
					
					className = result.getString("classname");
				}
			}
		}
		
		return className;
	}
	
	
	public Round getRoundInfo(int roundId) throws SQLException {
		Round round = new Round();
		
		String query = "SELECT * FROM class c join round r on c.idclass = r.idclass WHERE r.idround = ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, roundId);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while(result.next()) {
					round.setClassName(result.getString("c.classname"));
					round.setDate(result.getDate("r.date"));
					round.setRoundId(result.getInt("r.idround"));
				}
			}
		}
		
		return round;
	}
	
	
	public Verbal getVerbalInfo(int verbalId) throws SQLException {
		Verbal verbal = new Verbal();
		
		String query = "SELECT * FROM verbal WHERE idverbal = ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, verbalId);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while(result.next()) {
					verbal.setDate(result.getTimestamp("dateverbal"));
					verbal.setVerbalID(verbalId);
				}
			}
		}
		
		return verbal;
	}
	

	public int getClassIdOfRound(int roundId) throws SQLException {
		
		String query = "SELECT * FROM round WHERE idround = ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, roundId);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {  //this should only loop once if the db is created correctly
					
					return result.getInt("idclass");
				}
			}
		}
		
		return -1;  //should never get here
	}
}
