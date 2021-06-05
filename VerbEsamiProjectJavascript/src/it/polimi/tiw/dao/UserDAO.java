package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//importing the bean user class 
import it.polimi.tiw.beans.User;


//salvo l'oggetto user con una certa chiave nella sessione poi lo andrò a riprendere quando mi servirà
public class UserDAO {
	//declaring the connection class with the db
	private Connection con;

	//constructor of the userDAO: we need a connection
	public UserDAO(Connection connection) {
		this.con = connection;
	}

	//method used to check in the db if the credentials provided as parameter
	//are valid thsis method is called in the doPost method of the CheckLogin Servlet
	public User checkCredentials(String usrn, String pwd) throws SQLException {
		String query = "SELECT id, username, name, surname, isProfessor, email FROM user WHERE username = ? AND password =?";
		
		//preparing the statement, otherwise it wouldn't be safe checking directly the credentials
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			//subsitution of the first and second "?"
			pstatement.setString(1, usrn);
			pstatement.setString(2, pwd);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					user.setProfessor(result.getBoolean("isProfessor"));
					user.setMail(result.getString("email"));
					return user;
				}
			}
		}
	}
}
