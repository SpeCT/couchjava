package com.cloudant.ejje;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangChar;
import com.ericsson.otp.erlang.OtpErlangFloat;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

/**
 * Erlang to JSON conversion functions
 * 
 * @author boorad
 *
 */
public class ErlangJson {

	public static String binstr(OtpErlangList data, int i) {
		return (String)to_json( data.elementAt(i));
	}

	public static JSONArray arr(OtpErlangList data, int i) {
		try {
			return to_json( (OtpErlangList)data.elementAt(i) );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject obj(OtpErlangList data, int i) {
		try {
			return to_json( (OtpErlangTuple)data.elementAt(i) );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject to_json(OtpErlangTuple t) {
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

	public static JSONArray to_json(OtpErlangList l) {
		JSONArray ret = new JSONArray();
		for( int i=0; i < l.arity();  i++ ) {
			ret.put( to_json(l.elementAt(i)) );
		}
		return ret;
	}

	// TODO: finish me
	public static Object to_json(OtpErlangObject o) {
		
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

			} else if (o instanceof OtpErlangAtom) {
				OtpErlangAtom atom = (OtpErlangAtom) o;
				String value = atom.atomValue();
				if ("null".equals(value)) { 
					return JSONObject.NULL;					
				} else if ("true".equals(value) || "false".equals(value)) {
					return atom.booleanValue();
				} else {
					System.out.println("Unknonw Atom Erlang type: " + o);
					return o.toString();
				}

			} else {
				System.out.println("unhandled Erlang type: " + o);
				return o.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

}
