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
	boolean Reset();
	
	JSONArray MapDoc(JSONObject doc);
	
	JSONObject Reduce(JSONArray reduceOut);
	
	JSONObject ReReduce(JSONObject doc);
	
	void Log(String logline);
}
