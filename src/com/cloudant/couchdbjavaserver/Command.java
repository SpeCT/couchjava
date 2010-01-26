/**
 *
 */
package com.cloudant.couchdbjavaserver;

import java.util.HashMap;

/**
 * @author hardtke
 *
 */
public enum Command {
	RESET ("reset"),
	ADD_LIBRARY ("add_library"),
	ADD_FUN ("add_fun"),
	MAP_DOC ("map_doc"),
	REDUCE ("reduce"),
	REREDUCE ("rereduce"),
	LOG ("log");
	static HashMap<String,Command> lookup;
	static {
		lookup = new HashMap<String, Command>();
		for (Command c : Command.values()) {
			lookup.put(c.getCouchCommand(), c);
		}
	}

	private String couchdbString;
	Command (String couchdbString) {
		this.couchdbString = couchdbString;
	}
	public String getCouchCommand() {
		return couchdbString;
	}
	public static Command getCommandFromString(String in) {
		if (in == null) return null;
		return lookup.get(in);
	}

}
