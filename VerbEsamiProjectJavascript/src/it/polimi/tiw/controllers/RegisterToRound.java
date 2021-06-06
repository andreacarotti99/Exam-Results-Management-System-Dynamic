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

import it.polimi.tiw.beans.Classe;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.ExtraInfoDAO;
import it.polimi.tiw.dao.GeneralChecksDAO;
import it.polimi.tiw.dao.RegisteredStudentsDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/RegisterToRound")
public class RegisterToRound extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    
    public RegisterToRound() {
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
		boolean isStudentAlreadyRegisteredToThisRound;
		boolean isStudentAttendingThisRoundsClass;
		try {
			isStudentAlreadyRegisteredToThisRound = generalChecksDAO.isStudentRegisteredToThisRound(user.getId(), roundId);
			isStudentAttendingThisRoundsClass = generalChecksDAO.isStudentAttendingThisRoundsClass(roundId, user.getId());
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		//checks for validity of the parameter passed in the URL (if he is already registered or if he isnt attending the round's class)
		if (isStudentAlreadyRegisteredToThisRound || !isStudentAttendingThisRoundsClass) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);        //status code 401
			response.getWriter().println("You can't register to this round");
			return;
		}
		
		//this is the actual dao that changes the db
		RegisteredStudentsDAO registeredStudentsDAO = new RegisteredStudentsDAO(connection);
		ExtraInfoDAO extraInfoDAO = new ExtraInfoDAO(connection);
		Classe classe;
		try {
			registeredStudentsDAO.registerStudentToRound(roundId, user.getId());
			
			classe = extraInfoDAO.getClassBeanOfRound(roundId);//this bean is used by javascript to refresh the rounds table
			
		}catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		
		Gson gson = new Gson();
		String jsonClasse = gson.toJson(classe);
		response.setStatus(HttpServletResponse.SC_OK);  //status code 200
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonClasse); //this bean is used by javascript to refresh the rounds table
		
		
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
