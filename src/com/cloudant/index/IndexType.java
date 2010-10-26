package com.cloudant.index;

import java.util.HashMap;

public enum IndexType {
	STRING ("string"),
	KEYWORD ("keyword"),
	INT ("int"),
	LONG ("long"),
	FLOAT ("float"),
	DOUBLE ("double"),
	DATE ("date"),
	JSONOBJECT ("jsonobject"),
	NULL ("null"),
	BOOLEAN ("boolean");
	static HashMap<String,IndexType> lookup;
	static {
		lookup = new HashMap<String, IndexType>();
		for (IndexType c : IndexType.values()) {
			lookup.put(c.getIndexType(), c);
		}
	}

	private String name;
	IndexType (String name) {
		this.name = name;
	}
	public String getIndexType() {
		return name;
	}
	public static IndexType getIndexTypeFromString(String in) {
		if (in == null) return null;
		return lookup.get(in);
	}

}
