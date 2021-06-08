package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.beans.RegisteredStudent;

public class RegisteredStudentsDAO {
	private Connection connection;

	public RegisteredStudentsDAO(Connection connection) {
		this.connection = connection;
	}

	
	public List<RegisteredStudent> getRegisteredStudentsOrdered(int roundId, String orderId, String orderDirection) throws SQLException {
		List<RegisteredStudent> registeredStudents = new ArrayList<RegisteredStudent>();
		
		//it shouldn't be a problem to insert orderId and orderDirection like this into the query because the two arguments are hardCoded into the method
		//to see how this method is caller go to the servlet: GoToRegisteredToRoundPage at lines: 130-200
		//we had to do this because prepared statement cannot be used for order by clause
		String query = "select r.idstudent, r.mark, r.idround, r.state, u.name, u.surname, u.email, s.studentnumber, s.degreecourse from (registered r left join user u on r.idstudent = u.id) join studentinfo s on s.id = u.id where idround = ? order by "+ orderId + " " + orderDirection;
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, roundId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					
					RegisteredStudent registeredStudent = new RegisteredStudent();
					registeredStudent.setStudentNumber(result.getInt("s.studentnumber"));
					registeredStudent.setSurname(result.getString("u.surname"));
					registeredStudent.setName(result.getString("u.name"));
					registeredStudent.setMail(result.getString("u.email"));
					registeredStudent.setDegreeCourse(result.getString("s.degreecourse"));
					registeredStudent.setMark(result.getInt("r.mark"));
					registeredStudent.setStatus(result.getInt("r.state"));
					registeredStudent.setId(result.getInt("r.idstudent"));
					registeredStudents.add(registeredStudent);

				}
			}
		return registeredStudents;	
		}
	}
	
	public List<RegisteredStudent> getStudentsWithNotInsertedMark(int roundId) throws SQLException {
		List<RegisteredStudent> studentsWithNotInsertedMark = new ArrayList<RegisteredStudent>();
		
		//it shouldn't be a problem to insert orderId and orderDirection like this into the query because the two arguments are hardCoded into the method
		//to see how this method is caller go to the servlet: GoToRegisteredToRoundPage at lines: 130-200
		//we had to do this because prepared statement cannot be used for order by clause
		String query = "select r.idstudent, r.mark, r.idround, r.state, u.name, u.surname, u.email, s.studentnumber, s.degreecourse from (registered r left join user u on r.idstudent = u.id) join studentinfo s on s.id = u.id where idround = ? and r.state = 0";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, roundId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					
					RegisteredStudent registeredStudent = new RegisteredStudent();
					registeredStudent.setStudentNumber(result.getInt("s.studentnumber"));
					registeredStudent.setSurname(result.getString("u.surname"));
					registeredStudent.setName(result.getString("u.name"));
					registeredStudent.setMail(result.getString("u.email"));
					registeredStudent.setDegreeCourse(result.getString("s.degreecourse"));
					registeredStudent.setMark(result.getInt("r.mark"));
					registeredStudent.setStatus(result.getInt("r.state"));
					registeredStudent.setId(result.getInt("r.idstudent"));
					studentsWithNotInsertedMark.add(registeredStudent);

				}
			}
		return studentsWithNotInsertedMark;	
		}
	}
	
	
	public RegisteredStudent findInfoStudentByRoundIDAndStudentID(int roundId, int studentId) throws SQLException {
		RegisteredStudent registeredStudent = null;
		
		String query = "select r.idstudent, r.mark, r.idround, r.state, u.name, u.surname, u.email, s.studentnumber, s.degreecourse from (registered r left join user u on r.idstudent = u.id) join studentinfo s on s.id = u.id where r.idround = ? and r.idstudent = ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, roundId);
			pstatement.setInt(2, studentId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					registeredStudent = new RegisteredStudent();
					registeredStudent.setId(result.getInt("r.idstudent"));
					registeredStudent.setStudentNumber(result.getInt("s.studentnumber"));
					registeredStudent.setSurname(result.getString("u.surname"));
					registeredStudent.setName(result.getString("u.name"));
					registeredStudent.setMail(result.getString("u.email"));
					registeredStudent.setDegreeCourse(result.getString("s.degreecourse"));
					registeredStudent.setMark(result.getInt("r.mark"));
					registeredStudent.setStatus(result.getInt("r.state"));
					registeredStudent.setRoundID(result.getInt("r.idround"));
				}
			}
		return registeredStudent;	
		}
	}
	
	
	
	
	public List<RegisteredStudent> findVerbalizedStudentsToRound(int roundId, int idVerbal) throws SQLException {
		List<RegisteredStudent> registeredStudents = new ArrayList<RegisteredStudent>();
		
		String query = "select r.idstudent, r.mark, r.idround, r.state, u.name, u.surname, u.email, s.studentnumber, s.degreecourse from (registered r left join user u on r.idstudent = u.id) join studentinfo s on s.id = u.id where idround = ? and state = 4 and idverbal = ?";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, roundId);
			pstatement.setInt(2, idVerbal);
			
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					
					RegisteredStudent registeredStudent = new RegisteredStudent();
					registeredStudent.setStudentNumber(result.getInt("s.studentnumber"));
					registeredStudent.setSurname(result.getString("u.surname"));
					registeredStudent.setName(result.getString("u.name"));
					registeredStudent.setMail(result.getString("u.email"));
					registeredStudent.setDegreeCourse(result.getString("s.degreecourse"));
					registeredStudent.setMark(result.getInt("r.mark"));
					registeredStudent.setStatus(result.getInt("r.state"));
					registeredStudent.setId(result.getInt("r.idstudent"));
					registeredStudents.add(registeredStudent);

				}
			}
		return registeredStudents;	
		}
	}
	
	
	
	public void registerStudentToRound(int roundId, int studentId) throws SQLException {
		
		String query = "INSERT INTO registered (idstudent, idround, mark, state) VALUE (?, ?, 0, 0)";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, studentId);
			pstatement.setInt(2, roundId);
			pstatement.executeUpdate();
		}
		
	}
}