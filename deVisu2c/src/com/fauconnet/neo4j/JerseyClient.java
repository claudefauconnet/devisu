

/**************************  TOUTLESENS LICENCE*************************

The MIT License (MIT)

Copyright (c) 2016 Claude Fauconnet claude.fauconnet@neuf.fr

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

**********************************************************************/


package com.fauconnet.neo4j;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;




	 




	
		
	 
	public class JerseyClient {
		
		public static String SERVER_ROOT_URI ="http://localhost:7474";
		
		public static void main(String[] args){
			JerseyClient client=new JerseyClient();
			//client.init();
			//String query="match (n:Techno) return n";
			String query="MATCH (t:Techno)-[r:IMPLEMENTS]->(dc:DC) return t,r,dc";
			client.test(query);
			
		
		}
		
		public void init(){
			WebResource resource = Client.create()
			        .resource( SERVER_ROOT_URI );
			ClientResponse response = resource.get( ClientResponse.class );
			System.out.println(response);
			System.out.println( String.format( "GET on [%s], status code [%d]",
			        SERVER_ROOT_URI, response.getStatus() ) );
			response.close();
	
		}
		
		public void test (String query){
			final String txUri = SERVER_ROOT_URI + "/db/data/transaction/commit";
			WebResource resource = Client.create().resource( txUri );

			String payload = "{\"statements\" : [ {\"statement\" : \"" +query + "\"} ]}";
			ClientResponse response = resource
			        .accept( MediaType.APPLICATION_JSON )
			        .type( MediaType.APPLICATION_JSON )
			        .header("Authorization","Basic bmVvNGo6dmkwbG9u")
			        .entity( payload )
			        .post( ClientResponse.class );

			System.out.println( String.format(
			        "POST [%s] to [%s], status code [%d], returned data: "
			                + System.getProperty( "line.separator" ) + "%s",
			        payload, txUri, response.getStatus(),
			        response.getEntity( String.class ) ) );
			
			response.close();
		}

	}
