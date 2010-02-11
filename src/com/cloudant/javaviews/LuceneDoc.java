package com.cloudant.javaviews;

import java.util.HashMap;
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

	public void Log(String message) {
		JSONArray out = new JSONArray();
		out.put("log");
		out.put(message);
		System.out.println(out.toString());
	}

	public JSONArray MapDoc(JSONObject doc) {
		JSONArray out = new JSONArray();
		try {
			String id = doc.getString("_id");
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
				index.addField(field, text, analyzer, fieldsToStore.get(field));
			}
			out = index.jsonMap();
		} catch (JSONException je) {
			out.put(new JSONArray());
			Log("Malformed document: " + doc.toString());
		} catch (Exception e) {
			out.put(new JSONArray());
			Log("Exception: " + e.toString());
		}
		return out;
	}

	@Override
	public JSONArray ReReduce(JSONArray reduceResults) {
		// TODO Auto-generated method stub
		if (reduceResults == null || reduceResults.length() == 0) {
			Log("Reduce called with empty map");
			return new JSONArray().put(JSONObject.NULL);
		}
		JSONArray allDocs = new JSONArray();
		try {
			for (int i = 0; i < reduceResults.length(); i++) {
				JSONArray jarr = reduceResults.getJSONArray(i);
				for (int j = 0; j < jarr.length(); j++) {
					JSONObject jobj = jarr.getJSONObject(j);
					allDocs.put(jobj);
				}
			}
		} catch (JSONException je) {
			Log("Fishy map result");
		}
		if (allDocs.length() > 0) {
				return new JSONArray().put(allDocs);
		} else {
				return new JSONArray().put(JSONObject.NULL);
		}
	}

	@Override
	public JSONArray Reduce(JSONArray mapResults) {
		if (mapResults == null || mapResults.length() == 0) {
			Log("Reduce called with empty map");
			return new JSONArray().put(JSONObject.NULL);
		}
		HashMap<String, JSONArray> wordCount = new HashMap<String, JSONArray>();
		for (int i = 0; i < mapResults.length(); i++) {
			try {
				JSONArray keyId = mapResults.getJSONArray(i).getJSONArray(0);
				String key = keyId.getString(0);
				String id = keyId.getString(1);
				// map result is an array of json objects
				JSONArray arr = mapResults.getJSONArray(i).getJSONArray(1);
				for (int j = 0; j < arr.length(); j++) {
					JSONObject thisObj = arr.getJSONObject(j);
					thisObj.put("_id", id);
					if (wordCount.containsKey(key)) {
						wordCount.put(key, wordCount.get(key).put(thisObj));
					} else {
						wordCount.put(key, (new JSONArray()).put(thisObj));
					}
				}
			} catch (JSONException je) {
				Log("Fishy map result");
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
