package com.point.iot.base.tools;

import java.security.NoSuchAlgorithmException;

/**
 * @author Josn
 * @Date 2012-1-12 下午03:56:54
 * @version V1.0
 */
public class MD5 {
	/**
	 * MD5加密
	 * @param str
	 * @return
	 */
	public static  String getMD5(String str) {
		try {
			Object obj = new Object();
			synchronized (obj) {
				java.security.MessageDigest messageDigest = java.security.MessageDigest.getInstance("MD5");
				messageDigest.reset();
				messageDigest.update(str.getBytes());
				byte[] byteArray = messageDigest.digest();
				char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
				int j = byteArray.length;
				char temp[] = new char[j * 2];
				int k = 0;
				for (int i = 0; i < j; i++) {
					byte b = byteArray[i];
					// System.out.println((int)b);
					// 将没个数(int)b进行双字节加密
					temp[k++] = hexDigits[b >> 4 & 0xf];
					temp[k++] = hexDigits[b & 0xf];
				}
				return new String(temp);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(getMD5("123456"));
	}
}
