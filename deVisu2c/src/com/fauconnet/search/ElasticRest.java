package com.fauconnet.search;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

import com.fauconnet.devisu.MongoProxy;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class ElasticRest {
//	public static String SERVER_ROOT_URI = "http://localhost:9200/";
	 public static String SERVER_ROOT_URI ="http://frhdstd-aefl016:9200";

	public static String AUTH_TOKEN = "Basic bmVvNGo6dmkwbG9u";
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static void main(String args[]) {
		// get("customer");
		// getIndices();

		// createIndex("customer");
		// deleteIndexes();
		// addItemToElastic();
		copyFeedsToElastic();
	}

	public static void copyFeedsToElastic() {
		String host = "localhost";
		String mongoDBname = "rss3d";
		String collection = "feedsDAP";
		String query = "{}";
		String index = "feeds_dap2";
		String type = "feed";

		collection = "feedsActu2015";
		index = "feeds_actu_2015";
		try {
			copyFeedsToElastic(host, mongoDBname, collection, query, index, type);

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public static void addItemToElastic(String index) {
		String entity = "{\"name\": \"John Doe2\"}";
		Client wsClient = getClient();
		put(wsClient, entity, "{}", null);

	}

	public static String search(String query) {
		// http://localhost:9200/feeds/_search?q=*&pretty'
		return "";
	}

	public static void createIndex(String name) {
		put(getClient(), name, "{}", null);
	}

	public static void copyFeedsToElastic(String host, String mongoDBname, String collection, String query, String index, String type) throws Exception {
		MongoProxy proxy = new MongoProxy(host, 27017, mongoDBname);
		DBObject queryJson = new BasicDBObject();
		DBObject fields = new BasicDBObject("title", 1);
		fields.put("description", 1);
		fields.put("pubDate", 1);
		fields.put("pubDate", 1);
		// fields.put("link", 1);
		// fields.put("source", 1);
		// fields.put("id", 1);
		// fields.put("_id", -1);
		// fields.put("id_", 1);

		DBCursor cursor = proxy.getDocumentsCursor(collection, queryJson, null, 100000000);

		Client wsClient = getClient();
		StringBuffer sb = new StringBuffer();
		int i = 1;
		int id;
		while (cursor.hasNext()) {
			DBObject result = cursor.next();
			// for (DBObject result : results) {
			Object idObj = result.get("id");
			if (idObj == null)
				continue;
			try {
				if(idObj.toString().indexOf(".")>-1)
				id = (int) Math.round((double) idObj);
				else
					id = (int) idObj;
			} catch (Exception e) {
				System.out.println("Wrong id :" + idObj);
				continue;
			}

			Date pubdate = (Date) result.get("pubDate");

			long pubDateLong;
			try {
				// date = sdf.format(pubdate);
				pubDateLong = returnMilliSeconds(pubdate);
			} catch (Exception e) {
				// System.out.println("Wrong date :"+pubdate+" for id :"+idObj);
				continue;
			}
			result.put("pubDate", pubDateLong);

			/*Date crawlDate = (Date) result.get("crawlDate");
			long crawlDateLong;
			try {
				// date = sdf.format(pubdate);
				crawlDateLong = returnMilliSeconds(crawlDate);
			} catch (Exception e) {
				// System.out.println("Wrong date :"+pubdate+" for id :"+idObj);
				continue;
			}

			result.put("crawlDate", crawlDateLong);*/

			result.removeField("_id");
			result.removeField("link");

			String json = result.toString();
			sb.append("{\"index\":{\"_index\":\"" + index + "\",\"type\":\"" + type + "\"}}\n");// _id\":\""+id+"\"}}\n");

			sb.append(json + "\n");

			if (i % 100 == 0) {
				int status = putBulk(wsClient, index, type, sb.toString());
				if (status != 200) {
					System.out.println("Error indexed");
					return;
				}
				System.out.println("" + i + "docs  indexed");
				sb.setLength(0);
			}
			i++;
			if (i == 2) {

				System.out.println(sb);
			}

		}
	}

	private static int putBulk(Client wsClient, String index, String type, String bulk) {
		WebTarget webTarget = wsClient.target(SERVER_ROOT_URI).path(index).path(type).path("_bulk");
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.put(Entity.entity(bulk, MediaType.APPLICATION_JSON));
		Object obj = response.readEntity(String.class);
		int status = response.getStatus();
		if (status != 200) {
			System.out.println(obj);
		}
		;
		response.close();
		return status;
	}

	public static void get(Client wsClient, String path) {

		WebTarget webTarget = wsClient.target(SERVER_ROOT_URI).path(path);

		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.get();

		Object obj = response.readEntity(String.class);

		System.out.println(response.getStatus());

		System.out.println(obj);

		System.out.println(response);
		System.out.println(String.format("GET on [%s], status code [%d]", SERVER_ROOT_URI, response.getStatus()));
		response.close();
	}

	public static int put(Client wsClient, String path, Object entity, String type) {
		if (type == null)
			type = MediaType.APPLICATION_JSON;

		WebTarget webTarget = wsClient.target(SERVER_ROOT_URI).path(path);
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.put(Entity.entity(entity, type));

		Object obj = response.readEntity(String.class);

		/*
		 * System.out.println(response.getStatus());
		 * 
		 * 
		 * System.out.println(obj);
		 * 
		 * System.out.println(response);
		 * System.out.println(String.format("GET on [%s], status code [%d]",
		 * SERVER_ROOT_URI, response.getStatus()));
		 */
		int status = response.getStatus();
		if (status != 200) {
			System.out.println(obj);
		}
		;
		response.close();
		return status;
	}

	public static int deleteAll(Client wsClient, String path, Object entity, String type) {
		/*
		 * curl -XDELETE 'http://localhost:9200/twitter/tweet/_query' -d '{
		 * "query" : { "match_all" : {} } }'
		 */

		return 0;
	}

	public static Client getClient() {
		ClientConfig wsClientConfiguration = new ClientConfig();
		Client wsClient = ClientBuilder.newClient(wsClientConfiguration);

		return wsClient;
	}

	public static long returnMilliSeconds(Date date) {

		Calendar calendar1 = Calendar.getInstance();
		// Calendar Calendar calendar2 = Calendar.getInstance();
		calendar1.set(1970, 01, 01);
		long milliseconds1 = calendar1.getTimeInMillis();
		long milliseconds2 = date.getTime();
		long diff = milliseconds2 - milliseconds1;
		// long seconds = diff / 1000;
		return diff;
	}

	public static void deleteIndexes() {
		// int[] indexes=new
		// int[]{18298,18737,22159,22670,18388,18329,21424,22530,18010,18627,18264,23394,21181,23426,23416,18683,22323,17907,18259,18378,22914,24068,22169,18233,21955,18377,22691,21537,22696,21793,18916,23423,17888,24023,23393,21954,22609,18303,21952,17965,22693,21848,18682,22671,17903,21092,18745,21853,18640,22610,22223,23418,21911,18220,22532,18742,18623,18349,17933,23051,21587,18629,18208,22692,21435,22163,17899,21791,18917,23643,18375};
		Client client = getClient();
		int[] indexes = new int[] { 24068, 18742, 18623, 18349, 17933, 23051, 21587, 18629, 18208, 2269, 21435, 22163, 17899, 21791, 18917, 23643, 18375 };

		for (int index : indexes) {

			String path = "" + index;
			String entity = "{}";
			WebTarget webTarget = client.target(SERVER_ROOT_URI).path(path);
			Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
			Response response = invocationBuilder.delete();

			Object obj = response.readEntity(String.class);
			response.close();

		}

	}

	public static String getIndices() {
		Client client = getClient();
		WebTarget webTarget = client.target(SERVER_ROOT_URI).path("_stats/index");
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.get();

		String obj = response.readEntity(String.class);
		DBObject jsonResponse = (DBObject) JSON.parse(obj);
		DBObject jsonIndices = (DBObject) jsonResponse.get("indices");

		System.out.println(jsonIndices);
		response.close();
		return jsonIndices.toString();

	}

}
