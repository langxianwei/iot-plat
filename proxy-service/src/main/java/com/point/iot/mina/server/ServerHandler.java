package com.point.iot.mina.server;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.point.iot.base.email.EMail;
import com.point.iot.base.message.TcpMessage;
import com.point.iot.base.tools.CommUtils;
import com.point.iot.mina.msg.MessageHandler;

/**
 * 服务端的Mina Handler
 * 
 * @author langxianwei
 * 
 */
public class ServerHandler extends IoHandlerAdapter {
	Logger logger = Logger.getLogger(ServerHandler.class);
	private MessageHandler messageHandler;

	public ServerHandler(MessageHandler msgHandler) {
		messageHandler = msgHandler;
	}

	public void exceptionCaught(IoSession session, Throwable t)
			throws Exception {
		t.printStackTrace();
	}

	private long mnLastErrEmailTime;
	private StringBuffer msbErrMsg = new StringBuffer();
	
	public void messageReceived(IoSession session, Object msg) throws Exception {
		if (!(msg instanceof TcpMessage)) {
			System.out.println("msg is not AbstractMessageProtocal" + msg);
			return;
		}
		TcpMessage messageReq = (TcpMessage) msg;
		if (messageReq.getCmd() == 0) {
			messageHandler.onMsg(messageReq, session);
			return;
		}
		int userID = 0;
		try {
			
			messageHandler.onMsg(messageReq, session);
		} catch (Exception e) {
			logger.fatal(messageHandler + "的onMsg()函数出现异常 " + e + "[UserID:" + userID + "]", e);
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			
			msbErrMsg.append("\n[" + CommUtils.parseDate(System.currentTimeMillis()) + "]" + messageHandler + "异常 " + e + "[UserID:" + userID + "][MsgID:" + messageReq.getCmd() + "]");
			msbErrMsg.append("\n=========================\n");
			msbErrMsg.append(writer.getBuffer().toString());
			if (System.currentTimeMillis() - mnLastErrEmailTime > 60000) {
				mnLastErrEmailTime = System.currentTimeMillis();
				EMail email = new EMail(new String[]{"server_watcher@puente.cn"}, messageHandler + "服务器异常", msbErrMsg.toString());
				email.sendAsTxt();
				msbErrMsg = new StringBuffer();
			}
		}
	}

	public void sessionCreated(IoSession session) throws Exception {
		logger.debug("sessionCreated " + session);
		session.getConfig().setIdleTime(IdleStatus.READER_IDLE, 60);
	}

	public void sessionOpened(IoSession session) throws Exception {
		logger.info("sessionOpened " + session);
	}

    public void messageSent(IoSession session, Object message) throws Exception {
        
    }
	
	public void sessionClosed(IoSession session) throws Exception {
		messageHandler.onSessionClosed(session);
	}

	public void sessionIdle(IoSession session, IdleStatus status)throws Exception {
		logger.info("客户端sessionIdle, " + (System.currentTimeMillis() - session.getLastReadTime()) + "毫秒没有上行。" + session.getRemoteAddress());
   		if((System.currentTimeMillis()-session.getLastReadTime())>1000*60*10){
   			Object userID = (Object)0;
   			if(session.containsAttribute("UserID")){    			
   				 userID = session.getAttribute("UserID");
   			}
			logger.info(" Player["+userID.toString()+"] -------------------------------------------Out of time, close user session  lastReadTime:"+session.getLastReadTime()+"["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:ms").format(new Date(session.getLastReadTime()))+"]   now:"+System.currentTimeMillis()+"["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:ms").format(new Date(System.currentTimeMillis()))+"]  session:"+session.getRemoteAddress());
			logger.info(session.getId()+"  -----------------------------------------Out of time, close user session");
			logger.info("System.currentTimeMillis():"+format(System.currentTimeMillis()));
			logger.info("session.getLastIoTime():"+format(session.getLastIoTime()));
			logger.info("session.getLastBothIdleTime():"+format(session.getLastBothIdleTime()));
			logger.info("session.getLastReaderIdleTime():"+format(session.getLastReaderIdleTime()));
			logger.info("session.getLastReadTime():"+format(session.getLastReadTime()));
			logger.info("session.getLastWriterIdleTime():"+format(session.getLastWriterIdleTime()));
			logger.info("session.getLastWriteTime():"+format(session.getLastWriteTime()));
			session.closeOnFlush();
    	}
	}
	public   String format(long time) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:ms").format(new Date(time));
	}
}
