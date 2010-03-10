package com.cloudant.javaviews;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.cloudant.couchdbjavaserver.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.util.*;

public class LuceneDoc implements JavaView {

	private Map<String, Float> fieldsToStore = null;
	
	private int reduceCount = 0;

	public void Log(String message) {
		JSONArray out = new JSONArray();
		out.put("log");
		out.put(message);
		System.out.println(out.toString());
	}

	public JSONArray MapDoc(JSONObject doc) {
		JSONArray out = new JSONArray();
		String id = null;
		try {
			id = doc.getString("_id");
			String rev = doc.getString("_rev");
			// Directory ramDir = new RAMDirectory();
			// IndexWriter writer = new IndexWriter(ramDir, new
			// StandardAnalyzer(Version.LUCENE_CURRENT),
			// IndexWriter.MaxFieldLength.UNLIMITED);
			// Document d = new Document();
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
			MemoryIndex index = new MemoryIndex();
			for (String field : fieldsToStore.keySet()) {
				String text = findFieldString(field, doc);
				if (field != null && text != null) {
					index.addField(field, text, analyzer, fieldsToStore.get(field));
				}
			}
			out = index.jsonMap();
		} catch (JSONException je) {
			out.put(new JSONArray());
			Log("Malformed document: " + doc.toString());
		} catch (Exception e) {
			out.put(new JSONArray());
			if (e != null ) {
				Log("Exception: " + e.toString());
			}
		}
		return out;
	}

	public JSONArray ReReduce(JSONArray reduceResults) {
//		return new JSONArray().put(JSONObject.NULL);
		return ReReduceAggregate(reduceResults);
	}
	

	public JSONArray ReReduceAggregate(JSONArray reduceResults) {
		// TODO Auto-generated method stub
		if (reduceResults == null || reduceResults.length() == 0) {
			Log("Reduce called with empty map");
			return new JSONArray().put(JSONObject.NULL);
		}
		JSONArray allDocs = new JSONArray();
		boolean good = true;
		HashMap<String, JSONArray> wordCount = new HashMap<String, JSONArray>();
//		HashSet<String> uniques = new HashSet<String>();
		try {
				for (int j = 0; j < reduceResults.length(); j++) {
					try {
					JSONObject jobj = reduceResults.getJSONObject(j);
//					if (jobj.has("FIELDS")) {
//						JSONArray fields = jobj.getJSONArray("FIELDS");
//						for (int k = 0; k < fields.length(); k++) {
//							uniques.add(fields.getString(k));
//						}
//					} else {
					Iterator<String> iter = (Iterator<String>)jobj.keys();
					while (iter.hasNext()) {
						String key = iter.next();
						if (wordCount.containsKey(key)) {
							wordCount.put(key, wordCount.get(key).put(jobj.getJSONArray(key)));
						} else {
							wordCount.put(key, jobj.getJSONArray(key));
						}
					}
						allDocs.put(jobj);
					} catch (JSONException je) {
						Log(je.getMessage());
						good = false;
					}
				}
			JSONObject job = new JSONObject();
			if (wordCount.size() > 0) {
				for (String key : wordCount.keySet()) {
					try {
						job.put(key, wordCount.get(key));
					} catch (JSONException je) {
						Log("Problem update output array");
					}
				}
				if (job.length() == 0) {
					return new JSONArray().put(JSONObject.NULL);
				} else {
					return new JSONArray().put(job);
				}
			}
		} 	catch (Exception e) {
			Log (e.getMessage());
		}
		return new JSONArray().put(JSONObject.NULL);
	}

	public JSONArray Reduce(JSONArray mapResults) {
//		return new JSONArray().put(JSONObject.NULL);
		return ReduceAggregate(mapResults);
	}
	
	private JSONArray ReduceAggregate(JSONArray mapResults) {
		if (mapResults == null || mapResults.length() == 0) {
			Log("Reduce called with empty map");
			return new JSONArray().put(JSONObject.NULL);
		}
		HashMap<String, JSONArray> wordCount = new HashMap<String, JSONArray>();
		HashMap<String, JSONArray> docCount = new HashMap<String, JSONArray>();
		HashSet<String> uniques = new HashSet<String>();
		for (int i = 0; i < mapResults.length(); i++) {
			try {
				JSONArray keyId = mapResults.getJSONArray(i).getJSONArray(0);
				String key = keyId.getString(0);
				String id = keyId.getString(1);
	//			String idkey = "ID" + id;
				// map result is an array of json objects
				JSONArray arr = mapResults.getJSONArray(i).getJSONArray(1);
				for (int j = 0; j < arr.length(); j++) {
					JSONObject thisObj = arr.getJSONObject(j);
//					try {
//						uniques.add(thisObj.getString("field"));
//					} catch (JSONException je) {
						
//					}
					JSONObject other = new JSONObject(thisObj, JSONObject.getNames(thisObj));
					thisObj.put("_id", id);
					other.put("term", key);
					if (wordCount.containsKey(key)) {
						wordCount.put(key, wordCount.get(key).put(thisObj));
					} else {
						wordCount.put(key, (new JSONArray()).put(thisObj));
					}
//					if (docCount.containsKey(idkey)) {
//						docCount.put(idkey, docCount.get(idkey).put(other));
//					} else {
//						docCount.put(idkey, (new JSONArray()).put(other));
//					}
				}
			} catch (JSONException je) {
				Log("Fishy map result");
			}
		}
		JSONObject job = new JSONObject();
//		if (!uniques.isEmpty()) {
//			try {
//				job.put("FIELDS", uniques);
//			} catch (JSONException je) {
//				Log("error adding uniques");
//			}
//		}
//		wordCount.putAll(docCount);
		if (wordCount.size() > 0) {
			for (String key : wordCount.keySet()) {
				try {
					job.put(key, wordCount.get(key));
				} catch (JSONException je) {
					Log("Problem update output array");
				}
			}
//			if (reduceCount < 5) {
//				Log(job.toString());
//				reduceCount++;
//			}
			if (job.length() == 0) {
				return new JSONArray().put(JSONObject.NULL);
			} else {
				return new JSONArray().put(job);
			}
		} else {
			return new JSONArray().put(JSONObject.NULL);
		}
	}

	/**
	 * This configures the list of fields to index.
	 * 
	 * @param A
	 *            string of the form "field1:boost1,field2:boost2,...", where
	 *            the boosts are the weights applied to each field. Boosts are
	 *            optional (default is 1.0)
	 */
	public void Configure(String config) {
		if (config == null || config.length() == 0) return;
		fieldsToStore = new HashMap<String, Float>();
		for (String s : config.split(",")) {
			String field;
			float boost;
			if (s.contains(":")) {
				field = s.substring(0,s.indexOf(":"));
				final String boostAsString = s.substring(s.indexOf(":") + 1);
				try {
					boost = Float.valueOf(boostAsString);
				} catch (NumberFormatException nfe) {
					boost = 1.0f;
				}
			} else {
				field = s;
				boost = 1.0f;
			}
			fieldsToStore.put(field,boost);
		}
		return;		
	}

	@SuppressWarnings("unchecked")
	public String findFieldString(String keyToFind, JSONObject obj) {
		if (obj.has(keyToFind)) {
			try {
				return obj.getString(keyToFind);
			} catch (JSONException je) {
				Log("Field " + keyToFind + " is not a string");
				return null;
			}
		}
		Iterator<String> keys = (Iterator<String>) obj.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			try {
				JSONObject jo = obj.getJSONObject(key);
				String out = findFieldString(keyToFind, jo);
				if (out != null)
					return out;
			} catch (JSONException je) {
				/* key not a JSONObject */
			}
		}
		return null;
	}

}
