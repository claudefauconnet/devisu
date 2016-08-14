package com.fauconnet.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fauconnet.devisu.MongoProxy;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class LdiTransform extends Object {
	 MongoProxy  proxy;
	public static void main(String[] args) {
		
		String idCol = "NUM_PROD";
		String[][] columnsToPivot = new String[][] { new String[] {"NUM_NOM_COMP", "POURCENT_COMP_PROD"} };
		LdiTransform transformer = new LdiTransform();

		try {
			transformer.proxy = new MongoProxy("localhost", 27017, "LDI");
			List<DBObject> newItems = transformer.pivot("LDI","produits", null, idCol, Arrays.asList(columnsToPivot));		
			for (DBObject newItem : newItems) {
				System.out.println(newItem);
			//	transformer.proxy.insert("produits-pivot", newItem);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<DBObject> pivot(String database,String collection, DBObject query, String idCol, List<String[]> columnsToPivot) throws Exception {

		if (query == null)
			query = new BasicDBObject();
		List<DBObject> data = proxy.getDocuments(collection, query, -1);

		List<String> ids = new ArrayList<String>();
		List<DBObject> newItems = new ArrayList<DBObject>();
		int index = -1;
		for (DBObject item : data) {
			String idVal = "" + item.get(idCol);
			if ((index = ids.indexOf(idVal)) < 0) {
				ids.add(idVal);
				DBObject newItem = new BasicDBObject(idCol, idVal);
			
				for (String[] colCouple : columnsToPivot) {
					String key=item.get(colCouple[0]).toString();
					if(key==null)
						continue;
					Object value=item.get(colCouple[1]);
					newItem.put(idCol+"_"+ key,value);	
				}
				newItems.add(newItem);

			} else {
				
				DBObject newItem = newItems.get(index);
			
					for (String[] colCouple : columnsToPivot) {
						String key=item.get(colCouple[0]).toString();
						if(key==null)
							continue;
						Object value=item.get(colCouple[1]);
						newItem.put(idCol+"_"+ key,value);	
				}

			}

		}
		return newItems;

	}
}
