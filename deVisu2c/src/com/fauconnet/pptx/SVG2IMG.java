package com.fauconnet.pptx;



import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.apache.batik.apps.rasterizer.Main;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class SVG2IMG  implements Runnable{
	private String dir = "C:\\Local\\workspace3\\deVisu2b\\WebContent\\data\\";
	private String path=dir+"ArchMap_2016534.svg";
	
	public SVG2IMG(String dir, String path){
		this.dir=dir;
		this.path=path;
	}
 
    public static void main(String[] args) throws Exception {
		String dir = "C:\\Local\\workspace3\\deVisu2b\\WebContent\\data\\";
    	String path=dir+"ArchMap_2016534.svg";
    	convertSVG(dir, path);
   
    }
    public void run(){
    	try {
			convertSVG();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

    private  void convertSVG() throws Exception{
    	String[] command=new String[]{"-scriptSecurityOff","-m","image/png","-d",dir,path};
    	Main.main(command);
  }
    
  
    
    
    
    public static void convertSVG(String dir, String path) {
    	//  Thread t = new Thread(new SVG2IMG(dir,path));
       //   t.start();
    	try{
    	String[] args=new String[]{"-scriptSecurityOff","-m","image/png","-d",dir,path};
    	  (new Main(args)).execute();
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}

    }
    
    
  
    
   
}