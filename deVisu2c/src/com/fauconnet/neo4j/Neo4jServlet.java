
/**************************  TOUTLESENS LICENCE*************************

The MIT License (MIT)

Copyright (c) 2016 Claude Fauconnet claude.fauconnet@neuf.fr

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

**********************************************************************/


package com.fauconnet.neo4j;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Servlet implementation class Neo4jServlet
 */
public class Neo4jServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/*public static String SERVER_ROOT_URI ="http://localhost:7474/";
	public static String AUTH_TOKEN ="Basic bmVvNGo6dmkwbG9u";*/
	
	
	
	
		
	//public static String SERVER_ROOT_URI ="http://vps254642.ovh.net:7474/";
	public static String SERVER_ROOT_URI ="http://127.0.0.1:7474/";
	public static String AUTH_TOKEN ="Basic bmVvNGo6dG91dGx1bml2ZXJz";

	private String MODIFY_PASSSWORD;
       
    /**
     * 
     * 
     * 
     * 
     * @see HttpServlet#HttpServlet()
     */
    public Neo4jServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
    
    
    
    public void init(){
    	
		try {
			URL url=this.getClass().getResource("config.properties");
			Properties properties=new Properties();
			properties.load(this.getClass().getResourceAsStream("config.properties"));
			SERVER_ROOT_URI =properties.getProperty("SERVER_ROOT_URI");
			if(!SERVER_ROOT_URI.endsWith("/"))
				SERVER_ROOT_URI+="/";
			AUTH_TOKEN =properties.getProperty("AUTH_TOKEN");
			MODIFY_PASSSWORD=properties.getProperty("MODIFY_PASSSWORD");
			System.out.println(SERVER_ROOT_URI);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		WebResource resource = Client.create()
		        .resource( SERVER_ROOT_URI );
		ClientResponse response = resource.get( ClientResponse.class );
	//	System.out.println(response);
	//	System.out.println( String.format( "GET on [%s], status code [%d]",
		       // SERVER_ROOT_URI, response.getStatus() ) );
		response.close();

	}
    
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		executeQuery(request, response);
		Writer writer=response.getWriter();
		writer.write("aaaaa");
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		executeQuery(request, response);
	}
	
	
	protected void executeQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	
		
		String mode=request.getParameter("mode");
		String payload=request.getParameter("payload");
		String urlSuffix=request.getParameter("urlSuffix");
		String queryString=request.getParameter("queryString");
		String result="";
	//	System.out.println("1++++"+queryString);
		
		if(mode !=null && mode.equals("listProperties")){
			result=callNeoGetAPI("/db/data/propertykeys","");
		
		
		}
		
		if(mode !=null && mode.equals("auth")){
			String password=request.getParameter("value");
			Writer writer=response.getWriter();
			if(true || password!=null && password.equals(MODIFY_PASSSWORD)){
				DBObject jsonObj=new BasicDBObject("auth","OK");
				writer.write(jsonObj.toString());
			}
			else{
				DBObject jsonObj=new BasicDBObject("auth","KO");
				writer.write(jsonObj.toString());
				
			}
			return;
				
			
		}
		
		if(mode !=null && mode.equals("POST")){
			//result=callNeoPostAPI("/db/data/"+path);
			result=callNeoPostAPI(urlSuffix, payload);
		}
		else{
		//result=callNeoPostAPI(query,"/db/data/transaction/commit");
			result=callNeoGetAPI(urlSuffix,queryString);
		}
		
	//	System.out.println(result);
		String contentType = "application/json";
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Type", contentType);
		response.getWriter().write(result);
		
	}
	
	
	
	
	
	
	public String callNeoPostAPI (String urlSuffix,String payload){
//System.out.println("3++++"+payload);
		final String txUri = SERVER_ROOT_URI + urlSuffix;
		WebResource resource = Client.create().resource( txUri );
		ClientResponse response = resource
		        .accept( MediaType.APPLICATION_JSON )
		        .type( MediaType.APPLICATION_JSON )
		        .header("Authorization",AUTH_TOKEN)
		        .entity( payload )
		        .post( ClientResponse.class );

	String str= response.getEntity( String.class);
//System.out.println(str);
		response.close();
		return str;
	}
	
	public String callNeoGetAPI (String urlSuffix,String queryString){

		String txUri = SERVER_ROOT_URI + urlSuffix;
		if(queryString!=null)
			txUri+="/"+queryString;
		WebResource resource = Client.create().resource( txUri );
		ClientResponse response = resource
		        .accept( MediaType.APPLICATION_JSON )
		        .type( MediaType.APPLICATION_JSON )
		        .header("Authorization",AUTH_TOKEN)
		        .get(ClientResponse.class );
		       

	String str= response.getEntity( String.class);
		response.close();
		return str;
	}
	
	

}
