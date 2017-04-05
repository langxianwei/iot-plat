package com.point.iot.utils;

import java.io.File;

import org.apache.log4j.Logger;

public class FilePathUtils {
	
	private static Logger logger = Logger.getLogger(FilePathUtils.class);

	public static String getFilePath(String file) {
		try {
			String currentString = Thread.currentThread().getContextClassLoader().getResource("").getFile();
			int index = currentString.indexOf("game-major");
			if (index > 0) {
				return currentString.substring(0, index) + "game-major" + File.separator + "config" + File.separator + file;
			}
		} catch (Exception e) {
			logger.error("getFilePath", e);
		}
		return "";

	}

	public static String getConfigCenterPath(String group,String file) {
		String pathString = "";
		try {
			String currentString = Thread.currentThread().getContextClassLoader().getResource("").getFile();
			int index = currentString.indexOf("config-manager-server");
			if (index > 0) {
				pathString = currentString.substring(0, index) + "game-major" + File.separator + "config-center";
				if (group!=null&&!group.equals("")) {
					pathString+= File.separator+group+ File.separator+file;
				}else if (file!=null&&!file.equals("")) {
					pathString+= File.separator+file;
				}
			}
		} catch (Exception e) {
			logger.error("getConfigCenterPath", e);
		}
		return pathString;
	}
	
	
	public static String getConfigCenterRootPath() {
		
		
		return "D:\\properties";
	}
	

}
