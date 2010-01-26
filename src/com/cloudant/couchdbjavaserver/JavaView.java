/**
 *
 */
package com.cloudant.couchdbjavaserver;
import org.json.*;

/**
 * @author hardtke
 *
 */
public interface JavaView {

	JSONArray MapDoc(JSONObject doc);
	
	JSONArray Reduce(JSONArray mapResults);
	// only first result in array is used!
	// null not valid return type
	
	JSONArray ReReduce(JSONArray reduceResults);
	// only first results in array is used!
	// null not valid return type

	void Configure(String config);
	
	void Log(String logline);
}
