package com.point.iot.base.email;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import com.point.iot.base.tools.CommUtils;

public class EmailHelper {
	static Logger logger = Logger.getLogger(EmailHelper.class);
	private static long sendTime = 0;
	public static final String SERVER_WATCHERS = "server_watcher@puente.cn";
//	StringBuilder sb = null;
	
	public static void send(Exception exception,  String title, String ...receivers) {

		if(System.currentTimeMillis() - sendTime < 60000) {
			return;
		}
		sendTime = System.currentTimeMillis();
		StringBuilder msbErrMsg = new StringBuilder();
		StringWriter writer = new StringWriter();
		exception.printStackTrace(new PrintWriter(writer));
		
		msbErrMsg.append("\n[" + CommUtils.parseDate(System.currentTimeMillis()) + "]" + "的onMsg()函数出现异常 " + exception);
		msbErrMsg.append("\n=========================\n");
		msbErrMsg.append(writer.getBuffer().toString());
		EMail email = new EMail(receivers, "服务器异常," + title, msbErrMsg.toString());
		email.sendAsTxt();
	}
	
	public static void send(Exception ex, String title, String content, String ...receivers) {
		if (ex == null) {
			ex = new Exception();
		}
		StringBuilder msbErrMsg = new StringBuilder();
		StringWriter writer = new StringWriter();
		if (ex != null) {
			ex.printStackTrace(new PrintWriter(writer));
		}
		msbErrMsg.append("\n[" + CommUtils.parseDate(System.currentTimeMillis()) + "]" + content);
		msbErrMsg.append("\n=========================\n");
		msbErrMsg.append(writer.getBuffer().toString());
		
		logger.info("SendEmail:" + title + "\n" +  msbErrMsg.toString());
		if(System.currentTimeMillis() - sendTime < 60000) {
			return;
		}
		sendTime = System.currentTimeMillis();
		EMail email = new EMail(receivers, title, msbErrMsg.toString());
		email.sendAsTxt();
	}
}
