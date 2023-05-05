package eu.matusi.manager.help;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class Help {
	
	private static DateFormat dateFormat = DateFormat.getDateTimeInstance();
	
	public static String removeZeros(String s) {
		String str = new String(s);
		if (str.charAt(0) == '0')
			str = str.substring(1);
		return str;
	}
	
	public static String addZeros(String s) {
		String str = new String(s);
		if (str.length() == 1)
			str = "0" + str;
		return str;
	}
	
	// create Calendar which represent date in format 1.12.2012 8:00:00
	public static Calendar stringToCal(String str) {
		Calendar cal = Calendar.getInstance(); //create new instance of calendar
		String[] arrBegin = str.split("[- ]");
		for (int i = 1; i <= 2; i++)
			arrBegin[i] = removeZeros(arrBegin[i]);

		String newStr = arrBegin[2] + "." + arrBegin[1] + "."
				+ arrBegin[0] + " " + arrBegin[3] + ":00";
		try {
			Date date = dateFormat.parse(newStr);
			cal.setTime(date);
			return cal;
		} catch (ParseException e) {
			return null;
		}
	}
	
	// create date String in format 2012-12-01 8:00
	public static String calToString(Calendar cal) {
		String str = dateFormat.format(cal.getTime());
		String[] arrBegin = str.split("[. ]");
		for (int i = 0; i < 2; i++)
			arrBegin[i] = addZeros(arrBegin[i]);
		
		str = arrBegin[2] + "-" + arrBegin[1] + "-" + arrBegin[0]
				+ " " + arrBegin[3];
		str = str.substring(0, str.length() - 3);
		return str;
	}
	
	public static String getDate(Calendar cal) {
		return dateFormat.format(cal.getTime()).split(" ")[0];
	}
	
	public static String getTime(Calendar cal) {
		String tmp = dateFormat.format(cal.getTime()).split(" ")[1];
		return tmp.substring(0, tmp.length() - 3);
	}
	
	public static int getDuration(String str) {
		return Integer.parseInt(str.substring(0, str.length() - 5));
	}
	
	public static String getDurationStr(String str) {
		return str.substring(0, str.length() - 5);
	}
	
	public static String durationToString(int x) {
		return new String (String.valueOf(x) + " min.");
	}
	
	public static String durStrToString(String str) {
		return str + " min.";
	}

}
