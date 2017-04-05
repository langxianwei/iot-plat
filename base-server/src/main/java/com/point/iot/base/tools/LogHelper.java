package com.point.iot.base.tools;

public class LogHelper {

//	private static String title = "{Table:\"%s\",User:%d,message:\"%s\"}";
	private static String title = "%s,%d,%s";

	public static String getTitle(String matchInstance, int userID, String message) {
		return String.format(title, matchInstance, userID, message);
	}

	public static String getTitle(String matchInstance, String message) {
		return String.format(title, matchInstance, -1, message);
	}
	
	public static String getTitle(int userID, String message) {
		return String.format(title, "null", userID, message);
	}
}