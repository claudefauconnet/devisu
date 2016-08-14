package com.fauconnet.old;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.fauconnet.devisu.I_DataManager;
import com.fauconnet.devisu.MongoProxy;
import com.fauconnet.devisu.XmlRadarModelManager;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class SqlDataManager implements I_DataManager {

	private long maxId;
	private MongoProxy proxy;
	private String dbName;
	private XmlRadarModelManager xmlModelManager;
	private String userId = "admin";
	private String idField = "id";

	
	public SqlDataManager(String dbName, String dataDirPath, String host, int port) throws Exception {
		this.dbName = dbName;
		try {
			xmlModelManager = new XmlRadarModelManager(dbName, dataDirPath);
			if (xmlModelManager != null)
				idField = xmlModelManager.getIdColumnName();
			
		} catch (Exception e) {
			
			e.getMessage();
		}
		
		try {
			proxy = new MongoProxy(host, port, dbName);
			
		} catch (Exception e) {
			throw new Exception (" Mongo Server not started or connected");
		}
	

	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	private String getDbName() {

		return dbName;
	}

	private int getMongoPort() {
		return 27017;
	}

	private String getMongoHost() {
		return "localhost";
	}

	private DBObject getItemById(String collectionName, int id) {
		DBObject query = new BasicDBObject("id", id);
		return proxy.getOneDocument(collectionName, query);
	}

	private String updateItemById(String collectionName, DBObject object) throws Exception {
		object.put("lastModified", new Date());
		object.put("modifiedBy", userId);
		Object id = object.get(idField);
		if (id == null)
			throw new Exception("object has nos id" + object.toString());
		DBObject query = new BasicDBObject("id", id);
		;
		return proxy.updateObject(collectionName, query, object);
	}

	public void updateField(String collectionName, int id, String field, String newValue, boolean save) throws Exception {

		DBObject obj = getItemById(collectionName, id);

		Object objIdValue = obj.get(idField);
		String field2 = xmlModelManager.getRole(field);
		if (field2 == null)
			field2 = field;

		if (newValue.equals("")) {
			obj.put(field2, newValue);
		} else if (newValue.matches("[\\-0-9]*")) {// int
			obj.put(field2, Integer.parseInt(newValue));
		} else if (newValue.matches("[\\-0-9.]*")) {// numerique
			float f = Float.parseFloat(newValue);
			obj.put(field2, f);
		} else
			obj.put(field2, newValue);

		updateItemById(collectionName, obj);

	}

	public void saveData(String collectionName, String json, String fileName) {
		proxy.updateMultipleObjects(collectionName, json);

	}

	public void updateItemFromRadar(String collectionName, int id, String jsonStr) {
		DBObject query = new BasicDBObject("id", id);
		DBObject object = (DBObject) JSON.parse(jsonStr);
		object.put("lastModified", new Date());
		object.put("modifiedBy", userId);
		proxy.updateObject(collectionName, query, object);

	}

	public void addRadarItem(String collectionName) {
		DBObject obj = new BasicDBObject();
		obj.put("name", "??");
		obj.put("id", maxId + 1);
		obj.put("x", 10);
		obj.put("y", 10);
		addAdminFields( obj,  collectionName );
		proxy.insert(collectionName, obj);

	}

	public void deleteItem(String collectionName, int id) {
		DBObject query = new BasicDBObject("id", id);
		proxy.removeByQuery(collectionName, query);

	}

	public String getRadarJsonData(String collectionName, String jsonQuery) {
		boolean save = false;
		boolean shouldSetItemsCoordinates=false;
		DBObject query;
		if (jsonQuery == null) {
			query = new BasicDBObject();
		} else
			query = (DBObject) JSON.parse(jsonQuery);
		List<DBObject> array = proxy.getDocuments(collectionName, query, 50000);
		BasicDBList arrayOut = new BasicDBList();
		int k = 0;
		for (int i = 0; i < array.size(); i++) {
			boolean shouldSave=false;
			DBObject obj2 = (DBObject) array.get(i);
			obj2.removeField("_id");
			DBObject objOut = new BasicDBObject();

			//objOut.put("action", "loadJSON"); // / a enlever

			Map<String, String> radarRoles = xmlModelManager.getRadarRoles();
			Iterator<String> it = radarRoles.keySet().iterator();
			while (it.hasNext()) {
				String role = it.next();
				String colName = radarRoles.get(role);
				Object val = obj2.get(colName);

				if (role.equals("id")) {

					if (val == null) {
						shouldSave = true;
						val = maxId++;// 0 d�conne ensuite...
						obj2.put("id", val);
					} else {

						try {
							maxId = Math.max(maxId, ((Integer) val).longValue());
						} catch (Exception e) {
							System.out.println("  pb parse " + val + "  " + e.toString());
						}
					}
					objOut.put("id", val);

				} else if (role.equals("x")) {

					if (val == null) {
						shouldSave = true;
						val = (k * 5);
						obj2.put("x", val);
					}
					objOut.put("x", val);

				} else if (role.equals("y")) {

					if (val == null) {
						shouldSave = true;
						val = (k * 5);
						obj2.put("y", val);
					}
					objOut.put("y", val);

				} else if (val != null) {
					objOut.put(role, val);

				}

			}

			Iterator<String> it2 = xmlModelManager.getFilters().iterator();
			while (it2.hasNext()) {
				String colName = it2.next();
				Object val = obj2.get(colName);
				objOut.put(colName, val);
			}

			k++;
			arrayOut.add(objOut);
			if(shouldSave){
				shouldSetItemsCoordinates=true;
				proxy.updateObject(collectionName, new BasicDBObject(), obj2);
			}
		}

		DBObject objOut2 = new BasicDBObject();
		if(shouldSetItemsCoordinates){
			objOut2.put("shouldSetItemsCoordinates", "yes");
		}
		objOut2.put("points", arrayOut);
		BasicDBList arrayOut2 = new BasicDBList();
		arrayOut2.add(objOut2);
		return arrayOut2.toString();

	}

	public String getDataJson(String collectionName, String queryStr, boolean reload) throws Exception {
		DBObject query;
		if (queryStr == null) {
			query = new BasicDBObject("id", new BasicDBObject("$gt", -1));
		} else {
			query = (DBObject) JSON.parse(queryStr);
		}
		return proxy.getDocuments(collectionName, query, 50000).toString();
	}

	public String getDetailedData(String collectionName, int id) throws Exception {
		DBObject query = new BasicDBObject("id", id);
		DBObject object = proxy.getOneDocument(collectionName, query);
		List<String> fields = xmlModelManager.getDetailedPageFields();
		Iterator<String> it = object.keySet().iterator();
		while (it.hasNext()) {
			String field = it.next();
			if (fields.indexOf(field) < 0)
				object.removeField(field);
		}
		return object.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String updateItem(String collectionName, String json) throws Exception {
		DBObject object = (DBObject) JSON.parse(json);
		object.put("lastModified", new Date());
		object.put("modifiedBy", userId);
		Object id = object.get(idField);
		if (id == null)
			throw new Exception("object has nos id" + object.toString());
		DBObject query = new BasicDBObject("id", id);
		return proxy.updateObject(collectionName, query, object);
	}

	public XmlRadarModelManager getXmlModelManager() {
		return xmlModelManager;

	}

	public void addItem(String collectionName, String jsonItem) {
		DBObject object = (DBObject) JSON.parse(jsonItem);
		addAdminFields( object,  collectionName );
		proxy.insert(collectionName, object);

	}

	public void createDB(String dbName) throws Exception {
		DB db = proxy.createDB(dbName);
		proxy.createCollection(db, "radar");
		proxy.createCollection(db, "nodes");
		proxy.createCollection(db, "links");
		proxy.createCollection(db, "details");
		proxy.createCollection(db, "admin");

	}

	public String getCollectionNames(String dbName) throws Exception {
		return proxy.listCollections();

	}

	public String getDBNames(String dbName) throws Exception {
		return proxy.listDBs();

	}

	public void addItems(String collectionName, String jsonItem) throws Exception {

		Object obj = JSON.parse(jsonItem);
		List<DBObject> list = (List<DBObject>) obj;
		Iterator<DBObject> it = list.iterator();
		while (it.hasNext()) {
			DBObject object = it.next();
			addAdminFields( object,  collectionName );
			proxy.insert(collectionName, object);

		}

	}
	
	private void addAdminFields(DBObject object, String collectionName ){
		if(object.get("id")==null){
			object.put("id",proxy.getMaxId( collectionName)+1);
		}			
		object.put("lastModified", new Date());
		object.put("modifiedBy", userId);
	}

	public String getDBNames() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserRights(String login, String password) {
		DBObject query= new BasicDBObject();
		query.put("login", login);
		query.put("password", password);
		return proxy.getOneDocument("user", query).toString();
	}

	

	
}
