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

import it.polimi.tiw.beans.RegisteredStudent;
import it.polimi.tiw.beans.Round;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.ExtraInfoDAO;
import it.polimi.tiw.dao.GeneralChecksDAO;
import it.polimi.tiw.dao.RegisteredStudentsDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/GetStudentsRegisteredToRound")
public class GetStudentsRegisteredToRound extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    
    public GetStudentsRegisteredToRound() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//removing a possible object from the session that is placed there by the GoToRegisteredToRoundsPage servlet
		HttpSession session = request.getSession();

		//no need to check the session variable 'user' because we are using filters
		User user = (User) session.getAttribute("user");	
	
		int roundId;
		try {
			roundId = Integer.parseInt(request.getParameter("roundId"));
		
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     //status code 400
			response.getWriter().println("Incorrect param values");
			return;
		}
		//creating the dao to do the checks before actually doing the real operations
		GeneralChecksDAO generalChecksDAO = new GeneralChecksDAO(connection);
		boolean isRoundOfThisProfessor;
		try {
			isRoundOfThisProfessor = generalChecksDAO.isRoundOfThisProfessor(user.getId(), roundId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		//checks for validity of the parameter passed in the URL
		if (!isRoundOfThisProfessor) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);        //status code 401
			response.getWriter().println("You aren't registered to this round");
			return;
		}
		
		
		//actual DAO to get the informations from the db
		RegisteredStudentsDAO registeredStudentsDAO = new RegisteredStudentsDAO(connection);
		List<RegisteredStudent> registeredStudents = new ArrayList<RegisteredStudent>();
		ExtraInfoDAO extraInfoDAO = new ExtraInfoDAO(connection);
		Round roundInfo;
		try {
			roundInfo = extraInfoDAO.getRoundInfo(roundId);
			
			//get the registered student with the initial ascendant order by student number
			registeredStudents = registeredStudentsDAO.getRegisteredStudentsOrdered(roundId, "s.studentnumber", "asc");
			
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create(); //custom Gson
		
		String registeredStudentsJson = gson.toJson(registeredStudents);
		String roundInfoJson = gson.toJson(roundInfo);
		
		String toClient = registeredStudentsJson + "%" + roundInfoJson;  //this is to pass two separate object serialized with json
		//we will use line.split("%") in javascript to separate the two json objects
		
		response.setStatus(HttpServletResponse.SC_OK);       //status code 200
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(toClient);
		
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
