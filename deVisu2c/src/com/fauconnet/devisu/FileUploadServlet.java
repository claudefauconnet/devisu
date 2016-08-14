package com.fauconnet.devisu;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
/**
 * A Java servlet that handles file upload from client.
 *
 * @author www.codejava.net
 */

public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
     
    // location to store file uploaded
    private static final String UPLOAD_DIRECTORY = "data";
 
    // upload settings
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB
    
    
  
	private String xmlFilesDir = "";
	private String dataDirPath = "";
	private String xmlFilePath = "";


	private Map<String, I_DataManager> dataManagers = new HashMap<String, I_DataManager>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */

	public void init() throws ServletException {

		try {
			dataDirPath = this.getServletContext().getRealPath("/data");
			xmlFilesDir = dataDirPath;
			xmlFilePath = this.getServletContext().getRealPath("/data/radars.xml");
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e.toString());

		}
	}

    /**
     * Upon receiving file upload submission, parses the request to read
     * upload data and saves the file on disk.
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // checks if the request actually contains upload file
    	
        if (!ServletFileUpload.isMultipartContent(request)) {
        	//printRequestParams(request);
        	String dbName=request.getParameter("dbName");
        	if (dbName==null){
        		PrintWriter writer = response.getWriter();
        		String responseStr="xml saved";
        		 writer.println("{\"OK\":\"" + responseStr + "\"}");
                 writer.flush();
                 return;
        	}
        	StringBuffer jb = new StringBuffer();
			String line = null;

				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					jb.append(line);
				if (jb.length() > 0){
					//System.out.println( jb.toString());
					try {
						saveRadarXml( dbName, jb.toString() );
					} catch (Exception e) {
						e.printStackTrace();
						throw new ServletException("Error: cannot save File");
					}
				}
				
            // if not, we stop here
          /*  PrintWriter writer = response.getWriter();
            writer.println("Error: Form must has enctype=multipart/form-data.");
            writer.flush();*/
            return;
        }
 
        // configures upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // sets memory threshold - beyond which files are stored in disk
        factory.setSizeThreshold(MEMORY_THRESHOLD);
        // sets temporary location to store files
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
 
        ServletFileUpload upload = new ServletFileUpload(factory);
         
        // sets maximum size of upload file
        upload.setFileSizeMax(MAX_FILE_SIZE);
         
        // sets maximum size of request (include file + form data)
        upload.setSizeMax(MAX_REQUEST_SIZE);
 
        // constructs the directory path to store upload file
        // this path is relative to application's directory
        String uploadPath = getServletContext().getRealPath("")
                + File.separator + UPLOAD_DIRECTORY;
         
        // creates the directory if it does not exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
 
        try {
            // parses the request's content to extract file data
            @SuppressWarnings("unchecked")
            List<FileItem> formItems = upload.parseRequest(request);
 
            if (formItems != null && formItems.size() > 0) {
            	String dbName=null;
            	String xmlData=null;
            	for (FileItem item : formItems) {
                	String name=item.getFieldName();
                	if(name.equals("dbName"))
                			dbName=item.getString();
                	if(name.equals("xmlData"))
                		xmlData=item.getString();
                	
            	}
            if(dbName==null || xmlData==null){
            	throw new Exception("Error: cannot save File");

            }
            	
            	try {
					saveRadarXml( dbName, xmlData );
					PrintWriter writer = response.getWriter();
	        		 String responseStr="Error: cannot save File";
	        		 writer.println("{\"OK\":\"" + responseStr + "\"}");
	                 writer.flush();
	                 return;
				} catch (Exception e) {
					e.printStackTrace();
					PrintWriter writer = response.getWriter();
	        		 String responseStr="Error: cannot save File";
	        		 writer.println("{\"OK\":\"" + responseStr + "\"}");
	                 writer.flush();
	                 return;
				}
            	
                // iterates over form's fields
           
            	/*for (FileItem item : formItems) {
                	System.out.println(item.getFieldName());
                	System.out.println(item.getString());
                	System.out.println(item);
                	
                    // processes only fields that are not form fields
                 /*   if (!item.isFormField()) {
                        String fileName = new File(item.getName()).getName();
                        String filePath = uploadPath + File.separator + fileName;
                        File storeFile = new File(filePath);
 
                        // saves the file on disk
                        item.write(storeFile);
                        request.setAttribute("message",
                            "Upload has been done successfully!");
                    }
                }*/
            }
        } catch (Exception ex) {
            request.setAttribute("message",
                    "There was an error: " + ex.getMessage());
        }
        // redirects client to message page
        getServletContext().getRequestDispatcher("/message.jsp").forward(
                request, response);
    }
    
    private void printRequestParams(HttpServletRequest request) {
		Iterator it = request.getParameterMap().keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			String value=(String)request.getParameterMap().get(key);
			System.out.println(key + "  :  " + value);
		}
	}
    
    public String saveRadarXml(String dbName, String xmlData ) throws Exception {

			String name = dbName + ".xml";
			String dir = xmlFilesDir.endsWith(File.separator) ? xmlFilesDir : xmlFilesDir + File.separator;
			FileWriter fw = new FileWriter(dir + name);
			System.out.println(dir + name);
			fw.write(xmlData);
			fw.close();
			return "ok";
}
}