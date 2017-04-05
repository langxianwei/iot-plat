package com.point.iot.manager.core.facade;

import org.apache.mina.core.session.IoSession;

import com.point.iot.base.message.TcpMessage;


/**
 * @author langxianwei
 * 
 */
public interface MessageManagerLogicHandler {

	void doExec(TcpMessage message, IoSession session);
}
