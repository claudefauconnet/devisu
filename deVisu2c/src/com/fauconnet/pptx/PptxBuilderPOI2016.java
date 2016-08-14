package com.fauconnet.pptx;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hslf.model.TextShape;
import org.apache.poi.util.TempFile;
import org.apache.poi.xslf.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.xslf.usermodel.TextAlign;
import org.apache.poi.xslf.usermodel.TextAutofit;
import org.apache.poi.xslf.usermodel.VerticalAlignment;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFHyperlink;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import com.fauconnet.devisu.MongoProxy;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class PptxBuilderPOI2016 {

	private int technoNumPage;

	private static String mongoURl = "localhost";
	private static int mongoPort = 27017;
	private static String mongoDB = "POT2016";
	private List<String> cache = new ArrayList<String>();
	private int sum_SC_UCs = 0;
	private int sum_SC_DCs = 0;
	private int sum_SC_Technos = 0;
	private int sum_SC_BBs = 0;
	// "frhdstd-aefl016"

	// public static String
	// templatesDir="D:\\workspace2\\deVisu2a\\WebContent\\data\\";
	// public static String
	// targetDir="D:\\workspace2\\deVisu2a\\WebContent\\data\\";

	public static String templatesDir = "C:\\Local\\workspace3\\deVisu2b\\WebContent\\data\\";
	public static String targetDir = "C:\\Local\\workspace3\\deVisu2b\\WebContent\\data\\";
	public static String pictureDir = "C:\\Local\\workspace3\\deVisu2b\\WebContent\\data\\pictures\\";

	public static String templateTechnosFile = "Template_Technologies_POT2016.pptx";
	public static String templateUseCasesFile = "Template_UseCases_POT2016.pptx";
	public static String templateScenariosFile = "Template_Scenarios_POT2016.pptx";

	public static String targetTechnosFile = "technologiesSheetsPOT2016.pptx";
	public static String targetUseCasesFile = "useCasesSheetsPOT2016.pptx";
	public static String targetScenariosFile = "scenariosSheetsPOT2016.pptx";

	String[] maturities = new String[] { "", "emerging", "adolescent", "first rollout", "main stream" };
	String[] marketSkills = new String[] { "", "initial", "low", "medium", "high" };
	String[] TotalSkills = new String[] { "", "initial", "low", "medium", "high" };
	String[] intensity = new String[] { "", "low", "medium", "high" };
	String[] horizon = new String[] { "", "short term (0-3 years)", "mid term (3-5 years)", "long term (>5 years)" };
	int imgSize = 200;
	MongoProxy mongo;

	public static void main(String args[]) throws Exception {
		if (false) {
			generateSVGImages();
			
		}

		String type = "UC";
		// type = "techno";
		PptxBuilderPOI2016 builder = new PptxBuilderPOI2016();

		XMLSlideShow slidShow = null;
		if (type.equals("techno")) {
			slidShow = builder.createPowerpoint(type, templatesDir + templateTechnosFile, null);
			builder.saveXMLSlideShowAsFile(slidShow, templatesDir + targetTechnosFile);
		}
		if (type.equals("UC")) {

			List<String> bus = new ArrayList<String>();
			 bus.add("GAZ-MKT");
			//bus.add("GAZ-TRADING");
			// bus.add("EP");
			// bus.add("RC");
			// bus.add("HD");

			for (String bu : bus) {
				DBObject query = new BasicDBObject("bu", bu);
				String _targetUseCasesFile = targetUseCasesFile.replace(".pptx", "_" + bu + ".pptx");
				slidShow = builder.createPowerpoint(type, templatesDir + templateUseCasesFile, null);
				builder.saveXMLSlideShowAsFile(slidShow, templatesDir + _targetUseCasesFile);
			}
		}

		if (type.equals("SC")) {
			slidShow = builder.createPowerpoint(type, templatesDir + templateScenariosFile, null);
			builder.saveXMLSlideShowAsFile(slidShow, templatesDir + targetScenariosFile);
		}

	}

	public XMLSlideShow createPowerpoint(String type, String templatePath, DBObject query) {
		try {
			mongo = new MongoProxy(mongoURl, mongoPort, mongoDB);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}

		try {
			String collection = "";
			if (type.equals("techno"))
				collection = "technologies";
			else if (type.equals("UC"))
				collection = "use_cases";
			else if (type.equals("SC"))
				collection = "scenarios";

			List<DBObject> items = getData(collection, query);
			
			
			items.sort(new Comparator<DBObject>(){

				
				@Override
				public int compare(DBObject a, DBObject b) {
					if (type.equals("techno")){
						return ((String) a.get("name")).compareTo((String)b.get("name"));
					}
					if (type.equals("UC")){
						Integer aBv= getIntValue( a.get("businessValue"));
						Integer bBv= getIntValue( b.get("businessValue"));
						if(aBv>bBv)
							return -1;
						if(aBv<bBv)
							return 1;
					}
					if (type.equals("SC")){
						Integer aBv= getIntValue(a.get("nUC"));
						Integer bBv= getIntValue( b.get("nUC"));
						
						if(aBv>bBv)
							return -1;
						if(aBv<bBv)
							return 1;
					}
					return 0;
				}
				
			});

			FileInputStream sourceStream = new FileInputStream(new File(templatePath));
			FileInputStream sourceStream2 = new FileInputStream(new File(templatePath));

			XMLSlideShow sourcePPT = new XMLSlideShow(sourceStream);
			XMLSlideShow targetPPT = new XMLSlideShow(sourceStream2);

			technoNumPage = 0;
			XSLFSlideLayout titleLayout = sourcePPT.getSlideMasters()[0].getLayout(SlideLayout.TITLE_ONLY);
			int counter = 1;
			int LIMIT = 1000;

			fillTableOfContents(items, 1, sourcePPT, targetPPT);
			if (true) {

				for (DBObject item : items) {
					if (!item.get("name").equals("")) {
						createAndFillSlides(item, sourcePPT, targetPPT);
						System.out.println("Processing :" + (counter++) + " -- " + item.get("techno"));
						if (counter > LIMIT)
							break;
					}
					if (counter > LIMIT)
						break;
				}
				targetPPT.removeSlide(2);
			}
			sourceStream.close();
			System.out.println("Presentation generated successfully");
			return targetPPT;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	private void fillTableOfContents(List<DBObject> items, int tablePageIndex, XMLSlideShow sourcePPT,
			XMLSlideShow targetPPT) {
		String str = "";
		int i = 1;
		for (DBObject item : items) {
			if (!item.get("name").equals("")) {
				str += "P." + (i++) + "..." + item.get("name") + "\n";

			}
		}
		XSLFSlide[] slides = targetPPT.getSlides();
		XSLFSlide newSlide = slides[tablePageIndex];
		/// newSlide.importContent(srcSlide);
		// newSlide.setFollowMasterGraphics(true);
		XSLFShape[] shapes = newSlide.getShapes();
		for (XSLFShape aShape : shapes) {
			if (aShape instanceof XSLFTextShape || aShape instanceof XSLFAutoShape) {

				XSLFTextShape aShape2 = (XSLFTextShape) aShape;
				// aShape2.setText("zzzzz");
				String key = aShape2.getText().trim();
				System.out.println("---" + key);
				if (key.indexOf("$tableOfContents") > -1) {// table des matieres
					aShape2.clearText();

					XSLFTextParagraph paragraph = aShape2.addNewTextParagraph();
					XSLFTextRun run = paragraph.addNewTextRun();
					run.setFontSize(8);
					// ((XSLFTextShape)aShape2).setFontSize(8);
					run.setText(str);
					// XSLFHyperlink link = run.createHyperlink();
					// link.setAddress("http://poi.apache.org");
					((XSLFTextShape) aShape2).setPlaceholder(Placeholder.FOOTER);

				}

			}

		}
	}

	public static boolean implementsInterface(Object object, Class interf) {
		return interf.isInstance(object);
	}

	public void createAndFillSlides(DBObject item, XMLSlideShow sourcePPT, XMLSlideShow targetPPT) {
		try {

			int count = 0;
			this.sum_SC_UCs = 0;
			this.sum_SC_DCs = 0;
			this.sum_SC_Technos = 0;
			this.sum_SC_BBs = 0;

			for (XSLFSlide srcSlide : sourcePPT.getSlides()) {

				if (count++ < 2) // on ne duplique pas les deux premieres slides
									// de
									// titre et table des matieres
					continue;

				XSLFSlideLayout slideLayout = srcSlide.getSlideLayout();

				XSLFSlide newSlide;

				newSlide = targetPPT.createSlide(slideLayout);
				newSlide.importContent(srcSlide);
				newSlide.setFollowMasterGraphics(true);

				XSLFShape[] shape = newSlide.getShapes();
				item.put("num", "" + (technoNumPage++));
				for (int j = 0; j < shape.length; j++) {

					XSLFShape aShape = shape[j];
					int KK = aShape.getShapeId();

					if (aShape instanceof XSLFTextShape || aShape instanceof XSLFAutoShape) {

						XSLFTextShape aShape2 = (XSLFTextShape) aShape;

						String key = aShape2.getText().trim();
						// System.out.println("..........." + key);
						System.out.println("---" + key);

						String replaceText = "";
						
						if (key.startsWith("#Sum_"))// on traite les totaux apres
							continue;
						if (key.indexOf("£") > -1) {// image
							try {
								drawPicture(targetPPT, newSlide, aShape, item, key);

							} catch (Exception e) {
								e.printStackTrace();
							}
							continue;

						}

						else if (key.indexOf("?") > -1) {
							System.out.println("+--" + key);
							replaceText = getLinkedData(item, key);
						}

						else if (key.indexOf("$") < 0 && key.indexOf("#") < 0) {
							aShape2.setVerticalAlignment(VerticalAlignment.MIDDLE);
							continue;
						} else {
							replaceText = getItemText(item, key);
						}

						aShape2.clearText();

						// http://www.tutorialspoint.com/apache_poi_ppt/apache_poi_ppt_formatting_text.htm

						XSLFTextParagraph paragraph = aShape2.addNewTextParagraph();
						XSLFTextRun run = paragraph.addNewTextRun();
						paragraph.setTextAlign(TextAlign.LEFT);
						if (key.indexOf("#name") > -1 || key.indexOf("#num") > -1) {
							// run.setFontColor(Color.getHSBColor(153, 76, 0));
							run.setFontColor(Color.white);
							// aShape2.setFillColor(Color.white);
							run.setFontSize(14);

							run.setBold(true);
						} else {
							run.setFontColor(java.awt.Color.darkGray);
							run.setFontSize(10);
							if (key.indexOf("$") > -1 || key.indexOf("?") > -1) {
								paragraph.setBullet(true);
								// paragraph.setLineSpacing(5);

							}
						}
						
					
							run.setText(replaceText);

						XSLFTextParagraph paragraph2 = aShape2.getTextParagraphs().get(0);
						// XSLFTextRun r1 = paragraph.getTextRuns().get(0);

						paragraph2.setTextAlign(TextAlign.LEFT);
						aShape2.setVerticalAlignment(VerticalAlignment.TOP);
						// aShape2.setTextAutofit(TextAutofit.SHAPE);
						aShape2.setTextAutofit(TextAutofit.NORMAL);

					}

				}

				processSCcounts(newSlide);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String getLinkedData(DBObject item, String key) {
		String str = "";
		String collection = "";
		String field = null;
		String query = null;

		cache = new ArrayList<String>();
		

		int id = getIntValue(item.get("id"));// Integer.valueOf((String) ""+
												// item.get("id"));//(Integer)
												// Math.round((float) (double)
												// item.get("id"));

		System.out.println("++++" + key);
		if (key.indexOf("?UC_DCs") > -1) {
			List<DBObject> items = new ArrayList<DBObject>();
			items.add(item);
			return getIncludedFieldsData(key, items);
		}
		if (key.indexOf("?UC_technos") > -1) {
			List<DBObject> items = new ArrayList<DBObject>();
			items.add(item);
			return getIncludedFieldsData(key, items);
		}

		if (key.indexOf("?dcs") > -1) {
			collection = "r_techno_dc";
			query = "techno_id";
			field = "dc_name";
		} else if (key.indexOf("?stronglyLinkedTo_name") > -1) {
			collection = "linkedTechnos";
			query = "techno_id";
			field = "stronglyLinked_name";
		}

		else if (key.indexOf("?weaklyLinkedTo_name") > -1) {
			collection = "linkedTechnos";
			query = "techno_id";
			field = "weaklyLinkedTo_name";
		}

		else if (key.indexOf("?SC_UCs") > -1) {
			collection = "use_cases";
			query = "scenario_id";
			field = "name";
		}

		else if (key.indexOf("?SC_technos") > -1) {
			collection = "use_cases";
			query = "scenario_id";
			field = "name";
			DBObject fieldsObj = null;
			DBObject queryObj = new BasicDBObject(query, id);
			List<DBObject> items = mongo.getDocuments(collection, queryObj, fieldsObj, 1000);
			return getIncludedFieldsData("?UC_technos", items);

		} else if (key.indexOf("?SC_DCs") > -1) {
			collection = "use_cases";
			query = "scenario_id";
			field = "name";
			DBObject fieldsObj = null;
			DBObject queryObj = new BasicDBObject(query, id);
			List<DBObject> items = mongo.getDocuments(collection, queryObj, fieldsObj, 1000);
			return getIncludedFieldsData("?UC_DCs", items);
		}

		else if (key.indexOf("?SC_BBs") > -1) {
			List<DBObject> items = new ArrayList<DBObject>();
			items.add(item);
			return getIncludedFieldsData(key, items);
		}

		if (query == null)
			return "???";
		DBObject fieldsObj = new BasicDBObject(field, 1);
		fieldsObj.put("bu", 1);
		fieldsObj.put("BD", 1);
		fieldsObj.put("BC", 1);
		fieldsObj.put("year", 1);
		DBObject queryObj = new BasicDBObject(query, id);
		List<DBObject> docs = mongo.getDocuments(collection, queryObj, fieldsObj, 1000);

		if (key.indexOf("?SC_UCs") > -1) {
			List<String> ucs = new ArrayList<String>();
			for (DBObject doc : docs) {
				this.sum_SC_UCs += 1;
				Object value = doc.get(field);
				if (value == null)
					continue;
				ucs.add("" + doc.get("bu") + " / " + doc.get("BD") + "/" + doc.get("BC") + " (" + doc.get("year")
						+ ") : " + doc.get(field) + "\n");
			}
			ucs.sort(new Comparator<String>() {
				public int compare(String a, String b) {
					return a.compareTo(b);
				}
			});
			for (String uc : ucs) {
				str += uc;
			}

		} else {
			for (DBObject doc : docs) {
				Object value = doc.get(field);

				if (value == null)
					continue;

				str += doc.get(field) + "\n";
			}

		}

		return str.trim();

	}

	private void processSCcounts(XSLFSlide newSlide) {
		XSLFShape[] shapes = newSlide.getShapes();
		for (XSLFShape aShape : shapes) {
			if (aShape instanceof XSLFTextShape || aShape instanceof XSLFAutoShape) {
				XSLFTextShape aShape2 = (XSLFTextShape) aShape;
				String key = aShape2.getText().trim();
			//System.out.println("--------------" + key);
				String value = "xx";
				if (key.equals("#Sum_SC_UCs"))
					value = ("" + sum_SC_UCs);
				else if (key.equals("#Sum_SC_DCs"))
					value = ("" + sum_SC_DCs);
				else if (key.equals("#Sum_SC_Technos"))
					value = ("" + sum_SC_Technos);
				else if (key.equals("#Sum_SC_BBs"))
					value = ("" + sum_SC_BBs);

				if (!value.equals("xx")) {
					aShape2.clearText();
					
					XSLFTextParagraph paragraph = aShape2.addNewTextParagraph();
					XSLFTextRun run = paragraph.addNewTextRun();
					run.setText(value);
					
					paragraph.setTextAlign(TextAlign.RIGHT);
					run.setFontColor(Color.white);
					// aShape2.setFillColor(Color.white);
				
					run.setFontSize(12);
				
				}

			}
		}

	}

	private String getIncludedFieldsData(String key, List<DBObject> items) {
		String str2 = "";

		for (DBObject item : items) {

			if (key.indexOf("?UC_DCs") > -1) {

				BasicDBList obj = (BasicDBList) item.get("dcs");
				if (obj == null)
					continue;
				ListIterator<Object> it = obj.listIterator();
				while (it.hasNext()) {

					DBObject obj2 = (DBObject) it.next();
					String name = (String) obj2.get("dc");// + "\n";
					if (cache.indexOf(name) < 0) {
						str2 += name + "\n";
						cache.add(name);
						this.sum_SC_DCs += 1;
						/*
						 * 
						 * BasicDBList obj3 = (BasicDBList) obj2.get("technos");
						 * if (obj3 == null) continue; ListIterator<Object> it2
						 * = obj3.listIterator(); str2 += " ["; while
						 * (it2.hasNext()) { DBObject obj4 = (DBObject)
						 * it2.next(); str2 += "" + obj4.get("name") + ","; }
						 * str2 += "]"; if (it.hasNext()) str2 += "\n";
						 */
					}
				}

			}

			else if (key.indexOf("?UC_technos") > -1) {
				List<String> technos = new ArrayList<String>();

				BasicDBList obj = (BasicDBList) item.get("dcs");
				if (obj == null)
					continue;
				ListIterator<Object> it = obj.listIterator();
				while (it.hasNext()) {
					DBObject obj2 = (DBObject) it.next();
					BasicDBList obj3 = (BasicDBList) obj2.get("technos");
					if (obj3 == null)
						continue;
					ListIterator<Object> it2 = obj3.listIterator();
					while (it2.hasNext()) {
						DBObject obj4 = (DBObject) it2.next();
						String techno = (String) obj4.get("name");
						if (cache.indexOf(techno) < 0) {
							this.cache.add(techno);
							str2 += techno+"\n";
							this.sum_SC_Technos += 1;
						}

					}

				}

			}

			else if (key.indexOf("?SC_BBs") > -1) {

				BasicDBList obj = (BasicDBList) item.get("buildingBlocks");
				if (obj == null)
					continue;
				ListIterator<Object> it = obj.listIterator();
				while (it.hasNext()) {
					DBObject obj2 = (DBObject) it.next();
					String name = (String) obj2.get("name");
					if (cache.indexOf(name) < 0) {
						this.cache.add(name);
						this.sum_SC_BBs += 1;
					}
					str2 += obj2.get("name") + " :" + obj2.get("comment") + "\n";// +obj2.get("comment");

				}

			}
		}
		if (str2.length() > 0)
			str2 = str2.substring(0, str2.length() - 1);
		return str2;

	}

	private String getItemText(DBObject item, String key) {
		int p = key.indexOf("$");
		if (p < 0)
			p = key.indexOf("#");
		if (p < 0)
			p = key.indexOf("£");
		if (p < 0)
			p = key.indexOf("?");
		if (p < 0) {
			System.out.println(key);
			return key;
		}

		key = key.substring(p + 1).trim();
		Object value = item.get(key);
		// System.out.println("--------------"+key+" : "+value);
		if (value == null) {

			return "";
		}

		value = processText(key, "" + value);
		return "" + value;

	}

	private String processText(String key, String text) {

		String str = "";
		// System.out.println(key);
		if (key.indexOf("id") > -1) {

			String sss = "aaaa";

		}

		if (key.indexOf("technologyMaturity") > -1) {
			if (text.matches("[0-9]"))
				str = maturities[Integer.parseInt(text)];
			else
				str = text + "??";
		} else if (key.indexOf("marketSkills") > -1) {
			if (text.matches("[0-9]"))
				str = marketSkills[Integer.parseInt(text)];
			else
				str = text + "??";
		} else if (key.indexOf("TotalSkills") > -1) {
			if (text.matches("[0-9]"))
				str = TotalSkills[Integer.parseInt(text)];
			else
				str = text + "??";
		} else if (key.indexOf("businessValue") > -1) {
			if (text.matches("[0-9]"))
				str = intensity[Integer.parseInt(text)];

		} else if (key.indexOf("strategyIndex") > -1) {
			if (text.matches("[0-9]"))
				str = horizon[Integer.parseInt(text)];

		} else if (key.indexOf("riskLevel") > -1) {
			if (text.matches("[0-9]"))
				str = intensity[Integer.parseInt(text)];

		} else if (key.indexOf("easeOfImpl") > -1) {
			if (text.matches("[0-9]"))
				str = intensity[Integer.parseInt(text)];

		}

		else {

			str = text.replaceAll(";", "\n").trim();
		}

		return str;
	}

	private void drawPicture(XMLSlideShow targetPPT, XSLFSlide slide, XSLFShape shape, DBObject item, String fieldName)
			throws Exception {
		String picturePath;
		if (fieldName.startsWith("£svg")) {
			picturePath = "ArchMap_" + item.get("id") + ".png";
		} else {
			picturePath = getItemText(item, fieldName);
		}

		if (!picturePath.startsWith("£")) {// on a trouvé l'image

			picturePath = pictureDir + picturePath;
			if (new File(picturePath).exists()) {
				byte[] img = extractBytes(picturePath);
				if (img == null || img.length==0)
					return;
				int idx = targetPPT.addPicture(img, XSLFPictureData.PICTURE_TYPE_PNG);
				double sw1 = shape.getAnchor().getWidth();
				double sh1 = shape.getAnchor().getHeight();

				//Rectangle2D anchor = shape.getAnchor();
				XSLFPictureShape picture = slide.createPicture(idx);
				Rectangle2D pictAnchor = picture.getAnchor();
				double iw1 = pictAnchor.getWidth();
				double ih1 = pictAnchor.getHeight();
				double coef = 1;

				double coefH = ih1 / sh1;
				double coefV = iw1 / sw1;

				coef = Math.max(coefH, coefV);
				coef = Math.max(coef, 1);
				iw1 = iw1 / coef;
				ih1 = ih1 / coef;

				//double x = shape.getAnchor().getMaxX() - iw1;
				//double y = shape.getAnchor().getMaxY() - ih1;
				double x = shape.getAnchor().getX() ;
				double y = shape.getAnchor().getY() ;
				
				// double x = anchor.getCenterX() - iw1 / 2;
				// double y = anchor.getCenterY() - ih1 / 2;
				picture.setAnchor(new Rectangle2D.Double(x, y, iw1, ih1));
				picture.setLineColor(Color.BLACK);
				// picture.setFlipHorizontal(true);

			}
		}
	}

	private List<DBObject> getData(String collection, DBObject query) throws Exception {

		if (query == null)
			query = new BasicDBObject();
		List<DBObject> items = mongo.getDocuments(collection, query, -1);
		items.sort(new Comparator<DBObject>() {

			@Override
			public int compare(DBObject item1, DBObject item2) {
				String title1 = "" + (String) item1.get("name");
				String title2 = "" + (String) item2.get("name");
				return title1.compareTo(title2);

			}
		});

		return items;

	}

	private List<DBObject> getExamplesdata(DBObject query) throws Exception {

		MongoProxy mongo = new MongoProxy(mongoURl, mongoPort, mongoDB);
		if (query == null)
			query = new BasicDBObject();
		List<DBObject> items = mongo.getDocuments("radarExamples", query, -1);
		items.sort(new Comparator<DBObject>() {

			@Override
			public int compare(DBObject item1, DBObject item2) {
				String title1 = "" + (String) item1.get("techno");
				String title2 = "" + (String) item2.get("techno");
				return title1.compareTo(title2);

			}
		});

		return items;

	}

	private List<DBObject> getUseCasesdata(DBObject query) throws Exception {

		MongoProxy mongo = new MongoProxy(mongoURl, mongoPort, mongoDB);
		if (query == null)
			query = new BasicDBObject();
		// query.put("bu", "HD");
		List<DBObject> items = mongo.getDocuments("use_cases", query, -1);
		items.sort(new Comparator<DBObject>() {

			@Override
			public int compare(DBObject item1, DBObject item2) {
				String title1 = item1.get("bu") + "/" + item1.get("BD") + "/" + item1.get("BC") + "/"
						+ item1.get("name");
				String title2 = item2.get("bu") + "/" + item2.get("BD") + "/" + item2.get("BC") + "/"
						+ item2.get("name");

				return title1.compareTo(title2);

			}
		});

		for (DBObject item : items) {
			String str = "";
			List<DBObject> dcs = (List<DBObject>) item.get("dcs");
			if (dcs != null) {

				List<String> allTechs = new ArrayList<String>();

				for (DBObject dc : dcs) {
					String techsStr = "[";
					List<DBObject> technos = (List<DBObject>) dc.get("technos");
					for (DBObject techno : technos) {
						String techName = (String) techno.get("name");
						if (allTechs.indexOf(techName) < 0)
							allTechs.add(techName);
						techsStr += techName + ",";
					}
					techsStr += "]";
					// str += dc.get("dc") + " " + techsStr + "\n";
					str += dc.get("dc") + "\n";
				}
				if (str.length() > 2)
					item.put("dig-caps", str.substring(0, str.length() - 2));
				String allTechsStr = "";
				for (String techno : allTechs) {
					allTechsStr += techno + "\n";
				}
				item.put("all-techs", allTechsStr);
			} else {
				System.out.println("no DCs for item : " + item.get("techno"));
			}

		}

		return items;
	}

	public byte[] extractBytes(String imagePath) throws IOException {
		if (!new File(imagePath).exists())
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
		BufferedImage img = ImageIO.read(new File(imagePath));
		ImageIO.write(img, "png", baos);
		baos.flush();

		String base64String = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(baos.toByteArray());
		baos.close();

		byte[] bytearray = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(base64String);

		return bytearray;
	}

	public void saveXMLSlideShowAsFile(XMLSlideShow pptx, String targetPath) throws Exception {
		FileOutputStream targetOuputStream = new FileOutputStream(new File(targetPath));
		pptx.write(targetOuputStream);
		targetOuputStream.close();
		System.out.println("Presentation edited successfully");
	}

	public FileInputStream getXMLSlideShowAsStream(XMLSlideShow pptx) throws Exception {
		StringWriter strw = new StringWriter();
		File file = TempFile.createTempFile("POT", "pptx");
		FileOutputStream targetOuputStream = new FileOutputStream(file);
		pptx.write(targetOuputStream);
		targetOuputStream.close();
		FileInputStream in = new FileInputStream(file);
		return in;

	}

	public File getXMLSlideShowFile(XMLSlideShow pptx) throws Exception {
		StringWriter strw = new StringWriter();
		File file = TempFile.createTempFile("POT", "pptx");
		FileOutputStream targetOuputStream = new FileOutputStream(file);
		pptx.write(targetOuputStream);
		targetOuputStream.close();
		return file;

	}

	public int getIntValue(Object obj) {
		String str = "" + obj;
		int p = str.indexOf(".");
		if (p > 0) {
			str = str.substring(0, p);
		}
		return Integer.valueOf((String) str);

	}

	public static void generateSVGImages() throws Exception {

		String collection = "scenarios";
		DBObject queryObj = new BasicDBObject();
		DBObject fieldsObj = null;
		MongoProxy mongo = new MongoProxy(mongoURl, mongoPort, mongoDB);
		List<DBObject> objs = mongo.getDocuments(collection, queryObj, fieldsObj, 1000);
		for (DBObject item : objs) {

			String svg = (String) item.get("svg");
			if (svg == null)
				continue;
			item.put("svgFileOK", "true");
			svg = svg.replace("\\", "");
			String file = "ArchMap_" + item.get("id") + ".svg";
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pictureDir + file), "UTF-8"));
			out.write(svg);
			out.close();
		}
		for (DBObject item : objs) {
			if (item.get("svgFileOK") != null) {
				String file = "ArchMap_" + item.get("id") + ".svg";
				SVG2IMG.convertSVG(pictureDir, pictureDir + file);
			}
		}

	}

}