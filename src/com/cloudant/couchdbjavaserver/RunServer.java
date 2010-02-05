package com.cloudant.couchdbjavaserver;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
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
//						views = null;
//						libUrls = null;
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
							System.out.println("{\"error\":\"add_library\",\"reason\":\"" + me.getMessage() + "\"}");
							break;
						}
					case ADD_FUN:
						try {
							JSONObject jobj = new JSONObject(arr.getString(1));
							String name = jobj.getString("classname");
							Log("add_fun " + name);
							JavaView view = getClass(name, libUrls);
							try {
								String config = jobj.getString("configure");
								view.Configure(config);
							} catch (JSONException je) {
								Log(name + " has no configure string");
								/* no configuration field */
							}
							views.add(view);
							System.out.println("true");
							break;
						} catch (Exception e) {
							System.out.println("{\"error\":\"add_fun\",\"reason\":\"" + e.getMessage() + "\"}");
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
						try {
//							Log("reduce " + line);
							JSONArray reduceOut = new JSONArray();
							List<JavaView> reduceViews = new ArrayList<JavaView>();
							final JSONArray reduceFuncs = arr.getJSONArray(1);
							// a simple list of class names
							for (int i = 0; i < reduceFuncs.length(); i++) {
								JavaView view = getClass(reduceFuncs.getString(i),libUrls);
								reduceViews.add(view);
							}
							final JSONArray mapresults = arr.getJSONArray(2);
							Log(mapresults.toString());
							for (JavaView view : reduceViews) {
								JSONArray thisResult = view.Reduce(mapresults);
								if (thisResult != null && thisResult.length() > 0) {
									reduceOut.put(new JSONArray().put(thisResult.get(0)));
								} else {
									throw new Exception("Error in reduce phase for " + view.getClass().getName());
								}
							}
							String outString = (new JSONArray().put(true).put(reduceOut)).toString();
//							Log(outString);
							System.out.println(outString);
							break;
						} catch (Exception e) {
							System.out.println("{\"error\":\"reduce\",\"reason\":\"" + e.getMessage() + "\"}");
							break;							
						}
					case REREDUCE: 
						try {
//							Log("rereduce " + line);
							JSONArray rereduceOut = new JSONArray();
							List<JavaView> rereduceViews = new ArrayList<JavaView>();
							final JSONArray rereduceFuncs = arr.getJSONArray(1);
							// a simple list of class names
								for (int i = 0; i < rereduceFuncs.length(); i++) {
								rereduceViews.add(getClass(rereduceFuncs.getString(i),libUrls));
							}
							final JSONArray mapresults = arr.getJSONArray(2);
							for (JavaView view : rereduceViews) {
								JSONArray thisResult = view.ReReduce(mapresults);
								if (thisResult != null && thisResult.length() > 0) {
									rereduceOut.put(new JSONArray().put(thisResult.get(0)));
								} else {
									throw new Exception("Error in rereduce phase for " + view.getClass().getName());
								}
							}
							System.out.println((new JSONArray().put(true).put(rereduceOut)).toString());
							break;
						} catch (Exception e) {
							System.out.println("{\"error\":\"rereduce\",\"reason\":\"" + e.getMessage() + "\"}");
							break;							
						}
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

	public static JavaView getClass(String classname, List<URL> libs) throws Exception {
		URLClassLoader loader = new URLClassLoader(libs.toArray(new URL[0]));
		Class<JavaView> cl = (Class<JavaView>) loader.loadClass(classname);
		Object o = cl.newInstance();
		return (JavaView)o;		
	}
	
}
