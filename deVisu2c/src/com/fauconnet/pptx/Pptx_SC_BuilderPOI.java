package com.fauconnet.pptx;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.poi.util.TempFile;
import org.apache.poi.xslf.usermodel.SlideLayout;
import org.apache.poi.xslf.usermodel.TextAlign;
import org.apache.poi.xslf.usermodel.TextAutofit;
import org.apache.poi.xslf.usermodel.VerticalAlignment;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
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
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Pptx_SC_BuilderPOI {
	private String currentObject = null;
	public static String pictureDir = "C:\\Local\\Temp\\media\\";
	public static String templateTechnosPath = "C:\\Local\\fichesPOT\\templateTechnosPOT4.pptx";
	public static String targetTechnosPath = "C:\\Local\\fichesPOT\\XXX_TechnosPOT4.pptx";
	public static String templateUseCasesPath = "C:\\Local\\fichesPOT\\templateUseCasesPOT4.pptx";
	public static String targetUseCasessPath = "C:\\Local\\fichesPOT\\XXX_UseCasesPOT4.pptx";

	public static void main(String args[]) throws Exception {
		Pptx_SC_BuilderPOI builder = new Pptx_SC_BuilderPOI();
	XMLSlideShow slidShow=builder.createTechnosPowerpoint(templateTechnosPath, null);
	//	XMLSlideShow slidShow=builder.createUseCasesPowerpoint(templateUseCasesPath,null);
		builder.saveXMLSlideShowAsFile(slidShow,targetUseCasessPath);

	}

	public XMLSlideShow createTechnosPowerpoint(String templatePath, DBObject query) {
		currentObject = "techno";
		try {

			List<DBObject> items = getTechnosdata(query);

			FileInputStream sourceStream = new FileInputStream(new File(templatePath));
			FileInputStream sourceStream2 = new FileInputStream(new File(templatePath));

			XMLSlideShow sourcePPT = new XMLSlideShow(sourceStream);
			XMLSlideShow targetPPT = new XMLSlideShow(sourceStream2);

			int kk = targetPPT.getSlides().length;

			XSLFSlideLayout titleLayout = sourcePPT.getSlideMasters()[0].getLayout(SlideLayout.TITLE_ONLY);
			int counter = 1;
			for (DBObject item : items) {
				if (!item.get("techno").equals("")) {
					createAndFillSlides(item, sourcePPT, targetPPT);
					System.out.println("Processing :" + (counter++) + " -- " + item.get("techno"));
					if (counter > 1000)
						break;
				}
				if(counter>10000)
					break;
			}
			targetPPT.removeSlide(1);
			sourceStream.close();
			System.out.println("Presentation generated successfully");
			return targetPPT;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	

	

	public XMLSlideShow createUseCasesPowerpoint(String templatePath,DBObject query) {
		currentObject = "useCase";
		try {

			List<DBObject> items = getUseCasesdata(query);

			FileInputStream sourceStream = new FileInputStream(new File(templatePath));
			FileInputStream sourceStream2 = new FileInputStream(new File(templatePath));

			XMLSlideShow sourcePPT = new XMLSlideShow(sourceStream);
			XMLSlideShow targetPPT = new XMLSlideShow(sourceStream2);

			int kk = targetPPT.getSlides().length;

			XSLFSlideLayout titleLayout = sourcePPT.getSlideMasters()[0].getLayout(SlideLayout.TITLE_ONLY);
			int counter = 1;
			for (DBObject item : items) {
				if (!item.get("name").equals("")) {
					createAndFillSlides(item, sourcePPT, targetPPT);
					System.out.println("Processing :" + (counter++) + " -- " + item.get("name"));
					if (counter > 10000)
						break;
				}
			}
			targetPPT.removeSlide(1);
			sourceStream.close();
			System.out.println("Presentation generated successfully");
			return targetPPT;

			

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public void createAndFillSlides(DBObject item, XMLSlideShow sourcePPT, XMLSlideShow targetPPT) {
		try {
			/*
			 * XSLFSlideLayout mainLayout=null; for (XSLFSlideMaster master :
			 * sourcePPT.getSlideMasters()) { for (XSLFSlideLayout layout :
			 * master.getSlideLayouts()) {
			 * //System.out.println(layout.getType());
			 * System.out.println(layout.getName());
			 * if(layout.getName().equals("Titre et contenu")){
			 * mainLayout=layout; }
			 * 
			 * } }
			 */

			int count = 0;
			for (XSLFSlide srcSlide : sourcePPT.getSlides()) {

				if (count++ == 0) // on ne duplique pas la premiere slide de
									// titre
					continue;

				XSLFSlideLayout slideLayout = srcSlide.getSlideLayout();
				XSLFSlide newSlide;
			
				if (count == 5) {//examples
					if(true)
						continue;
					newSlide = targetPPT.createSlide(slideLayout);
					DBObject query = new BasicDBObject("radarDetails_id", Integer.parseInt("" + item.get("id")));
					List<DBObject> exampleItems = getExamplesdata(query);
					int nExemplesBySlide=2;
					int index=0;
					int oldIndex=0;
				
					while(index<exampleItems.size()){
						newSlide = targetPPT.createSlide(slideLayout);
						newSlide.importContent(srcSlide);
						newSlide.setFollowMasterGraphics(true);
						
											index+=nExemplesBySlide;
						List<DBObject> pageExampleItems=new ArrayList();
						for(int i=oldIndex;i<exampleItems.size()&& i<index;i++){
							pageExampleItems.add(exampleItems.get(i));		
						}
						drawExamples(targetPPT, newSlide,item,pageExampleItems );
						oldIndex=index;
						
						
					}
					
				
					continue;
				}else{
					newSlide = targetPPT.createSlide(slideLayout);
					// XSLFSlideLayout layout= srcSlide.getMasterSheet();
					newSlide.importContent(srcSlide);
					newSlide.setFollowMasterGraphics(true);

					
				}

				XSLFShape[] shape = newSlide.getShapes();

				for (int j = 0; j < shape.length; j++) {

					XSLFShape aShape = shape[j];
					int KK = aShape.getShapeId();

					// XSLFAutoShape aShape2 = (XSLFAutoShape) aShape;
					if (aShape instanceof XSLFTable) {
						/*
						 * XSLFTable aTableShape = (XSLFTable) aShape; CTTable
						 * table=aTableShape.getCTTable();
						 */

					} else if (aShape instanceof XSLFTextShape) {
						XSLFTextShape aShape2 = (XSLFTextShape) aShape;
						String key = aShape2.getText();

						String replaceText = getItemText(item, key);
						aShape2.setText(replaceText);

						if(!key.startsWith("$")){
							
							aShape2.setVerticalAlignment(VerticalAlignment.MIDDLE);
							continue;
						}
						if (key.equals("$ex_corps")) {

							drawPicture(targetPPT, newSlide, aShape2, item, "$ex_img");
						}

						if (!key.equals("$TECHNO")) {
							XSLFTextParagraph paragraph = aShape2.getTextParagraphs().get(0);
							XSLFTextRun r1 = paragraph.getTextRuns().get(0);

							// r1.setText(replaceText);
							paragraph.setTextAlign(TextAlign.LEFT);
							//aShape2.setVerticalAlignment(VerticalAlignment.TOP);
							aShape2.setVerticalAlignment(VerticalAlignment.MIDDLE);
							// aShape2.setFillColor(new Color(200, 200, 255));
							 aShape2.setTextAutofit(TextAutofit.SHAPE);
							// r1.setFontColor(Color.DARK_GRAY);
							//r1.setFontSize(14);
							aShape2.setTextAutofit(TextAutofit.NORMAL);

							if (currentObject.equals("useCase")) {
								if (key.equals("$description") || key.equals("$dig-caps") || key.equals("$all-techs")) {
									r1.setFontSize(14);
									r1.setFontColor(Color.DARK_GRAY);
									aShape2.setVerticalAlignment(VerticalAlignment.TOP);
								
									
									if(key.equals("$dig-caps") || key.equals("$all-techs")){
										paragraph.setBullet(true);
									}
									if(key.equals("$all-techs")){
										r1.setBold(true);
									}
									
								} else if (key.startsWith("$")) {
									r1.setFontSize(18);
									r1.setFontColor(new Color(128, 0, 0));
								}
							}
							if (currentObject.equals("techno")) {
								if(key.equals("$inf_corps") || key.equals("$usage_corps") || key.equals("$ex_corps")|| key.equals("$usage_corps") || key.equals("$useCases") || key.equals("$impact")|| key.equals("$benefits")){
								
									r1.setFontSize(14);
									r1.setFontColor(Color.DARK_GRAY);
									aShape2.setVerticalAlignment(VerticalAlignment.TOP);
									//aShape2.set
								
								}
							}
						}
					}

					// if(key.equals("$Techno"))

					// System.out.println(str);
					// System.out.println(aShape.getShapeName());
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void drawExamples(XMLSlideShow targetPPT, XSLFSlide exampleSlide,DBObject radarItem,  List<DBObject> exampleItems) throws Exception {
		if(false)
			return;
		// examples
		

		XSLFShape[] shape = exampleSlide.getShapes();
		/*for (int j = 0; j < shape.length; j++) {
			XSLFShape aShape = shape[j];
			int KK = aShape.getShapeId();
			XSLFTextShape aShape2 = (XSLFTextShape) aShape;
			String key = aShape2.getText();
			String replaceText = getItemText(radarItem, key);
			aShape2.setText(replaceText);
		}*/
		if (exampleItems.size() > 0) {
			int i=0;
			double y = 60;
			for (DBObject exItem : exampleItems) {
				XSLFAutoShape exampleShape = exampleSlide.createAutoShape();
				if(i==0){
					i++;
					String replaceText = getItemText(exItem, "$TECHNO");
					exampleShape.setText(replaceText);
				}

				String exText = getItemText(exItem, "$corps");
				exampleShape.setText(exText);
				XSLFTextParagraph paragraph = exampleShape.getTextParagraphs().get(0);
				XSLFTextRun r1 = paragraph.getTextRuns().get(0);

				// r1.setText(exText);
				paragraph.setTextAlign(TextAlign.LEFT);
				exampleShape.setVerticalAlignment(VerticalAlignment.TOP);
				exampleShape.setFillColor(new Color(250, 250, 255));
				exampleShape.setLineColor(Color.BLUE);
				// aShape2.setTextAutofit(TextAutofit.NORMAL);
				// r1.setFontColor(Color.DARK_GRAY);

				TextAutofit autofit = exampleShape.getTextAutofit();
			//	exampleShape.setTextAutofit(TextAutofit.SHAPE);
			
				r1.setFontSize(12);
				Rectangle2D textAnchor = new Rectangle2D.Double(150, y, 500, 200);
				y += 220;
				exampleShape.setAnchor(textAnchor);
				exampleShape.setTextAutofit(TextAutofit.NORMAL);
				drawPicture(targetPPT, exampleSlide, exampleShape, exItem, "$img");

			}

		}

	}

	private String getItemText(DBObject item, String key) {
		if (!key.startsWith("$"))
			return key;
		if (key.equals("$Techno") || key.equals("$TECHNO"))
			key = key.toLowerCase();

		Object value = item.get(key.substring(1));
		if (value == null)

			return "";
		return "" + value;

	}

	private void drawPicture(XMLSlideShow targetPPT, XSLFSlide slide, XSLFShape shape, DBObject item, String fieldName) throws Exception {
		String picturePath = getItemText(item, fieldName);
		if (!picturePath.startsWith("$")) {// on a trouvé// l'image

			picturePath = pictureDir + picturePath;
			if (new File(picturePath).exists()) {
				byte[] img = extractBytes(picturePath);
				int idx = targetPPT.addPicture(img, XSLFPictureData.PICTURE_TYPE_PNG);

				Rectangle2D anchor = shape.getAnchor();
				XSLFPictureShape picture = slide.createPicture(idx);
				Rectangle2D pictAnchor = picture.getAnchor();
				double coef = pictAnchor.getWidth() / 80;
				double height = pictAnchor.getHeight() / coef;
				double y = anchor.getCenterY() - height / 2;
				picture.setAnchor(new Rectangle2D.Double(50, y, 80, height));
				// picture.setFlipHorizontal(true);

			}
		}
	}

	private List<DBObject> getTechnosdata(DBObject query) throws Exception {
		MongoProxy mongo = new MongoProxy("localhost", 27017, "POT");
		if (query == null)
			query = new BasicDBObject();
		List<DBObject> items = mongo.getDocuments("radarDetails", query, -1);
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

	private List<DBObject> getExamplesdata(DBObject query) throws Exception {

		MongoProxy mongo = new MongoProxy("localhost", 27017, "POT");
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

		MongoProxy mongo = new MongoProxy("localhost", 27017, "POT");
		if (query == null)
			query = new BasicDBObject();
		//query.put("bu", "HD");
		List<DBObject> items = mongo.getDocuments("use_cases", query, -1);
		items.sort(new Comparator<DBObject>() {

			@Override
			public int compare(DBObject item1, DBObject item2) {
				String title1 = item1.get("bu") + "/" + item1.get("BD") + "/" + item1.get("BC") + "/" + item1.get("name");
				String title2 = item2.get("bu") + "/" + item2.get("BD") + "/" + item2.get("BC") + "/" + item2.get("name");

				return title1.compareTo(title2);

			}
		});

		for (DBObject item : items) {
			String str = "";
			List<DBObject> dcs = (List<DBObject>) item.get("dcs");
			if(dcs!=null){
				
			
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
				//str += dc.get("dc") + " " + techsStr + "\n";
				str += dc.get("dc") + "\n";
			}
			if(str.length()>2)				
				item.put("dig-caps", str.substring(0,str.length()-2));
			String allTechsStr = "";
			for (String techno : allTechs) {
				allTechsStr += techno + "\n";
			}
			item.put("all-techs", allTechsStr);
			}
			else {
				System.out.println("no DCs for item : "+item.get("techno"));
			}
				
		}

		return items;
	}

	public byte[] extractBytes(String imagePath) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
		BufferedImage img = ImageIO.read(new File(imagePath));
		ImageIO.write(img, "png", baos);
		baos.flush();

		String base64String = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(baos.toByteArray());
		baos.close();

		byte[] bytearray = com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(base64String);

		return bytearray;
	}


public void saveXMLSlideShowAsFile(XMLSlideShow pptx,String targetPath) throws Exception{
	FileOutputStream targetOuputStream = new FileOutputStream(new File(targetPath));
	pptx.write(targetOuputStream);
	targetOuputStream.close();
	System.out.println("Presentation edited successfully");
}

public FileInputStream getXMLSlideShowAsStream(XMLSlideShow pptx) throws Exception{
	StringWriter strw=new StringWriter();
	 File file=TempFile.createTempFile("POT", "pptx");
	 FileOutputStream targetOuputStream = new FileOutputStream(file);
		pptx.write(targetOuputStream);
		targetOuputStream.close();
		FileInputStream	in=new FileInputStream(file);
		return in;
	
}
public File getXMLSlideShowFile(XMLSlideShow pptx) throws Exception{
	StringWriter strw=new StringWriter();
	 File file=TempFile.createTempFile("POT", "pptx");
	 FileOutputStream targetOuputStream = new FileOutputStream(file);
		pptx.write(targetOuputStream);
		targetOuputStream.close();
		return file;
		
	
}
}