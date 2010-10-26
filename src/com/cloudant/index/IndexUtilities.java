package com.cloudant.index;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IndexUtilities {
	private static boolean STDOUTOK = false;
	public static String ObjectToString(Object o) {
			if (o instanceof String) {
				return (String)o;
			} else if (o instanceof Number) {
				return String.valueOf(o);
			} else if (o instanceof JSONArray) {
				Map<String, String> next = MapJSONArray((JSONArray)o, "");
				StringBuilder sb = new StringBuilder();
				for (String s: next.values()) {
					sb.append(s);
					sb.append(", ");
				}
				return sb.toString();
			} else if (o instanceof JSONObject) {
				if ((JSONObject)o == JSONObject.NULL) {
					return JSONObject.NULL.toString();
				} else {
					Map<String, String> next = MapJSONObject((JSONObject)o, ".");
					StringBuilder sb = new StringBuilder();
					for (String s: next.values()) {
						sb.append(s);
						sb.append(", ");
					}				
				}
			} else if (o instanceof Boolean) {
				return ((Boolean)o).toString();
			}
			return null;
}
public static Map<String, String> MapJSONArray(JSONArray jarr, String prefix) {
	if (jarr == null) return null;
	Map<String, String> map = new HashMap<String, String>();
	for (int i = 0; i < jarr.length(); i++) {
		String name = (prefix != null ? prefix : "");
		try {
		Object o = jarr.get(i);
			if (o instanceof String) {
				map.put(name, (String)o);
			} else if (o instanceof Number) {
				map.put(name, String.valueOf(o));
			} else if (o instanceof JSONArray) {
				Map<String, String> next = MapJSONArray((JSONArray)o, name);
				map.putAll(next);
			} else if (o instanceof JSONObject) {
				if ((JSONObject)o == JSONObject.NULL) {
					map.put(name, JSONObject.NULL.toString());
				} else {
					Map<String, String> next = MapJSONObject((JSONObject)o, name + ".");
					map.putAll(next);
				}
			} else if (o instanceof Boolean) {
				map.put(name, ((Boolean)o).toString());
			}
		} catch (JSONException je) {
			if (STDOUTOK) System.out.println(je.getMessage());
		}
	}
	return map;
}
public static Map<String, String> MapJSONObject(JSONObject jobj, String prefix) {
	if (jobj == null) return null;
	Iterator<String> keys = (Iterator<String>)jobj.keys();
	Map<String, String> map = new HashMap<String, String>();
	while (keys.hasNext()) {
		String key = keys.next();
		String name = (prefix != null ? prefix : "") + key;
		try {
		Object o = jobj.get(key);
			if (o instanceof String) {
				map.put(name, (String)o);
			} else if (o instanceof Number) {
				map.put(name, String.valueOf(o));
			} else if (o instanceof JSONArray) {
				Map<String, String> next = MapJSONArray((JSONArray)o, name);
				map.putAll(next);
			} else if (o instanceof JSONObject) {
				if ((JSONObject)o == JSONObject.NULL) {
					map.put(name, JSONObject.NULL.toString());
				} else {
					Map<String, String> next = MapJSONObject((JSONObject)o, name + ".");
					map.putAll(next);
				}
			} else if (o instanceof Boolean) {
				map.put(name, ((Boolean)o).toString());
			} 
		} catch (JSONException je) {
			if (STDOUTOK) System.out.println(je.getMessage());
		}
	}
	return map;
}

}
