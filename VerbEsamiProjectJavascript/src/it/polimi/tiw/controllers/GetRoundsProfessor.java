package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.Round;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.GeneralChecksDAO;
import it.polimi.tiw.dao.RoundsDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/GetRoundsProfessor")
public class GetRoundsProfessor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    
    public GetRoundsProfessor() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		//no need to check the session variable 'user' because we are using filters
		User user = (User) session.getAttribute("user");
		
		int classId;
		try {
			classId = Integer.parseInt(request.getParameter("classId"));
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     //status code 400
			response.getWriter().println("Incorrect param values");
			return;
		}
		
		//creating the dao to do the checks before actually doing the real operations
		GeneralChecksDAO generalChecksDAO = new GeneralChecksDAO(connection);
		boolean isTaughtByProfessor;
		try {
			isTaughtByProfessor = generalChecksDAO.isClassTaughtByProfessor(user.getId(), classId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		
		//checks for validity of the parameter passed in the URL
		if (!isTaughtByProfessor) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);        //status code 401
			response.getWriter().println("User not attending this class");
			return;
		}
		
		//real DAO to do the actual intended operation
		RoundsDAO roundsDAO = new RoundsDAO(connection);
		List<Round> rounds = new ArrayList<Round>();
		try {

			//extracting the list of rounds of the professor
			rounds = roundsDAO.findAllRoundsOfThisClass(classId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create(); //custom Gson
		
		String jsonRounds = gson.toJson(rounds);
		response.setStatus(HttpServletResponse.SC_OK);       //status code 200
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonRounds);
		
		
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
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
