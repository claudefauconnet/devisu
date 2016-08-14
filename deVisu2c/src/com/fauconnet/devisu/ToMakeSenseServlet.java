package com.fauconnet.devisu;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ToMakeSenseServlet
 */
public class ToMakeSenseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	String Path="D:\\sinequa\\2MakeSense\\export\\classifiers";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ToMakeSenseServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		executeRequest( request,  response) ;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		executeRequest( request,  response) ;
	}
	
	protected void executeRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
