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

import it.polimi.tiw.beans.Classe;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.ClassesDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/GetClassesStudent")
public class GetClassesStudent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public GetClassesStudent() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		//no need to check the session variable 'user' because we are using filters
		User user = (User) session.getAttribute("user");
		
		
		ClassesDAO classesDAO = new ClassesDAO(connection);
		List<Classe> classes = new ArrayList<Classe>();
		try {
			classes = classesDAO.findClassesByStudentId(user.getId());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);   //status code 500
			response.getWriter().println("Not possible to recover missions");
			return;
		}
		
		
		Gson gson = new Gson();
		String jsonClasses = gson.toJson(classes);
		response.setStatus(HttpServletResponse.SC_OK);       //status code 200
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonClasses);
		
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
