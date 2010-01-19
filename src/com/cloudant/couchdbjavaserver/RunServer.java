package com.cloudant.couchdbjavaserver;

import java.util.Scanner;
import java.util.ServiceLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RunServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServiceLoader<JavaView> views = ServiceLoader.load(JavaView.class);
		System.out.println(views.toString());
		System.out.println(views.iterator().hasNext());
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
						views = null;
						System.out.println("true");
						break;
					case ADD_FUN:
						System.out.println("true");
						break;
					case MAP_DOC:
						JSONArray out = new JSONArray();
						final JSONObject jsondoc = arr.getJSONObject(1);
						System.out.println("Calling views");
						for (JavaView view : views) {
							System.out.println(view.getClass());
							out.put(view.MapDoc(jsondoc));
						}
						System.out.println(out.toString());
						break;
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

}
