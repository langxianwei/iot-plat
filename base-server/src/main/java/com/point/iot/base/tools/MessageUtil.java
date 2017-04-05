package com.point.iot.base.tools;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;

public class MessageUtil {

	public static String getString(IoBuffer buff, int len, Charset charset){
		byte[] ab = new byte[len];
		buff.get(ab);
		String s;
		s = new String(ab, charset);
		return s;
	}
	public static byte[] getBytes(IoBuffer buff, int len){
		if (len == 0) {
			return new byte[0];
		}
		byte[] ab = new byte[len];
		buff.get(ab);
		return ab;
	}
	/**
	 * 用16进制打印消息体
	 * @param ab
	 * @return
	 */
	public static String getHexStringDump(byte[] ab) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ab.length; i++) {
            if (i == 0) {
            } else if (i % 16 == 0) {
                sb.append("\n");
            } else if (i % 4 == 0) {
                sb.append(" ");
            }
            String s = Integer.toHexString(ab[i]);
            if (s.length() == 1) {
            	s = "0" + s;
            } else if (s.length() > 2) {
            	s = s.substring(s.length() - 2);
            }
            sb.append(s + " ");
        }
		return sb.toString();
	}
}
