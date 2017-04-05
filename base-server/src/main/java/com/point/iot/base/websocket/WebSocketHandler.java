package com.point.iot.base.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

public class WebSocketHandler {
	/**
	 * 消息为websocket标志
	 */
	public static final String WEBSOCKET_MESSAGE = "WEBSOCKET_MESSAGE";
	public static final String FIRST_MESSAGE = "FIRST_MESSAGE";
	/**
	 * Websocket握手头消息中前几个字节
	 */
	public static final byte[] WEBSOCKET_HANDLERSHAKE_HEAD = {0x47, 0x45, 0x54, 0x20};
	public static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	public static final String HEADER_CODE = "iso-8859-1";
	public static final String PROTOCOL = "chat";
	public static final int SPLITVERSION = 4;
	private int webSocketVersion = 0;
	
	/**
	 * 握手
	 * @param input
	 * @param session
	 */
	public boolean handlerShake(InputStream input,IoSession session){
		try {
			IoBuffer ioBuffer = IoBuffer.allocate(10240); 
			String reqestLine = new String(Tools.readLine(input));
			Map<String, String> requestHeaders = new HashMap<String, String>();
			while (true) {
				byte[] bts = Tools.readLine(input);
				if (bts[0] == '\r' && bts[1] == '\n') {
					break;
				}
				String line = new String(bts);
				int mh = line.indexOf(':');
				requestHeaders.put(line.substring(0, mh), line.substring(mh + 1).trim());
			}
			//HTTP request headers!
			
			// Upgrade:WebSocket is null , so it's a general http request!
			if(requestHeaders.get("Upgrade") == null){
				String url = "http://127.0.0.1" + reqestLine.split(" ")[1];
				//socket.setSoTimeout(5000);
				url = new URL(url).getPath();
				Tools.writeFile(SocketServer.class.getResource("/").getPath() + url, session);
				return false;
			}
			// HTTP response header!
			Map<String,String> resMap = new HashMap<String, String>();
			String responsLine = "HTTP/1.1 101 Web Socket Protocol Handshake\r\n";
			resMap.put("Upgrade", "WebSocket");
			resMap.put("Connection", "Upgrade");
			if(requestHeaders.get("Origin") != null)
				resMap.put("Sec-WebSocket-Origin", requestHeaders.get("Origin"));
			resMap.put("Sec-WebSocket-Location", "ws://" + requestHeaders.get("Host").trim() + "/");

			byte[] content = null;
			//Sec-WebSocket-Version: 4  及 以上
			String version = requestHeaders.get("Sec-WebSocket-Version");
			if(version != null){
				webSocketVersion = new Integer(version);
			}
			if(webSocketVersion >= 4){
				String code = requestHeaders.get("Sec-WebSocket-Key") + GUID;
				byte[] bts = MessageDigest.getInstance("SHA1").digest(code.getBytes(HEADER_CODE));
				code = Tools.getBASE64(bts);
				resMap.put("Sec-WebSocket-Accept", code);
				//Sec-WebSocket-Protocol: chat
				resMap.put("Sec-WebSocket-Protocol", PROTOCOL);
				if(requestHeaders.get("Sec-WebSocket-Version") != null)
					resMap.put("Sec-WebSocket-Version", requestHeaders.get("Sec-WebSocket-Version"));
				if(requestHeaders.get("Sec-WebSocket-Origin") != null)
					resMap.put("Sec-WebSocket-Origin", requestHeaders.get("Sec-WebSocket-Origin"));
			}else{
				// the end 8 Byte!
				int len = 8; // in.available();
				byte[] key3 = new byte[len];
				if (input.read(key3) != len)
					throw new RuntimeException();
				String key1 = requestHeaders.get("Sec-WebSocket-Key1");
				String key2 = requestHeaders.get("Sec-WebSocket-Key2");
				int k1 = Tools.parseWebsokcetKey(key1);
				int k2 = Tools.parseWebsokcetKey(key2);

				byte[] sixteenByte = new byte[16];
				System.arraycopy(Tools.intTo4Byte(k1), 0, sixteenByte, 0, 4);
				System.arraycopy(Tools.intTo4Byte(k2), 0, sixteenByte, 4, 4);
				System.arraycopy(key3, 0, sixteenByte, 8, 8);
				byte[] md5 = MessageDigest.getInstance("MD5").digest(sixteenByte);
				content = md5;
			}

			//return user message
			ioBuffer.put(responsLine.getBytes(HEADER_CODE));
			for(String key : resMap.keySet()){
				ioBuffer.put((key + ": " + resMap.get(key) + "\r\n").getBytes(HEADER_CODE));
			}
			ioBuffer.put("\r\n".getBytes(HEADER_CODE));

			if(content != null){
				ioBuffer.put(content);
			}
			ioBuffer.flip(); 
			session.write(ioBuffer); 
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 读取内容
	 * @param input
	 */
	public byte[] readContent(InputStream input){
		try {
			byte[] bts;
			if(webSocketVersion < SPLITVERSION){
				bts = Tools.readFrame(input);
				if(bts == null) return null;
				String message = new String(bts, Tools.CHARSET_UTF8);
				System.out.println("00消息:"+message+" 长度："+bts.length);
				System.out.println("00消息:" + Arrays.toString(bts));
			}else{
				WebSocketV6Fram ws = WebSocketV6Fram.parseWebSocketV6Fram(input);
				bts = WebSocketV6Fram.readWebSocketV6(input, ws);
				String message = new String(bts, Tools.CHARSET_UTF8);
				System.out.println("消息:"+message);
			}
			return Tools.getFromBASE64(new String(bts));
//			return bts;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public synchronized void sendMessage(byte[] msg,IoSession ioSession) throws IOException {
//		System.out.println("WebSocketHandler  160行 回复内容："+Arrays.toString(msg));
		String message =  Tools.getBASE64(msg);
		if(webSocketVersion < 4)
			Tools.writeFrame(ioSession, message);
		else
			WebSocketV6Fram.writeWebSocketV6Fram(ioSession, message);
	}
}
