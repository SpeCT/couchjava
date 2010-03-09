package com.cloudant.couchdbjavaserver;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudant.ejje.ViewServer;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangChar;
import com.ericsson.otp.erlang.OtpErlangFloat;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpNode;

public class JavaViewServer extends ViewServer {

	private String mboxname = "";
	
//	private List<JavaView> views = new ArrayList<JavaView>();
	private List<JavaView> views = new ArrayList<JavaView>();
	private ClassUrls classUrls = ClassUrls.getInstance();
//	private List<URL> libUrls = new ArrayList<URL>();

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
			String event = binstr(data,0);
			//Log("prompt '" + event + "'");
			Command c = Command.getCommandFromString(event);
			if (c == null) {
				throw new JSONException("Unrecognized view server command: "
						+ event);
			}
			switch (c) {
			case RESET:
//				views.clear();
				return "true";
			case ADD_LIBRARY:
				String urlString = binstr(data, 1);
//				Log("add_library " + urlString);
				boolean res = classUrls.addUrl(urlString);
				if( res ) {
					return "true";
				} else {
					return "false";
				}
			case ADD_FUN:
				try {
					JSONObject jobj = new JSONObject(binstr(data,1));
					String name = jobj.getString("classname");
					//Log("add_fun " + name);
					JavaView view = getClass(name, classUrls.getUrls());
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
				final JSONObject jsondoc = obj(data,1);
				for (JavaView view : views) {
					ret.put(view.MapDoc(jsondoc));
				}
				return ret.toString();
			case REDUCE:
				try {
					//Log("reduce");
					JSONArray reduceOut = new JSONArray();
					List<JavaView> reduceViews = new ArrayList<JavaView>();
					final JSONArray reduceFuncs = arr(data,1);
					// a simple list of class names
					for (int i = 0; i < reduceFuncs.length(); i++) {
						JavaView view = getClass(reduceFuncs.getString(i),
								classUrls.getUrls());
						reduceViews.add(view);
					}
					final JSONArray mapresults = arr(data,2);
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
					final JSONArray rereduceFuncs = arr(data,1);
					// a simple list of class names
					for (int i = 0; i < rereduceFuncs.length(); i++) {
						rereduceViews.add(getClass(rereduceFuncs.getString(i),
								classUrls.getUrls()));
					}
					final JSONArray mapresults = arr(data,2);
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
	
	// Erlang -> JSON helper methods
	private String binstr(OtpErlangList data, int i) {
		return (String)to_json( data.elementAt(i));
	}

	private JSONArray arr(OtpErlangList data, int i) {
		try {
			return to_json( (OtpErlangList)data.elementAt(i) );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	private JSONObject obj(OtpErlangList data, int i) {
		try {
			return to_json( (OtpErlangTuple)data.elementAt(i) );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
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
	private JavaView getClass(String classname, List<URL> libs)
			throws Exception {
		URLClassLoader loader = new URLClassLoader(libs.toArray(new URL[0]));
		Class<JavaView> cl = (Class<JavaView>) loader.loadClass(classname);
		Object o = cl.newInstance();
		return (JavaView) o;
	}

	
	// Erlang -> JSON functions
    
	private JSONObject to_json(OtpErlangTuple t) {
		JSONObject ret = new JSONObject();
		try {
			if( t.arity() == 1 ) {
				OtpErlangObject o = t.elementAt(0);
				OtpErlangList list = (OtpErlangList)o;
				for( int i=0; i < list.arity();  i++ ) {
					OtpErlangTuple e = (OtpErlangTuple)list.elementAt(i);
					ret.put((String)to_json(e.elementAt(0)),
							to_json(e.elementAt(1)));
				}
			} else if( t.arity() == 2 ) {
				ret.put( (String)to_json(t.elementAt(0)),
						 to_json(t.elementAt(1)) );
			} else {
				throw( new Exception("bad tuple arity for JSON"));
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return ret;
	}

	private JSONArray to_json(OtpErlangList l) {
		JSONArray ret = new JSONArray();
		for( int i=0; i < l.arity();  i++ ) {
			ret.put( to_json(l.elementAt(i)) );
		}
		return ret;
	}
	
	// TODO: rewrite this like the ones above
	private Object to_json(OtpErlangObject o) {
		
		try {
			if (o instanceof OtpErlangBinary) {
				return new String(((OtpErlangBinary) o).binaryValue());

			} else if (o instanceof OtpErlangChar) {
				return ((OtpErlangChar) o).charValue();

			} else if (o instanceof OtpErlangString) {
				return ((OtpErlangString) o).stringValue();

			} else if (o instanceof OtpErlangInt) {
				return ((OtpErlangInt) o).intValue();

			} else if (o instanceof OtpErlangLong) {
				return ((OtpErlangLong) o).longValue();

			} else if (o instanceof OtpErlangFloat) {
				return ((OtpErlangFloat) o).floatValue();

			} else if (o instanceof OtpErlangTuple) {
				return to_json( ((OtpErlangTuple) o) );

			} else if (o instanceof OtpErlangList) {
				return to_json( ((OtpErlangList) o) );

			} else {
				System.out.println("unhandled Erlang type: " + o);
				return o.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
//	public static void main(String args[]) {
//		System.out.println("wut");
//		
//		// build up a doc
//		OtpErlangTuple id = mk_tup("_id", "b");
//		OtpErlangTuple rev = mk_tup("_rev", "1-b");
//		OtpErlangTuple title = mk_tup("title", "A multiple word title");
//		
//		OtpErlangObject[] arr = new OtpErlangObject[3];
//		arr[0] = id;
//		arr[1] = rev;
//		arr[2] = title;
//		OtpErlangList list = new OtpErlangList(arr);
//		
//		OtpErlangTuple o = new OtpErlangTuple(list);
//		
//		JavaViewServer jvs = new JavaViewServer();
//		JSONObject jdoc = jvs.to_json(o);
//		try {
//			System.out.println( jdoc.toString(2) );
//			
//		} catch( Exception e ) {
//			System.out.println("fail");
//		}
//	}
//
//	private static OtpErlangTuple mk_tup(String k, String v) {
//
//		OtpErlangString[] arr = new OtpErlangString[2];
//		arr[0] = new OtpErlangString(k);
//		arr[1] = new OtpErlangString(v);
//		return new OtpErlangTuple(arr);
//	}

}
