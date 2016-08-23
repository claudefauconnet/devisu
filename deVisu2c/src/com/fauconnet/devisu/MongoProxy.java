package com.fauconnet.devisu;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.CommandResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;
import org.bson.types.ObjectId;

import static java.util.concurrent.TimeUnit.SECONDS;

// or

/*
 * 
 * 
 * show dbs
 Switch to a new database named mydb, with the following operation:
 use mydb
 Confirm that your session has the mydb database as context, by checking the value of the db object, which returns the name of the current database, as follows:
 db

 db.nodes.find();

 * 
 * 
 * mongoimport --db POT --collection nodes --type json --file POT_nodes_Graph.json --jsonArray
 * 
 * 
 * 
 */
public class MongoProxy {

	private static boolean LOG_UPDATE = true;
	MongoClient mongoClient;
	protected DB db;

	public MongoProxy(String host, int port, String dbName) throws UnknownHostException {
		mongoClient = new MongoClient(host, port);
		db = mongoClient.getDB(dbName);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			/*
			 * MongoProxy proxy = new MongoProxy("localhost", 27017, "rss3d");
			 * DBObject query2 = new BasicDBObject("hashcode",-996749175);
			 * DBObject obj=proxy.getOneDocument("feeds2",query2); String
			 * title=(String) obj.get("title"); System.out.println(title);
			 * for(int i=0;i<title.length();i++){
			 * System.out.println(title.charAt(i)+" --- "+((int)
			 * title.charAt(i))); }
			 */

			MongoProxy proxy = new MongoProxy("localhost", 27017, "POT2016");
			
		 List<String> objs=proxy.getDistinct("use_cases_tree", new BasicDBObject(), "type");
		 for(String obj:objs){
			 String str=obj.toString();
			 System.out.println(str);;
		 }
	
		 
		 
		 
			proxy.listCollections();
			
			DBObject query=new BasicDBObject();
			DBObject fields=new BasicDBObject("techno",1);
			fields.put("year",1);
			List<DBObject> result=proxy.getDocuments("radar", query, fields, 100);
			System.out.println(result);

			// Object obj=proxy.executeJavascript("getTreeNodes", new
			// String[]{"Countries"});
		//	Object obj = proxy.executeJavascript("return getTreeNodes('Countries')");
		//	System.out.println(obj);

			// Object obj= proxy.getGroupStat( "nodes", new
			// BasicDBObject(),"$max", "id_");

			/*
			 * int n = proxy.getMaxId("radar"); proxy.addIds("links");
			 * proxy.mapReduce(); if (true) return;
			 * 
			 * System.out.println(proxy.listCollections());
			 */
			/*
			 * DBObject query=null; proxy.listCollections(); query=new
			 * BasicDBObject("nature", "technology"); ObjectId id=new
			 * ObjectId("54706d69d85cc166ecefcfa7"); query=new
			 * BasicDBObject("_id", id); proxy.getJson("nodes",query,1000);
			 * 
			 * String json =
			 * "{  \"id\" : 25 , \"label\" : \"High Performance ComputingV?///ZZZZ\" , \"nature\" : \"technology\"}"
			 * ; DBObject query = new BasicDBObject("id", 25);
			 * 
			 * String _id = proxy.updateObject("nodes", query, json);
			 * 
			 * proxy.removeBy_id("nodes", _id);
			 */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addIds(String collectionName) {
		Iterator<DBObject> it = getDocuments(collectionName, null, -1).iterator();
		int id = getMaxId(collectionName);
		while (it.hasNext()) {
			DBObject object = it.next();
			if (object.get("id") == null) {
				object.put("id", id++);
				updateObject(collectionName, null, object);
			}
		}

	}

	public String listCollections() {

		Set<String> colls = db.getCollectionNames();

		DBObject collections = new BasicDBList();
		int i = 0;
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (String s : colls) {
			if (i++ > 0)
				sb.append(",");
			// System.out.println(s);
			sb.append(new BasicDBObject("name", s).toString());
		}
		sb.append("]");

		return sb.toString();
	}

	public String listDBs() {

		List<String> dbs = mongoClient.getDatabaseNames();
		int i = 0;
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (String s : dbs) {
			if (i++ > 0)
				sb.append(",");
			// System.out.println(s);
			sb.append(new BasicDBObject("name", s).toString());
		}
		sb.append("]");
		System.out.println(sb);

		return sb.toString();
	}

	public List<DBObject> getDocuments(String collectionName, DBObject query, int limit) {
		DBCollection coll = db.getCollection(collectionName);
		// DBObject sort = new BasicDBObject("id", -1);
		DBCursor cursor = coll.find(query);// .sort(sort);
		if (limit > 0)
			cursor = cursor.limit(limit);
		return cursor.toArray();
	}

	public DBCursor getDocumentsCursor(String collectionName, DBObject query, int limit) {
		DBCollection coll = db.getCollection(collectionName);
		// DBObject sort = new BasicDBObject("id", -1);
		DBCursor cursor = coll.find(query);
		if (limit > 0)
			cursor = cursor.limit(limit);
		return cursor;
	}

	public List<DBObject> getDocuments(String collectionName, DBObject query, DBObject fields, int limit) {
		DBCollection coll = db.getCollection(collectionName);
		DBObject sort = new BasicDBObject("id", -1);
		DBCursor cursor = coll.find(query, fields).sort(sort);
		if (limit > 0)
			cursor = cursor.limit(limit);
		List<DBObject> result=cursor.toArray();
		return result;
	}
	public DBCursor getDocumentsCursor(String collectionName, DBObject query, DBObject fields, int limit) {
		DBCollection coll = db.getCollection(collectionName);
		DBObject sort = new BasicDBObject("id", -1);
		DBCursor cursor = coll.find(query, fields).sort(sort);
		
		return cursor;
	}

	public DBObject getOneDocument(String collectionName, DBObject query) {
		DBCollection coll = db.getCollection(collectionName);
		return coll.findOne(query);

	}

	public DBObject count(String collectionName, DBObject query) {
		DBCollection coll = db.getCollection(collectionName);
		long count;
		if (query == null) {
			count = coll.count();
		} else {
			count = coll.count(query);
		}
		return new BasicDBObject("count", (int) count);

	}

	public List<String> getDistinct(String collectionName, DBObject query, String key) {
		DBCollection coll = db.getCollection(collectionName);
		List<String> result = coll.distinct(key, query);
		return result;
	}

	public String insert(String collectionName, String json) {
		// json="{ \"_id\" : { \"$oid\" : \"54706d69d85cc166ecefcfa7\"} , \"id\" : 25 , \"label\" : \"High Performance ComputingZZZZZZZ\" , \"nature\" : \"technology\"}";
		DBCollection coll = db.getCollection(collectionName);
		Charset.forName("UTF-8").encode(json);
		DBObject newObj = (DBObject) JSON.parse(json);
		coll.insert(newObj, WriteConcern.ACKNOWLEDGED);
		Object objId2 = newObj.get("_id");
		return objId2.toString();

	}

	public String insert(String collectionName, DBObject newObj) {
		DBCollection coll = db.getCollection(collectionName);
		coll.insert(newObj, WriteConcern.ACKNOWLEDGED);
		Object objId2 = newObj.get("_id");
		return objId2.toString();

	}

	// http://docs.mongodb.org/manual/tutorial/aggregation-zip-code-data-set/
	// http://stackoverflow.com/questions/24545492/how-to-sum-of-values-from-mongo-collection-using-java//
	/*
	 * db.zipcodes.aggregate( [ { $group: { _id: { state: "$state", city:
	 * "$city" }, pop: { $sum: "$pop" } } }, { $group: { _id: "$_id.state",
	 * avgCityPop: { $avg: "$pop" } } } ] )
	 */
	public float getGroupStat(String collectionName, DBObject query, String operator, String field) {
		DBCollection coll = db.getCollection(collectionName);
		DBObject result = coll.find(query, new BasicDBObject(field, 1).append("_id", -1)).sort(new BasicDBObject(field, -1)).limit(1).next();
		return (float) new Float((int) result.get(field));

	}

	public int getMaxId(String collectionName) {
		DBObject sort = new BasicDBObject();
		sort.put("id", -1);
		DBCollection coll = db.getCollection(collectionName);
		DBCursor cursor = coll.find().sort(sort).limit(1);

		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			Object idObj = obj.get("id");
			if (idObj != null) {
				if (idObj instanceof Double) {
					return ((Double) idObj).intValue();

				}
				if (idObj instanceof Long) {
					return ((Long) idObj).intValue();

				} else if (idObj instanceof Integer) {
					return (Integer) idObj;
				}

				return (int) idObj;
			}
		}
			return 0;
		//return -9999999;

	}

	public String updateFields(String collectionName, DBObject query, DBObject fields) {
		DBCollection coll = db.getCollection(collectionName);
		/*DBObject obj = coll.findOne(query);
		//System.out.println(obj);
		DBObject fieldSet = new BasicDBObject("$set", fields);
		WriteResult result = coll.update(query, fieldSet, true, true);*/
		
		/*DBObject carrier= new BasicDBObject();
		(BasicDBList)fields.
		
		for(Object field: ((BasicDBList)fields)){
			carrier.put(key,fields.get(key));
		}   */
		BasicDBObject set = new BasicDBObject("$set", fields);
		coll.update(query, set);
		
		
		
		
	//	logUpdate(collectionName, query, fields);
		/*
		 * System.out.println ("------"); String
		 * obj2=coll.find(query).toString(); System.out.println ("???"+obj2);
		 */
		/*
		 * if (oldObj == null) {
		 * System.out.println(" error:object not found for query " + query);
		 * return null; }
		 */
		// String _id=result.getUpsertedId().toString();
		// String _id = oldObj.get("_id").toString();
		return null;

	}

	public void logUpdate(String collectionName, DBObject query, DBObject fields) {
		if (LOG_UPDATE) {
			DBObject obj = new BasicDBObject();
			obj.put("collectionName", collectionName);
			obj.put("query", query);
			obj.put("fields", fields);
			obj.put("date", new Date());
			insert("updateLog", obj);
		}

	}

	public String updateObject(String collectionName, DBObject query, String json) {
		DBObject object = (DBObject) JSON.parse(json);
		return updateObject(collectionName, query, object);

	}

	public String updateObject(String collectionName, DBObject query, DBObject object) {
		DBCollection coll = db.getCollection(collectionName);

		if (query == null) {
			Object id = object.get("_id");
			if (id == null) {// new object
				System.out.println(" error:no query or _id for json " + object.toString());
				return null;
			}
			ObjectId objId = new ObjectId("" + id);
			query = new BasicDBObject("_id", objId);
		}
		DBObject sort = new BasicDBObject("id", -1);
		coll.update(query, object, true, false);
		logUpdate(collectionName, query, object);
		return "object updated";

	}

	public String updateMultipleObjects(String collectionName, String json) {
		BasicDBList objects = (BasicDBList) JSON.parse(json);
		return updateMultipleObjects(collectionName, objects);
	}

	public String updateMultipleObjects(String collectionName, BasicDBList objects) {
		DBCollection coll = db.getCollection(collectionName);
		Iterator it = objects.iterator();
		while (it.hasNext()) {
			DBObject query = null;
			DBObject object = (DBObject) it.next();

			Object id = object.get("_id");
			if (id == null) // new object
				id = object.get("id");
			if (id == null) {
				System.out.println(" error:no  _id for json " + object.toString());
				continue;
			}
			query = new BasicDBObject("id", id);
			updateObject(collectionName, query, object);
		}
		return "items collection updated";
	}

	public String removeByJson(String collectionName, String json) {
		DBCollection coll = db.getCollection(collectionName);
		DBObject obj = (DBObject) JSON.parse(json);
		WriteResult result = coll.remove(obj);
		return "object removed";
	}

	public String removeByQuery0(String collectionName, DBObject query) {
		DBCollection coll = db.getCollection(collectionName);
		DBObject obj = coll.findOne(query);
		if (obj == null) {
			System.out.println(" error:object not found with query " + query);
			return null;
		}
		WriteResult result = coll.remove(obj);
		return "object removed";
	}

	public String removeByQuery(String collectionName, DBObject query) {
		DBCollection coll = db.getCollection(collectionName);
		Iterator<DBObject> it = coll.find(query).iterator();
		if (it == null || !it.hasNext()) {
			System.out.println(" error:object not found with query " + query);
			return null;
		}
		int i = 0;
		while (it.hasNext()) {
			coll.remove(it.next());
			i++;
		}
		return "objects removed : " + i;
	}

	public String removeBy_id(String collectionName, String _id) {
		ObjectId objId = new ObjectId("" + _id);
		DBObject query = new BasicDBObject("_id", objId);
		DBCollection coll = db.getCollection(collectionName);
		DBObject obj = coll.findOne(query);
		if (obj == null) {
			System.out.println(" error:object not found with _id " + _id);
			return null;
		}
		WriteResult result = coll.remove(obj);
		return "object removed";
	}

	public DB createDB(String dbname) {

		return mongoClient.getDB(dbname);
	}

	public DBCollection createCollection(DB db, String collectionName) {
		if (db.collectionExists(collectionName))
			return db.getCollection(collectionName);
		return db.createCollection(collectionName, new BasicDBObject());
	}

	public String executeCommand(com.mongodb.DBObject command, int options) {

		// db.copyDatabase({"fromdb":"IN","todb":"IN","fromhost":"10.31.242.248"})
		// com.mongodb.CommandResult
		return "";
	}

	public DBObject executeJavascript(String script) throws Exception {
		if (script.indexOf("return ") < 0)
			script = "return " + script;
		Object obj = db.doEval(script, null);
		CommandResult result = db.doEval(script, null);
		BasicDBList list = (BasicDBList) result.get("retval");
		return list;

	}

	public String mapReduce() {
		try {
			String map = "function() { var output= {source:this.source, target:this.target ," + " sourceNature:db.nodes.findOne({id:this.sourceId}).nature,"
					+ "targetNature:db.nodes.findOne({id:this.targetId}).nature};" + "emit(this._id, output)}";

			String reduce = "function(key, values) {" + " var outs={ source:null ,target:null, sourceNature:null , targetNature:null}" + " values.forEach(function(v){" + " if(outs.source ==null){"
					+ " outs.source = v.source" + "}" + " if(outs.target ==null){" + "     outs.target = v.target" + " } if(outs.sourceNature ==null){" + " outs.sourceNature = v.sourceNature}"
					+ "if(outs.targetNature ==null){" + "outs.targetNature = v.targetNature" + "}" + " });" + "return outs;" + "};";
			String result = "";
			DBCollection coll = db.getCollection("links");
			MapReduceOutput out = coll.mapReduce(map, reduce, "tmp", null);
			for (DBObject o : out.results()) {

				System.out.println(o.toString());

			}

		} catch (Exception e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}
		return "";
	}

}
