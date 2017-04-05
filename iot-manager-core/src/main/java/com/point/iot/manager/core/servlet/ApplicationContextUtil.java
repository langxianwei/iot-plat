package com.point.iot.manager.core.servlet;

import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.point.iot.base.message.TcpMessage;
import com.point.iot.manager.core.facade.MessageManagerFacade;
import com.point.iot.manager.core.facade.MessageManagerLogicHandler;
import com.tongqu.base.message.shrtcp.MessageCodecFactory;

public class ApplicationContextUtil {
	
	private static ApplicationContext context;
	private static Object lock=new Object();
	
	public static void registor() {
		synchronized (lock) {
			if (context == null) {
				context = new ClassPathXmlApplicationContext("yahe-manager-spring.xml");  
			}
		}
	}
	/**
	 * 获取解码编码器的工厂类
	 * @return
	 */
	public static Map<String, MessageCodecFactory> getMessageCodecFactory(){
		return context.getBeansOfType(MessageCodecFactory.class);
	}
	
	/**
	 * 转发消息
	 * @param message
	 * @param session
	 */
	public  static void callIotRequestProvider(TcpMessage message, IoSession session){
		MessageManagerLogicHandler handler = null;
		Map<String, MessageManagerFacade> map = context.getBeansOfType(MessageManagerFacade.class);
		for(Map.Entry<String, MessageManagerFacade> entry : map.entrySet()){
			MessageManagerFacade facade = entry.getValue();
			if(facade.getFacadeMap() != null){
				handler = (MessageManagerLogicHandler) facade.getFacadeMap().get(message.getProtocolType());
				if(handler != null){
					 handler.doExec(message, session);
				}
			}
		}
	}

	public static ApplicationContext getContext() {
		return context;
	}

	public static void setContext(ApplicationContext context) {
		ApplicationContextUtil.context = context;
	}

}
