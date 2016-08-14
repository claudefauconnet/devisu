package com.fauconnet.rss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fauconnet.devisu.MongoProxy;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class VerbatimExplorer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			//ImportVerbatims();
			getTags();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static String getTags() throws IOException{
		
			MongoProxy mongoProxy = new MongoProxy("localhost", 27017, "DigitalPortfolio");
		Map<String,Object>map=new HashMap<String, Object>();
		List<DBObject> rssItems = mongoProxy.getDocuments("verbatims", new BasicDBObject(), 100000)	;
			map.put("data",rssItems)	;
			map.put("method","getTags")	;
			map.put("maxTags","50")	;
		TagProcessor processor= new TagProcessor();
		String str=processor.process(map);
		System.out.println(str);
		return str;
	}
	
	
	public static void ImportVerbatims() throws IOException{

		MongoProxy monProxy = new MongoProxy("localhost", 27017, "DigitalPortfolio");
		String  dataPath= "C:\\Local\\verbatimsDigitalTotalSurvey.txt";
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(dataPath), "UTF-8"));
		String line="";
		while ((line=br.readLine())!=null){
			DBObject obj=new BasicDBObject("title",line);
			monProxy.insert("verbatims", obj);
		}
		br.close();
		
	
		
	}

}
