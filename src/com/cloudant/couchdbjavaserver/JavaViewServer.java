package com.cloudant.couchdbjavaserver;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudant.ejje.ErlangJson;
import com.cloudant.ejje.ViewServer;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpNode;

/**
 * implementation of a Java View Server for CouchDB
 * 
 * @author hardtke, boorad
 */
public class JavaViewServer extends ViewServer {

	private String mboxname = "";
	
	private List<JavaView> views = new ArrayList<JavaView>();
	private List<URL> libUrls = new ArrayList<URL>();

	public JavaViewServer() {}
	
	/*
	 * public functions
	 */

	public Runnable getMboxThread(OtpNode node, String name) {
		return new JVSMboxThread(node, name);
	}

	public void setName(String mboxname) {
		this.mboxname = mboxname;
	}

	public String prompt(OtpErlangList data) {

		try {
			if (data.arity() == 0)
				throw new JSONException("Empty JSON array");
			String event = ErlangJson.binstr(data,0);
			Log("prompt '" + event + "'");
			Command c = Command.getCommandFromString(event);
			if (c == null) {
				throw new JSONException("Unrecognized view server command: "
						+ event);
			}
			switch (c) {
			case RESET:
				views.clear();
				libUrls.clear();
				return "true";
			case ADD_LIBRARY:
				String urlString = ErlangJson.binstr(data, 1);
				//Log("add_library " + urlString);
				try {
					if( libUrls.add(new URL(urlString)) ) {
						return "true";
					} else {
						return "false";
					}
				} catch (MalformedURLException me) {
					return error(me);
				}
			case ADD_FUN:
				try {
					JSONObject jobj = new JSONObject(ErlangJson.binstr(data,1));
					String name = jobj.getString("classname");
					//Log("add_fun " + name);
					JavaView view = getClass(name, libUrls);
					try {
						String config = jobj.getString("configure");
						view.Configure(config);
					} catch (JSONException je) {
						Log(name + " has no configure string");
						/* no configuration field */
					}
					if( views.add(view) ) {
						return "true";
					} else {
						return "false";
					}
					
				} catch (Exception e) {
					return error(e);
				}
			case MAP_DOC:
				JSONArray ret = new JSONArray();
				final JSONObject jsondoc = ErlangJson.obj(data,1);
				for (JavaView view : views) {
					ret.put(view.MapDoc(jsondoc));
				}
				return ret.toString();
			case REDUCE:
				try {
					//Log("reduce");
					JSONArray reduceOut = new JSONArray();
					List<JavaView> reduceViews = new ArrayList<JavaView>();
					final JSONArray reduceFuncs = ErlangJson.arr(data,1);
					// a simple list of class names
					for (int i = 0; i < reduceFuncs.length(); i++) {
						JavaView view = getClass(reduceFuncs.getString(i),
								libUrls);
						reduceViews.add(view);
					}
					final JSONArray mapresults = ErlangJson.arr(data,2);
					// Log(mapresults.toString());
					for (JavaView view : reduceViews) {
						JSONArray thisResult = view.Reduce(mapresults);
						if (thisResult != null && thisResult.length() > 0) {
							reduceOut.put(thisResult.get(0));
						} else {
							throw new Exception("Error in reduce phase for "
									+ view.getClass().getName());
						}
					}
					String outString = (new JSONArray().put(true)
							.put(reduceOut)).toString();
					//Log(outString);
					return outString;
				} catch (Exception e) {
					return error(e);
				}
			case REREDUCE:
				try {
					//Log("rereduce");
					JSONArray rereduceOut = new JSONArray();
					List<JavaView> rereduceViews = new ArrayList<JavaView>();
					final JSONArray rereduceFuncs = ErlangJson.arr(data,1);
					// a simple list of class names
					for (int i = 0; i < rereduceFuncs.length(); i++) {
						rereduceViews.add(getClass(rereduceFuncs.getString(i),
								libUrls));
					}
					final JSONArray mapresults = ErlangJson.arr(data,2);
					for (JavaView view : rereduceViews) {
						JSONArray thisResult = view.ReReduce(mapresults);
						if (thisResult != null && thisResult.length() > 0) {
							rereduceOut.put(thisResult.get(0));
						} else {
							throw new Exception("Error in rereduce phase for "
									+ view.getClass().getName());
						}
					}
					String outString = (new JSONArray().put(true)
							.put(rereduceOut)).toString();
					//Log(outString);
					return outString;
				} catch (Exception e) {
					e.printStackTrace();
					return error(e);
				}
			default:
				throw new JSONException("Unrecognized view server command: "
						+ event);
			}
		} catch (JSONException je) {
			return error(je);
		}
	}

	/*
	 * private functions
	 */
	
	private String error(Exception e) {
		e.printStackTrace();
		return error(e);
	}

	// logger
	private void Log(String message) {
		JSONArray out = new JSONArray();
		out.put(mboxname);
		out.put("log");
		out.put(message);
		System.out.println(out.toString());
	}

	// load jar's view class
    @SuppressWarnings("unchecked")
	private JavaView getClass(String classname, List<URL> libs) {
    	try {
	    	URLClassLoader loader = new URLClassLoader(libs.toArray(new URL[0]));
	    	Thread.currentThread().setContextClassLoader(loader);
			Class<JavaView> cl = (Class<JavaView>) loader.loadClass(classname);
			Object o = cl.newInstance();
			return (JavaView) o;
    	} catch( Exception e ) {
    		e.printStackTrace();
    		System.exit(1);
    	}
    	return null;
	}

}
