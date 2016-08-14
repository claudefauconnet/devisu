package com.fauconnet.devisu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram.Bucket;
import org.elasticsearch.search.sort.SortOrder;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;



/*
 * 
 * http://javadoc.kyubu.de/elasticsearch/v1.4.2/
 * 
 * 
 */
public class ElasticSearchManager {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Date endDate = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(endDate); // Now use today date.
		c.add(Calendar.DATE, -10000); // Adding 5 days
		Date startDate = c.getTime();
		c.add(Calendar.DATE, 10000); // Adding 5 days
		endDate = c.getTime();

		ElasticSearchManager manager;
		try {
			manager = new ElasticSearchManager();
			//manager.searchInFeedsTitles(new String[] { "hollande", "merkel" }, startDate, endDate);
			manager.searchAndAggregate("feeds_dap","feed",new String[] { "hollande"}, startDate,endDate,"week");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Client client;

	public ElasticSearchManager() {
		try{
		this.client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		// this.client= new TransportClient().addTransportAddress(new
		// InetSocketTransportAddress("localhost", 9300));
	}

	public List<DBObject> searchInFeedsTitles(String index,String type,String[] keywords, Date startDate, Date endDate) {
		List<DBObject> objs = new ArrayList<DBObject>();
		if (keywords.length == 0 && startDate == null && endDate == null)
			return objs;

		SearchRequestBuilder requestBuilder = getSearchRequest( index, type,keywords, startDate, endDate);
		SearchResponse response = requestBuilder.execute().actionGet();

		SearchHit[] results = response.getHits().getHits();
		System.out.println("Current results: " + results.length);

		for (SearchHit hit : results) {
			Map<String, Object> result = hit.getSource();
			DBObject obj = new BasicDBObject(result);
			objs.add(obj);
			// System.out.println(result);

		}

		return objs;

	}

	public List<DBObject> searchAndAggregate(String index,String type,String[] keywords, Date startDate, Date endDate, String  periodicityStr) {
		DateHistogram.Interval periodicity=DateHistogram.Interval.DAY;
		if(periodicityStr.equals("week"))
				 periodicity=DateHistogram.Interval.WEEK;
		else if(periodicityStr.equals("month"))
			 periodicity=DateHistogram.Interval.MONTH;
		if(periodicityStr.equals("year"))
			 periodicity=DateHistogram.Interval.YEAR;
		if(periodicityStr.equals("day"))
			 periodicity=DateHistogram.Interval.DAY;
		if(periodicityStr.equals("hour"))
			 periodicity=DateHistogram.Interval.HOUR;
		
		
		List<DBObject> objs = new ArrayList<DBObject>();
		SearchRequestBuilder requestBuilder = getSearchRequest( index, type,keywords, startDate, endDate);
		requestBuilder.addAggregation(AggregationBuilders.dateHistogram("histo").field("pubDate").interval(periodicity).subAggregation(
                  AggregationBuilders.sum("pubDate").field("pubDate")));
	//	AggregationBuilders.terms(name)
		
					requestBuilder.addSort("pubDate",SortOrder.ASC);
                  SearchResponse response=  requestBuilder.execute().actionGet();
                  DateHistogram histo=response.getAggregations().get("histo");
                  List<Bucket > buckets=(List<Bucket>) histo.getBuckets();
                  for (Bucket  bucket :buckets){
                	 DateTime key= bucket.getKeyAsDate();
                	 Long date=key.toDate().getTime();
                	long freq= bucket.getDocCount();
                	 DBObject obj = new BasicDBObject();
                	 obj.put("x",date);
                	 obj.put("y",freq);
                	 objs.add(obj);
                	
                  }
		
                //  System.out.println(objs);

		return objs;

	}

	private SearchRequestBuilder getSearchRequest(String index,String type,String[] keywords, Date startDate, Date endDate) {
		SearchRequestBuilder requestBuilder = client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setFrom(0).setSize(1000000);

		if (keywords.length > 0 && keywords[0].length() > 0) {
			requestBuilder.setQuery(QueryBuilders.termQuery("title", keywords[0]));
		} else {
			requestBuilder.setQuery(QueryBuilders.regexpQuery("title", ".*"));

		}
startDate=null;
		if (startDate != null && endDate != null) {
			requestBuilder.setPostFilter(FilterBuilders.rangeFilter("pubDate").from(startDate.getTime()).to(endDate.getTime()));
		}
		for (int i = keywords.length - 1; i > 0; i--) {
			requestBuilder.setPostFilter(FilterBuilders.termFilter("title", keywords[i]));
		}
		return requestBuilder;
	}
}
