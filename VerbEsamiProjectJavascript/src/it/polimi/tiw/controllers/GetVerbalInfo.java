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
import it.polimi.tiw.beans.Verbal;
import it.polimi.tiw.dao.ExtraInfoDAO;
import it.polimi.tiw.dao.GeneralChecksDAO;
import it.polimi.tiw.dao.RegisteredStudentsDAO;
import it.polimi.tiw.dao.VerbalizationDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/GetVerbalInfo")
public class GetVerbalInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    
    public GetVerbalInfo() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		User user = (User) session.getAttribute("user");
		
		int roundId;
		try {
			roundId = Integer.parseInt(request.getParameter("roundId"));
		} catch(NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);     //status code 400
			response.getWriter().println("Incorrect param values");
			return;
		}
		//creating the dao to do the checks before actually doing the real operations
		GeneralChecksDAO generalChecksDAO = new GeneralChecksDAO(connection);
		VerbalizationDAO verbalizationDAO = new VerbalizationDAO(connection);
		boolean isRoundOfThisProfessor;
		int newVerbalId;
		try {
			isRoundOfThisProfessor = generalChecksDAO.isRoundOfThisProfessor(user.getId(), roundId);
			newVerbalId = verbalizationDAO.getVerbalIdOfTheNewVerbal(roundId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		//checks for validity of the parameter passed in the URL
		if (!isRoundOfThisProfessor || newVerbalId == -1) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);        //status code 401
			response.getWriter().println("You can't see this verbal");
			return;
		}
		
		//real dao that gets all the information needed
		RegisteredStudentsDAO registeredStudentsDAO = new RegisteredStudentsDAO(connection);	
		List<RegisteredStudent> registeredStudents = new ArrayList<RegisteredStudent>();
		ExtraInfoDAO extraInfoDAO = new ExtraInfoDAO(connection);
		Verbal verbal;
		Round round;
		try {
			//extracting the list of students registered to the given roundId and verbalId
			registeredStudents = registeredStudentsDAO.findVerbalizedStudentsToRound(roundId, newVerbalId);
			verbal = extraInfoDAO.getVerbalInfo(newVerbalId);
			round = extraInfoDAO.getRoundInfo(roundId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);     //status code 500
			response.getWriter().println("Error in database retrieving data");
			return;
		}
		
		Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create(); //custom Gson
		Gson gson2 = new GsonBuilder().setDateFormat("dd/MM/yyyy HH:mm:ss").create();       //gson to format date and time
		
		String registeredStudentsJson = gson.toJson(registeredStudents);
		String verbalJson = gson2.toJson(verbal);
		String roundJson = gson.toJson(round);
		
		String toClient = registeredStudentsJson + "%" + verbalJson + "%" + roundJson;  //this is to pass three separate object serialized with json
		//we will use line.split("%") in javascript to separate the three json objects
		
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
