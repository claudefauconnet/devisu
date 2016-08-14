package com.fauconnet.devisu;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ImagesPOTtester {

	public static void main(String[] args) {
		try {
			MongoProxy proxy = new MongoProxy("localhost", 27017, "POT");
			File dir = new File("C:\\Local\\POTdata\\media");
			String []images=dir.list();
			List<String> imagesList=Arrays.asList(images);
			List<DBObject>list=proxy.getDocuments("radarDetails", new BasicDBObject(), -1);
			for(DBObject obj : list){
			String img=(String) obj.get("inf_img");
			
			int p=imagesList.indexOf(img);
			String message="";
			if(p>-1)
				message=img+" ok in "+obj.get("techno");
			else
				message=img+"!!!! ko in "+obj.get("techno");
	
			System.out.println(message);
			
			}
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
