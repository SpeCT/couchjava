package com.cloudant.javaviews;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.cloudant.couchdbjavaserver.*;
/**
 * This is an example of a JavaView.  This class recursively finds a field in a JSON document (finds most shallow instance of field).  The map function splits the fields on whitespace and returns a list of [word, position] pairs, where position is the position of the word in the field.
 * The reduce/rereduce function counts how often each word appears in the database in the given field.  
 * The design document will look like (assuming you want to split fields name "title" and "text":
 * {
 *   "_id":"_design/splittext",
 *   "language":"java",
 *   "views" : {
 *   "title" : {"map":"{\"classname\":\"com.cloudant.javaviews.SplitText\",\"configure\":\"title\"}","reduce":"com.cloudant.javaviews.SplitText"},
 *	 "text" : {"map":"{\"classname\":\"com.cloudant.javaviews.SplitText\",\"configure\":\"text\"}","reduce":"com.cloudant.javaviews.SplitText"}
 *	}    
 *}
 * @author hardtke
 *
 */
public class SplitText implements JavaView {
	
	private String fieldToSplit;

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
			String text = findFieldString(fieldToSplit, doc);
			int i = 0;
			if (text != null && text.length() > 0) {
				for (String s : text.split(" ")) {
					out.put(new JSONArray().put(s).put(i));
					i++;
				}
			} else {
				out.put(new JSONArray());
			}
		} catch (JSONException je) {
			out.put(new JSONArray());
			Log("Malformed document: " + doc.toString());
		}
		return out;
	}


	public JSONArray ReReduce(JSONArray reduceResults) {
		// TODO Auto-generated method stub
		if (reduceResults == null || reduceResults.length() == 0) {
			Log("Reduce called with empty map");
			return new JSONArray().put(JSONObject.NULL);
		}
		HashMap<String, Integer> wordCount = new HashMap<String,Integer>();
		try {
			for (int i = 0; i < reduceResults.length(); i++){
//				JSONArray jarr = reduceResults.getJSONArray(i);
//				for (int j = 0; j < jarr.length(); j++){
					JSONObject jobj = reduceResults.getJSONObject(i);
					Iterator iter = jobj.keys();
					while (iter.hasNext()) {
						String key = (String)iter.next();
						int value = jobj.getInt(key);
						if (wordCount.containsKey(key)) {
							wordCount.put(key,wordCount.get(key) + value);
						} else {
							wordCount.put(key, value);
						}
					}
//				}
			}
		} catch (JSONException je) {
				Log("Fishy map result");
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

	public JSONArray Reduce(JSONArray mapResults) {
		if (mapResults == null || mapResults.length() == 0) {
			Log("Reduce called with empty map");
			return new JSONArray().put(JSONObject.NULL);
		}
		HashMap<String, Integer> wordCount = new HashMap<String,Integer>();
		for (int i = 0; i < mapResults.length(); i++){
			try {
				JSONArray keyId = mapResults.getJSONArray(i).getJSONArray(0);
				String key = keyId.getString(0);
				if (wordCount.containsKey(key)) {
					wordCount.put(key,wordCount.get(key) + 1);
				} else {
					wordCount.put(key, 1);
				}
			} catch (JSONException je) {
				je.printStackTrace();
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
			if (job.length() != 0) {
				return new JSONArray().put(job);
			}
		}
		return new JSONArray().put(JSONObject.NULL);
	}

	public void Configure(String config) {
		fieldToSplit = config;
		return;		
	}
	
	@SuppressWarnings("unchecked")
	public String findFieldString(String keyToFind, JSONObject obj) {
		if (obj.has(keyToFind)) {
			try {
				return obj.getString(keyToFind);
			} catch (JSONException je){
				Log("Field " + keyToFind + " is not a string");
				return null;
			}
		}
		Iterator<String> keys = (Iterator<String>)obj.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			try {
				JSONObject jo = obj.getJSONObject(key);
				String out = findFieldString(keyToFind, jo);
				if (out != null) return out;
			} catch (JSONException je) {
				/* key not a JSONObject */
			}
		}
		return null;
	}

}
