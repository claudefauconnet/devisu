package com.fauconnet.thesaurus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;

import com.fauconnet.devisu.MongoProxy;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class importRapraThesaurus {

	private static Map <String,String> semanticRoles=new HashMap<String,String>();;
	private static Map <String,String> lexicalRoles=new HashMap<String,String>();;
	private static String currentParentTerm="";
	private static int indexId;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		importData();
		initCodes();

	}
	

	public static void importData() {
		try {
		MongoProxy mongo = new MongoProxy("localhost", 27017, "thesaurus");
		String fileStr = "C:\\local\\theaurusRAPRA.txt";
	
			FileInputStream sourceStream = new FileInputStream(new File(fileStr));
			InputStreamReader reader = new InputStreamReader(sourceStream, "UTF-8");
			BufferedReader br = new BufferedReader(reader);
			String line = "";
			while ((line = br.readLine()) != null) {
				DBObject obj = transformLine(line);
				if(obj==null){
					System.out.println("PB on line :"+line);
					continue;
				}
				System.out.println(obj);
				mongo.insert("RapraTesaurusRaw", obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	private static void initCodes(){
	semanticRoles.put("IT","Index Term");
	semanticRoles.put("NP","Non-Polymer Term");
	semanticRoles.put("LO","Geographical Location");
	lexicalRoles.put("BT","Broader Terms");
	lexicalRoles.put("NT","Narrower Terms");
	lexicalRoles.put("MT","Synonym");
	indexId=0;
	}
	
	

	private static  DBObject transformLine(String line) {
		indexId++;
		DBObject obj =new BasicDBObject() ;
		obj.put("id", indexId);
		
		int p=line.indexOf(" (");
		if(p>-1){// terme parent
			String term=line.substring(0,p);
			int q=line.indexOf(")",p);
			if(q<0){
				System.out.println("WRONG LINE :"+line);
				return null;
			}
			String semanticRole=line.substring(p+2,q);
			semanticRole=semanticRoles.get(semanticRole);
			if(semanticRole==null){
				System.out.println("WRONG LINE :"+line);
				return null;
			}
			obj.put("term", term);
			obj.put("parentTerm", semanticRole);
			
			currentParentTerm=term;
		}
		else  //terme enfant{
			obj.put("parentTerm", currentParentTerm);
			p=line.indexOf("Classification Code ");
			if(p>-1){
				p+="Classification Code".length();
				String term=line.substring(p+1);
				obj.put("term", term);
				obj.put("type", "code");
			}
			p=line.indexOf("USE ");
			String term=line.substring(p+1);
			
			if(p>-1){	
				obj.put("term", term);
				obj.put("type", "USE");
			}
		int p=line.indexOf("USE");
		if(p<0)
			return null;
			
		}
		
		
		
	
		return obj;
	}

}
