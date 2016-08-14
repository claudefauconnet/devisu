package com.fauconnet.devisu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;

import com.fauconnet.old.FileDataManager;
import com.fauconnet.pptx.PptxBuilderPOI;
import com.fauconnet.rss.Rss3DMongoExplorer;
import com.fauconnet.rss.VerbatimExplorer;
import com.fauconnet.xml.CustomDOMTree;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

//import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class RadarServlet
 */
public class DeVisuServlet extends HttpServlet implements I_RemoteActions {
	private static final long serialVersionUID = 1L;

	private boolean saveOnEachChange = true;
	private String xmlFilesDir = "";
	private String dataDirPath = "";
	private String xmlFilePath = "";
	private String dataManagerType;
	private String dbHost;
	private int dbPort;
	private int NloadDataManager = 0;
	Rss3DMongoExplorer rss3DExplorer = null;

	private Map<String, I_DataManager> dataManagers = new HashMap<String, I_DataManager>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */

	public void init() throws ServletException {

		try {
			dataDirPath = this.getServletContext().getRealPath("/pot/data");
			xmlFilesDir = dataDirPath;
			xmlFilePath = this.getServletContext().getRealPath("/pot/data/radars.xml");
			dataManagerType = this.getServletContext().getInitParameter("dataManagerType");
			dbHost = this.getServletContext().getInitParameter("dbHost");
			dbPort = Integer.parseInt(this.getServletContext().getInitParameter("dbPort"));
			if (dataManagerType == null) {
				String str = "no dataManagerType configured in web.xml file ";
				System.out.println(str);
				throw new ServletException(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e.toString());

		}
	}

	public DeVisuServlet() {
		super();

		// TODO Auto-generated constructor stub
	}

	
	
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String responseStr = "";
		String responseType = "";
		/*
		 * String user = (String) request.getSession().getAttribute("user"); if
		 * (user == null) { ;// response.sendRedirect("login.html"); }
		 */
		String action = request.getParameter("action");
		String dbName = request.getParameter("dbName");
		String collectionName = request.getParameter("collectionName");
		String jsonData = getData(request);
		String jsonQuery = request.getParameter("jsonQuery");
		String idStr = request.getParameter("id");
		String jsonFields = request.getParameter("jsonFields");

		int id = -1;
		if (idStr != null) {
			try {
				id = Integer.parseInt(idStr);
			} catch (Exception e) {
				System.out.println("!!!!!!!!!!cannot parseToInt :" + idStr);
				throw new ServletException(e);
			}
		}
		try {
			// System.out.println(action +"  : "+jsonQuery+ " : "+jsonData);
			if (action == null) {
				response.setHeader("Content-Type", "text/text");
				response.getWriter().write("SERVER ERROR : no action param in request");
				throw new ServletException("no action param in request");
			}
			if (action.equals("resetAllDataManagers")) {
				dataManagers.clear();
				responseStr = "DataManagers resetted";
				responseType = "text";
			}

			if (action.equals("saveData")) {
				responseStr = saveData(dbName, collectionName, jsonData);
				responseType = "text";
			} else if (action.equals("loadDataFields") || action.equals("loadData")) {
				String jsonProcessing = request.getParameter("jsonProcessing");

				if (jsonProcessing != null && jsonProcessing.length() == 0)
					jsonProcessing = null;

				Object obj = null;
				String fields = request.getParameter("fields");
				if (fields == null)
					obj = loadData(dbName, collectionName, jsonQuery, jsonProcessing);
				else
					obj = loadDataFields(dbName, collectionName, jsonQuery, fields, jsonProcessing);
				if (obj instanceof String) {
					responseStr = (String) obj;
					responseType = "json";
				} else {

					File file = (File) obj;
					response.setContentType("application/octet-stream");

					response.setContentLength((int) file.length());
					response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));

					OutputStream out = response.getOutputStream();
					try (FileInputStream in = new FileInputStream(file)) {
						byte[] buffer = new byte[4096];
						int length;
						while ((length = in.read(buffer)) > 0) {
							out.write(buffer, 0, length);
						}
					}
					out.flush();
					return;
				}

			} else if (action.equals("getDataBetweenDates")) {
				String dateField = request.getParameter("dateField");
				String startTimeStr = request.getParameter("startTime");
				String endTimeStr = request.getParameter("endTime");
				String jsonProcessing = request.getParameter("jsonProcessing");

				if (jsonProcessing != null && jsonProcessing.length() == 0)
					jsonProcessing = null;

				responseStr = (String) getDataBetweenDates(dbName, collectionName, dateField, Long.parseLong(startTimeStr), Long.parseLong(endTimeStr), jsonQuery, jsonFields, jsonProcessing);

				responseType = "json";
			} else if (action.equals("updateItem")) {
				responseStr = updateItem(dbName, collectionName, jsonData);
				responseType = "text";
			} else if (action.equals("updateItems")) {
				responseStr = updateItems(dbName, collectionName, jsonData);
				responseType = "text";
			} else if (action.equals("updateItemByQuery")) {
				responseStr = updateItemByQuery(dbName, collectionName, jsonQuery, jsonData);
				responseType = "text";

			} else if (action.equals("updateItemFields")) {

				responseStr = updateItemFields(dbName, collectionName, jsonQuery, jsonFields);
				responseType = "text";
			}

			else if (action.equals("addItem")) {
				responseStr = addItem(dbName, collectionName, jsonData);
				responseType = "json";

			} else if (action.equals("addItems")) {
				responseStr = addItems(dbName, collectionName, jsonData);
				responseType = "json";

			} else if (action.equals("deleteItem")) {
				responseStr = deleteItem(dbName, collectionName, id);
				responseType = "text";
			} else if (action.equals("deleteItemByQuery")) {
				responseStr = deleteItemByQuery(dbName, collectionName, jsonQuery);
				responseType = "text";
			}

			else if (action.equals("count")) {
				responseStr = "" + count(dbName, collectionName, jsonQuery);
				responseType = "json";
			} else if (action.equals("getDistinct")) {
				String key = request.getParameter("key");
				responseStr = getDistinct(dbName, collectionName, jsonQuery, key);
				responseType = "json";

			} else if (action.equals("getGroupStat")) {
				String field = request.getParameter("field");
				String operator = request.getParameter("operator");
				responseStr = "" + getGroupStat(dbName, collectionName, jsonQuery, operator, field);
				responseType = "json";
			}

			// *******************specific to
			// Graph***********************************
			else if (action.equals("getGraphAlldescendantLinksAndNodes")) {
				responseStr = "" + getGraphAlldescendantLinksAndNodes(dbName, id);
				responseType = "json";
			}

			// *******************specific to
			// Radar***********************************
			else if (action.equals("updateRadarCoordinates")) {
				String coordx = request.getParameter("coordx");
				String coordy = request.getParameter("coordy");
				responseStr = updateRadarCoordinates(dbName, collectionName, id, coordx, coordy);
				responseType = "text";
			}

			/*
			 * if (action.equals("updateItemJsonFromRadar")) {// DANGERUX pour
			 * les champs qui ne sont pas dans le radar responseStr =
			 * updateItemJsonFromRadar(dbName,collectionName, id, jsonData);
			 * responseType = "text"; }
			 */

			else if (action.equals("getRadarPoints")) {
				String radarName = request.getParameter("radarName");
				responseStr = getRadarPoints(dbName, radarName, collectionName, jsonQuery);
				responseType = "json";
			}

			else if (action.equals("getRadarDetails")) {
				String radarName = request.getParameter("radarName");
				responseStr = getRadarDetails(dbName, radarName, id);
				responseType = "json";

			} else if (action.equals("updateRadarComment")) {
				String comment = request.getParameter("comment");
				responseStr = updateRadarComment(dbName, collectionName, id, comment);
				responseType = "text";

			} else if (action.equals("addNewRadarItem")) {
				responseStr = addNewRadarItem(dbName);
				responseType = "json";

			}

			else if (action.equals("tryLogin")) {
				String login = request.getParameter("login");
				String password = request.getParameter("password");
				responseStr = tryLogin(dbName, login, password);
				responseType = "json";
			}

			// generate powerpoint*****************// A FINIR
			else if (action.equals("createTechnosPowerpoint")) {
				PptxBuilderPOI pptBuilder = new PptxBuilderPOI();
				// pptBuilder.createTechnosPowerpoint(templatePath, targetPath,
				// query);
			}

			// *******************admin***********************************

			else if (action.equals("createDB")) {
				createDB(dbName);
				responseType = "text";
			} else if (action.equals("getDBNames")) {
				responseStr = getDBNames(dbName);
				responseType = "json";
			} else if (action.equals("getCollectionNames")) {
				responseStr = getCollectionNames(dbName);
				responseType = "json";
			}

			// **************************sql**************************************
			else if (action.equals("execSql")) {
				String sqlConn = request.getParameter("sqlConn");
				String sqlRequest = request.getParameter("sqlRequest");
				responseStr = execSql(dbName, sqlConn, sqlRequest);
				responseType = "json";
			}
			// **************************javascript**************************************
			else if (action.equals("executeJavascript")) {
				String script = request.getParameter("script");
				responseStr = executeJavascript(dbName, script);
				responseType = "json";
			}

			// **************************rss3d
			// feeds**************************************
			else if (action.equals("aggregateFeeds")) {
				String expression = request.getParameter("expression");
				String periodType = request.getParameter("periodType");
				responseStr = aggregateFeeds(expression, periodType, jsonQuery);
				responseType = "json";
			} else if (action.equals("getFeeds")) {
				String expression = request.getParameter("expression");
				String periodType = request.getParameter("periodType");
				String limitStr = request.getParameter("limit");
				int limit = 1000;
				if (limitStr != null)
					limit = Integer.parseInt(limitStr);
				responseStr = getFeeds(expression, jsonQuery, limit);
				responseType = "json";
			}
		 else if (action.equals("getVerbatimsTagCloud")) {
			 responseStr =VerbatimExplorer.getTags();
			 responseType = "json";
		}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("SERVER ERROR :" + e.getMessage());
			/*
			 * responseStr = "SERVER ERROR :" + e.getMessage(); responseType =
			 * "text";
			 */

		}
		String contentType = "text/plain";
		if (responseType.equals("json"))
			contentType = "application/json";
		else if (responseType.equals("xml"))
			contentType = "application/xml";
		else if (responseType.equals("text")) {
			contentType = "application/json";
			responseStr = "{\"OK\":\"" + responseStr + "\"}";
		}

		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Type", contentType);
		// URLEncoder.encode(responseStr,"UTF-8");
		// responseStr=responseStr.replace(";", ",");

		response.getWriter().write(responseStr);
		//System.out.println(responseStr);
	}

	// *********************general CRUD operations**************************

	private String getData(HttpServletRequest request) throws ServletException, UnsupportedEncodingException {
		// System.out.println ("!-----------!"+Charset.defaultCharset());
		String method = request.getMethod();
		if (method.equals("POST")) {
			// printRequestParams( request);
			StringBuffer jb = new StringBuffer();
			String line = null;
			try {
				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					jb.append(line);
				if (jb.length() > 0)
					return jb.toString();
				return request.getParameter("jsonData");
			} catch (Exception e) {
				e.printStackTrace();
				throw new ServletException(e.toString());
			}

		} else {
			String str = request.getParameter("jsonData");
			if (str == null)
				return null;
			return str;
			/*
			 * String str1=new String(str.getBytes("iso-8859-1"));
			 * 
			 * 
			 * //return str1; return new String(str.getBytes("iso-8859-1"),
			 * "UTF-8"); // return request.getParameter("jsonData");
			 */
		}
	}

	private void printRequestParams(HttpServletRequest request) {
		Iterator it = request.getParameterMap().keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			System.out.println(key + "  :  " + request.getParameterMap().get(key).toString());
		}
	}

	public Object loadData(String dbName, String collectionName, String jsonQuery, String jsonProcessing) throws Exception {
		return getDataManager(dbName).getDataJson(collectionName, jsonQuery, jsonProcessing);

	}
	

	public Object loadDataFields(String dbName, String collectionName, String jsonQuery, String fields, String jsonProcessing) throws Exception {
		return getDataManager(dbName).getDataFields(collectionName, jsonQuery,fields, jsonProcessing);
	}


	public Object getDataBetweenDates(String dbName, String collectionName, String dateField, long startTime, long endTime, String jsonQuery, String jsonFields, String jsonProcessing)
			throws Exception {
		return getDataManager(dbName).getDataBetweenDates(collectionName, dateField, startTime, endTime, jsonQuery, jsonFields, jsonProcessing);

	}

	public String saveData(String dbName, String collectionName, String jsonItems) throws Exception {
		jsonItems = jsonItems.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
		jsonItems = URLDecoder.decode(jsonItems, "UTF-8");
		getDataManager(dbName).saveData(collectionName, jsonItems, null);
		return "data saved";

	}

	public String addItem(String dbName, String collectionName, String jsonItem) throws Exception {
		DBObject newItem = getDataManager(dbName).addItem(collectionName, jsonItem);
		return "{\"status\":\"item added\",\"id\":" + newItem.get("id") + "}";
	}

	public String addItems(String dbName, String collectionName, String jsonItem) throws Exception {
		List<DBObject> newItems = getDataManager(dbName).addItems(collectionName, jsonItem);
		return "{\"status\":\"item added\",\"size\":" + newItems.size() + "}";
	}

	public String updateItem(String dbName, String collectionName, String jsonItem) throws Exception {
		getDataManager(dbName).updateItem(collectionName, jsonItem);
		return "item updated";
	}

	public String updateItemByQuery(String dbName, String collectionName, String jsonQuery, String jsonItem) throws Exception {
		getDataManager(dbName).updateItemByQuery(collectionName, jsonQuery, jsonItem);
		return "item updated";
	}

	@Override
	public String updateItemFields(String dbName, String collectionName, String jsonQuery, String jsonFields) throws Exception {
		getDataManager(dbName).updateItemFields(collectionName, jsonQuery, jsonFields);
		return "fields updated";
	}

	public String updateItems(String dbName, String collectionName, String jsonData) throws Exception {
		getDataManager(dbName).updateItems(collectionName, jsonData);
		return "items updated";
	}

	public String deleteItem(String dbName, String collectionName, int id) throws Exception {
		if (id < 0)
			throw new ServletException("No valid id parameter in query");
		getDataManager(dbName).deleteItem(collectionName, id);
		return "item deleted";
	}

	@Override
	public String deleteItemByQuery(String dbName, String collectionName, String jsonQuery) throws Exception {
		getDataManager(dbName).deleteItemByQuery(collectionName, jsonQuery);
		return "item deleted";
	}

	@Override
	public String getDistinct(String dbName, String collectionName, String jsonQuery, String key) throws Exception {
		return getDataManager(dbName).getDistinct(collectionName, jsonQuery, key);
	}

	/******************************* tree Specific operations ****************************/
	public String getGraphAlldescendantLinksAndNodes(String dbName, int id) throws Exception {
		return getDataManager(dbName).getGraphAlldescendantLinksAndNodes(id);
	}

	/******************************* radar Specific operations ****************************/

	public String updateRadarCoordinates(String dbName, String collectionName, int id, String coordx, String coordy) throws Exception {

		getDataManager(dbName).updateField(collectionName, id, "x", coordx, false);
		getDataManager(dbName).updateField(collectionName, id, "y", coordy, saveOnEachChange);
		return "coordinates updated";
	}

	/*
	 * public String updateItemJsonFromRadar(String dbName,String
	 * collectionName, int id, String jsonItem) throws Exception { if (id < 0)
	 * throw new ServletException("No id parameter in query");
	 * getDataManager(dbName).updateItemFromRadar(collectionName, id, jsonItem);
	 * return "item updated";
	 * 
	 * }
	 */

	public String getRadarPoints(String dbName, String radarName, String collectionName, String jsonQuery) throws Exception {
		return getDataManager(dbName).getRadarJsonData(radarName, collectionName, jsonQuery);
	}

	public String getRadarDetails(String dbName, String radarName, int id) throws Exception {

		return getDataManager(dbName).getDetailedData(radarName, "radarDetails", id);
	}

	public String updateRadarComment(String dbName, String collectionName, int id, String comment) throws Exception {
		// System.out.println("----1 comment----" + comment);
		if (comment == null || comment.length() == 0)
			return "";
		comment = URLDecoder.decode(comment, "UTF-8");

		// separation du link []
		String link = null;
		int p = comment.indexOf("[");
		int q = comment.indexOf("]");
		if (p > -1 && q > -1) {
			link = comment.substring(p + 1, q);
			comment = comment.replace("[" + link + "]", "");

		}
		// separation du symbole <>
		String symbol = null;
		p = comment.indexOf("<");
		q = comment.indexOf(">");
		if (p > -1 && q > -1) {
			symbol = comment.substring(p + 1, q);
			comment = comment.replace("<" + symbol + ">", "");

		}
		if (id > -1) {

			getDataManager(dbName).updateField(collectionName, id, "comment", comment, false);
			getDataManager(dbName).updateField(collectionName, id, "link", link, false);
			getDataManager(dbName).updateField(collectionName, id, "symbol", symbol, saveOnEachChange);

		}
		return "comment updated";
	}

	public String addNewRadarItem(String dbName) throws Exception {
		String newId = getDataManager(dbName).addRadarItem("radar");
		return "{newId:" + newId + "}";
	}

	public String tryLogin(String dbName, String login, String password) throws Exception {
		return getDataManager(dbName).getUserRights(login, password);
	}

	public String getCollectionNames(String dbName) throws Exception {
		return getDataManager(dbName).getCollectionNames(dbName);
	}

	public String getDBNames(String dbName) throws Exception {
		return getDataManager(dbName).getDBNames(dbName);
	}

	public I_DataManager getDataManager(String dbName) throws Exception {
		I_DataManager dataManager = (I_DataManager) dataManagers.get(dbName);
		if (dataManager == null) {
			dataDirPath = dataDirPath.endsWith(File.separator) ? dataDirPath : dataDirPath + File.separator;

			if (dataManagerType.equals("mongo")) {
				dataManager = new MongoDataManager(dbName, dataDirPath, dbHost, dbPort);
				dataManager.setContext(this.getServletContext());

				// Il faut attendre que le fichier XML soit enregistr�
				/*
				 * while (NloadDataManager++ < 10 &&
				 * dataManager.getXmlModelManager() == null) {
				 * Thread.sleep(500); dataManager = null;
				 * getDataManager(dbName); }
				 */

			} else if (dataManagerType.equals("file")) {
				dataManager = new FileDataManager(dbName, dataDirPath);
			}
			dataManagers.put(dbName, dataManager);
		}

		return dataManager;
	}

	public String saveRadarXml(String dbName, String xml, HttpServletRequest request) throws Exception {
		System.out.println("*****************see FileUploadServelt for implementation*************");
		return null;
	}

	public void createDB(String dbName) throws Exception {
		getDataManager(dbName).createDB(dbName);

	}

	public String count(String dbName, String collectionName, String jsonQuery) throws Exception {
		return getDataManager(dbName).count(collectionName, jsonQuery);

	}

	@Override
	public String getGroupStat(String dbName, String collectionName, String jsonQuery, String operator, String field) throws Exception {
		return "" + getDataManager(dbName).getGroupStat(collectionName, jsonQuery, operator, field);
	}

	public String execSql(String dbName, String sqlConn, String sqlRequest) throws Exception {
		String query = "{\"type\":\"sqlConn\",\"name\":\"" + sqlConn + "\"}";
		String connJson = (String) getDataManager(dbName).getDataJson("admin", query, null);
		if (connJson == null)
			throw new Exception("connection not found :" + sqlConn);
		BasicDBList conObjList = (BasicDBList) JSON.parse(connJson);
		if (conObjList.size() == 0)
			return new BasicDBList().toString();
		DBObject conObj = (DBObject) conObjList.get(0);
		SqlProxy sqlProxy = new SqlProxy((String) conObj.get("driver"), (String) conObj.get("url"), (String) conObj.get("login"), (String) conObj.get("password"));

		return sqlProxy.executeReadQuery(sqlRequest).toString();
	}

	@Override
	public String executeJavascript(String dbName, String script) throws Exception {
		return getDataManager(dbName).executeJavascript(script);

	}

	/***************************** RSS3D **********************************************/
	public String aggregateFeeds(String expression, String periodType, String query) throws Exception {
		if (rss3DExplorer == null) {
			rss3DExplorer = new Rss3DMongoExplorer(dbHost, dbPort, "rss3d");
		}
		return rss3DExplorer.aggregateFeeds(expression, periodType, query);

	}

	public String getFeeds(String expression, String jsonQuery, int limit) throws Exception {
		if (rss3DExplorer == null) {
			rss3DExplorer = new Rss3DMongoExplorer(dbHost, dbPort, "rss3d");
		}
		return rss3DExplorer.getFeeds(expression, jsonQuery, limit);
	}

	
	/*
	 * private String replacer(String data) {
	 * 
	 * try { data = data.replaceAll("%(?![0-9a-fA-F]{2})", "%25"); data =
	 * data.replaceAll("\\+", "%2B"); data = URLDecoder.decode(data, "utf-8"); }
	 * catch (Exception e) { e.printStackTrace(); } return data; }
	 */

}
