package com.fauconnet.search;



import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.core.Response;



import org.glassfish.jersey.client.*;




/**
 * Servlet implementation class Neo4jServlet
 */
public class ElasticServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static String SERVER_ROOT_URI ="http://localhost:7474/";
	public static String AUTH_TOKEN ="Basic bmVvNGo6dmkwbG9u";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ElasticServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		executeQuery(request, response);
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
		if(mode !=null && mode.equals("POST")){
			//result=callNeoPostAPI("/db/data/"+path);
			result=callNeoPostAPI(urlSuffix, payload);
		}
		else{
		//result=callNeoPostAPI(query,"/db/data/transaction/commit");
			result=callNeoGetAPI(urlSuffix,queryString);
		}
		
		
		String contentType = "application/json";
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Type", contentType);
		response.getWriter().write(result);
		
	}
	
	
	
	public void init(){
		ClientConfig wsClientConfiguration = new ClientConfig();

		
		// Initialize client
		Client wsClient = ClientBuilder.newClient(wsClientConfiguration);
		WebTarget webTarget = wsClient.target("http://frhdstd-aefl016:9200/").path("employees");
		 
		Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.get();
		Object obj=response.getEntity();
		 
	
		     
		System.out.println(response.getStatus());
		
		System.out.println(obj);


		System.out.println(response);
		System.out.println( String.format( "GET on [%s], status code [%d]",
		        SERVER_ROOT_URI, response.getStatus() ) );
		response.close();

	}
	
	public String callNeoPostAPI (String urlSuffix,String payload){
		/*final String txUri = SERVER_ROOT_URI + urlSuffix;
		WebResource resource = Client.create().resource( txUri );
		ClientResponse response = resource
		        .accept( MediaType.APPLICATION_JSON )
		        .type( MediaType.APPLICATION_JSON )
		        .header("Authorization",AUTH_TOKEN)
		        .entity( payload )
		        .post( ClientResponse.class );

	String str= response.getEntity( String.class);
	System.out.println(str);
		response.close();
		return str;*/
		return null;
	}
	
	public String callNeoGetAPI (String urlSuffix,String queryString){
	/*	String txUri = SERVER_ROOT_URI + urlSuffix;
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
		return str;*/
		return null;
	}
	
	

}
