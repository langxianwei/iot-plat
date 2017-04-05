package com.yahe.plat.protocol.plugin.codec;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import org.springframework.stereotype.Component;

import com.point.iot.base.tools.CommUtils;
import com.tongqu.base.message.shrtcp.MessageUtil;
import com.yahe.plat.protocol.plugin.tcp.model.CjyTcpMessage;
@Component
public class MessageTcpDecoder implements MessageDecoder{
	Logger logger = Logger.getLogger(MessageTcpDecoder.class) ;
	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer buf) {
		int remain = buf.remaining();
		if(remain <= 1){
			return  MessageDecoderResult.NOT_OK;
		}
		buf.mark();
		byte[] data = new byte[remain - 2];
		buf.get(data);
		byte cs = buf.get();
		int t = 0;
		for( int i =0; i< data.length; i ++){
			t += data[i];
		}
		int cs_cal = (t%256) ;
		
		//如果校验码校验不通过,则为无效消息
		if ( (cs & 0xFF) != (cs_cal & 0xFF)){
			buf.reset();
			return MessageDecoderResult.NOT_OK;
		}
		buf.reset();
		return MessageDecoderResult.OK;
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer buf, ProtocolDecoderOutput out) throws Exception {
		CjyTcpMessage message = new CjyTcpMessage();
		logger.info("收到采集仪消息" + buf.getHexDump());
		buf.mark();
		//帧起始符 0x68
		buf.get();
		// 获取设备编码
		int address_i = buf.getInt();
		message.setAddress(CommUtils.highAndLowAddressSwap(address_i));
		//帧起始符 0x16
		if ( buf.get() != 0x68){
			return null;
		}
		//控制码
		byte control = buf.get();
		message.setD7(control >> 7);
		message.setD6(control >> 6);
		
		//控制码 , 协议簇
		message.setCmd(control&0x0f);
		//数据体长度
		int length = buf.get();
		message.setLength(length);
		//数据体
		byte[] data = MessageUtil.getBytes(buf, length);
		message.setData(data);
		//CRC
		byte crc = (buf.get());
		//结束标志
		int finished = buf.get();
		if ( finished != 0x16 ){
			return MessageDecoderResult.NOT_OK;
		}
		buf.reset();
		//获取校验位前字节
		byte[] cpData = MessageUtil.getBytes(buf, 8 + length);
		int t = 0;
		for( int i =0; i< cpData.length; i ++){
			t += cpData[i];
		}
		int crc_cal = (t%256) ;
		
		logger.info("获取的CRC值" + (crc & 0xFF) + ",计算的crc值:" + crc_cal + ",address:" + message.getAddress());
		//如果校验码校验不通过,则为无效消息
		if ( (crc & 0xFF) != (crc_cal & 0xFF)){
			return MessageDecoderResult.NOT_OK;
		}
		buf.get();
		buf.get();
		out.write(message);
		return MessageDecoderResult.OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
