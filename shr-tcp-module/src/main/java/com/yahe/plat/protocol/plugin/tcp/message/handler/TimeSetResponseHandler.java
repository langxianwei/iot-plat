package com.yahe.plat.protocol.plugin.tcp.message.handler;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.springframework.stereotype.Component;

import com.point.iot.base.message.TcpMessage;
import com.point.iot.manager.core.facade.MessageManagerLogicHandler;
import com.yahe.plat.protocol.plugin.tcp.TcpNetCmd;
import com.yahe.plat.protocol.plugin.tcp.model.CjyTcpMessage;
/**
 * 时间设置响应逻辑类
 * 收到设置时间响应消息后，发送退出登录消息。
 * @author langxianwei 2017年3月18日
 *
 */
@Component
public class TimeSetResponseHandler implements MessageManagerLogicHandler{
	Logger logger = Logger.getLogger(TimeSetResponseHandler.class);
	
	@Override
	public void doExec(TcpMessage message, IoSession session) {
		CjyTcpMessage tcpMsg = (CjyTcpMessage)message;
		logger.debug(message.getCmd() + "-----------------" );
		//登出响应消息
		CjyTcpMessage logoutReq = new CjyTcpMessage();
		logoutReq.setAddress(tcpMsg.getAddress());
		logoutReq.setCmd(TcpNetCmd.ACK & TcpNetCmd.LOGOUT_ID);
		logoutReq.setLength(0);
		session.write(logoutReq);
		//TODO 删除session信息
		session.closeNow();
	}
}
