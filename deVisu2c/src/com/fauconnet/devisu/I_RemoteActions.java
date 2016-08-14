package com.fauconnet.devisu;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.mongodb.DBObject;

public interface I_RemoteActions {

	// ************** general CRUD*********************************************
	public Object loadData(String dbName, String collectionName, String jsonQuery,String jsonProcessing) throws Exception;
	
	public Object loadDataFields(String dbName, String collectionName, String jsonQuery, String fields,String jsonProcessing) throws Exception;
	
	public  Object getDataBetweenDates(String dbName, String collectionName,String dateField, long startTime,long endTime, String jsonQuery, String jsonFields,String jsonProcessing) throws Exception;

	public String count(String dbName,String collectionName,String jsonQuery) throws Exception;
	
	public  String getDistinct(String dbName,String collectionName, String jsonQuery,String key)  throws Exception;
	
	public String saveData(String dbName, String collectionName, String jsonItems) throws Exception;

	public String addItem(String dbName, String collectionName, String jsonItem) throws Exception;
	
	public String addItems(String dbName, String collectionName, String jsonItem) throws Exception ;

	public String updateItem(String dbName, String collectionName, String jsonItem) throws Exception;
	
	public String updateItems(String dbName, String collectionName, String jsonData) throws Exception;
	
	public  String updateItemByQuery(String dbName,String collectionName, String jsonQuery, String jsonItem) throws Exception;
	
	public  String updateItemFields(String dbName,String collectionName, String jsonQuery,String jsonFields) throws Exception;
	
	public String deleteItem(String dbName, String collectionName, int id) throws Exception;
	
	public  String deleteItemByQuery(String dbName, String collectionName,  String jsonQuery) throws Exception;
	
	public String getGroupStat(String dbName,String collectionName, String jsonQuery,String operator, String field)throws Exception;
		
	
	// ***************************Graph***********************************************
		public abstract String getGraphAlldescendantLinksAndNodes(String dbName,int id)  throws Exception ;


	// ***************************Radar***********************************************
	public String addNewRadarItem(String dbName) throws Exception;

	public String updateRadarCoordinates(String dbName,String collectionName, int id, String coordx, String coordy) throws Exception;

	/*public String updateItemJsonFromRadar(String dbName,String collectionName, int id, String jsonItem) throws Exception;*/

	public String getRadarPoints(String dbName,String radarName,String collectionName, String jsonQuery) throws Exception;

	public String getRadarDetails(String dbName,String radarName, int id) throws Exception;


	// *****************************others**************************************************

	
	public String saveRadarXml(String dbName, String xml, HttpServletRequest request) throws Exception;

	public String tryLogin(String dbName, String login, String password) throws Exception;
	
	public String getCollectionNames(String dbName) throws Exception;

	public String getDBNames(String dbName) throws Exception ;
	
	public  String executeJavascript(String dbName,String script)   throws Exception;
	
	
	//**********************************SQL*************************************************
	public String execSql(String dbName, String sqlConn,String sqlRequest) throws Exception ;
	
	
	
	//*****************************RSS3D***************************************************
	/*
	 * expression regexp
	 * periodType :$year $month $dayOfMonth $hour
	 * 
	 */
	public String aggregateFeeds(String expression, String periodType, String jsonQuery ) throws Exception;
	public String getFeeds(String expression,String jsonQuery,int  limit)throws Exception;
	
	
	
	
	
	

}
