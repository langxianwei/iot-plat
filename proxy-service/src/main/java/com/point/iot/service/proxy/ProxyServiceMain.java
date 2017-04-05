package com.point.iot.service.proxy;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import com.point.iot.base.config.ConfigManager;

public class ProxyServiceMain {
	static Logger logger = Logger.getLogger(ProxyServiceMain.class);
    public static void main(String[] args) { 
//    	FlashBiz fb = new FlashBiz();
    	ConfigManager configManager = new ConfigManager();
    	logger.debug("HallService服务启动......");
		try {
			ProxyBiz.mConfigMap = configManager.getConfig();
			ProxyBiz.getInst().init();
			logger.debug("HallService服务启动完成!");
		} catch (FileNotFoundException e) {
			logger.error("无法找到配置文件", e);
		} catch (Exception e) {
			logger.error("服务启动失败!", e);
		}
		
    } 
    
}
