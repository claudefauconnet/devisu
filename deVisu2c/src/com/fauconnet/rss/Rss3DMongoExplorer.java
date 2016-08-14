package com.fauconnet.rss;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.fauconnet.devisu.MongoProxy;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class Rss3DMongoExplorer extends MongoProxy {

	public static void main(String[] args) {
		try {
			Rss3DMongoExplorer explorer = new Rss3DMongoExplorer("localhost", 27017, "rss3d");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Rss3DMongoExplorer(String host, int port, String dbName) throws UnknownHostException {
		super(host, port, dbName);
	}
	
	
	
	public String getFeeds(String expression,String jsonQuery,int  limit){
		DBCollection coll = db.getCollection("feeds");
		BasicDBObject queryObj=getQueryObject( expression,  jsonQuery);
		BasicDBObject sortObj=new BasicDBObject( "pubDate2",  -1);
		
		BasicDBObject fields = new BasicDBObject("title",1);
		fields.put("pubDate",1);
		fields.put("link",1);
		fields.put("channelId",1);
		DBCursor cursor = coll.find(queryObj,fields).sort(sortObj);
		if (limit > 0)
			cursor = cursor.limit(limit);
		return cursor.toArray().toString();
		
		
	}
	
	
	
	private  BasicDBObject getQueryObject(String expression, String jsonQuery){
		
		BasicDBObject queryObj = new BasicDBObject();
		if (jsonQuery != null){
	
			BasicDBList queryObj_ = (BasicDBList) JSON.parse(jsonQuery);
			Iterator<Object> it=queryObj_.iterator();
			
			
			BasicDBList timeCriteriaList =new BasicDBList();
			while(it.hasNext()){
				String expression0="";
				BasicDBObject queryObj_2 =(BasicDBObject)it.next();
				String key=queryObj_2.keySet().iterator().next();
				Object value=queryObj_2.get(key);
				String value2=""+value;
				if( value2.length()==1)
					value2="0"+value2;
				if(key.equals("$year")){
					expression0=""+value2+"-";
				}
				if(key.equals("$month")){
					expression0="[0-9]*-"+value2+"-";
				}
				if(key.equals("$dayOfMonth")){
					
					expression0= "-"+value2+" ";
				}
				
				timeCriteriaList.add(new BasicDBObject("pubDate",java.util.regex.Pattern.compile(expression0)));
				
				
				
			}
			if(timeCriteriaList.size()>0)
				queryObj.put("$and",timeCriteriaList);
			
			
			
			
	
			
		}
		queryObj.put("title", java.util.regex.Pattern.compile(expression, Pattern.CASE_INSENSITIVE));
		return queryObj;
	}

	public String aggregateFeeds(String expression, String periodType, String jsonQuery) {
		String str = "";
		try {
			BasicDBObject queryObj=getQueryObject( expression,  jsonQuery);
			DBCollection coll = db.getCollection("feeds");
			// long count = coll.count(new BasicDBObject());

			// create our pipeline operations, first with the $match
			// DBObject match = new BasicDBObject("$match", new
			// BasicDBObject("title", " $regex: /sarkosy/, $options: 'i'"));
		

		//	 long count = coll.count(queryObj);
//System.out.println("N feeds ="+count+"    "+queryObj);
			DBObject match = new BasicDBObject("$match", queryObj);
			// build the $projection operation
			DBObject fields = new BasicDBObject("pubDate2", 1);
			fields.put("_id", 0);
			fields.put("id", 1);
			DBObject project = new BasicDBObject("$project", fields);

			/*
			 * { $group: { _id: { $dayOfYear: "$date"}, click: { $sum: 1 } } } )
			 */
			// Now the $group operation
			DBObject groupFields = new BasicDBObject("_id", new BasicDBObject(periodType, "$pubDate2"));
			groupFields.put("count", new BasicDBObject("$sum", 1));
			DBObject group = new BasicDBObject("$group", groupFields);

			// Finally the $sort operation
			DBObject sort = new BasicDBObject("$sort", new BasicDBObject("_id", 1));

			// run aggregation
			List<DBObject> pipeline = Arrays.asList(match, project, group, sort);

			AggregationOutput output = coll.aggregate(pipeline);
			for (DBObject result : output.results()) {
				result.put(periodType, result.get("_id"));
			}
			str = output.results().toString();
			for (DBObject result : output.results()) {
				//System.out.println(result);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
	
	
	
	

	public static void test() {

		MongoClient mongoClient;
		try {
			mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("rss3d");
			DBCollection coll = db.getCollection("feeds");
			// long count = coll.count(new BasicDBObject());

			// create our pipeline operations, first with the $match
			// DBObject match = new BasicDBObject("$match", new
			// BasicDBObject("title", " $regex: /sarkosy/, $options: 'i'"));
			BasicDBObject q = new BasicDBObject();
			q.put("title", java.util.regex.Pattern.compile("Hollande"));
			long count = coll.count(q);

			DBObject match = new BasicDBObject("$match", q);
			// build the $projection operation
			DBObject fields = new BasicDBObject("pubDate2", 1);
			fields.put("_id", 0);
			fields.put("id", 1);
			DBObject project = new BasicDBObject("$project", fields);

			/*
			 * { $group: { _id: { $dayOfYear: "$date"}, click: { $sum: 1 } } } )
			 */
			// Now the $group operation
			DBObject groupFields = new BasicDBObject("_id", new BasicDBObject("$year", "$pubDate2"));
			groupFields.put("count", new BasicDBObject("$sum", 1));
			DBObject group = new BasicDBObject("$group", groupFields);

			// Finally the $sort operation
			DBObject sort = new BasicDBObject("$sort", new BasicDBObject("_id", -1));

			// run aggregation
			List<DBObject> pipeline = Arrays.asList(match, project, group, sort);

			AggregationOutput output = coll.aggregate(pipeline);

			for (DBObject result : output.results()) {
				System.out.println(result);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
