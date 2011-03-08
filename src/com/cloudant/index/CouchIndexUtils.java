package com.cloudant.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CouchIndexUtils {
	public static Map<String, List<Object>> MapJSONObject2Object(JSONObject jobj, String prefix) {
		if (jobj == null) return null;
		Iterator<String> keys = (Iterator<String>)jobj.keys();
		Map<String, List<Object>> map = new HashMap<String, List<Object>>();
		while (keys.hasNext()) {
			String key = keys.next();
			String name = (prefix != null ? prefix : "") + key;
			try {
			Object o = jobj.get(key);
			if (o == null) continue;
			List<Object> l = map.get(name);
			if (l == null) map.put(name, l = new ArrayList<Object>());
			if (o instanceof String) {
					l.add(o);
				} else if (o instanceof Number) {
					l.add(o);
				} else if (o instanceof JSONArray) {
					Map<String, List<Object>> next = MapJSONArray2Object((JSONArray)o, name);
					for (String key1 : next.keySet()) {
						List<Object> li = map.get(key1);
						if (li == null) map.put(key1, li = new ArrayList<Object>());
						li.addAll(next.get(key1));
					}
				} else if (o instanceof JSONObject) {
					if ((JSONObject)o == JSONObject.NULL) {
						l.add(JSONObject.NULL.toString());
					} else {
						Map<String, List<Object>> next = MapJSONObject2Object((JSONObject)o, name + ".");
						for (String key1 : next.keySet()) {
							List<Object> li = map.get(key1);
							if (li == null) map.put(key1, li = new ArrayList<Object>());
							li.addAll(next.get(key1));
						}
					}
				} else if (o instanceof Boolean) {
					l.add(o);
				} 
			} catch (JSONException je) {
			}
		}
		return map;
	}
	public static Map<String, List<Object>> MapJSONArray2Object(JSONArray jarr, String prefix) {
		if (jarr == null) return null;
		Map<String, List<Object>> map = new HashMap<String, List<Object>>();
		for (int i = 0; i < jarr.length(); i++) {
			String name = (prefix != null ? prefix : "");
			try {
			Object o = jarr.get(i);
			if (o == null) continue;
			List<Object> l = map.get(name);
			if (l == null) map.put(name, l = new ArrayList<Object>());
				if (o instanceof String) {
					l.add(o);
				} else if (o instanceof Number) {
					l.add(o);
				} else if (o instanceof JSONArray) {
					Map<String, List<Object>> next = MapJSONArray2Object((JSONArray)o, name);
					for (String key : next.keySet()) {
						List<Object> li = map.get(key);
						if (li == null) map.put(key, li = new ArrayList<Object>());
						li.addAll(next.get(key));
					}
				} else if (o instanceof JSONObject) {
					if ((JSONObject)o == JSONObject.NULL) {
						l.add(JSONObject.NULL.toString());
					} else {
						Map<String, List<Object>> next = MapJSONObject2Object((JSONObject)o, name + ".");
						for (String key : next.keySet()) {
							List<Object> li = map.get(key);
							if (li == null) map.put(key, li = new ArrayList<Object>());
							li.addAll(next.get(key));
						}
					}
				} else if (o instanceof Boolean) {
					l.add(o);
				}
			} catch (JSONException je) {
			}
		}
		return map;
	}
	
	
}
