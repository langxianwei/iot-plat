package com.yahe.plat.protocol.plugin.tcp.model;

import com.point.iot.base.message.TcpMessage;
import com.yahe.plat.protocol.plugin.utils.Constant;

public class CjyTcpMessage extends TcpMessage {
	// 同步头，用于区别TCP消息协议
		private String synchHeader;
		//地址域由4个字节构成，为十六进制数。作为为设备号使用
		private int address;
		//控制码C D7
		private int d7;
		//控制码C D6
		private int d6;
		
		// 消息协议数据长度
		private int length;
		// 去除同步头后所有字节的CRC值
		private byte uiCrc;
		
		public CjyTcpMessage(){
			setProtocolType(Constant.SHUANGRUI_CAIJIYI_TCP);
		}
		public String getSynchHeader() {
			return synchHeader;
		}
		public void setSynchHeader(String synchHeader) {
			this.synchHeader = synchHeader;
		}
		public int getAddress() {
			return address;
		}
		public void setAddress(int address) {
			this.address = address;
		}
		public int getD7() {
			return d7;
		}
		public void setD7(int d7) {
			this.d7 = d7;
		}
		public int getD6() {
			return d6;
		}
		public void setD6(int d6) {
			this.d6 = d6;
		}
		public int getLength() {
			return length;
		}
		public void setLength(int length) {
			this.length = length;
		}
		public byte getUiCrc() {
			return uiCrc;
		}
		public void setUiCrc(byte uiCrc) {
			this.uiCrc = uiCrc;
		}
		
}
