package com.point.iot.utils.ip;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import net.sf.json.JSONObject;

/**
 * @author Wang Zhaohui 2013-7-9 上午11:20:57 IP信息获取地址Util
 */

public class IPInfoUtil {

	private static final String REQUEST_URL_STRING = "http://ip.taobao.com/service/getIpInfo.php";
	private static final String ENCODE = "utf-8";
	/**
	 * 将数字转换成IP
	 * 
	 * @param ip
	 * @return IP地址
	 */
	private static String intIp2String(int ip) {
		if(ip == 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append(ip >>> 24).append(".");
		sb.append((ip & 0x00FFFFFF) >>> 16).append(".");
		sb.append((ip & 0x0000FFFF) >>> 8).append(".");
		sb.append(ip & 0x000000FF);
		return sb.toString();
	}

	/**
	 * IP转为数字
	 * 
	 * @param ip
	 * @return
	 */
	public static int StringIp2Int(String ip) {
		int result = 0x00000000;
		String[] ips = ip.split("\\.");
		for (int i = 0; i < ips.length; i++) {
			int temp = Integer.parseInt(ips[i]);
			result |= temp * (1 << (3 - i) * 8);
		}
		return result;
	}

	/**
	 * 得到未转码的地址信息JSON字符串
	 * 
	 * @param ip
	 * @return
	 */
	private static String getResult(int ip) {
		String result = "";
		String sIP = intIp2String(ip);
		String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
		if (!sIP.matches(regex)) {
			return "";
		}
		// 判断IP
		// 不符合返回""

		String args = "ip=" + sIP;

		URL url = null;
		HttpURLConnection connection = null;

		try {
			url = new URL(REQUEST_URL_STRING);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			// out.writeBytes("format=json");
			out.writeBytes(args);
			out.flush();
			out.close();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), ENCODE));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			reader.close();
			result = buffer.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return result;
	}

	/**
	 * 转码JSON字符串
	 * 
	 * @param jsonString
	 * @return
	 */
	private static String decodeUnicode(String jsonString) {
		if("".equals(jsonString)) {
			return "";
		}
		char aChar;
		int len = jsonString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = jsonString.charAt(x++);
			if (aChar == '\\') {
				aChar = jsonString.charAt(x++);
				if (aChar == 'u') {
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = jsonString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed      encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't') {
						aChar = '\t';
					} else if (aChar == 'r') {
						aChar = '\r';
					} else if (aChar == 'n') {
						aChar = '\n';
					} else if (aChar == 'f') {
						aChar = '\f';
					}
					outBuffer.append(aChar);
				}
			} else {
				outBuffer.append(aChar);
			}
		}
		return outBuffer.toString();
	}

	/**
	 * 获取AddressInfo对象
	 * 
	 * @param address
	 * @return
	 */
	private static AddressInfo paserString2Address(String address) {
		if ("".equals(address.trim()) || null == address) {
			return null;
		}
		AddressInfo info = new AddressInfo();
		JSONObject obj = JSONObject.fromObject(address);
		if (1 == obj.getInt("code")) {
			return null;
		}
		JSONObject infoObj = JSONObject.fromObject(obj.get("data"));
		info.setCountry(infoObj.getString("country"));
		info.setArea(infoObj.getString("area"));
		info.setRegion(infoObj.getString("region"));
		info.setCity(infoObj.getString("city"));
		info.setIsp(infoObj.getString("isp"));
		return info;
	}

	/**
	 * 公开接口，返回AddressInfo对象
	 * 
	 * @param ip
	 * @return
	 */
	public static AddressInfo getAddressInfo(int ip) {
		String notTranscode = getResult(ip);
		String transcode = decodeUnicode(notTranscode);
		return paserString2Address(transcode);
	}

	/**
	 * 返回地址信息字符串
	 * 
	 * @param ip
	 * @return
	 */
	public static String getLocation(int ip) {
		AddressInfo info = getAddressInfo(ip);
		return info == null ? "未知IP" : info.toString();

	}

	public static void main(String[] args) {

		String result = getLocation(StringIp2Int("106.37.24.19"));
		if ( result == null ){
			return;
		}
		String[] addr = result.split(",");
		if ( addr.length > 3 ){
			if ( addr[2].equals("山东省")){
				System.out.println(result);
				
			}
		}
		
	}

}
