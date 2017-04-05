package com.point.iot.mina.client;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.point.iot.base.email.EMail;
import com.point.iot.base.message.TcpMessage;
import com.point.iot.base.tools.CommUtils;
import com.point.iot.mina.msg.MsgHandler;

/**
 * 客户端的Mina Handler
 * @author langxianwei
 *
 */
public class ClientHandler extends IoHandlerAdapter {

	Logger logger = Logger.getLogger(ClientHandler.class);
	private MsgHandler mMsgHandler;
	private SocketClient mSockClient;
	private static int mSessionCount ;
	private boolean mIsReconnect ;
	/**
	 * 
	 * @param msgHandler
	 * @param sockClient
	 */
	public ClientHandler(MsgHandler msgHandler, SocketClient sockClient, boolean bIsReconnect) {
		mMsgHandler = msgHandler;
		mSockClient = sockClient;
		mIsReconnect = bIsReconnect ;
	}

	public void exceptionCaught(IoSession session, Throwable t)
			throws Exception {
		t.printStackTrace();
		session.close(true);
	}

	private long mnLastErrEmailTime;
	private StringBuffer msbErrMsg = new StringBuffer();
	private TcpMessage mLastPack;
	private boolean wrongMsgID = false;
	
	public void messageReceived(IoSession session, Object msg) throws Exception {
		if (mMsgHandler == null) {
			return;
		}
		if (!(msg instanceof TcpMessage)) {
			logger.debug("msg is not JMessageProtocalReq" + msg);
			return; 
		}
		TcpMessage messageProtocal = (TcpMessage) msg;
		/*if (messageProtocal.getCmd() == 0x80000000) {
			//如果是心跳应答
			return;
		}*/
		try {
			mLastPack = messageProtocal;
			mMsgHandler.onMsg(messageProtocal, session);
		} catch (Exception e) {
			e.printStackTrace();
			logger.fatal(mMsgHandler + "的onMsg()函数出现异常 " + e.getMessage(), e);
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			msbErrMsg.append("\n[" + CommUtils.parseDate(System.currentTimeMillis()) + "]" + mMsgHandler + "异常[UID:" + messageProtocal.getCmd() + "][MsgID:" + Integer.toHexString(messageProtocal.getCmd()) + "]" + e);
			msbErrMsg.append("\n=========================\n");
			msbErrMsg.append(writer.getBuffer().toString());

			if (wrongMsgID && mLastPack != null) {
				wrongMsgID = false;
				msbErrMsg.append("\n" + messageProtocal.toString());
			}
			if (System.currentTimeMillis() - mnLastErrEmailTime > 60000) {
				mnLastErrEmailTime = System.currentTimeMillis();
				EMail email = new EMail(new String[]{"server_watcher@puente.cn"}, mMsgHandler + "客户端异常", msbErrMsg.toString());
				email.sendAsTxt();
				msbErrMsg = new StringBuffer();
			}
		}
	}

	private long mLastSendNetBreakLogTime;
	private StringBuffer mNetBreakLog = new StringBuffer();
	
    public void sessionClosed(IoSession session) throws Exception {
    	try {
			--mSessionCount ;
	    	logger.debug("session closed ,and current connected user count =[" + 	mSessionCount + "]");
			mSockClient.reset();
			
			//如果链接断开，则立刻启动重连线程，如果是机器人链接，不重连
			if(mIsReconnect) { 
				(new Thread(mSockClient)).start();
			}else{
				if(session!=null){
					session.close(true);
					session.getService().dispose();
				}
				if(mSockClient!=null)mSockClient.close(true);
			}
		} catch (Exception e) {
			logger.error(mMsgHandler + "的sessionClosed()函数出现异常 ：" + e.getMessage(),e);
		}
    	
    	//服务器断网了，发邮件通知
    	mNetBreakLog.append(CommUtils.parseDate(System.currentTimeMillis()) +  " 服务器的Socket被断开，开始自动重连。" + session);
    	if (System.currentTimeMillis() - mLastSendNetBreakLogTime > 3 * 60000) {
    		mLastSendNetBreakLogTime = System.currentTimeMillis();
    		EMail mail = new EMail(new String[]{"server_watcher@puente.cn"}, "服务器间的Socket断开通知", mNetBreakLog.toString());
    		mail.send();
    		mNetBreakLog = new StringBuffer();
    	}
    
    }

	public void sessionCreated(IoSession session) throws Exception {
		session.getConfig().setBothIdleTime(30);
		++ mSessionCount ;
		logger.info("Session created,and current connected user count =[" + 	mSessionCount + "]" + session.getRemoteAddress() );
	}
	
    public void sessionIdle(IoSession session, IdleStatus status) {
    	//向服务端发送心跳消息
    	/*IoBuffer buffer1 = IoBuffer.allocate(28).setAutoExpand(true);
    	buffer1.put("<aa><aa><aa><ab>".getBytes());
    	buffer1.put(new byte[]{0, 10, 0, 0, 0, 0, 0, 0, 0, 0});
    	buffer1.flip();
		session.write(buffer1);*/
//		logger.debug("发送IDLE消息：" + session);
		if (System.currentTimeMillis() - session.getLastReadTime() > 60000) {
			String sErrMsg = (System.currentTimeMillis() - session.getLastReadTime()) + "毫秒没有收到消息，主动断开连接" + session; 
			logger.fatal(sErrMsg);
			mSockClient.close(true);
//			EMail email = new EMail(new String[]{"server_watcher@puente.cn"}, sErrMsg, sErrMsg);
//			email.send();
		}
    }
    public void messageSent(IoSession session, Object message) throws Exception {

    }
}
