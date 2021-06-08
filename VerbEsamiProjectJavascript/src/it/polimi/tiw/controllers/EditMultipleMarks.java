package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.EditMarkDAO;
import it.polimi.tiw.dao.GeneralChecksDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/EditMultipleMarks")
@MultipartConfig
public class EditMultipleMarks extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    
    public EditMultipleMarks() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		User user = (User) session.getAttribute("user");
		
		int[] marks;
		int[] studentIds;
		int arraySize;
		int roundId;
		try {
			roundId = Integer.parseInt(request.getParameter("roundId"));
			
			String[] stringMarks = request.getParameterValues("marks[]");
			
			String[] stringStudentIds = request.getParameterValues("studentIds[]");
			
			arraySize = stringMarks.length;
			
			if (arraySize != stringStudentIds.length) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     //status code 400
				response.getWriter().println("Incorrect param values");
				return;
			}
			
			marks = new int[arraySize];
			for (int i = 0; i < arraySize; i++) {
				marks[i] = Integer.parseInt(stringMarks[i]);
			}
			
			studentIds = new int[arraySize];
			for (int i = 0; i < arraySize; i++) {
				studentIds[i] = Integer.parseInt(stringStudentIds[i]);
			}
			
			
		}catch(NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     //status code 400
			response.getWriter().println("Incorrect param values");
			return;
		}
		//this array is to separate the inserted values from the non inserted ones by the user
		boolean[] inserted = new boolean[arraySize];
		for (int i = 0; i < arraySize; i++) {
			
			if (marks[i] == 0) {
				inserted[i] = false;
			}
			else {
				inserted[i] = true;
			}
		}
		
		//now we start the real checks
		for (int i = 0; i < arraySize; i++) {
			if (marks[i] >= 32 || marks[i] <= -1 || (marks[i] >= 4 && marks[i] <= 17)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     //status code 400
				response.getWriter().println("Incorrect param values");
				return;
			}
		}
		
		//creating the dao to do the checks before actually doing the real operations
		GeneralChecksDAO generalChecksDAO = new GeneralChecksDAO(connection);
		boolean isRoundOfThisProfessor;
		try {
			connection.setAutoCommit(true);                                       /////////////////////////resetting autoCommit to true
			
			isRoundOfThisProfessor = generalChecksDAO.isRoundOfThisProfessor(user.getId(), roundId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		//checks for validity of the parameter passed in the URL
		if (!isRoundOfThisProfessor) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);        //status code 401
			response.getWriter().println("You can't modify this round marks");
			return;
		}
		
		boolean isStudentRegisteredToThisRound;
		boolean isMarkNotInsertedYet;
		try {
			for (int i = 0; i < arraySize; i++) {
				isStudentRegisteredToThisRound = generalChecksDAO.isStudentRegisteredToThisRound(studentIds[i], roundId);
				isMarkNotInsertedYet = generalChecksDAO.isMarkNotInserted(roundId, studentIds[i]);
				
				if (!isStudentRegisteredToThisRound || !isMarkNotInsertedYet) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);        //status code 401
					response.getWriter().println("You can't modify one of the marks that you selected");
					return;
				}
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		
		
		//this is the DAO that actually changes the db
		EditMarkDAO editMarkDAO = new EditMarkDAO(connection);
		try {

			connection.setAutoCommit(false);             /////////////////////////setting autoCommit to false
			
			for (int i = 0; i < arraySize; i++) {
				if (inserted[i]) {
					editMarkDAO.createNewMarkAndSetToInserted(marks[i], studentIds[i], roundId);
				}
			}
			
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
		response.getWriter().print(roundId);
		
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
