package com.fauconnet.search;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.fauconnet.devisu.MongoProxy;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/*
 * 
 * http://javadoc.kyubu.de/elasticsearch/v1.4.2/
 * 
 */
public class TestElasticSearch {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestElasticSearch test = new TestElasticSearch();
	//test.deleteIndex("rss3d");
		test.indexDocs("rss3d");
		// List<String> words=new ArrayList<String>();

	//	test.findDocs("hollande");
	}

	public void indexDocs(String index) {
		try {
			//
			MongoProxy proxy = new MongoProxy("localhost", 27017, "rss3d");
			DBObject query = new BasicDBObject();
			DBCursor cursor = proxy.getDocumentsCursor("feedsDAPX", query, -1);
			int i = 0;
			Client client = getClient();
			for (DBObject doc : cursor) {

				try {
					String title = (String) doc.get("title");
					Date pubDate = (Date) doc.get("pubDate");
					Object desciption = doc.get("description");
					Object source = doc.get("source");
					String site = "";
					if(source!=null)
						site=(String) source;
					Object id = doc.get("id");
					String mongoId="";
					if(id!=null)
						mongoId=""+id;
					if (title == null || pubDate == null)
						continue;
					if (desciption == null)
						desciption = "";
					Map<String, Object> json = new HashMap<String, Object>();

					json.put("title", title);
					json.put("pubDate", pubDate);
					json.put("mongoId", mongoId);
					json.put("site", site);
					json.put("description", "" + desciption);

					IndexResponse response = client.prepareIndex(index, "feed", "" + doc.get("hashcode")).setSource(json).execute().actionGet();
					// System.out.println("--------------" + response);
					i += 1;
					if (i % 1000 == 0) {
						System.out.println("----indexed------" + i);
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

			}
			client.close();
			System.out.println("done" + cursor.size() + " docs");

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List findDocs(String keyword) {
		Client client = getClient();
		Date endDate = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(endDate); // Now use today date.
		c.add(Calendar.DATE, -1200); // Adding 5 days
		Date startDate = c.getTime();

		c.add(Calendar.DATE, 1200); // Adding 5 days
		endDate = c.getTime();

		SearchResponse response = client.prepareSearch("feeds-dap").setTypes("feed").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.termQuery("title", "hollande"))
				.setPostFilter(FilterBuilders.rangeFilter("pubDate").from(startDate).to(endDate))
				.setPostFilter(FilterBuilders.termFilter("title", "sarkozy"))
				.setFrom(0).setSize(1000000)
				.execute().actionGet();
		// .setQuery(QueryBuilders.termQuery("title", "Ayrault")) // Query
				// .setPostFilter(FilterBuilders.rangeFilter("pubDate").from(12).to(18))//
				// // Filter
				// .setQuery(QueryBuilders.multiMatchQuery("title", "hollande"
				// ))

				
				// .setFrom(0).setSize(60).setExplain(true)
				
		/*
		 * SearchHit[] hits=response.getHits().hits(); for( int
		 * i=0;i<hits.length;i++){ SearchHit hit=hits[i];
		 * System.out.println(hit.sourceAsString()); }
		 */

		SearchHit[] results = response.getHits().getHits();
		System.out.println("Current results: " + results.length);
		for (SearchHit hit : results) {
			Map<String, Object> result = hit.getSource();
			System.out.println(result);
		}
		client.close();
		return null;
	}

	public void deleteIndex(String indexName) {
		Client client = getClient();
		final DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
		final DeleteIndexResponse deleteIndexResponse = client.admin().indices().delete(deleteIndexRequest).actionGet();
		client.close();
		if (!deleteIndexResponse.isAcknowledged()) {
			System.out.println("Index " + indexName + " not deleted");
		} else {
			System.out.println("Index " + indexName + " deleted");
		}
	}

	private Client getClient() {
		return new TransportClient().addTransportAddress(new InetSocketTransportAddress("192.168.1.76", 9300));
	}

}
