package com.point.iot.service.proxy.utils;

import org.apache.mina.core.session.IoSession;

import com.point.iot.utils.ip.IPInfoUtil;

public class SessionUtils {
	
	
	public static String getIpAddr(IoSession session) {
		if (session == null || session.getRemoteAddress() == null) {
			return "";
		}
		try {
			String ip = session.getRemoteAddress().toString();
			if (ip.indexOf(':') > 0) {
				ip = ip.substring(0, ip.indexOf(':'));
			} else {
				return "";
			}
			ip = ip.replace("/", "");
			return ip;
		} catch (Exception ex) {
			return "";
		}
	}
	
	public static int getIntIPAddr(IoSession session) {
		String ip = getIpAddr(session);
		return "".equals(ip.trim()) ? 0 : IPInfoUtil.StringIp2Int(ip);
	}

}
