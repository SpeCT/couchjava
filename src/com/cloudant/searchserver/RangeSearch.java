package com.cloudant.searchserver;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudant.index.CouchdbIndexReader;




public class RangeSearch extends HttpServlet implements Closeable {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String field = "*";
//		response.setContentType("application/json;charset=utf-8");
//		Cookie[] cookies = request.getCookies(); 
		PrintWriter out = new PrintWriter(System.out);
		out = response.getWriter();
		JSONObject jout = new JSONObject();
		String query = request.getParameter("q");
		String range = request.getParameter("range");
		String rangeStart = request.getParameter("rangestart");
		String rangeEnd = request.getParameter("rangeend");
		if (query == null && (range==null || rangeStart==null || rangeEnd==null)) {
			out.println("must specify query with \"q=\" or rangefield and values with \"range=<field>&rangestart=<start>&rangeend=<end>\"");
			out.close();
			return; 
		}
		String startString = request.getParameter("start");
		int start = 0;
		if (startString != null) {
			start = Integer.parseInt(startString);
		}
		String endString = request.getParameter("end");
		int end = start + 10;
		if (endString != null) {
			end = Integer.parseInt(endString);
		}
		String queryString = request.getQueryString();
		String urlString = null;
		if (queryString.contains("url=")) {
			int startUrl = queryString.indexOf("url=");
			int endUrl = queryString.indexOf("&",startUrl);
			if (endUrl > -1) {
				urlString = queryString.substring(startUrl+4,endUrl);
			} else {
				urlString = queryString.substring(startUrl +4);
			}
		}
//		System.out.println(queryString + " : " + urlString);
//		String urlString = request.getParameter("url");
		try {
		if (urlString == null) {
			urlString = "http://ec2-174-129-116-148.compute-1.amazonaws.com:5984/hayward/";
//			urlString = "http://localhost:5984/twitter/";
			// comment out for testing
//			jout.put("error", "need to specify index url as parameter");
//			out.println(jout.toString());
//			out.close();
//			return;
		}
	    IndexReader reader = CouchdbIndexReader.open(urlString, "admin", "iaj3nubr3abr2oyff1ye");
	    

	    Searcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
	    QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, field, analyzer);
	    Query luceneQuery = null;
	    try {
	    	if (query != null) {
	    		luceneQuery = parser.parse(query);
	    	}
		    if (range != null && rangeStart != null && rangeEnd != null) {
		    	Term t = new Term("cloudant_range",range + "," + rangeStart + ","+rangeEnd);
		    	Query rangeQuery = new TermQuery(t);
		    	BooleanQuery bq = new BooleanQuery();
		    	if (luceneQuery != null) bq.add(luceneQuery,BooleanClause.Occur.MUST);
		    	bq.add(rangeQuery,BooleanClause.Occur.MUST);
		    	luceneQuery = bq;
		    }
	    } catch (ParseException pe) {
			jout.put("error", "cannot parse query " + query);
			out.println(jout.toString());
			out.close();
			return;
	    	
	    }
	    long starttime = System.currentTimeMillis();
	    ScoreDoc[] hits = null;
	    int numTotalHits = 0;
	    int maxToFetch = end;
	    TopScoreDocCollector collector = TopScoreDocCollector.create(
			          maxToFetch, false);
	    searcher.search(luceneQuery, collector);
	    hits = collector.topDocs().scoreDocs;
		numTotalHits = collector.getTotalHits();
	    long totalTime = System.currentTimeMillis() - starttime;
	      
//	    System.out.println("number of hits = " + numTotalHits);
	    jout.put("matching_docs", numTotalHits);
	    jout.put("time", totalTime);
	        
//	        long time = System.currentTimeMillis() - starttime;
	        
	    JSONArray jsonArr = new JSONArray();
	    int max = Math.min(numTotalHits, end);
//	    System.out.println("max = " + max);
	    for (int i = start; i < max; i++) {
//	    	System.out.println("i = " + i);
	    	try {
	            jsonArr.put(((CouchdbIndexReader)reader).getCouchId(hits[i].doc));
	    	} catch (NoSuchFieldException nfe) {
	    		String err = "Can't find couch id for lucene id " + String.valueOf(hits[i].doc);
	    		jsonArr.put(err);
	    	}
	    }
	    if (urlString.contains("twitter")) {
	    	List<String> results = new ArrayList<String>();
	    	for (int i = 0; i<jsonArr.length(); i++) {
	    		results.add(jsonArr.getString(i));
	    	}
	    	Collections.sort(results, Collections.reverseOrder());
	    	for (int i = 0; i<jsonArr.length(); i++) {
	    		jsonArr.put(i, results.get(i));
	    	}	    	
	    }
	    
	    jout.put("values",jsonArr);
	    } catch (JSONException e) {
	    	
	    }
		
		out.println(jout.toString());
		out.close();
	}
		
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

}
