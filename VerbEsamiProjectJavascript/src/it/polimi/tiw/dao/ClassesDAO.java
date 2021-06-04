package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.Classe;

public class ClassesDAO {
	private Connection connection;

	public ClassesDAO(Connection connection) {
		this.connection = connection;
	}

	
	public List<Classe> findClassesByProfessorId(int professorId) throws SQLException {
		List<Classe> classes = new ArrayList<Classe>();
		
		String query = "SELECT * from class where idprofessor = ? ORDER BY classname DESC";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, professorId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					
					Classe classe = new Classe();
					classe.setClassID(result.getInt("idclass"));
					classe.setClassName(result.getString("classname"));
					classes.add(classe);
					
				}
			}
		return classes;	
		}
	}
	
	
	public List<Classe> findClassesByStudentId(int studentId) throws SQLException {
		List<Classe> classes = new ArrayList<Classe>();
		
		String query = "SELECT * FROM attend left join class on (attend.idclass = class.idclass) where idstudent = ? ORDER BY classname DESC ";
		
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, studentId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					
					Classe classe = new Classe();
					classe.setClassID(result.getInt("idclass"));
					classe.setClassName(result.getString("classname"));
					classes.add(classe);
					
				}
			}
		return classes;	
		}
	}
	
}