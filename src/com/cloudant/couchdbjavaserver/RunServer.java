package com.cloudant.couchdbjavaserver;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.ServiceLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RunServer {

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<JavaView> views = new ArrayList<JavaView>();
		List<URL> libUrls = new ArrayList<URL>();
		Scanner sc = new Scanner(System.in);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			try {
				JSONArray arr = new JSONArray(line);
				if (arr.length() == 0) throw new JSONException("Empty JSON array");
				String event = arr.getString(0);
				Command c = Command.getCommandFromString(event);
				if (c == null) {
					System.out.println("");
					throw new JSONException("Unrecognized view server command: " + event);
				}
				switch (c) {
					case RESET:
						Log("reset");
						views = null;
						libUrls = null;
						System.out.println("true");
						break;
					case ADD_LIBRARY:
						String urlString = arr.getString(1);
						Log("add_library " + urlString);
						try {
							libUrls.add(new URL(urlString));
							System.out.println("true");
							break;
						} catch (MalformedURLException me) {
							System.out.println("{\"error\":\"1\",\"reason\":\"" + me.getMessage() + "\"}");
							break;
						}
					case ADD_FUN:
						try {
							JSONObject jobj = new JSONObject(arr.getString(1));
							String name = jobj.getString("classname");
							Log("add_fun " + name);
							URLClassLoader loader = new URLClassLoader(libUrls.toArray(new URL[0]));
							Class<JavaView> cl = (Class<JavaView>) loader.loadClass(name);
							Object o = cl.newInstance();
							views.add((JavaView)o);
							System.out.println("true");
							break;
						} catch (Exception e) {
							System.out.println("{\"error\":\"1\",\"reason\":\"" + e.getMessage() + "\"}");
							break;
						}
					case MAP_DOC:
						JSONArray out = new JSONArray();
						final JSONObject jsondoc = arr.getJSONObject(1);
						for (JavaView view : views) {
							out.put(view.MapDoc(jsondoc));
						}
						System.out.println(out.toString());
						break;
					case REDUCE: 
						JSONObject reduceOut = new JSONObject();
						final JSONArray mapresults = arr.getJSONArray(3);
						for (JavaView view : views) {
							reduceOut = view.Reduce(mapresults);
						}
						System.out.println(reduceOut.toString());

					default: 
						System.out.println("");
						throw new JSONException("Unrecognized view server command: " + event);
				}
			}  catch (JSONException je) {
				JSONArray out = new JSONArray();
				out.put("log");
				out.put(je.toString() + " " + line);
				System.out.println(out.toString());
			}
		}

	}
	
	public static void Log(String message) {
		JSONArray out = new JSONArray();
		out.put("log");
		out.put(message);
		System.out.println(out.toString());		
	}

}
