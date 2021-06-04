package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.beans.Verbal;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class VerbalizationDAO {
	private Connection con;

	public VerbalizationDAO(Connection connection) {
		this.con = connection;
	}

	/*
	 * 1) create a new verbal in the db table verbal
	 * 2) get the verbalId assigned automatically by the db upon creation
	 * 3) set the rejected marks to failed
	 * 4) set all rejected or published marks to verbalized and assign the verbalId to them
	 */



	//4
	public void updateNewVerbalIdRegisteredAndSetVerbalized(Integer idround, Integer newVerbalId) throws SQLException {

		String query = "UPDATE registered SET idverbal = ?, state = 4 where idround = ? and (state = 2 or state = 3)";
	
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			
			pstatement.setInt(1, newVerbalId);

			pstatement.setInt(2, idround);
		
			pstatement.executeUpdate();
		}	
	}
	
	
	//3
	public void setRejectedMarksToFailedMark(int idRound) throws SQLException {
		
		String query = "UPDATE registered SET mark = 2 WHERE idround = ? AND state = 3";
		
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, idRound);
		
			pstatement.executeUpdate();
		}
	}
	
	
	
	
	//2
	public int getVerbalIdOfTheNewVerbal(Integer idround) throws SQLException {
		
		String query2 = "SELECT * FROM verbal WHERE idround = ? ORDER BY idverbal DESC"; 

		try (PreparedStatement pstatement = con.prepareStatement(query2);) {
			pstatement.setInt(1, idround);
				
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {
					return result.getInt("idverbal");
				}
				else {
					//this is used id a user manually calls the GoToVerbalPage servlet without having ever created a verbal
					return -1;
				}
			}
			
		}
		
	}


	//1
	public void createNewVerbal(Integer idround) throws SQLException {
		Timestamp date = new Timestamp(new Date().getTime());
		
		String query = "INSERT INTO verbal (dateverbal, idround) VALUE (?, ?)";
		
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setTimestamp(1, date);
			pstatement.setInt(2, idround);
			pstatement.executeUpdate();
		}
		
	}
	
	
}

