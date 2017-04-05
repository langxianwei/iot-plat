package com.point.iot.mina.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.point.iot.base.message.TcpMessage;
import com.point.iot.mina.codc.factory.MessageCodecFactory;
import com.point.iot.mina.msg.MsgHandler;

/**
 * 客户端
 * @author langxianwei
 *
 */
public class SocketClient implements Runnable {
	Logger logger = Logger.getLogger(SocketClient.class);
	private NioSocketConnector mConnector;
	private ConnectFuture mFuture;
	//服务器地址
	private String mAddr;
	//服务器端口
	private int mPort;
	//
	private ClientHandler mClientHandler;
	//发送消息的队列
	private final Queue<TcpMessage> mSendingQueue = new ConcurrentLinkedQueue<TcpMessage>();
	//是否握手成功
	private boolean mHandShakeCompleted;
	//握手消息ID
	private int mHandShakeMsgID;

	public SocketClient(String addr, int port) {
		this(addr, port, null, 0);
		logger.info("MinaSocketClient初始化 " + addr + ":" + port);
	}
	
	/**
	 * 构造函数
	 * @param addr 地址
	 * @param port 端口
	 * @param msgHandler 消息处理类
	 * @param handShakeMsgID 握手消息ID
	 */
	public SocketClient(String addr, int port, MsgHandler msgHandler, int handShakeMsgID) {
		mAddr = addr;
		mPort = port;
		mClientHandler = new ClientHandler(msgHandler, this, true);
		mHandShakeMsgID = handShakeMsgID;
		if (handShakeMsgID == 0) {
			//客户端不发握手消息
			mHandShakeCompleted = true;
		}
		//
		connect(true);
	}
	public String getmAddr() {
		return mAddr;
	}
	public void setmAddr(String mAddr) {
		this.mAddr = mAddr;
	}
	/**
	 * 构造函数
	 * @param addr 地址
	 * @param port 端口
	 * @param msgHandler 消息处理类
	 * @param handShakeMsgID 握手消息ID
	 * @param bIsReconnect  是否需要重连
	 */
	public SocketClient(String addr, int port, MsgHandler msgHandler, int handShakeMsgID, boolean bIsReconnect) {
		mAddr = addr;
		mPort = port;
		mClientHandler = new ClientHandler(msgHandler, this, bIsReconnect);
		mHandShakeMsgID = handShakeMsgID;
		if (handShakeMsgID == 0) {
			//客户端不发握手消息
			mHandShakeCompleted = true;
		}
		//
		connect(bIsReconnect);
	}
	
	/**
	 * 重置ConnectFuture，为重连做准备
	 */
	public void reset() {
		if (mFuture != null) {
			mFuture.cancel();
			mFuture = null;
		}
		if (mHandShakeMsgID != 0) {
			mHandShakeCompleted = false;
		}
	}

	/**
	 * 校验是否合法的握手应答
	 * @param msgID
	 * @return
	 */
	public boolean verifyHandShakeResp(int msgID) {
		if ((mHandShakeMsgID | 0x80000000) == msgID) {
			return true;
		}
		return false;
	}
	/**
	 * 握手成功
	 */
	public void setHandShakeCompleted() {
		mHandShakeCompleted = true;
		sendAllMsgInQueue();
	}
	
	public boolean isHandShakeCompleted() {
		return mHandShakeCompleted;
	}
	
	/**
	 * 开始连接
	 * @throws InterruptedException 
	 */
	private synchronized void connect (boolean bIsReconnect) {
		connect(bIsReconnect, false);
	}
	
	
	private synchronized void connect (boolean bIsReconnect, boolean bUseReadOperation) {
		try {
			if (mFuture != null && mFuture.isConnected()) {
				return;
			}
			if (mPort == 0) {
				return;
			}
			mConnector = new NioSocketConnector();
			//设置连接超时时间
			mConnector.setConnectTimeoutMillis(2000);
			mConnector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MessageCodecFactory(Charset.forName("UTF-8"))));
			mConnector.setHandler(mClientHandler);
	//		logger.info("尝试连接 Server " + mAddr + ":" + mPort+"--------------------------------------------");
			mFuture = mConnector.connect(new InetSocketAddress(mAddr, mPort));
			//设置IDLE时间
			mConnector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, 40);
			mConnector.getSessionConfig().setTcpNoDelay(false);
			mConnector.getSessionConfig().setUseReadOperation(bUseReadOperation);
			mFuture.awaitUninterruptibly();
			boolean connected=mFuture.isConnected();
			if (!connected && bIsReconnect) {
	//			logger.info("connect fail mFuture=[ " + mFuture + "],mAddr =[" + mAddr + ":" + mPort);
				reset();
				mConnector.dispose(true);
				//如果链接失败，则重新启动链接重试线程
				(new Thread(this)).start();
			} else {
				//如果连接成功，则发送所有队列中的消息
				logger.info("connect success " + mAddr + ":" + mPort);
				sendAllMsgInQueue();
			}
		} catch (Exception e) {
			logger.fatal("SocketClient ["+mAddr+"  :  "+ mPort+"] connect()失败    Exception: " + e + " " + Arrays.toString(e.getStackTrace()));
			//如果链接失败，则重新启动链接重试线程
			(new Thread(this)).start();
		}
	}
	
	/**
	 * 若链接失败，每N秒重试一次链接
	 */
	public void run() {
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
		}
		connect(true);
	}
		
	/**
	 * 判断是否已经连接上
	 * @return
	 */
	public boolean isConnected(){
		if(mFuture !=null && mFuture.isConnected()){
			return true;
		}
		return false;
	}
	
	/**
	 * 发送所有队列中的消息
	 */
	private void sendAllMsgInQueue() {
		if (mFuture == null || !mFuture.isConnected()) {
			return;
		}
		while (!mSendingQueue.isEmpty()) {
			//如果握手完成
			TcpMessage message = mSendingQueue.poll();
			mFuture.getSession().write(message);
		}
	}

	public long getScheduledWriteMessages() {
		IoSession session = mFuture.getSession();
		return session.getScheduledWriteMessages();
	}
	public long getScheduledWriteBytes() {
		IoSession session = mFuture.getSession();
		return session.getScheduledWriteBytes();
	}
	
	
	
	/**
	 * 发送消息包
	 * @param pack
	 */
	public void send(TcpMessage message) {
		try {
			if (mFuture != null && mFuture.isConnected()) {
				//如果连接正常，则直接发送
				mFuture.getSession().write(message);
				return;
			}
			//若连接不正常，则把消息加入队列中
			mSendingQueue.add(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isActive(){
		return mConnector.isActive();
	}
	
	public void close(boolean immediately) {
		if(mFuture!=null){
			mFuture.getSession().close(immediately);
			mFuture.getSession().getService().dispose();
		}
		if(mConnector!=null)mConnector.dispose();
	}
	
	public String toString() {
		return "SocketClient，地址" + mAddr + ":" + mPort + ", mSendingQueue.size=" + mSendingQueue.size() + mFuture.toString();
	}
	
}
