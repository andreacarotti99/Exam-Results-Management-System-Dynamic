package it.polimi.tiw.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

public class ConnectionHandler {

	
	//this method is called in the ConnectionHandler 
	public static Connection getConnection(ServletContext context) throws UnavailableException {
		Connection connection = null;
		try {
			//saving in driver, url, user and password the variable in the web.xml file
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			//using the driverManager to establish the connection with the sql db
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
		return connection;
	}

	public static void closeConnection(Connection connection) throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}
	
}
