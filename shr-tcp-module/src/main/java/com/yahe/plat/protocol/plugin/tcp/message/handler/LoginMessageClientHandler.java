package com.yahe.plat.protocol.plugin.tcp.message.handler;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.springframework.stereotype.Component;

import com.point.iot.base.message.TcpMessage;
import com.point.iot.manager.core.facade.MessageManagerLogicHandler;
import com.tongqu.base.message.shrtcp.SHRTcpMessage;
import com.yahe.plat.protocol.plugin.tcp.TcpNetCmd;
import com.yahe.plat.protocol.plugin.tcp.model.CjyTcpMessage;
/**
 * 登录处理逻辑类
 * 收到登录消息后，发送登录响应消息。同时，发送发送控制码为0x04的帧召测数据
 * @author langxianwei 2017年3月18日
 *
 */
@Component
public class LoginMessageClientHandler implements MessageManagerLogicHandler{
	Logger logger = Logger.getLogger(LoginMessageClientHandler.class);
	
	@Override
	public void doExec(TcpMessage message, IoSession session) {
		CjyTcpMessage tcpMsg = (CjyTcpMessage)message;
		logger.debug("收到设备" + tcpMsg.getAddress() + "的登录消息" );
		//TODO 更新登录设备的状态，包括ip，在线状态等
		//登录响应消息
		CjyTcpMessage loginResp = new CjyTcpMessage();
		loginResp.setAddress(tcpMsg.getAddress());
		loginResp.setCmd(TcpNetCmd.LOGIN_ID);
		loginResp.setLength(0);
		session.write(loginResp);
		
		//召测数据
		CjyTcpMessage dataReq = new CjyTcpMessage();
		dataReq.setAddress(tcpMsg.getAddress());
		dataReq.setCmd(TcpNetCmd.READ_DATA_ID);
		dataReq.setLength(0);
		session.write(dataReq);
	}
}
