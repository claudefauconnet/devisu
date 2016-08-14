package com.fauconnet.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Element;

import com.fauconnet.devisu.I_DataManager;
import com.fauconnet.devisu.XmlRadarModelManager;

public class FileDataManager implements I_DataManager {
	private String dirRootPath;
	private String dataDirPath;
	private Element radarElt;
	private File jsonFile;
	private JSONArray dataJsonCache;
	private Map<String, String> radarRoles = new HashMap<String, String>();
	private XmlRadarModelManager xmlModelManager;
	private String idColumnName = "";
	private boolean idIsInt = true;
	private long maxId;

	private static JsonFactory factory = new JsonFactory();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {

			// DataManager manager= new DataManager(tree,"IGEO-BIG-DATA","");
			I_DataManager manager = new FileDataManager("IGEO-BIG-DATA", "C:\\Local\\workspace\\radar2\\WebContent\\IGEO-BIG-DATA.json");
			// manager.addConstantElements(tree.getFirstElement("radar", null),
			// tree);
			manager.getRadarJsonData(null,null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public FileDataManager(String dbName, String dataDirPath) throws Exception {
		xmlModelManager = new XmlRadarModelManager(dbName, dataDirPath);
		this.dataDirPath = dataDirPath;
		String jsonPath = dataDirPath + dbName + ".json";

		this.jsonFile = new File(jsonPath);
		if (!jsonFile.exists())
			this.jsonFile.createNewFile();
	}

	public FileDataManager(String jsonFilePath) {
		this.jsonFile = new File(jsonFilePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fauconnet.radar.I_DataManager#updateField(long,
	 * java.lang.String, java.lang.String, boolean)
	 */
	public void updateField(String collectionName,int id, String field, String newValue, boolean save) throws Exception {
		// System.out.println(field+" ---"+newValue);
		if (dataJsonCache == null)
			getDataJson(null,null,true);
		if (newValue != null) {

			for (int i = 0; i < dataJsonCache.size(); i++) {
				JSONObject obj = (JSONObject) dataJsonCache.get(i);
				Object obj2 = obj.get(idColumnName);
				String field2 = radarRoles.get(field);
				if (field2 == null)
					field2 = field;
				if ((Integer)obj2==id || obj2.equals(id) || obj2.equals("" + id)) {
					if (newValue.equals("")) {
						obj.put(field2, newValue);
					} else if (newValue.matches("[\\-0-9]*")) {// int
						obj.put(field2, Integer.parseInt(newValue));
					} else if (newValue.matches("[\\-0-9.]*")) {// numerique
						obj.put(field2, Float.parseFloat(newValue));
					} else
						obj.put(field2, newValue);
					break;
				}

			}
		}
		if (save) {
			saveData(null,dataJsonCache.toString(), null);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fauconnet.radar.I_DataManager#getDataJson(boolean)
	 */
	public String getDataJson(String collectionName,String query, boolean reload) {
		// TODO Auto-generated method stub
		if (reload || dataJsonCache == null) {
			StringBuffer sb = new StringBuffer();

			BufferedReader br;
			try {

				br = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile), "UTF-8"));
				String line = "";
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
					System.out.println("---line :" + line);
				}
				br.close();
				dataJsonCache = (JSONArray) JSONValue.parse(sb.toString());
				return sb.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}
		} else
			return dataJsonCache.toString();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fauconnet.radar.I_DataManager#saveData(java.lang.String,
	 * java.lang.String)
	 */
	public void saveData(String collectionName,String json, String fileName) {
		if (json == null)
			json = dataJsonCache.toJSONString();
		try {
			File f = jsonFile;
			if (fileName != null) {
				fileName = dataDirPath + fileName;
				f = new File(fileName);
			}
			FileWriter fw = new FileWriter(f);
			// System.out.println("---json--- :" + json);
			fw.write(json);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fauconnet.radar.I_DataManager#updateItemFromRadar(java.lang.String,
	 * java.lang.String)
	 */
	public void updateItemFromRadar(String collectionName,int id, String jsonStr) throws Exception{
		JSONObject newJsonObj = (JSONObject) JSONValue.parse(jsonStr);
		JSONObject storedObj = getJsonObjFromCacheById(id);
		// System.out.println(oldObj);

		if (newJsonObj != null) {// update
			// JSONObject obj2=(JSONObject)obj.clone();//pb iterator concurrenty
			// lors de la modif java.util.ConcurrentModificationException
			String realKey = null;
			String value = null;
			Iterator<Object> it3 = newJsonObj.keySet().iterator();
			while ((it3.hasNext())) {
				String key = (String) it3.next();
				if (key == null)
					continue;
				if (key.equals("id"))
					continue;
				// System.out.println(key);
				if (key == null)
					continue;
				if (key.equals("id"))
					continue;
				if (key.equals("null"))
					continue;
				// System.out.println(key);
				realKey = radarRoles.get(key);

				if (realKey == null)
					realKey = key;

				// else
				// System.out.println(key + "  " + realKey+ "  "+value);
				value = "" + newJsonObj.get(key);
				if (realKey.equals("mutualisable"))
					realKey = key;

				updateField(null,id, key, value, false);
			}
			// System.out.println(newJsonObj);
			// System.out.println(storedObj);
			saveData( null,dataJsonCache.toJSONString(), null);
		}
		return;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fauconnet.radar.I_DataManager#addItem()
	 */
	public void addRadarItem(String collectionName) {
		JSONObject obj = new JSONObject();
		obj.put("name", "??");
		obj.put("id", maxId + 1);
		obj.put("x", 10);
		obj.put("y", 10);
		dataJsonCache.add(obj);
		saveData(null,null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fauconnet.radar.I_DataManager#deleteItem(java.lang.String)
	 */
	public void deleteItem(String collectionName,int id) {
		for (int i = 0; i < dataJsonCache.size(); i++) {
			JSONObject obj2 = (JSONObject) dataJsonCache.get(i);
			if (("" + obj2.get("id")).equals(id)) {
				dataJsonCache.remove(obj2);
				saveData(null,null, null);
				return;
			}
		}

	}
	
	public String updateItem(String collectionName, String json) throws Exception {
		System.out.println(" updateItem not implemented yet in FileDataManager");
		throw new Exception (" updateItem not implemented yet in FileDataManager");
	
		
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fauconnet.radar.I_DataManager#getRadarJsonData()
	 */
	public String getRadarJsonData(String collectionName) {
		boolean save = false;
		String json = getDataJson(null,null,true);
		System.out.println(json);
		Object obj = JSONValue.parse(json);
		JSONArray array = (JSONArray) obj;
		JSONArray arrayOut = new JSONArray();
		int k = 0;
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj2 = (JSONObject) array.get(i);
			JSONObject objOut = new JSONObject();
			
			objOut.put("action", "loadJSON"); // / a enlever


			Map<String, String> radarRoles = xmlModelManager.getRadarRoles();
			Iterator<String> it = radarRoles.keySet().iterator();
			while (it.hasNext()) {
				String role = it.next();
				String colName = radarRoles.get(role);
				Object val = obj2.get(colName);

				if (role.equals("id")) {

					if (val == null) {
						save = true;
						val = maxId++;// 0 d�conne ensuite...
						obj2.put("id", val);
					} else {
						idIsInt = true;// ((String)val).matches("[0-9]*");
						try {
							maxId = Math.max(maxId, ((Long) val).longValue());
						} catch (Exception e) {
							System.out.println("  pb parse " + val + "  " + e.toString());
						}
					}
					objOut.put("id", val);

				} else if (role.equals("x")) {

					if (val == null) {
						save = true;
						val = (k * 5);
						obj2.put("x", val);
					}
					objOut.put("x", val);

				} else if (role.equals("y")) {

					if (val == null) {
						save = true;
						val = (k * 5);
						obj2.put("y", val);
					}
					objOut.put("y", val);

				} else if (val != null) {
					objOut.put(role, val);

				}

			}
			
			Iterator<String> it2=xmlModelManager.getFilters().iterator();
			while(it2.hasNext()){
				String colName=it2.next();
				Object val = obj2.get(colName);
				objOut.put(colName, val);
			}
			
			
			k++;
			arrayOut.add(objOut);

		}
		


		
		
		

		if (save) {
			dataJsonCache = array;
			String jsonStr = JSONValue.toJSONString(dataJsonCache);
			saveData(null,jsonStr, null);
		}

		JSONObject objOut2 = new JSONObject();
		objOut2.put("retour", arrayOut);
		JSONArray arrayOut2 = new JSONArray();
		arrayOut2.add(objOut2);
		return arrayOut2.toString();

		// System.out.println(itemsMap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fauconnet.radar.I_DataManager#getDataJsonCache()
	 */
	private JSONArray getDataJsonCache( ) {
		return dataJsonCache;
	}

	
	private void setDataJsonCache(JSONArray dataJsonCache) {
		this.dataJsonCache = dataJsonCache;
	}

	
	private JSONObject getJsonObjFromCache(String key, Object value) {

		for (int i = 0; i < dataJsonCache.size(); i++) {
			JSONObject val2 = new JSONObject();
			val2.put("val", value);

			JSONObject obj = (JSONObject) dataJsonCache.get(i);

			if (obj.get(key).equals(val2.get("val")))
				return obj;
			if (obj.get(key) == (val2.get("val")))
				return obj;

			/*
			 * System.out.println(key +"  "+value+"  "+obj.get(key)); if
			 * (value.getClass()==String.class){ if(obj.get(key)!=null &&
			 * obj.get(key).equals(value)) return obj; } else{ Number
			 * number=(Number)obj.get(key); Double number2=Double.parsvalue;
			 * if(number==null && number.doubleValue().equals(Number.)) return
			 * obj; }
			 */

		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fauconnet.radar.I_DataManager#getJsonObjFromCacheById(java.lang.String
	 * )
	 */
	private JSONObject getJsonObjFromCacheById(int id) {
		if (dataJsonCache == null)
			getDataJson(null,null,true);
		for (int i = 0; i < dataJsonCache.size(); i++) {

			JSONObject obj = (JSONObject) dataJsonCache.get(i);

			if (Integer.valueOf("" + obj.get("id")) ==id)
				return obj;

		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fauconnet.radar.I_DataManager#getDetailedData(java.lang.String)
	 */
	public String getDetailedData(String collectionName,int id) throws Exception {
		JSONObject obj = getJsonObjFromCacheById(id);
		if (obj == null)
			throw new Exception("Object with id " + id + " not found");

		JSONObject objOut = new JSONObject();
		objOut.put("permissionEdit", "true");
		objOut.put("action", "loadJSON");
		objOut.put("group", "eNovation");
		Iterator<Element> it = xmlModelManager.getTree().getElements("field", radarElt).iterator();

		while (it.hasNext()) {
			Element fieldElt = it.next();
			// String colName = "col" + k;
			String colName = fieldElt.getAttribute("name");
			// System.out.println(colName);
			String toDisplay = fieldElt.getAttribute("displayInDetailsPage");
			if (toDisplay == null)
				toDisplay = fieldElt.getAttribute("radarRole");
			if (toDisplay != null) {// && toDisplay.equals("true")){
				Object val = obj.get(colName);
				if (val == null)
					val = "";
				objOut.put(colName, val);
				objOut.put(colName, val);
				radarRoles.put(colName, colName);

			}

		}
		return objOut.toJSONString();
	}
	public String loadDataFile(String fileName) throws Exception {

		String str = dataDirPath + File.separator + fileName;
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(str), "UTF-8"));
		String line;

		while ((line = br.readLine()) != null) {
			// System.out.println(line);
			sb.append(line + "");
		}
		br.close();
		return sb.toString();

	}

	public void saveDataFile(String data, String fileName) throws Exception {

		// fileName="X"+fileName;
		String str = dataDirPath + File.separator + fileName;

		FileWriter fw = new FileWriter(new File(str));
		fw.write(data);
		fw.close();
		/*
		 * FileOutputStream fos=new FileOutputStream(fileName); byte[] out =
		 * UnicodeUtil.convert(data.getBytes("UTF-16"), "UTF-8");
		 * fos.write(out); fos.close();
		 */
		System.out.println("---file SAVED--- :" + str);

	}

	public  void mergeDataFile(String data, String fileName) throws Exception {

		// fileName="X"+fileName;
		String str = dataDirPath + File.separator + fileName;

		ObjectMapper mapper = new ObjectMapper(factory);
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		File oldFile=new File(str);
		List<Object> oldData;
		if(oldFile.exists())
			oldData= mapper.readValue(new FileInputStream(oldFile), List.class);
		else
			oldData=new ArrayList<Object>();
		List<Object> newData = mapper.readValue(data, List.class);

		Map<Integer, Object> oldDataMap = new HashMap<Integer, Object>();
		Iterator<Object> it1 = oldData.iterator();
		while (it1.hasNext()) {
			Object obj = it1.next();
			oldDataMap.put(obj.hashCode(), obj);
		}

		Map<Integer, Object> newDataMap = new HashMap<Integer, Object>();
		Iterator<Object> it2 = newData.iterator();
		while (it2.hasNext()) {
			Object obj = it2.next();
			newDataMap.put(obj.hashCode(), obj);
		}

		// on enleve dans odlData ceux qui ne sont plus dans new Data
		Iterator<Integer> it3 = oldDataMap.keySet().iterator();
		Set<Integer> keysToRemove = new HashSet<Integer>();
		while (it3.hasNext()) {
			Integer key = it3.next();
			Object obj = newDataMap.get(key);
			if (obj == null)
				keysToRemove.add(key);

		}

		// on ajoute dans odlData ceux qui sont nouveaux dans newData
		Iterator<Integer> it4 = newDataMap.keySet().iterator();
		while (it4.hasNext()) {
			Integer key = it4.next();
			Object obj = oldDataMap.get(key);
			if (obj == null) {
				Object obj0 = newDataMap.get(key);
				oldDataMap.put(key, obj0);
			}
		}

		// on sauve old data mis à jour
		List<Object> dataUpdated = new ArrayList<Object>();
		Iterator<Integer> it5 = oldDataMap.keySet().iterator();
		while (it5.hasNext()) {
			Integer key = it5.next();
			if (!keysToRemove.contains(key))
				dataUpdated.add(oldDataMap.get(key));
		}
		ObjectMapper mapper2 = new ObjectMapper();
		FileWriter fw = new FileWriter(str);
		mapper.writeValue(fw, dataUpdated);

	}

	public XmlRadarModelManager getXmlModelManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRadarJsonData(String collectionName, String jsonQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addItem(String collectionName, String jsonItem) throws Exception{
		JSONObject obj = (JSONObject)new JSONParser().parse(jsonItem);
		dataJsonCache.add(obj);
		saveData(null,null, null);

		
	}

	public void createDB(String dbName) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public String getCollectionNames() throws Exception {
		// TODO Auto-generated method stub
		return "{}";
	}

	public String getDBNames() throws Exception {
		// TODO Auto-generated method stub
		return "{}";
	}



	
}
