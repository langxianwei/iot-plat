package com.point.iot.mina.msg;

import org.apache.mina.core.session.IoSession;

import com.point.iot.base.message.TcpMessage;

/**
 * 接收消息处理接口
 * @author langxianwei
 *
 */
public interface MsgHandler {
	public void onMsg(TcpMessage message, IoSession session);
	public void onSessionClosed(IoSession session);
	/**
	 * 握手成功
	 * @param session
	 * @param handShakeMsgID
	 */
	public void onHandShakeCompleted(IoSession session, int handShakeMsgID);
}
