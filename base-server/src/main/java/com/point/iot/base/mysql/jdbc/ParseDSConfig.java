package com.point.iot.base.mysql.jdbc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.point.iot.base.config.ConfigManager;
import com.point.iot.base.mysql.jdbc.DSConfigBean;

/**
 * @author chenyanlin
 * 
 */
public class ParseDSConfig {
	/**
	 * 构造函数
	 */
	public ParseDSConfig() {
	}

	/**
	 * 读取xml配置文件
	 * 
	 * @param path
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Vector readConfigInfo(String path) {
		String rpath = ConfigManager.getFullFilePath(path);
		Vector dsConfig = null;
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(rpath);// 读取路径文件
			dsConfig = new Vector();
			SAXBuilder sb = new SAXBuilder();
			Document doc = sb.build(fi);
			Element root = doc.getRootElement();
			List pools = root.getChildren();
			Element pool = null;
			Iterator allPool = pools.iterator();
			String driver = "";
			while (allPool.hasNext()) {
				pool = (Element) allPool.next();
				DSConfigBean dscBean = new DSConfigBean();
				dscBean.setType(pool.getChild("type").getText());
				dscBean.setName(pool.getChild("name").getText());
				System.out.println(dscBean.getName());
				String d = pool.getChild("driver").getText();
				if(!d.equals(driver)){
					dscBean.setDriver(pool.getChild("driver").getText());
					driver = d ;
				}
				dscBean.setUrl(pool.getChild("url").getText());
				dscBean.setUsername(pool.getChild("username").getText());
				dscBean.setPassword(pool.getChild("password").getText());
				dscBean.setMaxconn(Integer.parseInt(pool.getChild("maxconn").getText()));
				dsConfig.add(dscBean);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finally {
			try {
				fi.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return dsConfig;
	}

}
