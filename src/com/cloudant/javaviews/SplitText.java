package com.cloudant.javaviews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.cloudant.couchdbjavaserver.*;

public class SplitText implements JavaView {

    public void Log(String logline) {
        System.out.println();

    }

    public JSONArray MapDoc(JSONObject doc) {
        JSONArray out = new JSONArray();
        try {
            String id = doc.getString("_id");
            String rev = doc.getString("_rev");
            String text = doc.getString("title");
            if (text != null && text.length() > 0) {
                for (String s : text.split(" ")) {
                    out.put(new JSONArray().put(s).put(1));
                }
            } else {
                out.put(new JSONArray());
            }
        } catch (JSONException je) {
            out.put(new JSONArray());
            Log("Malformed document: " + doc.toString());
        }
        return out;
    }

    @Override
        public JSONObject ReReduce(JSONObject doc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
        public JSONObject Reduce(JSONArray doc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
        public boolean Reset() {
        // TODO Auto-generated method stub
        return false;
    }


}
