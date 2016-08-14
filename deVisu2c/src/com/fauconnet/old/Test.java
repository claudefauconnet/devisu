package com.fauconnet.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		File f = new File("C:\\Local\\workspace\\radarTool-1.0\\WebContent\\g1.txt");
		try {
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			String line = "";
			boolean prevStartQuote = false;
			boolean prevEndQuote = false;
			while ((line = br.readLine()) != null) {

				if (line.startsWith("\"")) {
					prevStartQuote = true;
					sb.append(line + ";");
				}
				else if (line.endsWith("\"")) {
					sb.append(line + "\n");
					prevStartQuote = false;
				} 
				else if (prevStartQuote == true) {
					sb.append(line + ";");
				}

				else {
					sb.append(line + "\n");
				}

			}
			System.out.println(sb);
			br.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
	
	

}
