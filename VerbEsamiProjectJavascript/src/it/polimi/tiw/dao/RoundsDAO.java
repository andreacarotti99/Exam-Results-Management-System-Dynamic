package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.Round;

public class RoundsDAO {
	private Connection connection;

	public RoundsDAO(Connection connection) {
		this.connection = connection;
	}
	
	
	//this method uses the other method to find all rounds for that class and then changes a variable value only if the student is registered to that round
	public List<Round> findRoundsByStudentAndClass(int studentId, int classId) throws SQLException {
		List<Round> rounds = this.findAllRoundsOfThisClass(classId);
		
		String query = "SELECT * FROM registered r join round o on r.idround = o.idround where r.idstudent = ? and o.idclass = ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, studentId);
			pstatement.setInt(2, classId);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					for (Round r: rounds) {
						if (r.getRoundId() == result.getInt("idround")) {
							r.setStudentRegistered(true);
						}
					}
				}
			}
		return rounds;	
		}
	}
	
	
	//this is a query to extract all rounds of this class
	public List<Round> findAllRoundsOfThisClass(int classId) throws SQLException {
		List<Round> rounds = new ArrayList<Round>();
		
		String query = "SELECT * FROM round WHERE idclass = ? order by date desc";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, classId);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Round round = new Round();
					round.setRoundId(result.getInt("idround"));
					round.setDate(result.getDate("date"));
					round.setStudentRegistered(false);
					rounds.add(round);
				}
			}
		return rounds;	
		}
	}
	
}