package com.cloudant.index;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CouchIndexUtils {
	
	public static JSONObject ConvertStringToJSON(String input) {
		if (input==null) return null;
		try {
			return new JSONObject(input);
		} catch	(JSONException je) {
			System.out.println(je.getMessage());
			return null;
		}
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
					map.put(name, ((JSONArray)o).toString());
				} else if (o instanceof JSONObject) {
					Map<String, String> next = MapJSONObject((JSONObject)o, name + ".");
					map.putAll(next);
				} else if (o instanceof Boolean) {
					map.put(key, ((Boolean)o).toString());
				}
			} catch (JSONException je) {
				System.out.println(je.getMessage());
			}
		}
		return map;
	}
	
	public static JSONArray GetTermData(String user, String pass, String baseUrl, String indexUrl, Term term) {
		String termText = term.text();
		String field = term.field();
		boolean all = field.equals("*");
		String url = baseUrl + indexUrl + "?key=" + termText;
		JSONObject jobj = GetJSONDocument(user, pass, url);
		JSONArray outArray = new JSONArray();
		try {
			JSONArray rows = jobj.getJSONArray("rows");
			if (rows == null || rows.length()==0) return outArray;
			for (int irow = 0; irow < rows.length(); irow++) {
				JSONObject row = rows.getJSONObject(irow);
				try {
					JSONArray values = row.getJSONArray("value");
					if (values != null && values.length() > 0) {
						for (int i=0;i<values.length();i++) {
							final JSONObject value = values.getJSONObject(i);
							if (all || value.getString("field").equals(field)) {
								outArray.put(value);
							}
						}
					}
				} catch (JSONException je) {
					/* no values in this row */
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());			
		}
		return outArray;
	}
	
	public static JSONObject GetJSONDocument(String user, String pass, String url) {
		return ConvertStringToJSON(GetDocument(user,pass,url));
	}
	
	public static String GetDocument(String user, String pass, String url) {
		try {
		if (url == null) return null;
		String fullUrl = url;
//		System.out.println(fullUrl);
		URL Url = new URL(fullUrl);
		HttpURLConnection conn = (HttpURLConnection)Url.openConnection(); 
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "text/plain");
		if (user != null && pass != null) {
			conn.setRequestProperty("Authorization", userNamePasswordBase64(user,pass));
		}
		// Get the response 
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = rd.readLine()) != null) { 
			sb.append(line);
		} 
		rd.close();
		return sb.toString();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return "Error with read";
		}
	}
	public static String PostDocument(String user, String pass, String url, String id, JSONObject document) {
		try {
		if (url == null || document == null) return null;
		String fullUrl = url + "/" + id;
//		System.out.println(fullUrl);
		URL Url = new URL(fullUrl);
		HttpURLConnection conn = (HttpURLConnection)Url.openConnection(); 
		conn.setDoOutput(true);
		conn.setRequestMethod("PUT");
		conn.setRequestProperty("Content-Type", "text/plain");
		if (user != null && pass != null) {
			conn.setRequestProperty("Authorization", userNamePasswordBase64(user,pass));
		}
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
		wr.write(document.toString()); 
		wr.flush();
		wr.close();
		// Get the response 
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = rd.readLine()) != null) { 
			sb.append(line);
		} 
		rd.close();
		return sb.toString();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return "Error with post";
		}
	}
	
	  public static String userNamePasswordBase64
      (String username, String password)
 {
   return "Basic " + base64Encode (username + ":" + password);
 }

 private final static char base64Array [] = {
     'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
     'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
     'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
     'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
     'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
     'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
     'w', 'x', 'y', 'z', '0', '1', '2', '3',
     '4', '5', '6', '7', '8', '9', '+', '/'
 };



 private static String base64Encode (String string)    {
   String encodedString = "";
   byte bytes [] = string.getBytes ();
   int i = 0;
   int pad = 0;
   while (i < bytes.length) {
     byte b1 = bytes [i++];
     byte b2;
     byte b3;
     if (i >= bytes.length) {
        b2 = 0;
        b3 = 0;
        pad = 2;
        }
     else {
        b2 = bytes [i++];
        if (i >= bytes.length) {
           b3 = 0;
           pad = 1;
           }
        else
           b3 = bytes [i++];
        }
     byte c1 = (byte)(b1 >> 2);
     byte c2 = (byte)(((b1 & 0x3) << 4) | (b2 >> 4));
     byte c3 = (byte)(((b2 & 0xf) << 2) | (b3 >> 6));
     byte c4 = (byte)(b3 & 0x3f);
     encodedString += base64Array [c1];
     encodedString += base64Array [c2];
     switch (pad) {
      case 0:
        encodedString += base64Array [c3];
        encodedString += base64Array [c4];
        break;
      case 1:
        encodedString += base64Array [c3];
        encodedString += "=";
        break;
      case 2:
        encodedString += "==";
        break;
      }
     }
     return encodedString;
 }

}
