package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.GeneralChecksDAO;
import it.polimi.tiw.dao.VerbalizationDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/VerbalizeMarks")
public class VerbalizeMarks extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    
    public VerbalizeMarks() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		User user = (User) session.getAttribute("user");
		
		int roundId;
		try {
			roundId = Integer.parseInt(request.getParameter("roundId"));
		}catch(NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     //status code 400
			response.getWriter().println("Incorrect param values");
			return;
		}
		//creating the dao to do the checks before actually doing the real operations
		GeneralChecksDAO generalChecksDAO = new GeneralChecksDAO(connection);
		boolean isRoundOfThisProfessor;
		boolean isRoundVerbalizable;
		try {
			connection.setAutoCommit(true);
			isRoundOfThisProfessor = generalChecksDAO.isRoundOfThisProfessor(user.getId(), roundId);
			isRoundVerbalizable = generalChecksDAO.isRoundVerbalizable(roundId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		//checks for validity of the parameter passed in the URL
		if (!isRoundOfThisProfessor) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);        //status code 401
			response.getWriter().println("You can't verbalize this round");
			return;
		}
		if (!isRoundVerbalizable) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     //status code 400
			response.getWriter().println("There are no published nor rejected marks to verbalize");
			return;
		}
		
		//This is the dao that creates the verbal
		VerbalizationDAO verbalizationDAO = new VerbalizationDAO(connection);
		int newVerbalId;
		try {
			//we need to create a transaction because the db gets modified with multiple queries
			connection.setAutoCommit(false);
			
			verbalizationDAO.createNewVerbal(roundId);
			
			newVerbalId = verbalizationDAO.getVerbalIdOfTheNewVerbal(roundId);
			
			verbalizationDAO.setRejectedMarksToFailedMark(roundId);
			
			verbalizationDAO.updateNewVerbalIdRegisteredAndSetVerbalized(roundId, newVerbalId);
			
			connection.commit();
			
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				System.out.println("Fatal error during rollback");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
				response.getWriter().println("Error in database retrieving data and during rollback, admin will rollback manually soon");
				return;
			}
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);       //status code 200
		
	}

	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
		}
	}
}
