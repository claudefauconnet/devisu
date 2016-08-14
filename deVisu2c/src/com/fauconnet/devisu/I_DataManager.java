package com.fauconnet.devisu;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import com.mongodb.DBObject;



public interface I_DataManager {
	public abstract Object getDataJson(String collectionName,String jsonQuery,String jsonProcessing) throws Exception;
	
	public abstract Object getDataFields(String collectionName,String jsonQuery,String fields, String jsonProcessing) throws Exception;
	
	public abstract void updateField(String collectionName,int id, String field, String newValue, boolean save) throws Exception;

	public abstract void saveData(String collectionName,String jsonItems, String fileName) throws Exception;

	public abstract DBObject addItem(String collectionName, String jsonItem) throws Exception;
	
	public abstract List<DBObject> addItems(String collectionName, String jsonItem) throws Exception;

	public abstract void deleteItem(String collectionName,int id) throws Exception;
	
	public  abstract void deleteItemByQuery(String collectionName,  String jsonQuery) throws Exception;
	
	public abstract String updateItem(String collectionName,String jsonItem) throws Exception;
	
	public abstract String updateItemByQuery(String collectionName, String jsonQuery, String jsonItem) throws Exception;
	
	public abstract String updateItemFields(String collectionName, String jsonQuery,String jsonFields) throws Exception;
	
	public abstract String updateItems(String collectionName,String jsonItem) throws Exception;
	
	public abstract String count(String collectionName,String jsonQuery) throws Exception;
	
	public abstract String getDistinct(String collectionName, String jsonQuery,String key)  throws Exception;
	
	public abstract  Object getDataBetweenDates(String collectionName,String dateField, long startTime,long endTime, String jsonQuery, String jsonFields,String jsonProcessing) throws Exception;
	
	public abstract float getGroupStat(String collectionName, String jsonQuery,String operator, String field)throws Exception ;
		
	public abstract String executeJavascript(String script)   throws Exception;
	
	
	// ***************************Radar***********************************************
	
	



	
	
	

	public abstract String getRadarJsonData(String radarModelName,String collectionName,String jsonQuery);
	
	
	public abstract void updateItemFromRadar(String collectionName,int id, String jsonItem) throws Exception;
	
	public abstract String addRadarItem(String collectionName)  throws Exception;
	
	
	
	public abstract XmlRadarModelManager getXmlModelManager() throws Exception;


	public abstract String getDetailedData(String radarModelName,String collectionName,int id) throws Exception;

	
	// *****************************others**************************************************
	public  abstract void createDB(String dbName) throws Exception;
	
	public abstract String getCollectionNames(String dbName) throws Exception ;

	public abstract String getDBNames(String dbName) throws Exception ;

	public abstract String getUserRights(String login, String password)  throws Exception ;
	

	// ***************************Graph***********************************************
		
	public abstract String getGraphAlldescendantLinksAndNodes(int id)  throws Exception ;

	public abstract void setContext(ServletContext servletContext);

	
	
	
	
}

