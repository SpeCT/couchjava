package com.cloudant.searchserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import javax.servlet.http.Cookie;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudant.index.CouchdbIndexReader;

public class ExternalServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			try {
				String field = "*";
//				response.setContentType("application/json;charset=utf-8");
				JSONObject input = new JSONObject(line);
				String peer = input.getString("peer");
				JSONObject headers = input.getJSONObject("headers");
//				Cookie[] cookies = input.getString("cookies"); 
				JSONObject jout = new JSONObject();
				JSONObject query = input.getJSONObject("query");
				String searchQuery = query.getString("q");
				if (searchQuery == null) {
					JSONObject output = new JSONObject();
					Integer response = new Integer(200);
					output.put("code", response);
					jout.put("error", "format: q=<search query>");
					output.append("json", jout);
					System.out.println(output.toString());
					continue; 
				}
				String sortField = query.optString("sortby", null);
				String startString = query.optString("start", null);
				int start = 0;
				if (startString != null) {
					start = Integer.parseInt(startString);
				}
				String endString = query.optString("end", null);
				int end = start + 10;
				if (endString != null) {
					end = Integer.parseInt(endString);
				}
				StringBuilder sb = new StringBuilder();
				String host = headers.getString("Host");
				if (!host.startsWith("http://")) sb.append("http://");
				sb.append(host);
				if (!host.endsWith("/")) sb.append("/");
				JSONObject userCtx = input.getJSONObject("userCtx");
				String db = userCtx.getString("db");
				sb.append(db);
				sb.append("/");
				String urlString = sb.toString();
				IndexReader reader = null;
//				if (cookies != null) {
//					reader = CouchdbIndexReader.open(urlString, cookies);
				try {
					try {
						String authorization = headers.getString("Authorization");
						reader = CouchdbIndexReader.open(urlString, authorization);
						if (!((CouchdbIndexReader) reader).checkIndexExists()) throw new IOException("search index not available for this database.  Please enable if you have a paid account, or contact support@cloudant.com for pricing information.");
					} catch (JSONException je) {
						reader = CouchdbIndexReader.open(urlString);
						if (!((CouchdbIndexReader) reader).checkIndexExists()) throw new IOException("search index not available for this database.  Please enable if you have a paid account, or contact support@cloudant.com for pricing information.");
					}
				} catch (IOException ioe) {
					JSONObject output = new JSONObject();
					Integer response = new Integer(200);
					output.put("code", response);
					jout.put("error", ioe.getMessage());
					output.append("json", jout);
					System.out.println(output.toString());
					continue;					
				}

			    Searcher searcher = new IndexSearcher(reader);
//			    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			    PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_CURRENT));
			    analyzer.addAnalyzer("cloudant_range", new KeywordAnalyzer());
			    Sort sorter = null;
			    if (sortField !=  null) {
			    	sorter = new Sort(new SortField(sortField, SortField.LONG));
			    }
			    // need to mess with security manager to get this working
			    //	    Analyzer analyzer = null;
//			    try {
//			    	analyzer = ((CouchdbIndexReader) reader).getAnalyzer();
//			    } catch (FileNotFoundException e) {
//			    	System.out.println("Using Standard Analyzer");
//				    analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);	    	
//			    }
			    QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, field, analyzer);
			    parser.setDefaultOperator(QueryParser.AND_OPERATOR);
			    Query luceneQuery = null;
			    try {
			    	luceneQuery = parser.parse(searchQuery);
			    } catch (ParseException pe) {
					jout.put("error", "cannot parse query " + query);
					JSONObject output = new JSONObject();
					Integer response = new Integer(200);
					output.put("code", response);
					output.append("json", jout);
					System.out.println(output.toString());
					continue;
			    	
			    }
			    long starttime = System.currentTimeMillis();
			    ScoreDoc[] hits = null;
			    int numTotalHits = 0;
			    if (sorter == null) {
			    	int maxToFetch = end;
			    	TopScoreDocCollector collector = TopScoreDocCollector.create(
					          maxToFetch, false);
			    	searcher.search(luceneQuery, collector);
			    	hits = collector.topDocs().scoreDocs;
				    numTotalHits = collector.getTotalHits();
			    } else {
			    	TopFieldDocs docs = searcher.search(luceneQuery, null, end, sorter);
			    	hits = docs.scoreDocs;
			    	numTotalHits = docs.totalHits;
			    }
			    JSONObject output = new JSONObject();
			    int status = ((CouchdbIndexReader)reader).getHttpResponse();
			    output.put("code", status);
			    long totalTime = System.currentTimeMillis() - starttime;
			    if (status != 200) {
					jout.put("error", "problem reading CouchDb");
					output.append("json", jout);
					System.out.println(output.toString());
					continue;
			    }
			    
//			    System.out.println("number of hits = " + numTotalHits);
			    jout.put("matching_docs", numTotalHits);
			    jout.put("time", totalTime);
			    jout.put("query",luceneQuery.toString());
			    
//			        long time = System.currentTimeMillis() - starttime;
			        
			    JSONArray jsonArr = new JSONArray();
			    int max = Math.min(numTotalHits, end);
//			    System.out.println("max = " + max);
			    for (int i = start; i < max; i++) {
//			    	System.out.println("i = " + i);
			    	try {
			            jsonArr.put(((CouchdbIndexReader)reader).getCouchId(hits[i].doc));
			    	} catch (NoSuchFieldException nfe) {
			    		String err = "Can't find couch id for lucene id " + String.valueOf(hits[i].doc);
			    		jsonArr.put(err);
			    	}
			    }
			    boolean sortById = false;
			    if (sortById) {
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
				output.append("json", jout);
				System.out.println(output.toString());
				
			}	catch (Exception e) {
				String message = (e == null) ? " Unknown " : e.getMessage();
				JSONObject output = new JSONObject();
				Integer response = new Integer(200);
				try {
				output.put("code", response);
				JSONObject jout = new JSONObject();
				jout.put("error", message);
				output.append("json", jout);
				System.out.println(output.toString());
				} catch (JSONException je) {
					
				}
			}
		}

	}
	
	
}
