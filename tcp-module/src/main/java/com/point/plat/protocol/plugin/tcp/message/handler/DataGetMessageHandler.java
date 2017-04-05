package com.point.plat.protocol.plugin.tcp.message.handler;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.springframework.stereotype.Component;

import com.point.iot.base.message.TcpMessage;
import com.point.iot.base.mysql.jdbc.DBConnectionManager;
import com.point.iot.base.mysql.jdbc.FunctionParam;
import com.point.iot.base.mysql.jdbc.QueryHelper;
import com.point.iot.base.tools.CommUtils;
import com.point.iot.manager.core.facade.MessageManagerLogicHandler;
import com.point.plat.protocol.plugin.tcp.TcpNetCmd;
import com.point.plat.protocol.plugin.tcp.model.CjyTcpMessage;
/**
 * 获取数据处理逻辑类
 * 收到数据消息后，发送数据响应消息。同时，发送发送控制码为0x03的帧设置时间
 * @author langxianwei 2017年3月18日
 *
 */ 
@Component
public class DataGetMessageHandler implements MessageManagerLogicHandler{
	Logger logger = Logger.getLogger(DataGetMessageHandler.class);
	
	@Override
	public void doExec(TcpMessage message, IoSession session) {
		CjyTcpMessage tcpMsg = (CjyTcpMessage)message;
		//TODO解析数据并入库
		byte[] data1 = tcpMsg.getData();
		ByteBuffer buf = ByteBuffer.wrap(data1);
		////通道0交流有效值，单位1mV交流干扰电压
		int ch0_effictive_value = CommUtils.highAndLowAddressSwap(buf.getInt());
		 //通道1 直流中位值，单位10uV自 然电位
		int ch1_middle_value = CommUtils.highAndLowAddressSwap(buf.getInt());
		////通道2 继电器闭合时直流中位值，10uV通电电位
		int ch2_middle_connect_value = CommUtils.highAndLowAddressSwap(buf.getInt());
		//通道2 继电器断开时直流中位值，10uV断电电位
		int ch2_middle_disconnect_value = CommUtils.highAndLowAddressSwap(buf.getInt());
		
		CjyTcpMessage dataReq = new CjyTcpMessage();
		dataReq.setAddress(tcpMsg.getAddress());
		dataReq.setCmd(TcpNetCmd.ACK & TcpNetCmd.SET_TIME_ID);
		String curDate = CommUtils.formatDate(new Date(Calendar.getInstance().getTimeInMillis()), "yy-M-dd-H-m-s");
		String[] ele = curDate.split("-");
		dataReq.setLength(ele.length);
		byte[] data = new byte[ele.length];
		for(int i = 0; i < ele.length; i ++){
			data[i] = CommUtils.parseByte(ele[i]);
		}
		dataReq.setData(data);
		session.write(dataReq);
	}
	
	
}
