package com.cloudant.index;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.Term;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CouchIndexUtils {
	
	private static boolean DEBUG = true;
	
	public static JSONObject ConvertStringToJSON(String input) {
		if (input==null) return null;
		try {
			return new JSONObject(input);
		} catch	(JSONException je) {
			System.out.println(je.getMessage());
			try {
				return new JSONObject().put("error", "GetDocument").put("Http Response", 406);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static String findFieldString(String keyToFind, JSONObject obj) {
		if (keyToFind == null || obj == null) return null;
		if (keyToFind.contains(".")) {
			String[] res = keyToFind.split("\\.", 2);
			if (obj.has(res[0])) {
				try {
					return findFieldString(res[1], obj.getJSONObject(res[0]));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
//					Log(e.getMessage());
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
//				Log("Field " + keyToFind + " is not a string");
				return null;
			}
		}
		return null;
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
				System.out.println(je.getMessage());
			}
		}
		return map;
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
				System.out.println(je.getMessage());
			}
		}
		return map;
	}
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
				System.out.println(je.getMessage());
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
				System.out.println(je.getMessage());
			}
		}
		return map;
	}
	
	public static JSONArray GetTermData(Credentials creds, String baseUrl, String indexUrl, Term term) {
		String termText = term.text();
		String field = term.field();
//		System.err.println(field + " " + termText);
		if (field.equals("cloudant_range")) {
			String[] s = termText.split(",");
			if (s == null || s.length != 3) return null;
			return GetRangeData(creds, baseUrl, indexUrl, s[0], s[1], s[2]);
		}
		JSONArray termKey = GetTermKey(field, termText);
		//re-implement later
		//		boolean all = field.equals("*");
//		String url = baseUrl + indexUrl + "?stale=ok&key=\"" + termText + "\"";
		String url = baseUrl + indexUrl + "?stale=ok&reduce=false&key=" + termKey.toString();
//		if (all) {
//			url = baseUrl + indexUrl + "?stale=ok&startkey=[\"" + termText + "\"]&endkey=[\"" + termText + "\",{}]&inclusive_end=true";
//		}
		JSONObject jobj = GetJSONDocument(creds, url);
//		System.err.println("Url: " + url);
//		System.err.println("results: " + jobj.toString());
		JSONArray outArray = new JSONArray();
		try {
			JSONArray rows = jobj.getJSONArray("rows");
			if (rows == null || rows.length()==0) return outArray;
			for (int irow = 0; irow < rows.length(); irow++) {
				JSONObject row = rows.getJSONObject(irow);
				try {
					String id = row.getString("id");
					JSONArray values = row.getJSONArray("value");
//			System.out.println("values: " + values.toString());
					if (values != null) {
						JSONObject vout = new JSONObject();
						for (int i=0;i<values.length();i++) {
							// first item should be json array with positions:
							if (i == 0) {
								try {
									JSONArray arr = values.getJSONArray(i);
									vout.put("p", arr);
									continue;
								} catch (JSONException je) {
									// old format continue
								}
							}
							JSONObject v = values.getJSONObject(i);
							Iterator<String> keys = v.keys();
							while (keys.hasNext()) {
								String key = keys.next();
								vout.put(key, v.get(key));
							}
						}
						vout.put("_id", id);
						outArray.put(vout);
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

	public static int GetDocFreq(Credentials creds, String baseUrl, String indexUrl, Term term) {
		String termText = term.text();
		String field = term.field();
		JSONArray termKey = GetTermKey(field,termText);
		//re-implement later
		//		boolean all = field.equals("*");
		String url = baseUrl + indexUrl + "?stale=ok&group=true&group_level=2&key=" + termKey.toString();
//		if (all) {
//			url = baseUrl + indexUrl + "?stale=ok&startkey=[\"" + termText + "\"]&endkey=[\"" + termText + "\",{}]&inclusive_end=true";
//		}
		JSONObject jobj = GetJSONDocument(creds, url);
//		System.err.println("Url: " + url);
//		System.err.println("results: " + jobj.toString());
		JSONArray outArray = new JSONArray();
		try {
			JSONArray rows = jobj.getJSONArray("rows");
			if (rows == null || rows.length()==0) return 0;
			// this should be length 1
			JSONObject row = rows.getJSONObject(0);
				try {
					final int value = row.getInt("value");
					return value;
				} catch (JSONException je) {
					return 0;
					/* no values in this row */
				}
		} catch (Exception e) {
			System.out.println(e.getMessage());			
			return 0;
		}
	}

	public static List<String> GetFieldNames(Credentials cred, String baseUrl, String indexUrl) {
		String url = baseUrl + indexUrl + "?stale=ok&group=true&group_level=1";
		List<String> fields = new ArrayList<String>();
		JSONObject jobj = GetJSONDocument(cred, url);
//		System.err.println("Url: " + url);
//		System.err.println("results: " + jobj.toString());
		try {
			JSONArray rows = jobj.getJSONArray("rows");
			if (rows == null || rows.length()==0) return fields;
			for (int irow = 0; irow < rows.length(); irow++) {
				JSONObject row = rows.getJSONObject(irow);
//				System.out.println(row.toString());
			// this should be length 1
				try {
					String value = row.getJSONArray("key").getString(0);
//					System.out.println(value);
					fields.add(value);
				} catch (JSONException je) {
					/* no values in this row */
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());			
		}
		return fields;
	}

	public static JSONArray GetRangeData(String user, String password, String baseUrl, String indexUrl, String field, String lowterm, String highterm) {
		return  GetRangeData(new Credentials(user, password, null, null), baseUrl, indexUrl, field, lowterm, highterm);

	}
	public static JSONArray GetTermKey(String field, String termText) {
		if (termText.endsWith("/n")) {
			return new JSONArray().put(field).put(JSONObject.stringToValue(termText.substring(0,termText.length()-2)));
		} else {
			return new JSONArray().put(field).put(termText);
		}	
	}
	
	public static JSONArray GetRangeData(Credentials creds, String baseUrl, String indexUrl, String field, String lowterm, String highterm) {
		if (field == null || lowterm == null || highterm == null) return null;
		JSONArray termKey = GetTermKey(field, lowterm);
		JSONArray endKey = GetTermKey(field, highterm);
		String url = baseUrl + indexUrl + "?stale=ok&reduce=false&startkey=" + termKey.toString() + "&endkey=" + endKey.toString() + "&inclusive_end=true";
		JSONObject jobj = GetJSONDocument(creds, url);
//		System.err.println("Url: " + url);
//		System.err.println("results: " + jobj.toString());
		JSONArray outArray = new JSONArray();
		try {
			JSONArray rows = jobj.getJSONArray("rows");
			if (rows == null || rows.length()==0) return outArray;
			for (int irow = 0; irow < rows.length(); irow++) {
				JSONObject row = rows.getJSONObject(irow);
				try {
					String id = row.getString("id");
					JSONArray key = row.getJSONArray("key");
					if (key == null || key.length() < 2 || !field.equals(key.getString(0))) continue;
					JSONArray values = row.getJSONArray("value");
//			System.out.println("values: " + values.toString());
					if (values != null) {
						JSONObject vout = new JSONObject();
						for (int i=0;i<values.length();i++) {
							// first item should be json array with positions:
							if (i == 0) {
								try {
									JSONArray arr = values.getJSONArray(i);
									vout.put("p", arr);
									continue;
								} catch (JSONException je) {
									// old format continue
								}
							}
							JSONObject v = values.getJSONObject(i);
							Iterator<String> keys = v.keys();
							while (keys.hasNext()) {
								String thisKey = keys.next();
								vout.put(thisKey, v.get(thisKey));
							}
						}
						vout.put("_id", id);
						outArray.put(vout);
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
	public static JSONArray GetSortData(String user, String pass, String baseUrl, String indexUrl, String field) {
		return GetSortData(new Credentials(user, pass, null, null), baseUrl, indexUrl, field);
	}
	public static JSONArray GetSortData(Credentials creds, String baseUrl, String indexUrl, String field) {
//		String url = baseUrl + indexUrl + "?stale=ok&group=true&key=\"" + termText + "\"";
		String url = baseUrl + indexUrl + "?stale=ok&group=true&group_level=2&startkey=[\"" + field + "\"]&endkey=[\"" + field + "\",{}]&inclusive_end=true";
		JSONObject jobj = GetJSONDocument(creds, url);
		if (DEBUG) System.err.println("Url: " + url);
		int maxToDisplay = Math.min(jobj.length(), 500); 
		if (DEBUG) System.err.println("results: " + jobj.toString().substring(0,maxToDisplay));
		JSONArray outArray = new JSONArray();
		try {
			JSONArray rows = jobj.getJSONArray("rows");
			if (rows == null || rows.length()==0) return outArray;
			for (int irow = 0; irow < rows.length(); irow++) {
				JSONObject row = rows.getJSONObject(irow);
				outArray.put(row);
//							}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());			
		}
		return outArray;
	}
	
	public static JSONObject GetJSONDocument(Credentials creds, String url) {
//		System.err.println("Getting " + url);
		return ConvertStringToJSON(GetDocument(creds,url));
	}
	
	public static String GetDocument(String user, String pass, String url) {
		return GetDocument(new Credentials(user, pass, null, null), url);
	}
	
	public static String GetDocument(Credentials creds, String url) {
		if (url == null) return null;
		String fullUrl = url;

//		System.out.println(fullUrl);
        try {	
		URL Url = new URL(fullUrl);
//		URL Url = new URL(tUrl.getProtocol(),tUrl.getHost(),URLEncoder.encode(tUrl.getPath(),"UTF-8"));
		HttpURLConnection conn = (HttpURLConnection)Url.openConnection(); 
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "text/plain");
		if (creds.getUser() != null && creds.getPassword() != null) {
			conn.setRequestProperty("Authorization", userNamePasswordBase64(creds.getUser(), creds.getPassword()));
		} else if (creds.getAuthorization() != null) {
			conn.setRequestProperty("Authorization", creds.getAuthorization());
		} else if (creds.getDbCoreCookie() != null) {
			Cookie[] cook = creds.getDbCoreCookie();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < cook.length; i++) {
				sb.append(cook[i].getName() + "=" + cook[i].getValue());
				if (i < cook.length -1) sb.append(",");
			}
			conn.setRequestProperty("Cookie", sb.toString());
		}
		// Get the response 
		try {
				conn.connect();
				if (conn.getResponseCode() != HttpServletResponse.SC_OK) {
					return new JSONObject().put("error", "GetDocument").put("Http Response", conn.getResponseCode()).put("url", url).toString();
				}
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
				try {
					return new JSONObject().put("error", "GetDocument").put("Http Response", 500).put("url", url).toString();
				} catch (Exception e1) {
					e1.printStackTrace();
					return null;
				}
			}
        } catch (Exception e3) {
        	e3.printStackTrace();
			try {
				return new JSONObject().put("error", "GetDocument").put("Http Response", 500).put("url", url).toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}        	
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
