package com.point.iot.base.websocket;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends IoHandlerAdapter{
	 private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
	 public void sessionCreated(IoSession session) throws Exception {
		 session.getConfig().setIdleTime(IdleStatus.READER_IDLE, 30);
		 System.out.println("sessionCreated……");
    }

    public void sessionOpened(IoSession session) throws Exception {
    	System.out.println("sessionOpened……");
    }

    public void sessionClosed(IoSession session) throws Exception {
    	System.out.println("sessionClosed……");
    }

    public void sessionIdle(IoSession session, IdleStatus status)  throws Exception {
    	System.out.println("sessionIdle……");
    	if(session.isClosing()){
    		System.out.println("sessionIdle      Closed");
    	}else{
    		if((System.currentTimeMillis()-session.getLastReadTime())>1000*60*2){
    			System.err.println(session.getId()+"  ----------------Out of time, close user session");
    			session.close(true);
    		}
    	}
    }

    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
    	System.out.println("exceptionCaught……"+cause.getMessage());
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("EXCEPTION, please implement "
                    + getClass().getName()
                    + ".exceptionCaught() for proper handling:", cause);
        }
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
    	if(session.containsAttribute("WebSocketHandlerShaked")){//WebSocket消息发送
    		IoBuffer ioBuffer = (IoBuffer)message;
    		WebSocketHandler webSocketHandler = new WebSocketHandler();
    		byte[] msg=webSocketHandler.readContent(ioBuffer.asInputStream());
    		byte[] msgbase64 = Tools.getFromBASE64(new String(msg));
    		System.out.println(new String(msgbase64));
    		
//    		
//    		IoBuffer iobuffer = IoBuffer.allocate(msg.length);
//    		
//    		MsgPack msgPack = new MsgPack();
    		String content = Tools.getBASE64(msgbase64);
    		webSocketHandler.sendMessage(content.getBytes(), session);
    	}else{
//	    	if ((message instanceof MsgPack)) {//普通消息
//	    		System.out.println("MsgPack");
//				MsgPack pack = (MsgPack) message;
//				pack.resetPos();
//				if ((pack.getMsgID() & 0x000000FF) == 0 && (pack.getMsgID()) != 0) {
//					// 如果是握手消息
//					return;
//				}
//	    	}else{//WebSocket握手消息
	    		IoBuffer ioBuffer = (IoBuffer)message;
	    		WebSocketHandler webSocketHandler = new WebSocketHandler();
	    		webSocketHandler.handlerShake(ioBuffer.asInputStream(),session);
	    		session.setAttribute("WebSocketHandlerShaked");
//	    	}
    	}
    }

    public void messageSent(IoSession session, Object message) throws Exception {
    	IoBuffer ioBuffer = (IoBuffer)message;
    	System.err.println("发送成功："+ioBuffer.toString());
    }
}
