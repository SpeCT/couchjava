package com.cloudant.index;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CouchIndexUtils {
	
	public static Map<String, Object> MapJSONObject2Object(JSONObject jobj, String prefix) {
		if (jobj == null) return null;
		Iterator<String> keys = (Iterator<String>)jobj.keys();
		Map<String, Object> map = new HashMap<String, Object>();
		while (keys.hasNext()) {
			String key = keys.next();
			String name = (prefix != null ? prefix : "") + key;
			try {
			Object o = jobj.get(key);
				if (o instanceof String) {
					map.put(name, o);
				} else if (o instanceof Number) {
					map.put(name, o);
				} else if (o instanceof JSONArray) {
					Map<String, Object> next = MapJSONArray2Object((JSONArray)o, name);
					map.putAll(next);
				} else if (o instanceof JSONObject) {
					if ((JSONObject)o == JSONObject.NULL) {
						map.put(name, JSONObject.NULL.toString());
					} else {
						Map<String, Object> next = MapJSONObject2Object((JSONObject)o, name + ".");
						map.putAll(next);
					}
				} else if (o instanceof Boolean) {
					map.put(name, ((Boolean)o).toString());
				} 
			} catch (JSONException je) {
			}
		}
		return map;
	}
	public static Map<String, Object> MapJSONArray2Object(JSONArray jarr, String prefix) {
		if (jarr == null) return null;
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < jarr.length(); i++) {
			String name = (prefix != null ? prefix : "");
			try {
			Object o = jarr.get(i);
				if (o instanceof String) {
					map.put(name, o);
				} else if (o instanceof Number) {
					map.put(name, o);
				} else if (o instanceof JSONArray) {
					Map<String, Object> next = MapJSONArray2Object((JSONArray)o, name);
					map.putAll(next);
				} else if (o instanceof JSONObject) {
					if ((JSONObject)o == JSONObject.NULL) {
						map.put(name, JSONObject.NULL.toString());
					} else {
						Map<String, Object> next = MapJSONObject2Object((JSONObject)o, name + ".");
						map.putAll(next);
					}
				} else if (o instanceof Boolean) {
					map.put(name, ((Boolean)o).toString());
				}
			} catch (JSONException je) {
			}
		}
		return map;
	}
	
}
