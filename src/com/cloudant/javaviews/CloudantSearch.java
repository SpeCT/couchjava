package com.cloudant.javaviews;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.cloudant.couchdbjavaserver.*;
import com.cloudant.index.CouchIndexUtils;

import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.util.*;

public class CloudantSearch implements JavaView {

	private Map<String, Float> fieldsToStore = null;
	
	

	public Analyzer getAnalyzer() {
		return new StandardAnalyzer(Version.LUCENE_CURRENT);
	}
	
	public void Log(String message) {
		JSONArray out = new JSONArray();
		out.put("log");
		out.put(message);
		System.out.println(out.toString());
	}

	public JSONArray MapDoc(JSONObject doc) {
		JSONArray out = new JSONArray();
		try {
			Analyzer analyzer = getAnalyzer();
			MemoryIndex index = new MemoryIndex();
			// user specified field set
			if (fieldsToStore != null) {
				for (String field : fieldsToStore.keySet()) {
					String text = findFieldString(field, doc);
					if (field != null && text != null) {
						index.addField(field, text, analyzer, fieldsToStore.get(field));
					}
				}
			} else {
				//index everything 
				Map<String, String> mapped = CouchIndexUtils.MapJSONObject(doc, null);
				for (String field : mapped.keySet()) {
					if ("_id".equals(field) || "_rev".equals(field)) continue;
					String text = mapped.get(field);
					if (field != null && text != null) {
						index.addField(field, text, analyzer, 1.0f);
					}
				}
			}
			out = index.jsonMap();
		} catch (Exception e) {
			out.put(new JSONArray());
			if (e != null ) {
				Log("Exception: " + e.toString());
			}
		}
		return out;
	}

	public JSONArray ReReduce(JSONArray reduceResults) {
		Log("com.cloudant.javaviews.CloudantSearch: Invalid ReReduce");
		return new JSONArray().put(JSONObject.NULL);
	}
	


	public JSONArray Reduce(JSONArray mapResults) {
		Log("com.cloudant.javaviews.CloudantSearch: Invalid Reduce");
		return new JSONArray().put(JSONObject.NULL);
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
		if (keyToFind == null || obj == null) return null;
		if (keyToFind.contains(".")) {
			String[] res = keyToFind.split("\\.", 2);
			if (obj.has(res[0])) {
				try {
					return findFieldString(res[1], obj.getJSONObject(res[0]));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log(e.getMessage());
					return null;
				}
			} else {
				return null;
			}
		}
		if (obj.has(keyToFind)) {
			try {
				return obj.getString(keyToFind);
			} catch (JSONException je) {
				Log("Field " + keyToFind + " is not a string");
				return null;
			}
		}
		return null;
	}

}
