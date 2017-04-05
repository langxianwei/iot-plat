package com.point.iot.base.email;

import java.util.Map;

import org.apache.log4j.Logger;

import com.point.iot.base.config.ConfigManager;

/**
 * 邮件发送类
 * @author langxianwei
 * 		EMail email = new EMail(new String[]{"server_watcher@puente.cn", "server_watcher@puente.cn"}, "为什么我的奖品还不发放？", "玩家12342343反馈：阿隆索的空间发了苦涩的阿隆索的分。");
		email.send();
 */
public class EMail implements Runnable {

	Logger logger = Logger.getLogger(EMail.class);
	private String[] masSendTo;
	private String msSubject;
	private String msContent;
	//
	private boolean mbSendAsHtml = true;
	
	private static String[] asToAddrList = {
			"server_watcher@puente.cn"
	};

	public static Map<String, String> configMap;
	private static String LOCAL_IP;
	private static String ENABLE_EMAIL;
	
	public EMail(String sSubject, String sContent) {
		this(asToAddrList, sSubject, sContent);
	}
	
	public EMail(String[] asSendTo, String sSubject, String sContent) {
		masSendTo = asSendTo;
		msSubject = sSubject;
		msContent = sContent;
		initConfig();
	}
	

	/**
	 * 启动选项 1.加载config 
	 * 
	 * @throws Exception
	 */
	private void initConfig() {
		if (configMap == null) {
			try {
				ConfigManager configManager = new ConfigManager();
				configMap = configManager.getConfig();
				LOCAL_IP = configMap.get("LOCAL_IP");
				ENABLE_EMAIL = configMap.get("tongqu.enable_email");
			} catch (Exception e) {
			}
		}
	}
	
	public void sendAsTxt() {
		mbSendAsHtml = false;
		send();
	}
	
	public void send() {
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run() {
		sendEMail();
	}
	
	private void sendEMail() {
		if (LOCAL_IP != null && LOCAL_IP.startsWith("10.18")) {
			//内网邮件只发给ServerWatcher
			for (int i=0; i<masSendTo.length; i++) {
				if (!"server_watcher@puente.cn".equalsIgnoreCase( masSendTo[i])) {
					masSendTo[i] = null;
				}
			}
		}
		
		// 这个类主要是设置邮件
		MailSenderObj mailInfo = new MailSenderObj();
		mailInfo.setMailServerHost("smtp.qq.com");
		mailInfo.setMailServerPort("465");
		mailInfo.setValidate(true);
		mailInfo.setUserName("langxianwei@puente.cn");
		mailInfo.setPassword("langwei");// 您的邮箱密码
		mailInfo.setFromAddress("langxianwei@puente.cn");
		mailInfo.setToAddress(masSendTo);
		mailInfo.setSubject(msSubject + "(From " + LOCAL_IP + ")");
		mailInfo.setContent("[From " + LOCAL_IP + "]\n" + msContent);
		
		if (!"true".equals(ENABLE_EMAIL)) {
			return;
		}
		// 这个类主要来发送邮件
		SimpleMailSender sms = new SimpleMailSender();
		if (mbSendAsHtml) {
			sms.sendHtmlMail(mailInfo);// 发送html格式
		} else {
			sms.sendTextMail(mailInfo);// 发送文体格式
		}
	}
}
