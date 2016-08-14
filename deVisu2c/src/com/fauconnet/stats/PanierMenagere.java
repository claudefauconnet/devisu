package com.fauconnet.stats;

import java.util.HashMap;
import java.util.List;

import com.mongodb.DBObject;

public class PanierMenagere {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void exec(List<DBObject> items, String idField, String valueField){
		HashMap<String,Integer> map=new HashMap<String,Integer>();
		for(DBObject item: items){
			String value=(String)item.get(valueField);
			String id=(String)item.get(idField);
			Integer freq;
			if((freq=map.get(value))==null){
				map.put(value, 1);
			}
			else{
			
		}
		
		
		
	}

}
