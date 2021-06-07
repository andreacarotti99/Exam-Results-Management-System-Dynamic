package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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


@WebServlet("/EditMark")
@MultipartConfig
public class EditMark extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    
    public EditMark() {
        super();
    }

    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }
	

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		User user = (User) session.getAttribute("user");
		
		int newMark;
		int roundId;
		int studentId;
		try {
			roundId = Integer.parseInt(request.getParameter("roundId"));
			studentId = Integer.parseInt(request.getParameter("studentId"));
			newMark = Integer.parseInt(request.getParameter("newMark"));
			
		}catch(NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     //status code 400
			response.getWriter().println("Incorrect param values");
			return;
		}
		
		if (newMark >= 32 || newMark <= 0 || (newMark >= 4 && newMark <= 17)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     //status code 400
			response.getWriter().println("Incorrect param values");
			return;
		}
		//creating the dao to do the checks before actually doing the real operations
		GeneralChecksDAO generalChecksDAO = new GeneralChecksDAO(connection);
		boolean isRoundOfThisProfessor;
		boolean isStudentRegisteredToThisRound;
		boolean isMarkEditable;
		try {
			isRoundOfThisProfessor = generalChecksDAO.isRoundOfThisProfessor(user.getId(), roundId);
			isStudentRegisteredToThisRound = generalChecksDAO.isStudentRegisteredToThisRound(studentId, roundId);
			isMarkEditable = generalChecksDAO.isMarkEditable(roundId, studentId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		//checks for validity of the parameter passed in the URL
		if (!isRoundOfThisProfessor || !isStudentRegisteredToThisRound || !isMarkEditable) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);        //status code 401
			response.getWriter().println("You can't register to this round");
			return;
		}
		
		
		//this is the DAO that actually changes the db
		EditMarkDAO editMarkDAO = new EditMarkDAO(connection);
		try {

			editMarkDAO.createNewMarkAndSetToInserted(newMark, studentId, roundId);
			
		} catch (SQLException e) {
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
