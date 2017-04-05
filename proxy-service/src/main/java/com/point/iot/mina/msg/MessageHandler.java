package com.point.iot.mina.msg;

import org.apache.mina.core.session.IoSession;

import com.point.iot.base.message.TcpMessage;

public interface MessageHandler {
	public void onMsg(TcpMessage messageReq, IoSession session);
	public void onSessionClosed(IoSession session);
	/**
	 * 握手成功
	 * @param session
	 * @param handShakeMsgID
	 */
	public void onHandShakeCompleted(IoSession session, int handShakeMsgID);
}
