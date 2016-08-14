package com.fauconnet.devisu;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.fauconnet.devisu.XmlRadarModelManager.RadarModel;
import com.fauconnet.xml.CustomDOMTree;

public class XmlRadarModelManager {

	private Map<String,RadarModel> radarModels=new HashMap<String,RadarModel>();
	private CustomDOMTree tree;
	private String treePath;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public XmlRadarModelManager(String dbName, String dataDirPath) throws Exception {
		treePath = dataDirPath + dbName + ".xml";
		System.out.println(dataDirPath);
		if(!new File(treePath).exists()){
			throw new Exception ("file "+treePath+" does not exist");
		}
		if (tree == null)
			tree = new CustomDOMTree(new File(treePath));
		System.out.println(dbName+"  : xml file  loaded");
		List<Element> radarElts = tree.getElements("radar", null);
		
		if (radarElts.size() == 0){
			System.out.println("no radar in xml file  ");
			throw new Exception("no radar in xml file ");
		}
		for(Element radarElt :radarElts ){
			String radarName=radarElt.getAttribute("name");
			RadarModel radarModel=new RadarModel(radarName);
			radarModels.put(radarName, radarModel);
			
			
		Element idFieldElt = tree.getFirstElement("field", "radarRole", "id", radarElt);
		if (idFieldElt != null)
			radarModel.idColumnName = (String) idFieldElt.getAttribute("name");
		
		/*
		 * else{ throw new Exception ("no id element in XML description"); }
		 */

		addConstantElements(radarElt,radarModel);
		initRadarRoles(radarElt,radarModel);
		setForeignKeys( radarElt, radarModel);
		initDetailedPagesFields(radarElt,radarModel);
		}
	}

	public void addConstantElements(Element radarElement,RadarModel model) {

		boolean save = false;

		if (tree.getFirstElement("field", "radarRole", "id", radarElement) == null) {
			Element fieldElt = tree.createElement("field");
			fieldElt.setAttribute("mandatory", "true");
			fieldElt.setAttribute("name", "id");
			fieldElt.setAttribute("type", "id");
			fieldElt.setAttribute("radarRole", "id");
			tree.insertElement(fieldElt, radarElement, 0);
			model.idColumnName = "id";
			save = true;
		}
		if (tree.getFirstElement("field", "radarRole", "x", radarElement) == null) {
			Element fieldElt = tree.createElement("field");
			fieldElt.setAttribute("mandatory", "true");
			fieldElt.setAttribute("name", "x");
			fieldElt.setAttribute("radarRole", "x");
			fieldElt.setAttribute("type", "number");
			tree.insertElement(fieldElt, radarElement, 1);
			save = true;
		}
		if (tree.getFirstElement("field", "radarRole", "y", radarElement) == null) {
			Element fieldElt = tree.createElement("field");
			fieldElt.setAttribute("mandatory", "true");
			fieldElt.setAttribute("name", "y");
			fieldElt.setAttribute("radarRole", "y");
			fieldElt.setAttribute("type", "number");
			tree.insertElement(fieldElt, radarElement, 2);
			save = true;
		}
		if (save) {
			try {
				tree.save(new File(treePath).toURI().toURL());
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

	}

	public void initRadarRoles(Element radarElement,RadarModel model) {
		Iterator<Element> it = tree.getElements("field", radarElement).iterator();

		while (it.hasNext()) {
			Element fieldElt = it.next();
			String colName = fieldElt.getAttribute("name");
			String isFilterstr = fieldElt.getAttribute("isFilter");

			if (isFilterstr != null) {
				model.filters.add(colName);
			}

			String role = fieldElt.getAttribute("radarRole");
			if (role == null || role.length() == 0)
				continue;

			if (role.equals("id")) {
				model.radarRoles.put("id", colName);
			} else if (role.equals("x")) {
				model.radarRoles.put("x", colName);
			} else if (role.equals("y")) {
				model.radarRoles.put("y", colName);
			} else if (role.equals("label")) {
				model.radarRoles.put("label", colName);
			} else if (role.equals("color")) {
				model.radarRoles.put("color", colName);
			} else if (role.equals("symbol")) {
				model.radarRoles.put("symbol", colName);
			} else if (role.equals("shape")) {
				model.radarRoles.put("shape", colName);
			} else if (role.equals("comment")) {
				model.radarRoles.put("comment", colName);
			} else if (role.equals("link")) {
				model.radarRoles.put("link", colName);
			} else if (role.equals("size")) {
				model.radarRoles.put("size", colName);
			}
		}
	}

	public void initDetailedPagesFields(Element radarElement,RadarModel model) {
		Iterator<Element> it = tree.getElements("field", radarElement).iterator();

		while (it.hasNext()) {
			Element fieldElt = it.next();
			String colName = fieldElt.getAttribute("name");
			String toDisplay = fieldElt.getAttribute("displayInDetailsPage");
			if (toDisplay == null)
				toDisplay = fieldElt.getAttribute("radarRole");
			if (toDisplay != null) {// && toDisplay.equals("true")){
				model.detailedPageFields.add(colName);
			}
		}
	}

	public List<String> getDetailedPageFields(RadarModel model) {
		return model.detailedPageFields;
	}

	public List<String> getFilters(RadarModel model) {
		return model.filters;
	}

	public String getRole(RadarModel model,String field) {
		Iterator<String> it = model.radarRoles.keySet().iterator();
		while (it.hasNext()) {
			String role = it.next();
			String field0 = model.radarRoles.get(role);
			if (field0.equals(field))
				return role;
		}
		return null;
	}

	public Map<String, String> getRadarRoles(RadarModel model) {
		return model.radarRoles;
	}

	public String getField(RadarModel model,String role) {
		return model.radarRoles.get(role);
	}

	public boolean isFieldFilter(RadarModel model,String field) {
		return (model.filters.indexOf(field) > -1);
	}

	public CustomDOMTree getTree() {
		return tree;
	}

	public void setTree(CustomDOMTree tree) {
		this.tree = tree;
	}

	public String getIdColumnName(RadarModel model) {
		return model.idColumnName;
	}
	
	public void setForeignKeys(Element radarElement,RadarModel model){
		Iterator<Element> it=tree.getElements("foreignKey", radarElement).iterator();
		List<String> foreignKeys=new ArrayList<String>();
		while(it.hasNext()){
			foreignKeys.add(it.next().getTextContent());
		}
		model.foreignKeys=foreignKeys;
	}
	
	public List<String> getForeignKeys(RadarModel model){
		return model.foreignKeys;
	}
	
	
	
	class RadarModel {
		 public RadarModel(String radarName) {
			this.name=radarName;
		}
		Map<String, String> radarRoles = new HashMap<String, String>();
		List<String> filters = new ArrayList<String>();
		 String idColumnName;
		 String name;
		 List<String> foreignKeys;
		 List<String> detailedPageFields = new ArrayList<String>();

		
	}



	public RadarModel getModel(String radarModelName) {
		return radarModels.get(radarModelName);
		
	}

}
