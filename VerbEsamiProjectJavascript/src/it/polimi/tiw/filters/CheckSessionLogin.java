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

/**
 * Servlet Filter implementation class CheckSessionLogin
 */
@WebFilter("/CheckSessionLogin")
public class CheckSessionLogin implements Filter {

    /**
     * Default constructor. 
     */
    public CheckSessionLogin() {
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
		System.out.print("CheckSessionLogin filter executing ...\n");
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/HomePage";
		
		//check if session is new or if user hasn't logged
		HttpSession s = req.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			s.setAttribute("errorMessage", "you aren't logged in, please log in again");
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
