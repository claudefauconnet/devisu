package com.fauconnet.devisu;






import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class LoadMongoToElastic implements LineProcessor {
	int lineNum = 0;
	int maxErrors=0;
	String[] headers = null;
	private Client elasticClient;
	Settings settings = new Settings();
	SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd hhmmss");

	int fileIndex = 0;

	// String elasticHost="FRHDSTD-AEFL016";
	String elasticHost = "127.0.0.1";
//String  elasticHost = "10.27.63.103";
	int elasticPort = 9300;
	int totalLines = 0;

	public static void main(String[] args) {
		String dirOrFileStr = "C:\\Local\\acccessit2015files";
		// dirOrFileStr="C:\\Local\\AccessIt\\data\\20150401000.TAB";
		dirOrFileStr = "C:\\Local\\AccessIt\\data";
		
		dirOrFileStr = "\\\\Frhdstd-aefl016\\downloads\\accessit2015files\\all";
		if (args.length > 0) {
			dirOrFileStr = args[0];
		}
		

		LoadMongoToElastic processor = new LoadMongoToElastic();
		

		int option= 0;
		
		
		switch (option) {
		case 0: {// import in index
			//http://frhdstd-aefl016:8081/accessit/indexer?action=index&dir=D%3A%5Caccessit2015files%5Call&deleteIndex=true
			//http://frhdstd-aefl016:8081/accessit/indexer?action=index&dir=D%3A%5Caccessit2015files%5Call&index=accessit2
			//http://frhdstd-aefl016:8081/accessit/indexer?action=index&dir==D%3A%5Caccessit2015files%5Cmissing&index=accessit2
			File dirOrFile = new File(dirOrFileStr);

		
			processor.elasticClient = processor.getClient();
			if (dirOrFile.isDirectory()) {
				processor.loadAndProcessFiles(dirOrFileStr, true);
			} else {
				processor.importCsvToElastic(dirOrFileStr, true);

			}
			processor.elasticClient.close();

			break;
		}
		case 1: {// search
			
			processor.doQuery(processor.settings.index,processor.settings.type , "last_name:LACROIX", "CSV");
			
			
			break;
		}
		
case 2: {// deleteIndex
			
			processor.deleteIndex("accessit");
			
			
			break;
		}
		}

	}

	public void startDirOrFileIndexation(String dirOrFileStr, String index, boolean deleteIndex) {
	
		File dirOrFile = new File(dirOrFileStr);

		LoadMongoToElastic processor = new LoadMongoToElastic();
		
		processor.settings.index =index;
		processor.settings.type = "entry";
		processor.settings.key = "at_id";
		
		processor.elasticClient = processor.getClient();
		if (dirOrFile.isDirectory()) {
			processor.loadAndProcessFiles(dirOrFileStr, deleteIndex);
		} else {
			processor.importCsvToElastic(dirOrFileStr, deleteIndex);

		}
		processor.elasticClient.close();

	}

	
	public void startMongoIndexation(MongoSettings mongoSettings, String index, boolean deleteIndex) {
		
	MongoP

		LoadMongoToElastic processor = new LoadMongoToElastic();
		
		processor.settings.index =index;
		processor.settings.type = "entry";
		processor.settings.key = "at_id";
		
		processor.elasticClient = processor.getClient();
		
		processor.elasticClient.close();

	}
	private void loadAndProcessFiles(String dirStr, boolean deleteIndex) {
		File dir = new File(dirStr);
		File[] files = dir.listFiles();
		// / boolean deleteIndex=true;

		for (File file : files) {

			if (file.getName().endsWith("TAB")) {
				System.out.println("" + (fileIndex++) + " importing  file " + file.getName());
				importCsvToElastic(file, deleteIndex);
			}
			deleteIndex = false;
		}
		System.out.println("DONE" + (fileIndex--) + "   files imported ");

	}

	private void importCsvToElastic(String uri, boolean deleteIndex) {
		importCsvToElastic(new File(uri), deleteIndex);
	}

	private void importCsvToElastic(File file, boolean deleteIndex) {

		try {
			if (deleteIndex) {
				System.out.println("DELETE index" + settings.index);
				deleteIndex(settings.index);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		loadAndProcessFile(file, this);
		System.out.println("--DONE--");

	}

	public void processLine(String line) {
		if (lineNum == 0) {// header
			headers = line.split("\t");
			System.out.println(Arrays.asList(headers).toString());

		} else {
			String[] values = line.split("\t");
			Map<String, Object> json = new HashMap<String, Object>();
			int i = 0;
			for (String value : values) {
				Object obj = convert(value);
				if (headers[i].equals("date_occurred_server") || headers[i].equals("time_occurred_server")){
					i++;
					continue;
				}
				json.put(headers[i++], obj);

			}
			Date date = getDate(json);
			if (date == null)
				return;
			json.put("date", date);
			totalLines++;
			if (lineNum % 1000 == 0)
				System.out.println("file : " + fileIndex + " lines imported :" + lineNum + " total lines" + totalLines);
			try {
				;
				String key = "" + json.get("date_occurred") + json.get("time_occurred") + "_AT_" + json.get("at_id");
				// IndexResponse response =
				// elasticClient.prepareIndex(settings.index, settings.type, ""
				// +
				// json.get(settings.key)).setSource(json).execute().actionGet();
				IndexResponse response = elasticClient.prepareIndex(settings.index, settings.type, key).setSource(json).execute().actionGet();
			} catch (Exception e) {
				if(maxErrors++>500)
					return;
				System.out.println("ERROR " + e.toString() );//+ "  :  " + json);
			}

		}
		lineNum++;

	}

	private Date getDate(Map<String, Object> map) {
		long day = (Long) map.get("date_occurred");
		String time = "" + (Long) map.get("time_occurred");
		while (time.length() < 6) {
			time = "0" + time;
		}

		try {
			Date date = dt.parse("" + day + " " + time);
			// System.out.println (date);
			return date;
		} catch (ParseException e) {
			System.out.println("--cannot parse date--" + day + " " + time + "   " + map.get(settings.key));
			return null;
		}

	}

	private static Object convert(String str) {

		if (str.matches("-*[0-9]+\\.+[0-9]*")) {

			;
			return Float.parseFloat(str);
		}
		if (str.matches("-*[0-9]+"))
			return Long.parseLong(str);
		return str;

	}

	private String loadAndProcessFile(File file, LineProcessor processor) {

		String text = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));

			String line = "";
			lineNum = 0;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				if (processor != null) {
					processor.processLine(line);
				} else
					text += line;

			}
			br.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text;
	}

	class Settings {
		String index;
		String type;
		String field;
		String key;

	}

	void deleteIndex(String indexName) {
		Client client = getClient();
		final DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
		final DeleteIndexResponse deleteIndexResponse = client.admin().indices().delete(deleteIndexRequest).actionGet();
		// client.close();
		if (!deleteIndexResponse.isAcknowledged()) {
			System.out.println("Index " + indexName + " not deleted");
		} else {
			System.out.println("Index " + indexName + " deleted");
		}
	}

	@SuppressWarnings("resource")
	private Client getClient() {

		if (elasticClient == null)
			// return new TransportClient().addTransportAddress(new
			// InetSocketTransportAddress("192.168.1.75", 9300));
			elasticClient = new TransportClient().addTransportAddress(new InetSocketTransportAddress(elasticHost, elasticPort));
		return elasticClient;

		// return new TransportClient().addTransportAddress(new
		// InetSocketTransportAddress("localhost", 9300));

	}

	public String doQuery(String index, String type, String query, String format) {

		SearchRequestBuilder requestBuilder = getClient().prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setFrom(0).setSize(1000000);
		// SearchRequestBuilder requestBuilder =
		// getClient().prepareSearch(index).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setFrom(0).setSize(1000000);
		int p = query.indexOf(":");
		if (p > -1) {
			String term = query.substring(0, p);
			String q = query.substring(p + 1);
			requestBuilder.setQuery(QueryBuilders.termQuery("last_name", "fau*"));

		} else if (query.length() > 0) {
			requestBuilder.setQuery(QueryBuilders.simpleQueryStringQuery(query));
		} else {
			requestBuilder.setQuery(QueryBuilders.regexpQuery("title", ".*"));

		}

		SearchResponse response = requestBuilder.execute().actionGet();

		SearchHit[] results = response.getHits().getHits();
		System.out.println("Current results: " + results.length);
		StringBuffer sb = new StringBuffer();

		for (SearchHit hit : results) {
			Map<String, Object> result = hit.getSource();

			System.out.println(result);
			sb.append(result + "\n");

		}
		return sb.toString();

	}

}
