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

public class importLexicalForms {
	static Map<String, String> semanticCodes = new HashMap<String, String>();
	static Map<String, String> lexicalCodes = new HashMap<String, String>();
	static String currentParentTerm = "";
	private static int indexId = 0;

	public static void main(String[] args) {
		initCodes();
		importData();

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
				if (obj == null) {
					// System.out.println("PB on line :"+line);
					continue;
				}
			//	System.out.println(obj);
				mongo.insert("RAPRA_RAW", obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void initCodes() {
		semanticCodes.put("IT", "Index term");
		semanticCodes.put("NP", "Non_Polymer term");
		semanticCodes.put("LO", "Geographical Location");

		lexicalCodes.put("BT ", "Broader term");
		lexicalCodes.put("NT ", "narrower term");
		lexicalCodes.put("MT ", "synonym term");

	}

	private static DBObject transformLine(String line) {
		if (line.length() == 0) {
			return null;
		}
		boolean stop = false;
		DBObject obj = new BasicDBObject();
		obj.put("id", indexId++);

		for (String code : semanticCodes.keySet()) {
			int p = line.indexOf(" (" + code + ")");
			if (p > -1) {
				stop = true;

				String term = line.substring(0, p);
				currentParentTerm = term;

				obj.put("term", term);
				obj.put("parent", code);
				break;
			}
		}

		if (!stop) {// enfant
			obj.put("parent", currentParentTerm);
			String preffix = "Classification Code ";
			int p = line.indexOf(preffix);
			if (p > -1) {// enfant
				p += preffix.length();
				String term = line.substring(p + 1);
				obj.put("term", term);
				obj.put("type", "code");
			}
			preffix = "USE ";
			p = line.indexOf(preffix);
			if (p > -1) {
				p += preffix.length();
				String term = line.substring(p + 1);
				obj.put("term", term);
				obj.put("type", "use");
			}
			boolean hasSyntaxicCode = false;
			for (String code : lexicalCodes.keySet()) {
				if (line.startsWith(code)) {
					String term = line.substring(3);
					obj.put("term", term);
					obj.put("type", code);
					hasSyntaxicCode = true;
					break;
				}
			}
			if (!hasSyntaxicCode) {
				obj.put("term", line);
				obj.put("type", "_synonym term");
			}

		}
		System.out.println(obj);

		return obj;
	}
	
	
	private void setNodes(){
		
		
	}
	

}
