package com.fauconnet.devisu;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
//import javax.xml.ws.spi.http.HttpContext;

import org.bson.BSON;
import org.bson.BSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.fauconnet.devisu.XmlRadarModelManager.RadarModel;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;

public class MongoDataManager implements I_DataManager {

	private boolean SEARCH_IN_ELASTICSEARCH = true;
	private long maxId;
	private MongoProxy proxy;
	private String dbName;
	private XmlRadarModelManager xmlRadarModelManager;
	private String userId = "admin";
	private String idField = "id";
	ElasticSearchManager searchManager = null;
	private ServletContext context;
	private String dataDirPath;
	
	private int maxDocsLimit=70000;

	public MongoDataManager(String dbName, String dataDirPath, String host, int port) throws Exception {
		this.dbName = dbName;
		this.dataDirPath = dataDirPath;
		try {
			xmlRadarModelManager = new XmlRadarModelManager(dbName, dataDirPath);

		} catch (Exception e) {

			e.getMessage();
		}

		try {
			proxy = new MongoProxy(host, port, dbName);

		} catch (Exception e) {
			throw new Exception(" Mongo Server not started or connected");
		}

	}
	
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MongoDataManager manager = new MongoDataManager("DAP", null, "localhost", 27017);
			String str = manager.getGraphAlldescendantLinksAndNodes(241);
			String s2 = str;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ServletContext getContext() {
		return context;
	}

	public void setContext(ServletContext context) {
		this.context = context;
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

	public float getGroupStat(String collectionName, String jsonQuery, String operator, String field) throws Exception {
		DBObject query = (DBObject) JSON.parse(jsonQuery);
		float n = proxy.getGroupStat(collectionName, query, operator, field);
		return n;
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

	public void updateRadarField(String radarModelName, String collectionName, int id, String field, String newValue, boolean save) throws Exception {

		DBObject obj = getItemById(collectionName, id);

		Object objIdValue = obj.get(idField);
		RadarModel radarModel = xmlRadarModelManager.getModel(radarModelName);
		String field2 = xmlRadarModelManager.getRole(radarModel, field);
		if (field2 == null)
			field2 = field;
		try {
			if (newValue.equals("")) {
				obj.put(field2, newValue);
			} else if (newValue.matches("[;\\-0-9]*")) {// int
				obj.put(field2, Integer.parseInt(newValue));
			} else if (newValue.matches("[\\-0-9.]*")) {// numerique
				float f = Float.parseFloat(newValue);
				obj.put(field2, f);
			} else
				obj.put(field2, newValue);
		} catch (Exception e) {
			System.out.println("--------Parse Error----" + newValue);
			e.printStackTrace();
		}
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

	public String addRadarItem(String collectionName) {
		DBObject obj = new BasicDBObject();
		obj.put("name", "??");
		obj.put("id", maxId + 1);
		obj.put("x", 10);
		obj.put("y", 10);
		addAdminFields(obj, collectionName);
		proxy.insert(collectionName, obj);
		return "" + obj.get("id");

	}

	public void deleteItem(String collectionName, int id) {
		DBObject query = new BasicDBObject("id", id);
		proxy.removeByQuery(collectionName, query);

	}

	public void deleteItemByQuery(String collectionName, String jsonQuery) {
		DBObject query = (DBObject) JSON.parse(jsonQuery);
		proxy.removeByQuery(collectionName, query);

	}

	public String getRadarJsonData(String radarModelName, String collectionName, String jsonQuery) {
		RadarModel radarModel = xmlRadarModelManager.getModel(radarModelName);
		boolean save = false;
		boolean shouldSetItemsCoordinates = false;
		DBObject query;
		if (jsonQuery == null) {
			query = new BasicDBObject();
		} else
			query = (DBObject) JSON.parse(jsonQuery);
		List<DBObject> array = proxy.getDocuments(collectionName, query, maxDocsLimit);
		BasicDBList arrayOut = new BasicDBList();

		int k = 100;
		for (int i = 0; i < array.size(); i++) {
			boolean shouldSave = false;
			DBObject obj2 = (DBObject) array.get(i);
			obj2.removeField("_id");
			DBObject objOut = new BasicDBObject();

			// objOut.put("action", "loadJSON"); // / a enlever

			Map<String, String> radarRoles = xmlRadarModelManager.getRadarRoles(radarModel);
			radarRoles.put("w","w");
			radarRoles.put("h","h");
			Iterator<String> it = radarRoles.keySet().iterator();
			while (it.hasNext()) {
				String role = it.next();
				String colName = radarRoles.get(role);
				// System.out.println (colName+"---"+role);
				Object val = obj2.get(colName);

				if (role.equals("id")) {

					if (val == null) {
						shouldSave = true;
						val = maxId++;// 0 dï¿½conne ensuite...
						obj2.put("id", val);
					} else {

						try {
							String str = "" + val;
							if (str.indexOf(".") > -1)
								maxId = Math.max(maxId, (long) Float.parseFloat(str));
							else
								maxId = Math.max(maxId, Long.parseLong(str));
						} catch (Exception e) {
							System.out.println("  pb parse " + val + "  " + e.toString());
						}
					}
					objOut.put("id", val);

				} else if (role.equals("x")) {

					if (val == null) {
						shouldSave = true;
						val = (10 );
						obj2.put("x", val);
					}
					objOut.put("x", val);

				} else if (role.equals("y")) {

					if (val == null) {
						shouldSave = true;
						val = (k );
						k=k+20;
						obj2.put("y", val);
					}
					objOut.put("y", val);

				} else if (val != null) {
					objOut.put(role, val);

				}
			

			}

			Iterator<String> it2 = xmlRadarModelManager.getFilters(radarModel).iterator();
			while (it2.hasNext()) {
				String colName = it2.next();
				Object val = obj2.get(colName);
				objOut.put(colName, val);
			}
			Iterator<String> it3 = xmlRadarModelManager.getForeignKeys(radarModel).iterator();
			while (it3.hasNext()) {
				String colName = it3.next();
				Object val = obj2.get(colName);
				objOut.put(colName, val);
			}

			
			arrayOut.add(objOut);
			if (shouldSave) {
				shouldSetItemsCoordinates = true;
				proxy.updateObject(collectionName, new BasicDBObject("id", obj2.get("id")), obj2);
			}
		}

		DBObject objOut2 = new BasicDBObject();
		if (shouldSetItemsCoordinates) {
			objOut2.put("shouldSetItemsCoordinates", "yes");
		}
		objOut2.put("points", arrayOut);
		BasicDBList arrayOut2 = new BasicDBList();
		arrayOut2.add(objOut2);
		return arrayOut2.toString();

	}

	private void processQuery(DBObject query) {
		List<String> keys = new ArrayList<String>();
		Iterator<String> it = query.keySet().iterator();
		while (it.hasNext()) {
			keys.add((String) it.next());

		}
		for (String key : keys) {
			if (key.indexOf("Regex_") > -1) {
				query.put(key.replaceAll("Regex_", ""), java.util.regex.Pattern.compile((String) query.get(key), Pattern.CASE_INSENSITIVE));
				query.removeField(key);
				System.out.println(query.toString());

			}

		}

	}

	
	
	public Object getDataJson(String collectionName, String jsonQuery, String jsonProcessing) throws Exception {
		return getDataFields( collectionName,  jsonQuery,  null,  jsonProcessing);

	}

	

	public Object getDataFields(String collectionName, String jsonQuery, String fields, String jsonProcessing) throws Exception {
		DBObject query;
		if (jsonQuery == null || jsonQuery.length() == 0) {
			query = new BasicDBObject("id", new BasicDBObject("$gt", -1));
		} else {

		}
		query = (DBObject) JSON.parse(jsonQuery);
		processQuery(query);
		List<DBObject> result =null;
		if(fields==null){
		 result = proxy.getDocuments(collectionName, query, maxDocsLimit);
		}else{
			DBObject fieldsArray = (DBObject) JSON.parse(fields);
			 result = proxy.getDocuments(collectionName, query,fieldsArray, maxDocsLimit);
		}

		if (jsonProcessing != null) {
			Object processingResult = doProcessing(jsonProcessing, result, query);
			if (processingResult != null)
				return processingResult;
		}
		return result.toString();
	}

	
	public Object getDataBetweenDates(String collectionName, String dateField, long startTime, long endTime, String jsonQuery, String jsonFields, String jsonProcessing) throws Exception {
		List<DBObject> result;

		DBObject query;
		if (jsonQuery == null || jsonQuery.length() == 0) {
			query = new BasicDBObject();
		} else {

			query = (DBObject) JSON.parse(jsonQuery);
		}
		if (!SEARCH_IN_ELASTICSEARCH ||jsonProcessing==null || jsonProcessing.indexOf("fauconnet")<0 ) {// à mieux gerer !!!!!! si pas de processing recherche dans Mongo
			Iterator it = query.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				if (key.indexOf("Regex_") > -1) {
					query.put(key.replaceAll("Regex_", ""), java.util.regex.Pattern.compile((String) query.get(key), Pattern.CASE_INSENSITIVE));
					query.removeField(key);
				}
			}

			/*
			 * DBObject dateQuery =
			 * QueryBuilder.start().put("pubDate").greaterThanEquals( new
			 * Date(startTime)).get();
			 * 
			 * 
			 * query.putAll(dateQuery);
			 */
			DBObject dateQuery = new BasicDBObject();
			if (startTime > 0)
				dateQuery.put("$gte", new Date(startTime));
			if (endTime > 0)
				dateQuery.put("$lt", new Date(endTime));

			query.put(dateField, dateQuery);

			if (jsonFields == null || jsonFields.equals("undefined")) {// ||
																		// fields.keySet().size()
																		// == 0)
																		// {
			//	System.out.println("-----------getDataBetweenDates---------Query :" + query);
				result = proxy.getDocuments(collectionName, query, maxDocsLimit);
			} else {
				DBObject fields = (DBObject) JSON.parse(jsonFields);
				result = proxy.getDocuments(collectionName, query, fields, maxDocsLimit);
			}
		} else {// -----------------------------//elasticSearch-----------------------------------------
			String index=collectionName;
			String type="feedsDAP";
			if (searchManager == null) {
				searchManager = new ElasticSearchManager();
			}
			String[] keywords = ((String) query.get("title")).split(",");

			if (jsonProcessing != null) {// histogram
				DBObject processing = (DBObject) JSON.parse(jsonProcessing);
				if (processing.keySet().size() > 0) {
					String processor = (String) processing.get("processor");
					if (processor != null) {
						if (processor.equals("com.fauconnet.rss.HistogramProcessor")) {// histogram
							String periodicity = (String) processing.get("periodicity");
							result = searchManager.searchAndAggregate(index, type,keywords, new Date(startTime), new Date(endTime), periodicity);
							return result.toString();
						}
					}
				}

			}
			result = searchManager.searchInFeedsTitles(index, type,keywords, new Date(startTime), new Date(endTime));
		}

		if (jsonProcessing != null && !jsonFields.equals("undefined")) {
			Object processingResult = doProcessing(jsonProcessing, result, null);
			if (processingResult != null)
				return processingResult;
		}
		return result.toString();

	}

	private Object doProcessing(String jsonProcessing, List<DBObject> result, DBObject query) throws Exception {
		DBObject processing = (DBObject) JSON.parse(jsonProcessing);
		if (processing.keySet().size() > 0) {
			String processor = (String) processing.get("processor");
			String method = (String) processing.get("method");
			if (processor != null) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("data", result);
				map.put("method", method);
				map.put("dataDirPath", dataDirPath);
				map.put("context", this.context);
				map.put("query", query);
				Object obj = Class.forName(processor).newInstance();
				return ((I_Processor) obj).process(map);
			}
		}
		return null;
	}

	public String getDetailedData(String collectionName, String radarModelName, int id) throws Exception {
		RadarModel radarModel = xmlRadarModelManager.getModel(radarModelName);
		DBObject query = new BasicDBObject("id", id);
		DBObject object = proxy.getOneDocument(collectionName, query);
		List<String> fields = xmlRadarModelManager.getDetailedPageFields(radarModel);
		Iterator<String> it = object.keySet().iterator();
		while (it.hasNext()) {
			String field = it.next();
			if (fields.indexOf(field) < 0)
				object.removeField(field);
		}
		return object.toString();
	}

	public String updateItem(String collectionName, String json) throws Exception {
		DBObject object = (DBObject) JSON.parse(json);
		addAdminFields(object, collectionName);
		Object id = object.get(idField);
		if (id == null)
			throw new Exception("object has nos id" + object.toString());
		DBObject query = new BasicDBObject("id", id);
		return proxy.updateObject(collectionName, query, object);
	}

	public XmlRadarModelManager getXmlModelManager() {
		return xmlRadarModelManager;

	}

	@Override
	public String updateItemByQuery(String collectionName, String jsonQuery, String jsonItem) throws Exception {
		DBObject query = new BasicDBObject();
		if (jsonQuery != null)
			query = (DBObject) JSON.parse(jsonQuery);
		DBObject item = (DBObject) JSON.parse(jsonItem);
		addAdminFields(item, collectionName);
		return proxy.updateObject(collectionName, query, item);
	}

	@Override
	public String updateItemFields(String collectionName, String jsonQuery, String jsonFields) throws Exception {

		DBObject query = new BasicDBObject();
		if (jsonQuery != null)
			query = (DBObject) JSON.parse(jsonQuery);

		DBObject fields = new BasicDBObject();
		if (jsonFields != null)
			fields = (DBObject) JSON.parse(jsonFields);
		return proxy.updateFields(collectionName, query, fields);

	}

	public DBObject addItem(String collectionName, String jsonItem) {
		DBObject object = (DBObject) JSON.parse(jsonItem);
		addAdminFields(object, collectionName);
		proxy.insert(collectionName, object);
		return object;

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

	public List<DBObject> addItems(String collectionName, String jsonItem) throws Exception {
		Charset.forName("UTF-8").encode(jsonItem);
		Object obj = JSON.parse(jsonItem);
		List<DBObject> list = (List<DBObject>) obj;
		Iterator<DBObject> it = list.iterator();
		while (it.hasNext()) {
			DBObject object = it.next();
			addAdminFields(object, collectionName);
			proxy.insert(collectionName, object);

		}
		return list;

	}

	private void addAdminFields(DBObject object, String collectionName) {
		if (object.get("id") == null) {
			object.put("id", proxy.getMaxId(collectionName) + 1);
		}
		object.put("lastModified", new Date());
		if (object.get("modifiedBy") == null)
			object.put("modifiedBy", userId);
		if (collectionName.equals("links")) {
			fillLinksNature(object);
		}
	}

	public String getDBNames() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserRights(String login, String password) {
		DBObject query = new BasicDBObject();
		query.put("login", login);
		query.put("password", password);
		DBObject obj = proxy.getOneDocument("users", query);
		if (obj != null) {
			DBObject obj2 = new BasicDBObject("login", obj.get("login"));
			obj2.put("role", obj.get("role"));
			return obj.toString();
		} else {
			return new BasicDBObject("error", "Invalid login/password").toString();
		}
	}

	public String count(String collectionName, String jsonQuery) {
		if (jsonQuery == null || jsonQuery.equals(""))
			jsonQuery = "{}";
		DBObject query = (DBObject) JSON.parse(jsonQuery);
		processQuery(query);
		return proxy.count(collectionName, query).toString();
	}

	@Override
	public String getDistinct(String collectionName, String jsonQuery, String key) throws Exception {
		if (jsonQuery == null || jsonQuery.equals(""))
			jsonQuery = "{}";
		DBObject query = (DBObject) JSON.parse(jsonQuery);
		List<String> objs= proxy.getDistinct(collectionName, query, key);
		String str2=JSON.serialize(objs);
		return str2;
	}

	/*
	 * methode spï¿½cifique pour les liens : ajout des
	 * 
	 * PROVISOIRE
	 */
	public void fillLinksNature(DBObject link) {
		if (link.get("source") == null || link.get("target") == null)
			return;
		if (link.get("sourceNature") != null && link.get("targetNature") != null)
			return;
		if (true)
			return;
		DBObject query = new BasicDBObject("label", link.get("source.label"));
		DBObject nodeS = proxy.getOneDocument("nodes", query);
		if (nodeS == null || nodeS.get("label") == null) {
		//	System.out.println("Import links : no node found for link : " + link.toString());
			return;
		}
		DBObject query2 = new BasicDBObject("label", link.get("target.label"));
		DBObject nodeT = proxy.getOneDocument("nodes", query2);
		if (nodeT == null || nodeT.get("label") == null) {
		//	System.out.println("Import links : no node found for link : " + link.toString());
			return;
		}
		link.put("sourceNature", nodeS.get("nature"));
		link.put("targetNature", nodeT.get("nature"));
		if (nodeS.get("sourceId") != null)
			link.put("sourceId", nodeS.get("id"));
		if (nodeT.get("sourceId") != null)
			link.put("targetId", nodeT.get("id"));

	}

	public String getGraphAlldescendantLinksAndNodes(int id) {
		DBObject query = new BasicDBObject("id", id);
		DBObject parentNode = proxy.getOneDocument("nodes", query);
		if (parentNode == null)
			return new BasicDBObject().toString();
		BasicDBList links = new BasicDBList();
		BasicDBList nodes = new BasicDBList();
		getRecuresiveDescendantLinksAndNodes(parentNode, (String) parentNode.get("nature"), links, nodes);
		BasicDBObject result = new BasicDBObject("links", links);
		result.put("nodes", nodes);
		return result.toString();

	}

	private void getRecuresiveDescendantLinksAndNodes(DBObject parentNode, String rootNodeNature, BasicDBList links, BasicDBList nodes) {
		BasicDBList or = new BasicDBList();
		or.add(new BasicDBObject("sourceId", parentNode.get("id")));
		or.add(new BasicDBObject("targetId", parentNode.get("id")));
		DBObject query = new BasicDBObject("$or", or);
		List<DBObject> links2 = proxy.getDocuments("links", query, -1);
		Iterator<DBObject> it = links2.iterator();
		while (it.hasNext()) {
			DBObject link = it.next();
			if (links.indexOf(link) < 0) {// on ne retient pas les liens de
											// mï¿½me
											// nature

				if (link.get("sourceId").equals(parentNode.get("id"))) {
					// System.out.println(parentNode + "   :  " + link);
					if (link.get("targetNature").equals(rootNodeNature)) {
						continue;
					} else {
						DBObject queryNodes = new BasicDBObject("id", link.get("targetId"));
						DBObject targetChildNode = proxy.getOneDocument("nodes", queryNodes);
						nodes.add(targetChildNode);
						queryNodes = new BasicDBObject("id", link.get("sourceId"));
						DBObject sourceChildNode = proxy.getOneDocument("nodes", queryNodes);
						nodes.add(sourceChildNode);
						links.add(link);
						getRecuresiveDescendantLinksAndNodes(targetChildNode, rootNodeNature, links, nodes);
					}
				}
				if (link.get("targetId").equals(parentNode.get("id"))) {
					// System.out.println(parentNode + "   :  " + link);
					if (link.get("sourceNature").equals(rootNodeNature)) {
						continue;
					} else {
						DBObject queryNodes = new BasicDBObject("id", link.get("sourceId"));
						DBObject sourceChildNode = proxy.getOneDocument("nodes", queryNodes);
						nodes.add(sourceChildNode);
						queryNodes = new BasicDBObject("id", link.get("targetId"));
						DBObject targetChildNode = proxy.getOneDocument("nodes", queryNodes);
						nodes.add(targetChildNode);
						links.add(link);
						getRecuresiveDescendantLinksAndNodes(sourceChildNode, rootNodeNature, links, nodes);
					}
				}
			}

		}

	}

	public Map toMap() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object removeField(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void putAll(Map arg0) {
		// TODO Auto-generated method stub

	}

	public void putAll(BSONObject arg0) {
		// TODO Auto-generated method stub

	}

	public Object put(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object get(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public boolean containsKey(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsField(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void markAsPartialObject() {
		// TODO Auto-generated method stub

	}

	public boolean isPartialObject() {
		// TODO Auto-generated method stub
		return false;
	}

	public String updateItems(String collectionName, String jsonItem) throws Exception {
		Object obj = JSON.parse(jsonItem);
		List<DBObject> list = (List<DBObject>) obj;
		Iterator<DBObject> it = list.iterator();
		while (it.hasNext()) {
			DBObject object = it.next();
			Object id = object.get(idField);
			if (id == null) {
				throw new Exception("object has nos id" + object.toString());
			}
			DBObject query = new BasicDBObject("id", id);
			addAdminFields(object, collectionName);
			proxy.updateObject(collectionName, query, object);

		}
		return "done";
	}

	@Override
	public void updateField(String collectionName, int id, String field, String newValue, boolean save) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String executeJavascript(String script) throws Exception {
		return proxy.executeJavascript(script).toString();

	}


}
