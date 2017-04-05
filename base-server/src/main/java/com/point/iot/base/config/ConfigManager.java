package com.point.iot.base.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.point.iot.utils.FilePathUtils;

/**
 * @author Josn
 * @Date 2011-9-21 下午06:19:10
 * @version V1.0
 */
public class ConfigManager {
	/**
	 * 是否部署到服务器
	 */
	public static final boolean IS_WINDOW = ((System.getProperties()
			.getProperty("os.name")).indexOf("Windows") >= 0);

	public static final boolean IS_MAC = ((System.getProperties()
			.getProperty("os.name")).indexOf("Mac") >= 0);

	private final static String WINDOW_PATH = "D:" + File.separator + "config"
			+ File.separator;

	private final static String LINUX_PATH = File.separator + "home"
			+ File.separator + "yhplat" + File.separator + "properties"
			+ File.separator;

	/**
	 * 读取配置文件
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getFullFilePath(String fileName) {
		if (IS_WINDOW) {
			return WINDOW_PATH + fileName;
		} else if (IS_MAC) {
			return FilePathUtils.getFilePath("") + fileName;
		} else {
			return LINUX_PATH + fileName;
		}
	}
	
	public static  String getConfigCenter() {
		if (IS_WINDOW) {
			return "D:" + File.separator  + "config_center/";
		} else if (IS_MAC) {
			return "/tongqu/config_center/";
		} else {
			return "/tongqu/server/config_center/";
		}
	}

	public Map<String, String> getConfig() throws FileNotFoundException,
			IOException {
		Map<String, String> configMap = new HashMap<String, String>();
		Properties prop = new Properties();

		FileInputStream fis;
		fis = new FileInputStream(getFullFilePath("config.properties"));
		prop.load(fis);
		Set<Entry<Object, Object>> entrySet = prop.entrySet();

		for (Entry<Object, Object> entry : entrySet) {
			if (entry.getKey() != null && entry.getValue() != null)
				configMap.put(String.valueOf(entry.getKey()).trim(), String
						.valueOf(entry.getValue()).trim());
		}
		return configMap;
	}


	public static boolean saveFile(Document document, File file) {
		boolean flag = true;
		try {
			/** 将document中的内容写入文件中 */
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			/** 编码 */
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
		} catch (Exception ex) {
			flag = false;
			ex.printStackTrace();
		}
		return flag;
	}

	public static boolean isDeploy() {
		return !(IS_MAC || IS_WINDOW);
	}
}
