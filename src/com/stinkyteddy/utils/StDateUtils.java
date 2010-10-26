package com.stinkyteddy.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.lucene.document.DateTools;
/**
*Copyright [2010] [David Hardtke]
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*/


/**
 * This class parses some common date string date formats and outputs them
 * as a java.util.Date object.  Additionally, the date can be converted
 * to and from Lucene date format.  This routine is helpful at normalizing
 * dates from multiple APIs. 
 * @author David Hardtke
 *
 */


public class StDateUtils {
	
	public static long MILLIS_PER_YEAR = 1000L * 60L * 60L * 24L * 365L;
	public static long MILLIS_PER_MONTH = 1000L * 60L * 60L * 24L * 30L;
	public static long MILLIS_PER_WEEK = 1000L * 60L * 60L * 24L * 7L;
	public static long MILLIS_PER_DAY = 1000L * 60L * 60L * 24L;
	public static long MILLIS_PER_HOUR = 1000L * 60L * 60L;
	public static long MILLIS_PER_MINUTE = 1000L * 60L;
	public static long MILLIS_PER_SECOND = 1000L;
	  

	
	static public Date parseBingDate(String dateString) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return parseDateString(dateString, df);
	}
	static public Date parseBossDate(String dateString) {
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return parseDateString(dateString, df);
	}
	
	static public Date parseTwitterDate(String dateString) {
//	Tue, 22 Sep 2009 23:16:19 +0000	
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
//		DateFormat df = new SimpleDateFormat("EEE',' dd MMM yyyy kk:mm:ss Z");
		return parseDateString(dateString, df);		
	}
	static public Date parseCollectaDate(String dateString) {
			DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
			return parseDateString(dateString, df);		
	}
	static public Date parseUnixDate(String dateString) {
		try {
			long date = Long.parseLong(dateString);
			return new Date(date);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
	
	public static Date parseVideoSurfDate(String dateString) {
		if (dateString == null || !dateString.contains("ago")) return null;
		Date d = null;
		Calendar today = Calendar.getInstance();
		String[] splitString = dateString.split(" ");
		if (splitString.length>1) {
			try {
				int number = Integer.parseInt(splitString[0]);
				String timeUnit = splitString[1];
				if (timeUnit.contains("yr")) {
					today.add(Calendar.DAY_OF_YEAR, - (365 * number));					
				} else if (timeUnit.contains("mo")) {
					today.add(Calendar.DAY_OF_YEAR, - (30 * number));										
				} else if (timeUnit.contains("da")) {
					today.add(Calendar.DAY_OF_YEAR, - (number));					
				} else if (timeUnit.contains("wk")) {
					today.add(Calendar.DAY_OF_YEAR, - (7 * number));										
				} else if (timeUnit.contains("hr")) {
					today.add(Calendar.HOUR_OF_DAY, - number);
				} else if (timeUnit.contains("mi")) {
					today.add(Calendar.MINUTE, - number);
				}
				return today.getTime();
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return d;
	}
	
	static public Date parseDateString(String dateString) {
		Date d = null;
		d = parseBingDate(dateString);
		if (d != null) return d;
		d = parseBossDate(dateString);
		if (d != null) return d;
		d = parseTwitterDate(dateString);
		if (d != null) return d;
		d = parseVideoSurfDate(dateString);
		if (d != null) return d;
		d = parseCollectaDate(dateString);
		if (d != null) return d;
		d = parseUnixDate(dateString);
		if (d != null) return d;
		return d;
	}
	
	static public Date parseDateString(String dateString, DateFormat df) {
		if (dateString == null || df == null) return null;
		try {
			Date out = df.parse(dateString);
			return out;
		} catch (ParseException pe) {
			return null;
		}
	}
	
	static public String makeIndexString(Date date) {
		if (date == null) return "";
		Date now = new Date();
		long diff = now.getTime() - date.getTime();
		if (diff/MILLIS_PER_YEAR > 0) {
			return DateTools.dateToString(date, DateTools.Resolution.YEAR);
		} else if (diff/MILLIS_PER_MONTH > 0) {
			return DateTools.dateToString(date, DateTools.Resolution.MONTH);
		} else if (diff/MILLIS_PER_DAY > 0) {
			return DateTools.dateToString(date, DateTools.Resolution.DAY);
		} else if (diff/MILLIS_PER_HOUR > 0) {
			return DateTools.dateToString(date, DateTools.Resolution.HOUR);
		} else if (diff/MILLIS_PER_MINUTE > 0) {
			return DateTools.dateToString(date, DateTools.Resolution.MINUTE);
		} else {
			return DateTools.dateToString(date, DateTools.Resolution.SECOND);
		}
	}
	
	static public String makePrintedString(String indexString) {
		if (indexString == null || indexString == "") return "";
		Date now = new Date();
		try {
			long indexed = DateTools.stringToTime(indexString);
			long diff = now.getTime() - indexed;
			long number = 0;
			String unit = "";
			if (diff/MILLIS_PER_YEAR > 0) {
				number = diff/MILLIS_PER_YEAR;
				unit = "year";
			} else if (diff/MILLIS_PER_MONTH > 0) {
				number = diff/MILLIS_PER_MONTH;
				unit = "month";
			} else if (diff/MILLIS_PER_WEEK > 0) {
				number = diff/MILLIS_PER_WEEK;
				unit = "week";
			} else if (diff/MILLIS_PER_DAY > 0) {
				number = diff/MILLIS_PER_DAY;
				unit = "day";
			} else if (diff/MILLIS_PER_HOUR > 0) {
				number = diff/MILLIS_PER_HOUR;
				unit = "hour";
			} else if (diff/MILLIS_PER_MINUTE > 0) {
				number = diff/MILLIS_PER_MINUTE;
				unit = "minute";
			} else if (diff/MILLIS_PER_SECOND> 0) {
				number = diff/MILLIS_PER_SECOND;
				unit = "second";
			} else {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			sb.append(String.valueOf(number));
			sb.append(" " + unit);
			if (number != 1) {
				sb.append("s");
			}
			sb.append(" ago");
			return sb.toString();
		} catch (Exception e) {
//			System.out.println(e.getMessage() + indexString);
			return "";
		}
	}
	static public long getAgeInMillis(String indexString) {
		if (indexString == null) return 0;
		Date now = new Date();
		try {
			long indexed = DateTools.stringToTime(indexString);
			long diff = now.getTime() - indexed;
			return diff;
		} catch (Exception e) {
//			System.out.println(e.getMessage());
			return 0;
		}
	}
	static public String convertMillisToString (long diff) {
		if (diff == 0) return "";
		try {
			long number = 0;
			String unit = "";
			if (diff/MILLIS_PER_YEAR > 0) {
				number = diff/MILLIS_PER_YEAR;
				unit = "year";
			} else if (diff/MILLIS_PER_MONTH > 0) {
				number = diff/MILLIS_PER_MONTH;
				unit = "month";
			} else if (diff/MILLIS_PER_WEEK > 0) {
				number = diff/MILLIS_PER_WEEK;
				unit = "week";
			} else if (diff/MILLIS_PER_DAY > 0) {
				number = diff/MILLIS_PER_DAY;
				unit = "day";
			} else if (diff/MILLIS_PER_HOUR > 0) {
				number = diff/MILLIS_PER_HOUR;
				unit = "hour";
			} else if (diff/MILLIS_PER_MINUTE > 0) {
				number = diff/MILLIS_PER_MINUTE;
				unit = "minute";
			} else if (diff/MILLIS_PER_SECOND> 0) {
				number = diff/MILLIS_PER_SECOND;
				unit = "second";
			} else {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			sb.append(String.valueOf(number));
			sb.append(" " + unit);
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}
	static public String convertMillisToFrequency (long diff) {
		if (diff == 0) return "";
		try {
			long number = 0;
			String unit = "";
			if (MILLIS_PER_SECOND/diff > 0) {
				number = MILLIS_PER_SECOND/diff;
				unit = "second";
			} else if (MILLIS_PER_MINUTE/diff > 0) {
				number = MILLIS_PER_MINUTE/diff;
				unit = "minute";
			} else if (MILLIS_PER_HOUR/diff > 0) {
				number = MILLIS_PER_HOUR/diff;
				unit = "hour";
			} else if (MILLIS_PER_DAY/diff > 0) {
				number = MILLIS_PER_DAY/diff;
				unit = "day";
			} else if (MILLIS_PER_WEEK/diff > 0) {
				number = MILLIS_PER_WEEK/diff;
				unit = "week";
			} else if (MILLIS_PER_MONTH/diff > 0) {
				number = MILLIS_PER_MONTH/diff;
				unit = "month";
			} else if (MILLIS_PER_YEAR/diff > 0) {
				number = MILLIS_PER_YEAR/diff;
				unit = "year";
			} else {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			sb.append(String.valueOf(number));
			sb.append(" " + unit);
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}


}

