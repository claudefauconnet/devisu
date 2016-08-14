package com.fauconnet.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DCprocessor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		doLinks();
	}
	
	public static void doLinks(){
	
	File f = new File("C:\\Local\\DC.txt");
	try {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
		String line = "";
		boolean prevStartQuote = false;
		boolean prevEndQuote = false;
		String [] headers=null;
		String str="";
		int n=0;
		while ((line = br.readLine()) != null) {
			if(n==0){
				n++;
				headers=line.split("\t");
				
			}else{
			String[] data=line.split("\t");
		if(data.length<2){
				//System.out.println(line);
				continue;
			}
			String col0=data[0];
			for(int i=1;i<data.length;i++){
				String cell=data[i];		
				if(!cell.equals("")){
					str+=col0+"\t"+headers[i]+"\n";
					
				}
			}
			}
		}
		System.out.println(str);
	}
	catch(Exception e){
		e.printStackTrace();
	}
	}

}
