package com.point.iot.service.proxy;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import com.point.iot.base.email.EMail;
import com.point.iot.base.message.TcpMessage;
import com.point.iot.base.timer.ActionManager;
import com.point.iot.base.timer.TimerActionListener;
import com.point.iot.manager.core.servlet.ApplicationContextUtil;
import com.point.iot.mina.msg.MessageHandler;
import com.point.iot.mina.server.SocketServer;

public class ProxyBiz implements MessageHandler, TimerActionListener, Runnable {
	Logger logger = Logger.getLogger(ProxyBiz.class);

	private static ProxyBiz mInstance;
	// 全局配置参数表
	public static Map<String, String> mConfigMap;
	/**
	 * Manager的DB服务
	 */

	public synchronized static ProxyBiz getInst() {
		if (mInstance == null) {
			ApplicationContextUtil.registor();
			mInstance = new ProxyBiz();
		}
		return mInstance;
	}

	private ProxyBiz() {
	}
	
	public void init() {
		new SocketServer(65001, this);
		(new Thread(this)).start();
	}
	/**
	 * 数据通过此接口转发到各个解析模块
	 */
	@Override
	public void onMsg(TcpMessage message, IoSession session) {
		ApplicationContextUtil.callIotRequestProvider(message, session);
	}

	@Override
	public void onSessionClosed(IoSession session) {
		if (session != null && session.isConnected())
			session.closeOnFlush();
	}

	@Override
	public void onHandShakeCompleted(IoSession session, int handShakeMsgID) {
		switch (handShakeMsgID) {
		default:
			logger.error("收到未知握手请求" + Integer.toHexString(handShakeMsgID));
			break;
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				ActionManager.getInstance().run();

				// 检查磁盘空间
				if (System.currentTimeMillis() - nLastCheckDiskTime > 5 * 60000) {
					checkDiskSpace("/", 50);
					checkDiskSpace("/home", 10);
				}
			} catch (Exception e) {
				e.printStackTrace(); 
			}
		}
	}

	private long nLastCheckDiskTime;

	
	/**
	 * 检查磁盘剩余空间
	 */
	private void checkDiskSpace(String path, int warningPercent) {
		try {
			nLastCheckDiskTime = System.currentTimeMillis();
			File f = new File(path);
			long n1 = f.getFreeSpace() / (1024 * 1024);
			long n2 = f.getTotalSpace() / (1024 * 1024);
			if (n2 == 0) n2 = 1;
			int nPercent = (int) (100 * n1 / n2);
			logger.info("“" + path + "”分区剩余磁盘空间：" + nPercent + "% [" + n1 + "m/" + n2 + "m]");
			if ("127.0.0.1".equals(mConfigMap.get("LOCAL_IP"))) {
				return;
			}
			if (nPercent < warningPercent) {
				String sContent = mConfigMap.get("LOCAL_IP") + "硬盘“" + path + "”分区可用空间低于"
						+ nPercent + "% [" + n1 + "m/" + n2 + "m]，请服务器相关同学处理一下。（本邮件将在5分钟后再次发送）";
				EMail email = new EMail(
						new String[] { "server_watcher@puente.cn" },
						mConfigMap.get("LOCAL_IP") + "硬盘“" + path + "”分区可用空间低于" + nPercent
								+ "%", sContent);
				email.send();
			}
		} catch (Exception ex) {
			logger.error("", ex);
		}
	}


	@Override
	public void onTimerActionExcute(Object paramObj) {

	}

}
