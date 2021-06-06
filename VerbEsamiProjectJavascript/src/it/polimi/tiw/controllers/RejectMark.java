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

import com.google.gson.Gson;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.EditMarkDAO;
import it.polimi.tiw.dao.GeneralChecksDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/RejectMark")
public class RejectMark extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public RejectMark() {
        super();
    }

    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		//no need to check the session variable 'user' because we are using filters
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
		boolean isStudentRegisteredToThisRound;
		boolean isMarkRejectable;
		try {
			isStudentRegisteredToThisRound = generalChecksDAO.isStudentRegisteredToThisRound(user.getId(), roundId);
			isMarkRejectable = generalChecksDAO.isMarkRejectable(roundId, user.getId());
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		//checks for validity of the parameter passed in the URL
		if (!isStudentRegisteredToThisRound || !isMarkRejectable) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);        //status code 401
			response.getWriter().println("You can't reject that mark");
			return;
		}
		
		//this is the dao that actually changes the db information
		EditMarkDAO editMarkDAO = new EditMarkDAO(connection);
		try {
			
			editMarkDAO.changeStatusToRejected(user.getId(), roundId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);  //status code 200
		
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
