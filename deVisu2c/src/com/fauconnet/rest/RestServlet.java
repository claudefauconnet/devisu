
/**************************  TOUTLESENS LICENCE*************************

The MIT License (MIT)

Copyright (c) 2016 Claude Fauconnet claude.fauconnet@neuf.fr

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

**********************************************************************/


package com.fauconnet.rest;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Servlet implementation class RestServlet
 */
public class RestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RestServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		executeQuery(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		executeQuery(request, response);

	}

	protected void executeQuery(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		String mode = request.getParameter("mode");
		String payload = request.getParameter("payload");
		String url = request.getParameter("url");
		String queryString = request.getParameter("queryString");
		String result = "";
		//System.out.println("1++++" + queryString);

		if (mode != null && mode.equals("POST")) {
			// result=callNeoPostAPI("/db/data/"+path);
			result = callPostAPI(url, payload);
		} else {
			// result=callNeoPostAPI(query,"/db/data/transaction/commit");
			result = callGetAPI(url, queryString);
		}

		String contentType = "application/json";
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Type", contentType);
		response.getWriter().write(result);

	}

	public String callPostAPI(String url, String payload) {
	//	System.out.println("4++++" + url);
	//	System.out.println("5++++" + payload);
		// System.out.println("3++++"+payload);
		final String txUri = url;
		WebResource resource = Client.create().resource(txUri);
		ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
				// .header("Authorization",AUTH_TOKEN)
				.entity(payload).post(ClientResponse.class);

		String str = response.getEntity(String.class);
	//	System.out.println(str);
		response.close();
		return str;
	}

	public String callGetAPI(String url, String queryString) {

		String txUri = url;
		if (queryString != null) {

		}

		// System.out.println(txUri);
		WebResource resource = Client.create().resource(txUri);
		ClientResponse response = resource
				// .accept( MediaType.APPLICATION_JSON )
				// .type( MediaType.APPLICATION_JSON )
				// .header("Authorization",AUTH_TOKEN)
				.get(ClientResponse.class);

		String str = response.getEntity(String.class);
		// System.out.println(str);
		response.close();
		return str;
	}

}
