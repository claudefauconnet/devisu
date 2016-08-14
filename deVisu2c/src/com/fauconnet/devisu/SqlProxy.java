package com.fauconnet.devisu;

import java.lang.reflect.Field;

import java.net.UnknownHostException;

import java.sql.Connection;

import java.sql.DriverManager;

import java.sql.ResultSet;

import java.sql.ResultSetMetaData;

import java.sql.SQLException;

import java.sql.Statement;

import java.sql.Types;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.Iterator;

import java.util.List;

import java.util.Map;

import java.util.HashMap;

import java.util.Set;

import org.bson.BSONObject;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import com.mongodb.BasicDBObject;

import com.mongodb.DBObject;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SqlProxy {

	private  String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private  String DB_URL = "jdbc:mysql://localhost:3306/reporting";
	private  String USER = "root";
	private  String PASS = "";

	private DB db;

	public void init() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");

	}

	public static void main(String[] args) {
		try {
			SqlProxy proxy = new SqlProxy();
			DBObject obj = proxy.executeReadQuery("select * from report_general limit 100");
			System.out.println(obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

	public SqlProxy(String jDBC_DRIVER, String dB_URL, String uSER, String pASS) {
		super();
		JDBC_DRIVER = jDBC_DRIVER;
		
		DB_URL = dB_URL;
		USER = uSER;
		PASS = pASS;
	}

	public SqlProxy() throws Exception {
		init();
	}
	
	
	public DBObject executeReadQuery(String sql) throws Exception {
		Class.forName(JDBC_DRIVER);
		Connection conn = null;

		Statement stmt = null;

		BasicDBList objs = new BasicDBList();

		try {
			// System.out.println("Connecting to database...");

			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// System.out.println("Creating statement...");

			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			List<String> colNames = new ArrayList<String>();

			int n = rs.getMetaData().getColumnCount();

			for (int i = 0; i < n; i++) {
				colNames.add(rs.getMetaData().getColumnName(i + 1));

			}
			int nR = 0;

			while (rs.next()) {
				nR++;

				DBObject obj = new BasicDBObject();

				for (int i = 0; i < n; i++) {
					String colName = colNames.get(i);

					obj.put(colName, rs.getObject(i + 1));

				}

				objs.add(obj);

			}
			// STEP 6: Clean-up environment
			rs.close();

			stmt.close();

			conn.close();

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();

		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();

		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();

			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();

			} catch (SQLException se) {
				se.printStackTrace();

			}// end finally try
		}// end try

		if (objs.size() > 1)
			return objs;

		if (objs.size() == 1)
			return (DBObject) objs.get(0);

		else
			return new BasicDBObject();

	}

	public String executeWriteQuery(String sql) {
		Connection conn = null;

		Statement stmt = null;

		BasicDBList objs = new BasicDBList();

		try {
			// System.out.println("Connecting to database...");

			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// System.out.println("Creating statement...");

			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			rs.close();

			stmt.close();

			conn.close();

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();

		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();

		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();

			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();

			} catch (SQLException se) {
				se.printStackTrace();

			}// end finally try
		}// end try
		return "Done";

	}

/*	private void addIds(String collectionName) {
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
			//
			System.out.println(s);
			sb.append(new BasicDBObject("name", s).toString());
		}
		sb.append("]");

		return sb.toString();
	}

	public String listDBs() {

//		List<String> dbs = mongoClient.getDatabaseNames();
//		int i = 0;
//		StringBuffer sb = new StringBuffer();
//		sb.append("[");
//		for (String s : dbs) {
//			if (i++ > 0)
//				sb.append(",");
//			// System.out.println(s);
//			sb.append(new BasicDBObject("name", s).toString());
//		}
//		sb.append("]");
//
//		System.out.println(sb);
//
//		return sb.toString();
//	}

	public List<DBObject> getDocuments(String collectionName, DBObject query, int limit) {
		DBCollection coll = db.getCollection(collectionName);

		DBObject sort = new BasicDBObject("id", -1);
		DBCursor cursor = coll.find(query).sort(sort);
		if (limit > 0)
			cursor = cursor.limit(limit);

		return cursor.toArray();
	}

	public DBObject getOneDocument(String collectionName, DBObject query) {
		DBCollection coll = db.getCollection(collectionName);
		return coll.findOne(query);

	}

	public String insert(String collectionName, String json) { 
 DBCollection coll = db.getCollection(collectionName);
// DBObject newObj =(DBObject) JSON.parse(json);
// coll.insert(newObj, WriteConcern.ACKNOWLEDGED);
 Object objId2 = newObj.get("_id");
 return
	  objId2.toString();

	  
	  }

	public String insert(String collectionName, DBObject newObj) {
		DBCollection coll = db.getCollection(collectionName);
	//	coll.insert(newObj, WriteConcern.ACKNOWLEDGED);
		Object objId2 = newObj.get("_id");
		return objId2.toString();

	}

	public int getMaxId(String collectionName) {
		DBObject sort = new BasicDBObject();
		sort.put("id", -1);
		DBCollection coll = db.getCollection(collectionName);
	//	DBCursor cursor = coll.find().sort(sort).limit(1);
		if (!cursor.hasNext())
			return 0;

		DBObject obj = cursor.next();
		Object idObj = obj.get("id");
		if (idObj == null)
			return 0;
		return (Integer) obj.get("id");
		// return 0;
		// (int)
		obj.get("id");

	}

	public String updateFields(String collectionName, DBObject query, DBObject fields) {
		DBCollection coll = db.getCollection(collectionName);

		DBObject fieldSet = new BasicDBObject("$set", fields);
		DBObject oldObj = coll.findAndModify(query, fieldSet);

		String _id = oldObj.get("_id").toString();
		return _id;

	}

	public String updateObject(String collectionName, DBObject query, String json) {
		DBObject object = (DBObject) JSON.parse(json);
		return updateObject(collectionName, query, object);
	}

	public String updateObject(String collectionName, DBObject query,
	  DBObject object) { 
 DBCollection coll = db.getCollection(collectionName);

	  
	  if (query == null) { Object id = object.get("_id");
 if (id == null) {//
	  new object System.out.println(" error:no query or _id for json " +
	  object.toString());
 return null;
 } ObjectId objId = new ObjectId("" +
	  id);
 query = new BasicDBObject("_id", objId);
 }
	  
	  DBObject sort=new BasicDBObject("id",-1);

	  
	  DBObject oldObj = coll.findAndModify(query,sort,object);
//,null,false,
	  object,true,false);
 if (oldObj == null) {
	  //System.out.println(" error:object not found for json " +
	  object.toString());
 return null;
 } String _id =
	  oldObj.get("_id").toString();
 return _id;
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

	public String removeByQuery(String collectionName, DBObject query) {
		DBCollection coll = db.getCollection(collectionName);
		DBObject obj = coll.findOne(query);
		if (obj == null) {
			System.out.println(" error:object not found with query " + query);
			return null;
		}
		WriteResult result = coll.remove(obj);
		return "object removed";
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

		Object mongoClient;
		return null;// mongoClient.getDB(dbname);
	}

	public DBCollection createCollection(DB db, String collectionName) {
		if (db.collectionExists(collectionName))
			return db.getCollection(collectionName);
		return db.createCollection(collectionName, new BasicDBObject());
	}

	public String executeCommand(com.mongodb.DBObject command, int options) {

		// db.copyDatabase({"fromdb":"IN","todb":"IN","fromhost":"10.31.242.248"})
		// com.mongodb.CommandResult return "";
		return "";
	}

	public static void main0(String[] args) {
		Connection conn = null;

		Statement stmt = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");

			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 4: Execute a query
			System.out.println("Creating statement...");

			stmt = conn.createStatement();

			String sql;

			sql = "SELECT id, first, last, age FROM Employees";

			ResultSet rs = stmt.executeQuery(sql);

			// STEP 5: Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				int id = rs.getInt("id");

				int age = rs.getInt("age");

				String first = rs.getString("first");

				String last = rs.getString("last");

				// Display values
				System.out.print("ID: " + id);

				System.out.print(", Age: " + age);

				System.out.print(", First: " + first);

				System.out.println(", Last: " + last);

			}
			// STEP 6: Clean-up environment
			rs.close();

			stmt.close();

			conn.close();

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();

		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();

		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();

			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();

			} catch (SQLException se) {
				se.printStackTrace();

			}// end finally try
		}// end try
		System.out.println("Goodbye!");

	}*/

}
