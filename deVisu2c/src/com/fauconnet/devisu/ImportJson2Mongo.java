package com.fauconnet.devisu;

import java.io.File;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import de.schlichtherle.io.FileInputStream;

public class ImportJson2Mongo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			MongoProxy proxy = new MongoProxy("localhost", 27017,"thesaurus");
		String fileStr = "C:\\Local\\workspace\\thesaurus2\\WebContent\\data\\kaliwatch.json";
		InputStreamReader isr= new InputStreamReader(new FileInputStream(new File(fileStr)),"UTF-8");
		

			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
