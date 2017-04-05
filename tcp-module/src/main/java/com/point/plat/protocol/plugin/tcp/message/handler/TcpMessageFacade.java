package com.point.plat.protocol.plugin.tcp.message.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.point.iot.base.message.MessageCodecRegister;
import com.point.iot.manager.core.facade.MessageManagerFacade;
import com.point.iot.manager.core.facade.MessageManagerLogicHandler;
import com.point.plat.protocol.plugin.codec.MessageTcpDecoder;
import com.point.plat.protocol.plugin.codec.MessageTcpEncoder;
import com.point.plat.protocol.plugin.tcp.TcpNetCmd;
import com.point.plat.protocol.plugin.tcp.model.CjyTcpMessage;

@Component
public class TcpMessageFacade implements MessageManagerFacade {
	Logger logger = Logger.getLogger(TcpMessageFacade.class);

	private Map<Integer, MessageManagerLogicHandler> facadeMap = new HashMap<Integer, MessageManagerLogicHandler>();
	@Resource(type = LoginMessageClientHandler.class)
	private LoginMessageClientHandler loginMessageClientHandler;
	@Resource(type = DataGetMessageHandler.class)
	private DataGetMessageHandler dataGetMessageHandler;
	@Resource(type = TimeSetResponseHandler.class)
	private TimeSetResponseHandler timeSetResponseHandler;
	
	@PostConstruct
	public void registry() {
		logger.info("====================TcpMessageFacade Registry=======================");
		//注册解码器及编码器
		MessageCodecRegister.addEncoder(CjyTcpMessage.class, new MessageTcpEncoder());
		MessageCodecRegister.addDecoder(CjyTcpMessage.class, new MessageTcpDecoder());
		// TCP协议解析组件
		this.facadeMap.put(TcpNetCmd.LOGIN_ID, loginMessageClientHandler);
		this.facadeMap.put(TcpNetCmd.READ_DATA_ID, dataGetMessageHandler);
		this.facadeMap.put(TcpNetCmd.SET_TIME_ID, timeSetResponseHandler);
	}

	@Override
	public Map<Integer, MessageManagerLogicHandler> getFacadeMap() {
		return facadeMap;
	}

}
