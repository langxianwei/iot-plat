package com.yahe.plat.protocol.plugin.codec;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.springframework.stereotype.Component;

import com.point.iot.base.tools.CommUtils;
import com.yahe.plat.protocol.plugin.tcp.model.CjyTcpMessage;
/**
 * 采集仪消息编码器
 * @author lenovo
 *
 */
@Component
public class MessageTcpEncoder implements MessageEncoder<CjyTcpMessage> {
	Logger logger = Logger.getLogger(MessageTcpEncoder.class);
	@Override
	public void encode(IoSession session, CjyTcpMessage message, ProtocolEncoderOutput out) throws Exception {
		IoBuffer buf = IoBuffer.allocate(100).setAutoExpand(true);  
		//帧起始符
		buf.put((byte)0x68);
		//地址域 低位在前，高位在后
		buf.putInt(CommUtils.highAndLowAddressSwap(message.getAddress()));
		//帧起始符
		buf.put((byte)0x68);
		//控制码
		buf.put((byte)message.getCmd());
		//数据体长度
		int len = 0;
		if ( message.getData() != null ){
			len = message.getData().length;
		}else{
			len = 0;
		}
		buf.put((byte)len);
		//数据体
		if ( len != 0 ){
			buf.put(message.getData());
		}
		//crc
		int t = 208 + message.getCmd() + len;
		byte[] address_b = CommUtils.intToBytes(message.getAddress());
		if ( address_b.length > 0 ){
			for(int i = 0; i < address_b.length; i ++){
				t += address_b[i];
			}
		}
		if ( len > 0 ){
			for(int i = 0; i < message.getData().length; i ++){
				t += message.getData()[i];
			}
		}
		byte crc = (byte)(t%256);
		buf.put(crc);
		//结束符
		buf.put((byte)0x16);
		
		buf.flip();
		logger.info("发送采集仪下行消息：" + message.getAddress() + ", 命令：" + message.getCmd());
		out.write(buf);
	}

}
