package it.polimi.tiw.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.User;

/**
 * Servlet Filter implementation class StudentFilter
 */
@WebFilter("/StudentFilter")
public class StudentFilter implements Filter {

    /**
     * Default constructor. 
     */
    public StudentFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		//start of filter request side
		System.out.print("StudentFilter executing ...\n");
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/HomePage";
		
		HttpSession s = req.getSession();
		User u = null;
		
		//we get from the session the user attribute
		u = (User) s.getAttribute("user");
		//check id user in the session is a student
		if (u.getIsProfessor()) {
			s.setAttribute("errorMessage", "you filthy hacker, you are not a student, we logged you off for security reasons");
			res.sendRedirect(loginpath);
			return;
		}
		
		//end of filter request-side
		chain.doFilter(request, response);
		//don't write any filter response side here
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
