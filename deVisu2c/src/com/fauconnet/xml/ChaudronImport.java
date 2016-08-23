package com.fauconnet.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.fauconnet.devisu.MongoProxy;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ChaudronImport {

	public static void main(String[] args) {

	
		try {
			ChaudronImport.parseCatalogue( "D:\\kohaBiblio.xml");
			//ChaudronImport.parseNotices( "D:\\kohaNotices.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void parseNotices(String file) throws Exception {

		if (!new File(file).exists()) {
			throw new Exception("file " + file + " does not exist");
		}

		CustomDOMTree tree = new CustomDOMTree(new File(file));
		MongoProxy proxy = new MongoProxy("localhost", 27017, "chaudron");

		List<Element> records = tree.getElements("record", null);
			int i = 0;
		Map<String, Integer> notices = new HashMap<String, Integer>();
		for (Element record : records) {
			if (i++ > 100000)
				break;
			
			List<Element> datafields = tree.getElements("datafield", null, null, record);
			for (Element datafield : datafields) {
				String tag=datafield.getAttribute("tag");
				List<Element> subfields = tree.getElements("subfield", null, null, datafield);
				for (Element subfield : subfields) {
					
				
				if (subfield != null) {
					String str = subfield.getTextContent();
					String code=subfield.getAttribute("code")	;
					str=tag+"_"+code+"_"+str;
					if (notices.get(str) == null){
						notices.put(str, 1);
					}
					else{
						notices.put(str, notices.get(str)+1);
					}
				}
				}
				 

			}
			if (i% 10 == 0)
				System.out.println("imported" + (i - 1));

		}
		 i = 0;
		for (String key : notices.keySet()) {
			DBObject obj=new BasicDBObject();
			int k=key.indexOf("_");
			int l=key.indexOf("_",k+1);
			String tag=key.substring(0, k);
			String code=key.substring(k+1, l);
			String nom=key.substring(l+1);
			int freq=notices.get(key);
			obj.put("tag", tag);
			obj.put("code", code);
			obj.put("nom", nom);
			obj.put("freq", freq);
			proxy.insert("KOHA_Notices", obj);
		

			//System.out.println(key);
		}
	}
	
	

	public static void parseCatalogue(String file) throws Exception {

		if (!new File(file).exists()) {
			throw new Exception("file " + file + " does not exist");
		}

		CustomDOMTree tree = new CustomDOMTree(new File(file));
		MongoProxy proxy = new MongoProxy("localhost", 27017, "chaudron");

		List<Element> records = tree.getElements("record", null);
		List<DBObject> objs = new ArrayList<DBObject>();
		int i = 0;
		for (Element record : records) {
			if (i++ > 100000)
				break;
			DBObject obj = new BasicDBObject();

			objs.add(obj);
			List<Element> datafields = tree.getElements("datafield", null, null, record);

			for (Element datafield : datafields) {
				String tag = datafield.getAttribute("tag");

				List<Element> subfields = tree.getElements("subfield", null, null, datafield);

				for (Element subfield : subfields) {
					String code = subfield.getAttribute("code");

					if (tag.equals("010")) {
						if (code.equals("a")) {
							obj.put("010a_ISBN", subfield.getTextContent());
						}
						if (code.equals("b")) {
							obj.put("010b_ISBN_qualificatif", subfield.getTextContent());
						}
						if (code.equals("c")) {
							obj.put("010_cISBN_dispo_prix", subfield.getTextContent());
						}

					}

					if (tag.equals("020")) {
						if (code.equals("a")) {
							obj.put("020a_BiblioNat_codePays", subfield.getTextContent());
						}
						if (code.equals("b")) {
							obj.put("020b_BiblioNat_numero", subfield.getTextContent());
						}

					}
					if (tag.equals("101")) {
						if (code.equals("a")) {
							obj.put("101a_Langue", subfield.getTextContent());
						}

					}
					if (tag.equals("102")) {
						if (code.equals("a")) {
							obj.put("102a_Pays_publication", subfield.getTextContent());
						}
						if (code.equals("b")) {
							obj.put("102b_BiblioNat_numero", subfield.getTextContent());
						}

					}
					if (tag.equals("105")) {
						if (code.equals("a")) {
							obj.put("105a_Donnees_codees_monographies", subfield.getTextContent());

						}
					}
					if (tag.equals("106")) {
						if (code.equals("a")) {
							obj.put("106a_Donnees_codees_textes", subfield.getTextContent());

						}
					}

					if (tag.equals("200")) {
						if (code.equals("a")) {
							obj.put("200a_Titre_propre", subfield.getTextContent());
						}
						if (code.equals("b")) {
							obj.put("200b_Type_document", subfield.getTextContent());
						}
						if (code.equals("e")) {
							obj.put("200e_Complement_titre", subfield.getTextContent());
						}
						if (code.equals("f")) {
							obj.put("200f_1ere_mention_responsabilite", subfield.getTextContent());
						}
					}

					if (tag.equals("210")) {
						if (code.equals("a")) {
							obj.put("210a_Lieu_pubblication", subfield.getTextContent());
						}
						if (code.equals("b")) {
							obj.put("210b_Nom_editeur", subfield.getTextContent());
						}
						if (code.equals("e")) {
							obj.put("210e_Date_Publication", subfield.getTextContent());
						}
						if (code.equals("g")) {
							obj.put("210_g_Nom_fabricant", subfield.getTextContent());
						}
					}

					if (tag.equals("215")) {
						if (code.equals("a")) {
							obj.put("215a_Description_importance_materielle", subfield.getTextContent());
						}
						if (code.equals("c")) {
							obj.put("215c_Autre_caract_materielle", subfield.getTextContent());
						}
						if (code.equals("d")) {
							obj.put("215d_Format", subfield.getTextContent());
						}

					}

					if (tag.equals("225")) {
						if (code.equals("a")) {
							obj.put("225a_Titre_collection", subfield.getTextContent());
						}
						if (code.equals("x")) {
							obj.put("225x_ISSN_collection", subfield.getTextContent());
						}

					}

					if (tag.equals("300")) {
						if (code.equals("a")) {
							obj.put("300a_note", subfield.getTextContent());
						}
						if (code.equals("x")) {
							obj.put("225x_ISSN_collection", subfield.getTextContent());
						}

					}

					if (tag.equals("410")) {
						if (code.equals("0")) {
							obj.put("410_0_Collection_Numero_notice", subfield.getTextContent());
						}
						if (code.equals("t")) {
							obj.put("410t_Collection_titre", subfield.getTextContent());
						}
						if (code.equals("x")) {
							obj.put("410x_Collection_ISSN", subfield.getTextContent());
						}
						if (code.equals("d")) {
							obj.put("410d_Collection_date_publication", subfield.getTextContent());
						}

					}

				}
			}
			proxy.insert("KOHA_catalogue", obj);
			if (i % 10 == 0)
				System.out.println("imported" + (i - 1));

		}

		System.out.println("DONE" + (i - 1));

	}

}
