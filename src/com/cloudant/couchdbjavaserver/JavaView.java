/**
 *
 */
package com.cloudant.couchdbjavaserver;
import org.json.*;

/**
 * This interface defines the methods that need to be implemented for a java view in
 * couchdb.
 * @author David Hardtke 
 *
 */
public interface JavaView {
/**
 * 
 * @param doc A couchdb document in the form of a JSONObject.  Required to have String fields "_id" and "_rev".
 * @return returns a JSONArray of JSONArrays with the map results for this document, where the JSONArray are of the form [key, value]. If there are no map results for this doc (document excluded from view), return new JSONArray().put(newJSONArray())", i.e. [[]].  Otherwise, output is [[key1,value1],[key2,value2],...] where the values can be any valid JSON type (JSONObject, JSONArray, number, String, true, false, JSONObject.NULL).
 */
	JSONArray MapDoc(JSONObject doc);
/**
 * Reduce step called for all map results produced for this view.  This function must be defined if "reduce" is specified in the design document.
 * @param mapResults a JSONArray containing the map results from the previous step.  Map results are of form [[key, id-of-doc], value].
 * @return  JSONArray containing a single value (i.e. JSONArray of length 1), where the value can be any valid JSON type (JSONObject, JSONArray, number, String, true, false, JSONObject.NULL).  JSONArray returned cannot be empty.  
 */
	JSONArray Reduce(JSONArray mapResults);

/**
 * ReReduce step called for output of reduce.  This function must be defined if "reduce" is specified in design document.  A reduce must also have a valid rereduce.
 * @param reduceResults JSONArray contain output of previous reduce steps.  The form is [value1, value2, value3, ...] where the values are any valid JSON type (JSONObject, JSONArray, Number, boolean, string, JSONObject.NULL)	
 * @return JSONArray containing a single value (i.e. JSONArray of length 1), where the value can be any valid JSON type (JSONObject, JSONArray, number, String, true, false, JSONObject.NULL).  JSONArray returned cannot be empty.  
 */
	JSONArray ReReduce(JSONArray reduceResults);
/**
 * 
 * @param config String used to initialize this instance of the JavaView interface.  User defined.  Configure method is called immediately after the constructor.
 */
	void Configure(String config);
/**
 * This method handles logging for the class.
 * @param logline Comment to be logged.
 * If it is desired that the message is passed to the couchdb log file, write a JSONArray with first value "log" and second value logLine to stdout, i.e.
 * 	public void Log(String message) {
 *		JSONArray out = new JSONArray();
 *		out.put("log");
 *		out.put(message);
 *		System.out.println(out.toString());		
 *	}
*/
	void Log(String logline);
}
